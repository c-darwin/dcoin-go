// +build !android,!ios

package stopdaemons

import (
	"os"
	"syscall"
	"github.com/democratic-coin/dcoin-go/packages/utils"
	"fmt"
	"os/signal"
)

/*
#include <stdio.h>
#include <signal.h>

extern void go_callback_int();
static inline void SigBreak_Handler(int n_signal){
    printf("closed\n");
	go_callback_int();
}
static inline void waitSig() {
    #if (WIN32 || WIN64)
    signal(SIGBREAK, &SigBreak_Handler);
    signal(SIGINT, &SigBreak_Handler);
    #endif
}
*/
import (
	"C"
)

//export go_callback_int
func go_callback_int() {
	SigChan <- syscall.Signal(1)
}

var SigChan chan os.Signal


func waitSig() {
	C.waitSig()
}

func Signals() {
	SigChan = make(chan os.Signal, 1)
	waitSig()
	var Term os.Signal = syscall.SIGTERM
	go func() {
		signal.Notify(SigChan, os.Interrupt, os.Kill, Term)
		<-SigChan
		fmt.Println("KILL SIGNAL")
		for _, ch := range utils.DaemonsChans {
			fmt.Println("ch.ChBreaker<-true")
			ch.ChBreaker<-true
		}
		for _, ch := range utils.DaemonsChans {
			fmt.Println(<-ch.ChAnswer)
		}
		log.Debug("Daemons killed")
		fmt.Println("Daemons killed")
		if utils.DB != nil && utils.DB.DB != nil {
			err := utils.DB.Close()
			fmt.Println("DB Closed")
			if err != nil {
				log.Error(utils.ErrInfo(err).Error())
				//panic(err)
			}
		}

		err := os.Remove(*utils.Dir + "/dcoin.pid")
		if err != nil {
			log.Error(utils.ErrInfo(err).Error())
			panic(err)
		}
		fmt.Println("removed " + *utils.Dir + "/dcoin.pid")
		os.Exit(1)
	}()
}
