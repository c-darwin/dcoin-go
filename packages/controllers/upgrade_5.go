package controllers

import (
	"github.com/c-darwin/dcoin-go/packages/utils"
	"strings"
	"github.com/c-darwin/dcoin-go/packages/geolocation"
	"fmt"
	"runtime"
)

type upgrade5Page struct {
	Alert           string
	UserId          int64
	Lang            map[string]string
	GeolocationLat  string
	GeolocationLon  string
	SaveAndGotoStep string
	UpgradeMenu     string
	Mobile          bool
}


var (
	geolocationLat string
	geolocationLon string
)

func (c *Controller) Upgrade5() (string, error) {

	log.Debug("Upgrade5")

	if !utils.Mobile() {
		if runtime.GOOS == "darwin" && !utils.IOS(){
			if coord, err := geolocation.CLLocation(); err == nil {
				geolocationLat = fmt.Sprintf("%.6f", coord.Latitude)
				geolocationLon = fmt.Sprintf("%.6f", coord.Longitude)
				fmt.Printf("darwin lat: %s\nlng: %s", geolocationLat, geolocationLon)
			}

		} else
		if coord, err := geolocation.GetLocation(); err == nil && runtime.GOOS != "darwin" {
			geolocationLat = fmt.Sprintf("%.6f", coord.Latitude)
			geolocationLon = fmt.Sprintf("%.6f", coord.Longitude)

			fmt.Printf("others lat: %s\nlng: %s", geolocationLat, geolocationLon)
		}
	}

	geolocation, err := c.Single("SELECT geolocation FROM " + c.MyPrefix + "my_table").String()
	if err != nil {
		return "", utils.ErrInfo(err)
	}
	if len(geolocation) > 0 {
		x := strings.Split(geolocation, ", ")
		if len(x) == 2 {
			geolocationLat = x[0]
			geolocationLon = x[1]
		}
	}

	saveAndGotoStep := strings.Replace(c.Lang["save_and_goto_step"], "[num]", "7", -1)
	upgradeMenu := utils.MakeUpgradeMenu(5)

	TemplateStr, err := makeTemplate("upgrade_5", "upgrade5", &upgrade5Page{
		Alert:           c.Alert,
		Lang:            c.Lang,
		SaveAndGotoStep: saveAndGotoStep,
		UpgradeMenu:     upgradeMenu,
		GeolocationLat:  geolocationLat,
		GeolocationLon:  geolocationLon,
		UserId:          c.SessUserId,
		Mobile:          utils.Mobile()})
	if err != nil {
		return "", utils.ErrInfo(err)
	}
	return TemplateStr, nil
}
