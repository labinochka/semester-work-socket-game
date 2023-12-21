package ru.itis.artists.socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import ru.itis.artists.controller.PaintController;
import ru.itis.artists.protocol.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient extends Thread {

    private final Socket client;

    private final PrintWriter toServer;

    private final BufferedReader fromServer;

    private PaintController controller;

    public SocketClient(String host, int port) {
        try {
            client = new Socket(host, port);
            toServer = new PrintWriter(client.getOutputStream(), true);
            fromServer = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void sendMessage(Message message) {
        try {
            String jsonMessage = new ObjectMapper().writeValueAsString(message);
            toServer.println(jsonMessage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                String futureMessage = fromServer.readLine();
                ObjectMapper objectMapper = new ObjectMapper();
                Message message = objectMapper.readValue(futureMessage, Message.class);

                switch (message.getType()) {
                    case PLAYER_CONNECTED -> {
                        Platform.runLater(() -> controller.startGame());
                    }
                    case START -> {
                        System.out.println(message.getType());
                        Platform.runLater(() -> controller.setTask(message.getBody()));
                    }
                    case PLAYER_DRAWS -> {
                        System.out.println(message.getType());
                        String[] coordinates = message.getBody().split(",");
                        Platform.runLater(() -> controller.fillRect(coordinates[0], coordinates[1], coordinates[2], coordinates[3]));
                    }
                    case PLAYER_CLEAR -> {
                        System.out.println(message.getType());
                        String[] coordinates = message.getBody().split(",");
                        Platform.runLater(() -> controller.clearRect(coordinates[0], coordinates[1], coordinates[2]));
                    }
                    case STOP -> {
                        System.out.println(message.getType());
                        Platform.runLater(() -> controller.stopGame());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setController(PaintController controller) {
        this.controller = controller;
    }
}
