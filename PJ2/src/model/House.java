package model;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.citizens.Citizen;
import main.Main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class House extends Element {
    public  ReentrantLock lock = new ReentrantLock();
    private String homeId;
    private boolean empty = true;
    private int numOfCitizens;
    private List<Citizen> citizens;
    private int positionX, positionY;
    private ImageView pic = new ImageView();

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    @Override
    public boolean isHospitalOrHouse() {
        return true;
    }

    @Override
    public void paintElement(int x, int y, boolean reset) {
        Platform.runLater(() -> Main.pane[x][y].getChildren().add(pic));
    }

    public House(String homeId, int positionX, int positionY) {
        this.homeId = homeId;
        this.citizens = new ArrayList<>();
        this.positionX = positionX;
        this.positionY = positionY;
        pic.setFitWidth(25);
        pic.setFitHeight(25);
        Image image = new Image(new File("src/resources/house1.jpg").toURI().toString());
        pic.setImage(image);
        paintElement(positionX, positionY, false);
    }

    public void addCitizen(Citizen citizen) {
        citizens.add(citizen);
        citizen.setPositionX(positionX);
        citizen.setPositionY(positionY);
        citizen.setHouse(this);
        empty = false;
        numOfCitizens++;
    }

    public int getPositionX() {
        return positionX;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    public String getHomeId() {
        return homeId;
    }

    public void setHomeId(String homeId) {
        this.homeId = homeId;
    }

    public int getNumOfCitizens() {
        return numOfCitizens;
    }

    public void setNumOfCitizens(int numOfCitizens) {
        this.numOfCitizens = numOfCitizens;
    }

    public List<Citizen> getCitizens() {
        return citizens;
    }

    public void setCitizens(List<Citizen> citizens) {
        this.citizens = citizens;
    }
}
