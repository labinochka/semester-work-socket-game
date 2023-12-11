package ru.itis.artists.protocols;

public class Message {

    Type type;
    String body;

    public Message() {
    }

    public Type getType() {
        return type;
    }

    public String getBody() {
        return body;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setBody(String body) {
        this.body = body;
    }

}
