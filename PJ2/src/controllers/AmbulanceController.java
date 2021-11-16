package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import model.Hospital;
import model.citizens.Citizen;
import main.Main;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class AmbulanceController implements Initializable {
    @FXML
    private Button addButton;
    @FXML
    private ListView<String> listView;

    public AmbulanceController() {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void initListView(List<Hospital> hospitalList) {
        for (Hospital hospital : hospitalList) {
            Platform.runLater(() -> {
                listView.getItems().add("Hospital " + hospital.getPositionX() + " " + hospital.getPositionY() + " kapacitet= " + hospital.getCapacity());
            });
        }
    }

    public void addClickedAction(MouseEvent mouseEvent) {
        for (int i = 0; i < Main.city.getDimension(); i++) {
            if (Main.city.getMap()[0][i].getElement() == null) {
                Hospital hospital = new Hospital(0, i, Citizen.numberOfCitizens);
                Controller.hospitals.add(hospital);
                Controller.threads.add(hospital);
                Controller.elements.add(hospital);
                Platform.runLater(() -> {
                    listView.getItems().add("Hospital " + hospital.getPositionX() + " " + hospital.getPositionY() + " kapacitet= " + hospital.getCapacity());
                });
                return;
            }
        }
    }
}
