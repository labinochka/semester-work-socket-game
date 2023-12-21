package ru.itis.artists.protocol;

public enum Type {

    PLAYER_CONNECTED(""),

    START(""),

    PLAYER_DRAWS(""),

    PLAYER_CLEAR(""),

    STOP("");

    private final String title;

    Type(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
