package main;

import controllers.Controller;
import controllers.StartController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Alarm;
import model.CityMap;
import model.citizens.Citizen;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class Main extends Application {
    public static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    public static long startTime;
    public static boolean mainAppRunning = true, stop = false;
    public static String filePath = "C:\\Users\\WIN10\\Desktop\\PJ2\\src\\filesToWatch\\podaci.txt";
    public static ReentrantLock fileLock = new ReentrantLock();
    public static CityMap city;
    public static StackPane[][] pane;
    public static TextArea textArea;
    public static Stack<Alarm> alarms = new Stack<>();
    public static Controller controller;
    private static Stage primaryStage;
    public static List<Citizen> zarazeni = new ArrayList<>();
    public static List<Citizen> oporavljeni = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/forms/startForm.fxml"));
        Parent root = (Parent) loader.load();
        StartController c = loader.getController();
        primaryStage.setTitle("CoronaCity");
        setPrimaryStage(primaryStage);
        primaryStage.setScene(new Scene(root, 250, 350));
        primaryStage.show();
    }

    public static void main(String[] args) {
        city = new CityMap();
        launch(args);
    }

    public static boolean isMainAppRunning() {
        return mainAppRunning;
    }

    public static void setMainAppRunning(boolean mainAppRunning) {
        Main.mainAppRunning = mainAppRunning;
    }

    private void setPrimaryStage(Stage stage) {
        Main.primaryStage = stage;
    }

    static public Stage getPrimaryStage() {
        return Main.primaryStage;
    }
}