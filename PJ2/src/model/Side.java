package model;

public enum Side {
    UP("UP"),
    DOWN("DOWN"),
    LEFT("LEFT"),
    RIGHT("RIGHT");

    private String message;

    Side(String m) {
        message = m;
    }
    public String toString() {
        return message;
    }
}
