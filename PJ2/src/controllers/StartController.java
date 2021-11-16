package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import main.Main;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class StartController implements Initializable {
    @FXML
    private TextField brojDjeceTextField;
    @FXML
    private TextField brojOdraslihTextField;
    @FXML
    private TextField brojStarihTextField;
    @FXML
    private TextField brojKucaTextField;
    @FXML
    private TextField brojKontrolnihPunktovaTextField;
    @FXML
    private TextField brojAmbulantnihVozilaTextField;
    @FXML
    private Button startButton;

    public StartController() {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> startButton.requestFocus());
        startButton.setOnAction((action) -> {
            if (brojAmbulantnihVozilaTextField.getText().equals("") || brojKontrolnihPunktovaTextField.getText().equals("") || brojKucaTextField.getText().equals("") ||
                    brojStarihTextField.getText().equals("") || brojOdraslihTextField.getText().equals("") || brojDjeceTextField.getText().equals("")) {
                System.out.println("Niste unijeli sve parametre!");
                return;
            }

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/forms/sample.fxml"));
            Parent root = null;
            try {
                root = (Parent) loader.load();
            } catch (IOException ex) {
                Main.LOGGER.log(Level.SEVERE, ex.toString(), ex );
            }
            Controller c = loader.getController();
            c.setGrid(brojKucaTextField.getText(), brojAmbulantnihVozilaTextField.getText(), brojKontrolnihPunktovaTextField.getText(),
                    brojStarihTextField.getText(), brojOdraslihTextField.getText(), brojDjeceTextField.getText());
            Main.controller = c;
            Main.textArea = c.getArea();
            Stage primaryStage = Main.getPrimaryStage();
            primaryStage.setScene(new Scene(root, 960, 700));
            primaryStage.show();
        });
    }
}
