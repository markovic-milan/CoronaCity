package model;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import model.citizens.Citizen;
import controllers.Controller;
import main.Main;

import java.io.File;
import java.util.Random;
import java.util.logging.Level;

import static model.Checkpoint.repaintCheckPoint;

public class Ambulance extends Element implements Runnable {
    private Hospital hospital, other;
    private int positionX, positionY;
    private Citizen citizen;
    public Image image;
    public boolean done, goingBack, replaceCheckpoint, exists;
    private Checkpoint checkpoint;
    public ImageView pic = new ImageView();
    private Runnable update;

    public Ambulance() {
        pic.setFitWidth(25);
        pic.setFitHeight(25);
        image = new Image(new File("src/resources/ambulance.jpg").toURI().toString());
        pic.setImage(image);
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

    @Override
    public void paintElement(int x, int y, boolean reset) {
        if (reset) {
            update = () -> {
                Main.pane[x][y].getChildren().clear();
                Main.pane[x][y].setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
            };
        } else {
            update = () -> {
                //     System.out.println("Update!");
                Main.pane[x][y].getChildren().add(pic);
            };
        }
        Platform.runLater(update);
    }

    public void resetField(int x, int y) {
        if (Main.city.getMap()[x][y].getElement() == this)
            synchronized (Main.city.getMap()[x][y]) {
                (Main.city.getMap())[x][y].setElement(null);
                paintElement(x, y, true);
            }
    }

    public void setCitizen(Citizen citizen, Hospital other) {
        this.citizen = citizen;
        this.other = other;
    }

    public void setAmbulanceToPosition(int x, int y, Side side) {
        synchronized (Main.city.getMap()[x][y]) {
            (Main.city.getMap())[x][y].setElement(this);
            paintElement(x, y, false);
        }
        if (side == Side.DOWN && Main.city.getMap()[x - 1][y].getElement() == this)
            resetField(x - 1, y);
        else if (side == Side.UP && Main.city.getMap()[x + 1][y].getElement() == this)
            resetField(x + 1, y);
        else if (side == Side.LEFT && Main.city.getMap()[x][y + 1].getElement() == this)
            resetField(x, y + 1);
        else if (side == Side.RIGHT && Main.city.getMap()[x][y - 1].getElement() == this)
            resetField(x, y - 1);
    }

    public void sleep(int timeInSeconds) {
        try {
            Thread.sleep(timeInSeconds * 500);
        } catch (InterruptedException ex) {
            Main.LOGGER.log( Level.SEVERE, ex.toString(), ex );
        }
    }

    public void pickRandomSide(Side side) {
        System.out.println("pickRandomSide");
        int random = new Random().nextInt(2);
        if (side == Side.RIGHT) {
            if (random == 0) {
                moveUp();
            } else {
                moveDown();
            }
        } else if (side == Side.LEFT) {
            if (random == 0) {
                moveUp();
            } else {
                moveDown();
            }
        } else if (side == Side.UP) {
            if (random == 0) {
                moveLeft();
            } else {
                moveRight();
            }
        } else {
            if (random == 0) {
                moveLeft();
            } else {
                moveRight();
            }
        }
    }

    public void move() {
        //svi ukucani bjez kuci
        citizen.getHouse().getCitizens().stream().filter(c -> c != citizen).forEach(c -> c.setGoHome(true));
        int xDistance, yDistance;

        while (!done && running) {
            synchronized (pauseLock) {
                if (!running) { // may have changed while waiting to
                    // synchronize on pauseLock
                    break;
                }
                if (paused) {
                    try {
                        synchronized (pauseLock) {
                            pauseLock.wait(); // will cause this Thread to block until
                            // another thread calls pauseLock.notifyAll()
                            // Note that calling wait() will
                            // relinquish the synchronized lock that this
                            // thread holds on pauseLock so another thread
                            // can acquire the lock to call notifyAll()
                            // (link with explanation below this code)
                        }
                    } catch (InterruptedException ex) {
                        Main.LOGGER.log( Level.SEVERE, ex.toString(), ex );
                        break;
                    }
                    if (!running) { // running might have changed since we paused
                        break;
                    }
                }
            }

            xDistance = Math.abs(citizen.getPositionX() - positionX);
            yDistance = Math.abs(citizen.getPositionY() - positionY);
            if (xDistance > yDistance) {
                if (positionX < citizen.getPositionX()) {
                    moveDown();
                } else {
                    moveUp();
                }
            } else {
                if (positionY < citizen.getPositionY()) {
                    moveRight();
                } else {
                    moveLeft();
                }
            }
            if (replaceCheckpoint) {
                repaintCheckPoint(checkpoint);
                replaceCheckpoint = false;
            }
            sleep(1);
            if (goingBack && hasArrived()) {
                goingBack = false;
                ((Hospital) Main.city.getMap()[citizen.getPositionX()][citizen.getPositionY()].getElement()).addAmbulance(this);
                hospital.addCitizenToHospital(citizen);
                Controller.numberOfAmbulances++;
                resetField(positionX, positionY);
                done = true;
            }
        }
    }

    @Override
    public void run() {
        move();
    }

    public boolean exists() {
        return exists;
    }

    public boolean hasArrived() {
        return Math.abs(hospital.getPositionX() - positionX) <= 1 && Math.abs(hospital.getPositionY() - positionY) < 1
                || Math.abs(hospital.getPositionX() - positionX) < 1 && Math.abs(hospital.getPositionY() - positionY) <= 1;
    }

    public void takeCitizen(Citizen citizen) {
        System.out.println("takeCitizen");
        citizen.resetField(citizen.getPositionX(), citizen.getPositionY());
        citizen.setPositionX(other.getPositionX());
        citizen.setPositionY(other.getPositionY());
        setHospital(other);
    }

    public void moveDown() {
        if (Main.city.getMap()[positionX + 1][positionY].getElement() == citizen) {
            takeCitizen(citizen);
            goingBack = true;
            return;
        }
        if (Main.city.getMap()[positionX + 1][positionY].getElement() != null) {
            if (Main.city.getMap()[positionX + 1][positionY].getElement().isHospitalOrHouse()) {
                pickRandomSide(Side.DOWN);
                return;
            } else if (Main.city.getMap()[positionX + 1][positionY].getElement().isCheckPoint()) {
                replaceCheckpoint = true;
                checkpoint = (Checkpoint) Main.city.getMap()[positionX + 1][positionY].getElement();
            }
            while (Main.city.getMap()[positionX + 1][positionY].getElement().isCitizen()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Main.LOGGER.log( Level.SEVERE, ex.toString(), ex );
                }
            }
        }
        setAmbulanceToPosition(positionX + 1, positionY, Side.DOWN);
        resetField(positionX, positionY);
        positionX++;
    }

