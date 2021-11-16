package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.*;
import model.citizens.Adult;
import model.citizens.Child;
import model.citizens.Citizen;
import model.citizens.Elder;
import main.Main;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;

import static main.Main.city;
import static main.Main.startTime;


public class Controller implements Initializable {
    @FXML
    private Button okButton; // alarm ok
    @FXML
    private Label alarmLabel;//alarm tekst
    @FXML
    private GridPane gridPane;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Label brojOporavljenihLabel;
    @FXML
    private Label brojZarazenihLabel;
    @FXML
    private TextArea textArea;
    @FXML
    private Button omoguciKretanjeButton;
    @FXML
    private Button posaljiVoziloButton;
    @FXML
    private Button pregledajAmbulanteButton;
    @FXML
    private Button pregledajStatistikuButton;
    @FXML
    private Button zaustaviSimulacijuButton;
    @FXML
    private Button pokreniPonovoButton;
    @FXML
    private Button zavrsiSimulacijuButton;
    private int broj = 0;
    public static List<Hospital> hospitals = new ArrayList<>();
    public static List<Runnable> threads = new ArrayList<>();
    public static int numberOfAmbulances;
    public static List<Element> elements = new ArrayList<>();

    public TextArea getArea() {
        return textArea;
    }

    public Controller() {
    }

    public void refreshMap() {
    }

