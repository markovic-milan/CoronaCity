package model.citizens;

import javafx.scene.image.Image;

import java.io.File;
import java.util.Random;

public class Child extends Citizen {

    public Child(String personId, String name, String lastName) {
        super(personId, name, lastName, 2020 - (new Random().nextInt(18)));
        setRadius(5);
        setPicture(new Image(new File("src/resources/child.jpg").toURI().toString()));
        setChild(true);
    }
}