    public void moveUp() {
        if (Main.city.getMap()[positionX - 1][positionY].getElement() == citizen) {
            takeCitizen(citizen);
            goingBack = true;
            return;
        }
        if (Main.city.getMap()[positionX - 1][positionY].getElement() != null) {
            if (Main.city.getMap()[positionX - 1][positionY].getElement().isHospitalOrHouse()) {
                pickRandomSide(Side.UP);
                return;
            } else if (Main.city.getMap()[positionX - 1][positionY].getElement().isCheckPoint()) {
                replaceCheckpoint = true;
                checkpoint = (Checkpoint) Main.city.getMap()[positionX - 1][positionY].getElement();
            }
            while (Main.city.getMap()[positionX - 1][positionY].getElement().isCitizen()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Main.LOGGER.log( Level.SEVERE, ex.toString(), ex );
                }
            }
        }
        setAmbulanceToPosition(positionX - 1, positionY, Side.UP);
        resetField(positionX, positionY);
        positionX--;
    }

    public void moveRight() {
        if (Main.city.getMap()[positionX][positionY + 1].getElement() == citizen) {
            takeCitizen(citizen);
            goingBack = true;
            return;
        }
        if (Main.city.getMap()[positionX][positionY + 1].getElement() != null) {
            if (Main.city.getMap()[positionX][positionY + 1].getElement().isHospitalOrHouse()) {
                pickRandomSide(Side.RIGHT);
                return;
            } else if (Main.city.getMap()[positionX][positionY + 1].getElement().isCheckPoint()) {
                replaceCheckpoint = true;
                checkpoint = (Checkpoint) Main.city.getMap()[positionX][positionY + 1].getElement();
            }
            while (Main.city.getMap()[positionX][positionY + 1].getElement().isCitizen()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Main.LOGGER.log( Level.SEVERE, ex.toString(), ex );
                }
            }
        }
        setAmbulanceToPosition(positionX, positionY + 1, Side.RIGHT);
        resetField(positionX, positionY);
        positionY++;
    }

    public void moveLeft() {
        if (Main.city.getMap()[positionX][positionY - 1].getElement() == citizen) {
            takeCitizen(citizen);
            goingBack = true;
            return;
        }
        if (Main.city.getMap()[positionX][positionY - 1].getElement() != null) {
            if (Main.city.getMap()[positionX][positionY - 1].getElement().isHospitalOrHouse()) {
                pickRandomSide(Side.LEFT);
                return;
            } else if (Main.city.getMap()[positionX][positionY - 1].getElement().isCheckPoint()) {
                replaceCheckpoint = true;
                checkpoint = (Checkpoint) Main.city.getMap()[positionX][positionY - 1].getElement();
            }
            while (Main.city.getMap()[positionX][positionY - 1].getElement().isCitizen()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Main.LOGGER.log( Level.SEVERE, ex.toString(), ex );
                }
            }
        }
        setAmbulanceToPosition(positionX, positionY - 1, Side.LEFT);
        resetField(positionX, positionY);
        positionY--;
    }

    public void setHospital(Hospital hospital) {
        this.hospital = hospital;
    }

    @Override
    public boolean isAmbulance() {
        return true;
    }
}
