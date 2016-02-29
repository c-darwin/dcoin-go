// +build !android,!ios

package dcoin

import (
	"fmt"
	"github.com/c-darwin/dcoin-go/packages/consts"
	"github.com/c-darwin/dcoin-go/packages/tcpserver"
	"github.com/c-darwin/dcoin-go/packages/utils"
	_ "github.com/mattn/go-sqlite3"
	"net"
	"net/http"
	"os"
	"regexp"
	"time"
)

func IosLog(text string) {
}


func NewBoundListener(maxActive int, l net.Listener) net.Listener {
	return &boundListener{l, make(chan bool, maxActive)}
}

type boundListener struct {
	net.Listener
	active chan bool
}

type boundConn struct {
	net.Conn
	active chan bool
}

func (l *boundListener) Accept() (net.Conn, error) {
	l.active <- true
	c, err := l.Listener.Accept()
	if err != nil {
		<-l.active
		return nil, err
	}
	return &boundConn{c, l.active}, err
}

func (l *boundConn) Close() error {
	err := l.Conn.Close()
	<-l.active
	return err
}


func httpListener(ListenHttpHost, BrowserHttpHost string) {
	l, err := net.Listen("tcp", ListenHttpHost)
	if err != nil {
		log.Error("%v", err)
		// Если это повторный запуск и он не из консоли, то открываем окно браузера, т.к. скорее всего юзер тыкнул по иконке
		if *utils.Console == 0 {
			openBrowser(BrowserHttpHost)
		}
		log.Error("%v", utils.ErrInfo(err))
		panic(err)
		os.Exit(1)
	}

	go func() {
		err = http.Serve(NewBoundListener(100, l), http.TimeoutHandler(http.DefaultServeMux, time.Duration(600*time.Second), "Your request has timed out"))
		if err != nil {
			log.Error("Error listening: %v (%v)", err, ListenHttpHost)
			panic(err)
			//os.Exit(1)
		}
	}()
}

func tcpListener() {
	db := utils.DB
	log.Debug("tcp")
	go func() {
		if db == nil || db.DB == nil {
			for {
				db = utils.DB
				if db != nil && db.DB != nil {
					break
				} else {
					utils.Sleep(3)
				}
			}
		}
		tcpHost := db.GetTcpHost()
		log.Debug("tcpHost: %v", tcpHost)
		// включаем листинг TCP-сервером и обработку входящих запросов
		l, err := net.Listen("tcp", tcpHost)
		if err != nil {
			log.Error("Error listening: %v", err)
			panic(err)
			//os.Exit(1)
		}
		//defer l.Close()
		go func() {
			for {
				conn, err := l.Accept()
				if err != nil {
					log.Error("Error accepting: %v", err)
					utils.Sleep(1)
					//panic(err)
					//os.Exit(1)
				} else {
					go func(conn net.Conn) {
						t := new(tcpserver.TcpServer)
						t.DCDB = db
						t.Conn = conn
						t.HandleTcpRequest()
					}(conn)
				}
			}
		}()
	}()

	// Листенинг для чата
	go func() {
		listener, err := net.Listen("tcp", ":"+consts.CHAT_PORT)
		if err != nil {
			log.Error("Error listening: %v", err)
			panic(err)
		}
		defer listener.Close()

		for {
			conn, _ := listener.Accept()
			log.Debug("main conn %v\n", conn)
			log.Debug("conn.RemoteAddr() %v\n", conn.RemoteAddr().String())

			go func(conn net.Conn) {
				buf := make([]byte, 4)
				_, err := conn.Read(buf)
				if err != nil {
					log.Debug("%v", err)
					return
				}
				// получим user_id в первых 4-х байтах
				userId := utils.BinToDec(buf)

				// и тип канала
				buf = make([]byte, 1)
				_, err = conn.Read(buf)
				if err != nil {
					log.Debug("%v", err)
					return
				}
				chType := utils.BinToDec(buf)
				log.Debug("userId %v chType %v", userId, chType)

				// мониторит входящие
				if chType == 0 {
					fmt.Println("chType 0", conn.RemoteAddr(), utils.Time())
					utils.ChatMutex.Lock()
					utils.ChatInConnections[userId] = 1
					utils.ChatMutex.Unlock()
					go utils.ChatInput(conn, userId)
				}
				// создаем канал, через который будем рассылать тр-ии чата
				if chType == 1 {
					re := regexp.MustCompile(`(.*?):[0-9]+$`)
					match := re.FindStringSubmatch(conn.RemoteAddr().String())
					if len(match) != 0 {
						fmt.Println("chType 1", conn.RemoteAddr(), utils.Time())
						// проверим, нет ли уже созданного канала для такого хоста
						if _, ok := utils.ChatOutConnections[userId]; !ok {
							fmt.Println("ADD", userId, conn.RemoteAddr(), utils.Time())
							connChan := make(chan *utils.ChatData, 100)
							utils.ChatMutex.Lock()
							utils.ChatOutConnections[userId] = &utils.ChatOutConnectionsType{MessIds: []int64{}, ConnectionChan: connChan}
							utils.ChatMutex.Unlock()
							fmt.Println("utils.ChatOutConnections", utils.ChatOutConnections)
							utils.ChatTxDisseminator(conn, userId, connChan)
						} else {
							fmt.Println("SKIP", userId, conn.RemoteAddr(), utils.Time())
							conn.Close()
						}
					}
				}
			}(conn)
		}
	}()
}
