package model.citizens;

public enum Gender {
    M("MALE"),
    F("FEMALE");

    private String message;

    Gender(String m) {
        message = m;
    }

    public String toString() {
        return message;
    }
}
