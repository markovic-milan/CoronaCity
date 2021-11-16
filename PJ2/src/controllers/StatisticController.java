package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import model.citizens.Citizen;
import model.citizens.Gender;
import main.Main;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class StatisticController implements Initializable {

    @FXML
    private ListView<String> statisticListView;
    @FXML
    private TextField textField;
    @FXML
    private Button defaultButton;
    @FXML
    private Button preuzmiButton;
    private String path = "";

    public StatisticController() {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void preuzmiButtonClicked(MouseEvent mouseEvent) {
        preuzmiButton.setDisable(true);
        path = textField.getText();

        try {
            upisiUCSV();
        } catch (Exception ex) {
            Main.LOGGER.log( Level.SEVERE, ex.toString(), ex );
        }
    }

    public void defaultButtonClicked(MouseEvent mouseEvent) {
        Platform.runLater(() -> textField.setText("C:\\Users\\WIN10\\Desktop\\PJ2\\src\\statistika\\statistika.csv"));
    }

    public void upisiUCSV() {
        System.out.println(path);
        try {
            FileOutputStream fos = new FileOutputStream("C:\\Users\\WIN10\\Desktop\\PJ2\\src\\statistika\\stats.csv", true);
            PrintWriter pw = new PrintWriter(fos);
            List<String> strings = statisticListView.getItems().stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
            pw.println("SEP=,");
            pw.println("Tip, Broj");
            int i = 0;
            for (String s : strings) {
                i++;
                if (i == 7) pw.println("" + "," + "");
                pw.println(s.split(" ")[0] + ", " + s.split(" ")[1]);
            }
            pw.flush();
            pw.close();
        } catch (FileNotFoundException ex) {
            Main.LOGGER.log( Level.SEVERE, ex.toString(), ex );
        }
    }

    public void initStatistic() {
        Platform.runLater(() -> {
            statisticListView.getItems().add("Zarazenih " + Main.zarazeni.size());
            statisticListView.getItems().add("Muskih " + Main.zarazeni.stream().filter(z -> z.getGender() == Gender.M).count());
            statisticListView.getItems().add("Zenskih " + Main.zarazeni.stream().filter(z -> z.getGender() == Gender.F).count());
            statisticListView.getItems().add("Djece " + Main.zarazeni.stream().filter(Citizen::isChild).count());
            statisticListView.getItems().add("Odraslih " + Main.zarazeni.stream().filter(Citizen::isAdult).count());
            statisticListView.getItems().add("Starih " + Main.zarazeni.stream().filter(Citizen::isElder).count());
            statisticListView.getItems().add("Oporavljenih " + Main.oporavljeni.size());
            statisticListView.getItems().add("Muskih " + Main.oporavljeni.stream().filter(z -> z.getGender() == Gender.M).count());
            statisticListView.getItems().add("Zenskih " + Main.oporavljeni.stream().filter(z -> z.getGender() == Gender.F).count());
            statisticListView.getItems().add("Djece " + Main.oporavljeni.stream().filter(Citizen::isChild).count());
            statisticListView.getItems().add("Odraslih " + Main.oporavljeni.stream().filter(Citizen::isAdult).count());
            statisticListView.getItems().add("Starih " + Main.oporavljeni.stream().filter(Citizen::isElder).count());
        });
    }
}
