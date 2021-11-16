package model.citizens;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import model.Checkpoint;
import model.Element;
import model.House;
import model.Side;
import main.Main;

import java.util.Random;
import java.util.logging.Level;

import static model.Checkpoint.repaintCheckPoint;

public abstract class Citizen extends Element implements Runnable {
    public static int numberOfCitizens;
    private Checkpoint checkpoint;
    private House house;

    public Gender getGender() {
        return gender;
    }

    private int positionX, positionY, failed, distance, birthYear, radius;
    private Side side;
    private boolean replaceCheckpoint, inHome;
    private boolean goHome, isElder, isChild, isAdult, backToHome;
    private String personId, name, lastName, homeId;
    private Gender gender;
    private double temperature;
    private ImageView pic = new ImageView();
    private Runnable update;

    public Citizen(String personId, String name, String lastName, int birthYear) {
        numberOfCitizens++;
        this.personId = personId;
        this.name = name;
        this.lastName = lastName;
        this.birthYear = birthYear;
        gender = Gender.values()[new Random().nextInt(Gender.values().length)];
        side = Side.values()[new Random().nextInt(Side.values().length)];
        generateTemperature();
        pic.setFitWidth(25);
        pic.setFitHeight(25);
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
    public boolean isCitizen() {
        return true;
    }

    @Override
    public void run() {
        backToHome = false;
        sleep(new Random().nextInt(5) + 1);
        //System.out.println(this + " canStart() = " + canStart());

        while (!canStart() && running) {
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
                        Main.LOGGER.log(Level.SEVERE, ex.toString(), ex);
                    }
                    if (!running) { // running might have changed since we paused
                        break;
                    }
                }
            }
            side = Side.values()[new Random().nextInt(Side.values().length)];
            System.out.println(this + " sleep");
            sleep(2);
        }
        move();
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
                Main.textArea.appendText(this.toString() + "\n");
            };
        }
        Platform.runLater(update);
    }

    public boolean canStart() {
        house.lock.lock();
        int X, Y;
        if (side == Side.RIGHT) {
            X = positionX;
            Y = positionY + 1;
        } else if (side == Side.LEFT) {
            X = positionX;
            Y = positionY - 1;
        } else if (side == Side.UP) {
            X = positionX - 1;
            Y = positionY;
        } else {
            X = positionX + 1;
            Y = positionY;
        }
        for (int i = X - 2; i <= X + 2; i++) {
            for (int j = Y - 2; j <= Y + 2; j++) {
                if (i >= 0 && i < Main.city.getDimension() && j >= 0 && j < Main.city.getDimension()) {
                    Element element = Main.city.getMap()[i][j].getElement();
                    if (element != null && element.isCitizen() && element != this) {
                        house.lock.unlock();
                        return false;
                    }
                }
            }
        }
        house.lock.unlock();
        return true;
    }

    public void resetField(int x, int y) {
        synchronized (Main.city.getMap()[x][y]) {
            (Main.city.getMap())[x][y].setElement(null);
            paintElement(x, y, true);
        }
    }

    public void setCitizenToPosition(int x, int y) {
        synchronized (Main.city.getMap()[x][y]) {
            (Main.city.getMap())[x][y].setElement(this);
            paintElement(x, y, false);
        }
        distance--;
        failed = 0;
        if (side == Side.DOWN && Main.city.getMap()[x - 1][y].getElement() == this)
            resetField(x - 1, y);
        else if (side == Side.UP && Main.city.getMap()[x + 1][y].getElement() == this)
            resetField(x + 1, y);
        else if (side == Side.LEFT && Main.city.getMap()[x][y + 1].getElement() == this)
            resetField(x, y + 1);
        else if (side == Side.RIGHT && Main.city.getMap()[x][y - 1].getElement() == this)
            resetField(x, y - 1);
    }

    public boolean restrictedDirection() {
        if (positionX == 0 && side == Side.UP) return true;
        else if (positionY == 0 && side == Side.LEFT) return true;
        else if (positionX == Main.city.getDimension() - 1 && side == Side.DOWN) return true;
        else if (positionY == Main.city.getDimension() - 1 && side == Side.RIGHT) return true;
        return false;
    }

    public void sleep(int timeInSeconds) {
        try {
            Thread.sleep(timeInSeconds * 1000);
        } catch (InterruptedException ex) {
            Main.LOGGER.log( Level.SEVERE, ex.toString(), ex );
        }
    }

    public void sendAllHome() {
        house.getCitizens().stream().filter(c -> c != this).forEach(c -> c.setGoHome(true));
    }

    public int getHalfRadius() {
        return getRadius() / 2;
    }

    public void move() {
        //setSide(Side.RIGHT);
        distance = 25;//new Random().nextInt(getRadius() - getHalfRadius()) + getHalfRadius();
        System.out.println("=====================================================");
        System.out.println("Distance  " + getName() + " = " + distance);
        inHome = false;
        while (running) {
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
            if (distance == 0 || goHome) {
                goHome();
                return;
            }
            if (side == Side.DOWN) {
                moveDown();
            } else if (side == Side.UP) {
                moveUp();
            } else if (side == Side.RIGHT) {
                moveRight();
            } else if (side == Side.LEFT) {
                moveLeft();
            }
            if (replaceCheckpoint) {
                repaintCheckPoint(checkpoint);
                replaceCheckpoint = false;
            }
            sleep(1);

        }

    }

    public boolean freeToGo(int posX, int posY) {
        //     System.out.println(this + " free to go " + restrictedDirection() + " " + !checkNextDistance(posX, posY));
        if (restrictedDirection() || !checkNextDistance(posX, posY))
            return false;
        Element element = Main.city.getMap()[posX][posY].getElement();
        if (element != null) {
            if (element.isCheckPoint()) {
                replaceCheckpoint = true;
                checkpoint = (Checkpoint) element;
            } else return !element.isHospitalOrHouse();
        }
        return true;
    }

    public boolean checkElement(int x, int y) {
        Element element = Main.city.getMap()[x][y].getElement();
        // System.out.println("Check element " + x + " " + y);
        // System.out.println("if " + (element != null && element.isCitizen()));
        if (element != null && element.isCitizen() && !((Citizen) element).inHome) {
            Citizen citizen = (Citizen) element;
            if (citizen.isChild && !isElder) {
                Child child = ((Child) element);
                if (side == Side.RIGHT && positionX == child.getPositionX() && positionY + 1 == child.getPositionY()) {
                    return false;
                } else if (side == Side.LEFT && positionX == child.getPositionX() && positionY - 1 == child.getPositionY()) {
                    return false;
                } else if (side == Side.UP && positionY == child.getPositionY() && positionX - 1 == child.getPositionY()) {
                    return false;
                } else if (side == Side.DOWN && positionY == child.getPositionY() && positionX + 1 == child.getPositionY()) {
                    return false;
                }
                return true;
            } else if (isChild && !citizen.isElder) {
                if (side == Side.RIGHT && positionX == citizen.getPositionX() && positionY + 1 == citizen.getPositionY()) {
                    return false;
                } else if (side == Side.LEFT && positionX == citizen.getPositionX() && positionY - 1 == citizen.getPositionY()) {
                    return false;
                } else if (side == Side.UP && positionY == citizen.getPositionY() && positionX - 1 == citizen.getPositionY()) {
                    return false;
                } else if (side == Side.DOWN && positionY == citizen.getPositionY() && positionX + 1 == citizen.getPositionY()) {
                    return false;
                }
                return true;
            }
            System.out.println(this + " je blokiran zbog " + ((Citizen) element).getName() + " na poziciji " + x + " " + y);
            return false;
        }
        return true;
    }

    public boolean checkNextDistance(int posX, int posY) {
        // System.out.println("Check next distance " + posX + " " + posY);
        if (side == Side.DOWN && (posX + 2) < (Main.city.getDimension() - 1)) {
            for (int i = posY - 2; i <= posY + 2; i++) {
                if (i >= 0 && i < Main.city.getDimension() && !checkElement(posX + 2, i)) {
                    failed++;
                    return false;
                }
            }
        } else if (side == Side.UP && (posX - 2) > 0) {
            for (int i = posY - 2; i <= posY + 2; i++) {
                if (i >= 0 && i < Main.city.getDimension() && !checkElement(posX - 2, i)) {
                    failed++;
                    return false;
                }
            }
        } else if (side == Side.RIGHT && (posY + 2) < (Main.city.getDimension() - 1)) {
            for (int i = posX - 2; i <= posX + 2; i++) {
                if (i >= 0 && i < Main.city.getDimension() && !checkElement(i, posY + 2)) {
                    failed++;
                    return false;
                }
            }
        } else if (side == Side.LEFT && (posY - 2) >= 0) {
            for (int i = posX - 2; i <= posX + 2; i++) {
                if (i >= 0 && i < Main.city.getDimension() && !checkElement(i, posY - 2)) {
                    failed++;
                    return false;
                }
            }
        }
        return true;
    }

    public void pickRandomSide() {
        System.out.println("Picking random side");
        if (failed == 4)
            sleep(1);
        int random = new Random().nextInt(3);
        if (side == Side.RIGHT) {
            if (random == 0) {
                setSide(Side.UP);
                moveUp();
            } else if (random == 1) {
                setSide(Side.DOWN);
                moveDown();
            } else {
                setSide(Side.LEFT);
                moveLeft();
            }
        } else if (side == Side.LEFT) {
            if (random == 0) {
                setSide(Side.UP);
                moveUp();
            } else if (random == 1) {
                setSide(Side.DOWN);
                moveDown();
            } else {
                setSide(Side.RIGHT);
                moveRight();
            }
        } else if (side == Side.UP) {
            if (random == 0) {
                setSide(Side.LEFT);
                moveLeft();
            } else if (random == 1) {
                setSide(Side.RIGHT);
                moveRight();
            } else {
                setSide(Side.DOWN);
                moveDown();
            }
        } else {
            if (random == 0) {
                setSide(Side.LEFT);
                moveLeft();
            } else if (random == 1) {
                setSide(Side.RIGHT);
                moveRight();
            } else {
                setSide(Side.UP);
                moveUp();
            }
        }
    }

    public void moveUp() {
        if (freeToGo(positionX - 1, positionY)) {
            //  System.out.println(this + " Moving UP");
            setCitizenToPosition(--positionX, positionY);
        } else {
            pickRandomSide();
        }
    }

    public void moveLeft() {
        if (freeToGo(positionX, positionY - 1)) {
            //     System.out.println(this + " moving LEFT");
            setCitizenToPosition(positionX, --positionY);
        } else {
            pickRandomSide();
        }
    }

    public void moveRight() {
        if (freeToGo(positionX, positionY + 1)) {
            //     System.out.println(this + " moving RIGHT");
            setCitizenToPosition(positionX, ++positionY);
        } else {
            pickRandomSide();
        }
    }

    public void moveDown() {
        if (freeToGo(positionX + 1, positionY)) {
            //    System.out.println(this + " moving DOWN");
            setCitizenToPosition(++positionX, positionY);
        } else
            pickRandomSide();
    }

    public boolean hasArrived() {
        return Math.abs(house.getPositionX() - positionX) <= 1 && Math.abs(house.getPositionY() - positionY) < 1
                || Math.abs(house.getPositionX() - positionX) < 1 && Math.abs(house.getPositionY() - positionY) <= 1;
    }

    public void goHome() {
        System.out.println(this + " goHome started");
        int xDistance, yDistance;
        while (!inHome && running) {
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
            xDistance = Math.abs(house.getPositionX() - positionX);
            yDistance = Math.abs(house.getPositionY() - positionY);
            if (xDistance > yDistance) {
                if (positionX < house.getPositionX()) {
                    side = Side.DOWN;
                    moveDown();
                } else {
                    side = Side.UP;
                    moveUp();
                }
            } else {
                if (positionY < house.getPositionY()) {
                    side = Side.RIGHT;
                    moveRight();
                } else {
                    side = Side.LEFT;
                    moveLeft();
                }
            }
            if (replaceCheckpoint) {
                repaintCheckPoint(checkpoint);
                replaceCheckpoint = false;
            }
            sleep(1);
            if (hasArrived()) {
                System.out.println(this + " in house");
                resetField(positionX, positionY);
                positionX = house.getPositionX();
                positionY = house.getPositionY();
                inHome = true;
            }
        }
    }

    public void generateTemperature() {
        Thread thread = new Thread(() -> {
            while (true) {
                temperature = new Random().nextInt(4) + 35 + new Random().nextDouble();
                sleep(1);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public boolean isElder() {
        return isElder;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public void setElder(boolean elder) {
        isElder = elder;
    }

    public boolean isChild() {
        return isChild;
    }

    public void setHouse(House house) {
        this.house = house;
        this.homeId = house.getHomeId();
        this.positionX = house.getPositionX();
        this.positionY = house.getPositionY();
    }

    public void setGoHome(boolean goHome) {
        this.goHome = goHome;
    }

    public House getHouse() {
        return house;
    }

    public void setPicture(Image image) {
        this.pic.setImage(image);
    }

    public void setChild(boolean child) {
        isChild = child;
    }

    public boolean isAdult() {
        return isAdult;
    }

    public void setAdult(boolean adult) {
        isAdult = adult;
    }

    public String getName() {
        return name;
    }

    public String getHomeId() {
        return homeId;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
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

    public String toString() {
        return name + personId + " " + side + " matrica" + "[" + positionX + "]" + "[" + positionY + "]";
    }
}