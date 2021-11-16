package model;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.citizens.Citizen;
import main.Main;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

public class Hospital extends Element implements Runnable {
    private int capacity;
    public Stack<Ambulance> ambulances = new Stack<>();
    private int positionX, positionY;
    private Map<Citizen, List<Double>> citizens = new HashMap<>();
    public Image image;
    public ImageView pic = new ImageView();
    private Runnable update;

    public Hospital(int positionX, int positionY, int numberOfCitizens) {
        int min = (int) Math.round(numberOfCitizens * 0.1);
        int max = (int) Math.round(numberOfCitizens * 0.15);
        capacity = 1;//new Random().nextInt(max - min + 1) + min;
        this.positionX = positionX;
        this.positionY = positionY;
        pic.setFitWidth(25);
        pic.setFitHeight(25);
        image = new Image(new File("src/resources/hospital.jpg").toURI().toString());
        pic.setImage(image);
        paintElement(positionX, positionY, false);
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

    public void addAmbulance(Ambulance ambulance) {
        ambulances.push(ambulance);
        ambulance.setHospital(this);
    }

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public boolean hasAmbulance() {
        return ambulances.size() > 0;
    }

    public void sendAmbulance(Citizen citizen, Hospital hospital) {
        capacity--;
        // addCitizenToHospital(citizen);
        Ambulance a = ambulances.pop();
        a.done = false;
        a.setCitizen(citizen, hospital);
        Thread t = new Thread(a);
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void paintElement(int x, int y, boolean reset) {
        update = () -> {
            //     System.out.println("Update!");
            Main.pane[x][y].getChildren().add(pic);
            Main.city.getMap()[x][y].setElement(this);
        };
        Platform.runLater(update);
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void addCitizenToHospital(Citizen citizen) {
        citizens.put(citizen, new ArrayList<Double>());
        System.out.println(citizen.getName() + " je smjesten u bolnicu!");
        Main.fileLock.lock();
        try {
            RandomAccessFile raf = new RandomAccessFile(Main.filePath, "rw");
            raf.seek(66);
            String string = raf.readLine();
            int broj = Integer.parseInt(string);

            raf.seek(66);
            raf.writeBytes("" + (broj + 1));
            raf.close();
        } catch (Exception ex) {
            Main.LOGGER.log( Level.SEVERE, ex.toString(), ex );
        }
        Main.fileLock.unlock();
    }

    public void removeCitizenFromHospital(Citizen citizen) {
        citizens.remove(citizen);
        //citizen go to home!

        Main.fileLock.lock();
        try {
            RandomAccessFile raf = new RandomAccessFile(Main.filePath, "rw");
            raf.seek(66);
            String string = raf.readLine();
            int broj = Integer.parseInt(string);

            raf.seek(66);
            raf.writeBytes("" + (broj - 1));
            raf.close();
        } catch (Exception ex) {
            Main.LOGGER.log( Level.SEVERE, ex.toString(), ex );
        }
        Main.fileLock.unlock();
        capacity++;
    }

    public void run() {
        double sum;
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
            for (Citizen citizen : citizens.keySet()) {
                citizens.get(citizen).add(citizen.getTemperature());
                System.out.println(citizen.getName() + " je u bolnici pod nadzorom! " + getPositionX() + " " + getPositionY());
                List<Double> temps = citizens.get(citizen);
                sum = 0;
                if (temps.size() >= 3) {
                    System.out.println("=========");
                    for (int i = 0; i < 3; i++) {
                        System.out.println("Temperatura " + temps.get(temps.size() - 1 - i));
                        sum += temps.get(temps.size() - 1 - i);
                    }
                    System.out.println("Suma/3= " + sum / 3);
                    if (sum / 3.0 < 37.0) {
                        System.out.println(citizen.getName() + " se oporavio!");
                        Main.oporavljeni.add(citizen);
                        removeCitizenFromHospital(citizen);
                        citizen.resume();
                        citizen.goHome();
                    }
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Main.LOGGER.log( Level.SEVERE, ex.toString(), ex );
            }
        }
    }

    @Override
    public boolean isHospitalOrHouse() {
        return true;
    }
    @Override
    public boolean isHospital() {
        return true;
    }
}
