package model.citizens;

import javafx.scene.image.Image;
import main.Main;

import java.io.File;
import java.util.Random;

public class Adult extends Citizen {

    public Adult(String personId, String name, String lastName) {
        super(personId, name, lastName, 2020 - (new Random().nextInt(47) + 18));
        setRadius((int) Math.ceil(0.25 * Main.city.getDimension()));
        setPicture(new Image(new File("src/resources/adult.jpg").toURI().toString()));
        setAdult(true);
    }
}
