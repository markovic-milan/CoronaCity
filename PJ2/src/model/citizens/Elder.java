package model.citizens;

import javafx.scene.image.Image;

import java.io.File;
import java.util.Random;

public class Elder extends Citizen {

    public Elder(String personId, String name, String lastName) {
        super(personId, name, lastName, 2020 - (new Random().nextInt(20) + 65));
        setRadius(3);
        setPicture(new Image(new File("src/resources/elder.jpg").toURI().toString()));
        setElder(true);
    }
}
