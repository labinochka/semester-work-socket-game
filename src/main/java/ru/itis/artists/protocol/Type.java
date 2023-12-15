package ru.itis.artists.protocol;

public enum Type {

    PLAYER_CONNECTED(""),
    PLAYER_DRAWS(""),
    STOP("");

    private final String title;

    Type(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
