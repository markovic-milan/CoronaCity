package model;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.citizens.Citizen;
import main.Main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Checkpoint extends Element implements Runnable {
    private List<Hospital> hospitals = new ArrayList<>();
    private List<Citizen> stopedCitizens = new ArrayList<>();
    private int positionX, positionY;
    private ImageView pic = new ImageView();

    public Checkpoint(int positionX, int positionY) {
        this.positionX = positionX;
        this.positionY = positionY;
        pic.setFitWidth(25);
        pic.setFitHeight(25);
        setPicture(new Image(new File("src/resources/checkPoint.jpg").toURI().toString()));
        paintElement(positionX, positionY, false);
    }

    public void setPicture(Image image) {
        pic.setImage(image);
    }

    public static void repaintCheckPoint(Checkpoint checkpoint) {
        synchronized (Main.city.getMap()[checkpoint.getPositionX()][checkpoint.getPositionY()]) {
            Main.city.getMap()[checkpoint.getPositionX()][checkpoint.getPositionY()].setElement(checkpoint);
        }
        System.out.println("Checkpoint restored!");
    }

    @Override
    public boolean isCheckPoint() {
        return true;
    }

    public void addHospital(Hospital hospital) {
        hospitals.add(hospital);
    }

    @Override
    public void paintElement(int x, int y, boolean reset) {
        Platform.runLater(() -> Main.pane[x][y].getChildren().add(pic));
    }

    public void stop() {
        running = false;
        // you might also want to interrupt() the Thread that is
        // running this Runnable, too, or perhaps call:
        resume();
        // to unblock
    }

    public void pause() {
        // you may want to throw an IllegalStateException if !running
        paused = true;
    }

    public void resume() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll(); // Unblocks thread
        }
    }

    public void run() {
        while (true) {
            for (int i = positionX - 1; i <= positionX + 1; i++) {
                for (int j = positionY - 1; j <= positionY + 1; j++) {
                    if (Main.city.getMap()[i][j].getElement() instanceof Citizen) {
                        Citizen citizen = (Citizen) Main.city.getMap()[i][j].getElement();
                        if (!stopedCitizens.contains(citizen) && citizen.getTemperature() >= 37) {
                            Main.zarazeni.add(citizen);
                            citizen.pause();
                            stopedCitizens.add(citizen);
                            Alarm alarm = new Alarm(citizen);
                            Main.alarms.push(alarm);
                            alarm.show(alarm.getMessage());
                            citizen.sendAllHome();
                            // hospitals.get(0).sendAmbulance(citizen);
                        }
                    }
                }
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException ex) {
                Main.LOGGER.log( Level.SEVERE, ex.toString(), ex );
            }
        }
    }

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }
}