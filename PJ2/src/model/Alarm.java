package model;

import model.citizens.Citizen;
import main.Main;

public class Alarm {
    private Citizen citizen;

    public Alarm(Citizen citizen) {
        this.citizen=citizen;
    }

    public String getMessage() {
        return "Zarazeni na poziciji " + citizen.getPositionX() + " " + citizen.getPositionY() + " houseID= " + citizen.getHomeId() + "!";
    }

    public void show(String message) {
        Main.controller.showAlarm(message);
    }

    public void setCitizen(Citizen citizen) {
        this.citizen = citizen;
    }

    public Citizen getCitizen() {
        return citizen;
    }
}
