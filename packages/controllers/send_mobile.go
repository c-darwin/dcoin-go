package controllers

import (
	"encoding/json"
	"errors"
	"github.com/c-darwin/dcoin-go/packages/sendnotif"
	"github.com/c-darwin/dcoin-go/packages/utils"
)

func (c *Controller) SendMobile() (string, error) {

	if !c.NodeAdmin || c.SessRestricted != 0 {
		return "", utils.ErrInfo(errors.New("Permission denied"))
	}

	sendnotif.SendMobileNotification("Test", "Test message")

	result, _ := json.Marshal(map[string]string{"success": "success"})
	return string(result), nil

}
