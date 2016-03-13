package controllers

import (
	"errors"
	"fmt"
	"github.com/democratic-coin/dcoin-go/packages/utils"
	"net/http"
	"time"
	"net/url"
//	"regexp"
	"strings"
	"encoding/json"
	"io/ioutil"
)


type Answer struct {
	Success bool `json:"success"`
	Error   string `json:"error"`
}

func SendEmail( cmd, email, user_id, text, subject string ) error {
	Client := &http.Client{
			Transport: http.DefaultTransport,
			Timeout:   20 * time.Second,
		}
	values := url.Values{}
	values.Set("email", email )
	values.Set("user_id", user_id )
	values.Set("subject", subject )
	values.Set("text", text )
	
	req, err := http.NewRequest("POST", `http://email.dcoin.club/` + cmd, 
	                          strings.NewReader(values.Encode()))
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", "application/x-www-form-urlencoded")
	res, e := Client.Do(req)
	if e != nil {
		return e
	}

	defer res.Body.Close()
	body, _ := ioutil.ReadAll(res.Body)

	var answer Answer
	err = json.Unmarshal( body, &answer )
	if err != nil {
		return err
	}
    return nil
}

func (c *Controller) SaveEmailAndSendTestMess() (string, error) {

	if c.SessRestricted != 0 {
		return "", utils.ErrInfo(errors.New("Permission denied"))
	}

	c.r.ParseForm()

	err := c.ExecSql(`UPDATE `+c.MyPrefix+`my_table	SET  email = ?`, c.r.FormValue("email"))
	if err != nil {
		return fmt.Sprintf(`{"error":"%s"}`, err), nil
	}
	mailData, err := c.OneRow("SELECT * FROM " + c.MyPrefix + "my_table").String()
	if err != nil {
		return fmt.Sprintf(`{"error":"%s"}`, err), nil
	}
	SendEmail( `setemail`, mailData["email"], mailData["user_id"], "Test", "Test" )

/*	mailServer := ""
	re := regexp.MustCompile(`(?i)^[0-9a-z\-\_\.@]+(gmail\.com|yahoo\.com|hotmail\.com|outlook\.com|live\.com|yandex\.ru|yandex\.com|ya\.ru|mail\.ru|bk\.ru|inbox\.ru|list\.ru)$`)
	match := re.FindStringSubmatch(c.r.FormValue("smtp_username"))
	if len(match) > 0 {
		mailServer = strings.ToLower(match[1])
	} else {
		return `{"error":"incorrect email"}`, nil
	}

	var smtp_server, smtp_port string
	switch mailServer {
	case "gmail.com": //+
		smtp_server = "smtp.gmail.com"
		smtp_port = "465"
	case "yahoo.com":
		smtp_server = "smtp.mail.yahoo.com"
		smtp_port = "465"
	case "hotmail.com", "outlook.com", "live.com": //-
		smtp_server = "smtp.live.com"
		smtp_port = "465"
	case "yandex.ru", "ya.ru", "yandex.com": //+
		smtp_server = "smtp.yandex.ru"
		smtp_port = "465"
	case "mail.ru", "bk.ru", "inbox.ru", "list.ru": //+
		smtp_server = "smtp.mail.ru"
		smtp_port = "465"
	}
	err := c.ExecSql(`
			UPDATE `+c.MyPrefix+`my_table
			SET  email = ?,
					smtp_server =  ?,
					use_smtp =  ?,
					smtp_port =  ?,
					smtp_ssl =  ?,
					smtp_auth =  ?,
					smtp_username = ?,
					smtp_password = ?
			`, c.r.FormValue("email"), smtp_server, 1, smtp_port, 1, 1, c.r.FormValue("smtp_username"), c.r.FormValue("smtp_password"))
	if err != nil {
		return fmt.Sprintf(`{"error":"%s"}`, err), nil
	}
	mailData, err := c.OneRow("SELECT * FROM " + c.MyPrefix + "my_table").String()
	if err != nil {
		return fmt.Sprintf(`{"error":"%s"}`, err), nil
	}

	err = c.SendMail("Test", "Test", mailData["email"], mailData, c.Community, c.PoolAdminUserId)
	if err != nil {
		return fmt.Sprintf(`{"error":"%s"}`, err), nil
	}*/
	return `{"success":"success"}`, nil

}