    public void setGrid(String brojKuca, String brojAmbulantnihVozila, String brojKontrolnihPunktova,
                        String brojStarih, String brojOdraslih, String brojDjece) {
        long start = System.currentTimeMillis();

        if (Integer.parseInt(brojDjece) != 0 && Integer.parseInt(brojOdraslih) == 0 && Integer.parseInt(brojStarih) == 0) {
            System.out.println("Djeca ne mogu biti sama u kuci");
            return;
        }
        numberOfAmbulances = Integer.parseInt(brojAmbulantnihVozila);
        System.out.println("Broj vozila je " + numberOfAmbulances);
        //setovanje gridpane
        Main.pane = new StackPane[city.getDimension()][city.getDimension()];
        for (int i = 0; i < city.getDimension(); i++) {
            for (int j = 0; j < city.getDimension(); j++) {
                Main.pane[i][j] = new StackPane();
                Main.pane[i][j].setPrefWidth(27);
                Main.pane[i][j].setPrefHeight(27);
                Main.pane[i][j].setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
                Main.pane[i][j].setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                GridPane.setRowIndex(Main.pane[i][j], i);
                GridPane.setColumnIndex(Main.pane[i][j], j);
                gridPane.getChildren().addAll(Main.pane[i][j]);
            }
        }
        gridPane.setPadding(new Insets(5, 5, 5, 5));

        int broj = Integer.parseInt(brojStarih);
        //kreiranje stanovnika svake vrste
        List<Citizen> citizens = new ArrayList<>();
        for (int i = 0; i < broj; i++) {
            Elder elder = new Elder(i + "", "ElderName" + i, "ELderLastName" + i);
            citizens.add(elder);
            threads.add(elder);
            elements.add(elder);
        }
        broj = Integer.parseInt(brojOdraslih);
        for (int i = 0; i < broj; i++) {
            Adult adult = new Adult(i + "", "AdultName" + i, "AdultLastName" + i);
            citizens.add(adult);
            threads.add(adult);
            elements.add(adult);
        }
        broj = Integer.parseInt(brojDjece);
        for (int i = 0; i < broj; i++) {
            Child child = new Child(i + "", "ChildName" + i, "ChildLastName" + i);
            citizens.add(child);
            threads.add(child);
            elements.add(child);
        }

        //postavljanje inicijalnih ambulanti
        Hospital hospital1 = new Hospital(0, 0, citizens.size());
        hospitals.add(hospital1);
        threads.add(hospital1);
        elements.add(hospital1);
        Hospital hospital2 = new Hospital(city.getDimension() - 1, city.getDimension() - 1, citizens.size());
        hospitals.add(hospital2);
        threads.add(hospital2);
        elements.add(hospital2);
        Hospital hospital3 = new Hospital(city.getDimension() - 1, 0, citizens.size());
        hospitals.add(hospital3);
        threads.add(hospital3);
        elements.add(hospital3);
        Hospital hospital4 = new Hospital(0, city.getDimension() - 1, citizens.size());
        hospitals.add(hospital4);
        threads.add(hospital4);
        elements.add(hospital4);
        city.getMap()[0][0].setElement(hospital1);
        city.getMap()[city.getDimension() - 1][city.getDimension() - 1].setElement(hospital2);
        city.getMap()[city.getDimension() - 1][0].setElement(hospital3);
        city.getMap()[0][city.getDimension() - 1].setElement(hospital4);

        //dodavanje vozila u ambulante
        for (int i = 0; i < numberOfAmbulances; ) {
            for (Hospital h : hospitals) {
                Ambulance ambulance = new Ambulance();
                threads.add(ambulance);
                elements.add(ambulance);
                h.addAmbulance(ambulance);
                i++;
                if (i <= numberOfAmbulances)
                    break;
            }
        }

        for (int i = 0; i < hospitals.size(); i++) {
            Thread thread = new Thread(hospitals.get(i));
            thread.setDaemon(true);
            thread.start();
        }

        //postavljanje kuca
        int randomX, randomY;
        List<House> houses = new ArrayList<>();
        broj = Integer.parseInt(brojKuca);
        for (int i = 0; i < broj; i++) {
            randomX = new Random().nextInt(city.getDimension() - 2) + 1;
            randomY = new Random().nextInt(city.getDimension() - 2) + 1;
            while (city.getMap()[randomX][randomY].getElement() != null) {
                randomX = new Random().nextInt(city.getDimension());
                randomY = new Random().nextInt(city.getDimension());
            }
            House house = new House(i + "", randomX, randomY);
            city.getMap()[randomX][randomY].setElement(house);
            elements.add(house);
            houses.add(house);
        }

        //grupisanje stanovnika po kucama sve dok ih ima
        int pos;
        while (!citizens.isEmpty()) {
            pos = 0;
            for (House h : houses) {
                Citizen c = citizens.get(pos++);
                if (c.isAdult() || c.isElder()) {
                    h.addCitizen(c);
                    citizens.remove(c);
                } else if (c.isChild() && !h.isEmpty()) {
                    h.addCitizen(c);
                    citizens.remove(c);
                }
                if (citizens.isEmpty() || pos >= citizens.size() - 1)
                    break;
            }
        }

        //postavljanje punktova
        Checkpoint checkpoint;
        broj = Integer.parseInt(brojKontrolnihPunktova);
        int randX, randY;
        for (int i = 0; i < broj; i++) {
            randX = new Random().nextInt(city.getDimension() - 4) + 2;
            randY = new Random().nextInt(city.getDimension() - 4) + 2;
            while (city.getMap()[randX][randY].getElement() != null) {
                randX = new Random().nextInt(city.getDimension());
                randY = new Random().nextInt(city.getDimension());
            }
            checkpoint = new Checkpoint(randX, randY);
            threads.add(checkpoint);
            elements.add(checkpoint);
            city.getMap()[randX][randY].setElement(checkpoint);
            Thread thread = new Thread(checkpoint);
            thread.setDaemon(true);
            thread.start();
        }

        Main.setMainAppRunning(true);
        Thread nit = new Thread(() -> {
            FileWatcher watcher = new FileWatcher("C:\\Users\\WIN10\\Desktop\\PJ2\\src\\filesToWatch", brojZarazenihLabel, brojOporavljenihLabel);
            watcher.start();
        });
        nit.setDaemon(true);
        nit.start();

        //   House house = new House(11 + "", 6, 4);
        //   city.getMap()[6][4].setElement(house);

        //    Citizen milan = new Adult("1", "Milan", "Markovic");
        //    house.addCitizen(milan);

        //    Checkpoint checkpoint = new Checkpoint(6, 10);
        //     checkpoint.addHospital((Hospital) city.getMap()[0][0].getElement());

        //     Thread t = new Thread(milan);
        //     t.setDaemon(true);
        //    t.start();

        //     Thread check = new Thread(checkpoint);
        //    check.setDaemon(true);
        //    check.start();

        System.out.println("Initialization time = " + (double) (System.currentTimeMillis() - start) / 1000 + "s");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void zavrsiOnAction(MouseEvent mouseEvent) {
        long time = System.currentTimeMillis() - startTime;
        try {
            PrintWriter pw = new PrintWriter(new FileWriter("C:\\Users\\WIN10\\Desktop\\PJ2\\src\\output\\"
                    + "SIM-JavaKov-20-" + DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss").format(LocalDateTime.now()) + ".txt"));
            pw.println("Vrijeme trajanja " + (double) (time) / 1000 + "s");
            pw.println("Ambulantnih vozila kreirano " + elements.stream().filter(Element::isAmbulance).count());
            pw.println("Starih kreirano " + elements.stream().filter(e -> e.isCitizen() && ((Citizen) e).isElder()).count());
            pw.println("Odraslih kreirano " + elements.stream().filter(e -> e.isCitizen() && ((Citizen) e).isAdult()).count());
            pw.println("Djece kreirano " + elements.stream().filter(e -> e.isCitizen() && ((Citizen) e).isChild()).count());
            pw.println("Punktova kreirano " + elements.stream().filter(Element::isCheckPoint).count());
            pw.println("Ambulanti kreirano " + elements.stream().filter(Element::isHospital).count());
            pw.println("Kuca kreirano " + elements.stream().filter(e -> e.isHospitalOrHouse() && !e.isHospital()).count());
            pw.flush();
            pw.close();
        } catch (Exception ex) {
            Main.LOGGER.log( Level.SEVERE, ex.toString(), ex );
        }
        Main.getPrimaryStage().close();
    }

    public void showAlarm(String message) {
        Platform.runLater(() -> {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/forms/alarmForm.fxml"));
            Parent root = null;
            try {
                root = (Parent) loader.load();
            } catch (IOException ex) {
                Main.LOGGER.log( Level.SEVERE, ex.toString(), ex );
            }
            Stage stage = new Stage();
            stage.setTitle("Alarm");
            stage.setScene(new Scene(root, 400, 256));
            stage.show();
            Label label = (Label) root.lookup("#alarmLabel");
            label.setText(message);
        });
    }

    public void clickedAction(MouseEvent mouseEvent) {
        okButton.getScene().getWindow().hide();
    }

    public Hospital closestHospitalWithFreeSpace(int x, int y) {
        Hospital closestHospital = null;
        double distance, closestDistance = 1000;
        int xDistance, yDistance;
        for (Hospital hospital : hospitals) {
            xDistance = Math.abs(hospital.getPositionX() - x);
            yDistance = Math.abs(hospital.getPositionY() - y);
            distance = Math.sqrt(Math.pow(xDistance, 2) + Math.pow(yDistance, 2));
            if (distance < closestDistance && hospital.getCapacity() > 0) {
                closestHospital = hospital;
                System.out.println(closestHospital.getPositionX() + " " + closestHospital.getPositionX());
                closestDistance = distance;
            }
        }
        return closestHospital;
    }

    public Hospital closestHospitalWithAmbulance(int x, int y) {
        Hospital closestHospital = null;
        double distance, closestDistance = 1000;
        int xDistance, yDistance;
        for (Hospital hospital : hospitals) {
            xDistance = Math.abs(hospital.getPositionX() - x);
            yDistance = Math.abs(hospital.getPositionY() - y);
            distance = Math.sqrt(Math.pow(xDistance, 2) + Math.pow(yDistance, 2));
            if (distance < closestDistance && hospital.hasAmbulance()) {
                closestHospital = hospital;
                System.out.println(closestHospital.getPositionX() + " " + closestHospital.getPositionX());
                closestDistance = distance;
            }
        }
        return closestHospital;
    }

    public void sendAmbulanceButtonClicked(MouseEvent mouseEvent) {
        if (numberOfAmbulances <= 0) {
            System.out.println("nema dovoljno ambulantnih vozila!");
            return;
        }
        while (numberOfAmbulances > 0 && !Main.alarms.empty()) {
            Alarm alarm = Main.alarms.pop();
            Citizen citizen = alarm.getCitizen();
            closestHospitalWithAmbulance(citizen.getPositionX(), citizen.getPositionY())
                    .sendAmbulance(alarm.getCitizen(), closestHospitalWithFreeSpace(citizen.getPositionX(), citizen.getPositionY()));
            numberOfAmbulances--;
        }
    }

    public void startSimulation() {
        Main.startTime = System.currentTimeMillis();
        System.out.println("Simulation started!");
        for (Runnable runnable : threads) {
            if (!((Element) runnable).isAmbulance()) {
                Thread thread = new Thread(runnable);
                thread.setDaemon(true);
                thread.start();
            }
        }
    }

    public void startButtonClicked(MouseEvent mouseEvent) {
        startSimulation();
    }

    public void checkHospitalsClicked(MouseEvent mouseEvent) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/forms/ambulances.fxml"));
        Parent root = null;
        try {
            root = (Parent) loader.load();
        } catch (IOException ex) {
            Main.LOGGER.log( Level.SEVERE, ex.toString(), ex );
        }
        AmbulanceController controller = loader.getController();
        controller.initListView(hospitals);
        Stage newStage = new Stage();
        newStage.setTitle("Ambulances");
        newStage.setScene(new Scene(root, 400, 300));
        newStage.show();
    }

    public void pregledajStatistikuClicked(MouseEvent mouseEvent) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/forms/statistic.fxml"));
        Parent root = null;
        try {
            root = (Parent) loader.load();
        } catch (IOException ex) {
            Main.LOGGER.log( Level.SEVERE, ex.toString(), ex );
        }
        StatisticController controller = loader.getController();
        controller.initStatistic();
        Stage newStage = new Stage();
        newStage.setTitle("Statistic");
        newStage.setScene(new Scene(root, 600, 400));
        newStage.show();
    }

    public void pauzirajClicked(MouseEvent mouseEvent) {
        //pauziraj sve niti
        Main.stop = true;
        for (Runnable runnable : Controller.threads) {
            Element element = (Element) runnable;
            if (element.isCitizen()) {
                ((Citizen) element).pause();
            } else if (element.isCheckPoint()) {
                ((Checkpoint) element).pause();
            } else if (element.isHospital()) {
                ((Hospital) element).pause();
            } else if (element.isAmbulance()) {
                ((Ambulance) element).pause();
            }
        }
        //serijalizuj
    }

    public void pokreniPonovoClicked(MouseEvent mouseEvent) {
        //deserijalizuj
        Main.stop = false;


        //probudi niti
        for (Runnable runnable : Controller.threads) {
            Element element = (Element) runnable;
            if (element.isCitizen()) {
                ((Citizen) element).resume();
            } else if (element.isCheckPoint()) {
                ((Checkpoint) element).resume();
            } else if (element.isHospital()) {
                ((Hospital) element).resume();
            } else if (element.isAmbulance()) {
                ((Ambulance) element).resume();
            }
        }
    }
}
