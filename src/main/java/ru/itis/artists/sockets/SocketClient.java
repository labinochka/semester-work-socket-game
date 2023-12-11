package ru.itis.artists.sockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import ru.itis.artists.controllers.PaintController;
import ru.itis.artists.protocols.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient extends Thread {
    private final Socket client;

    private PaintController controller;

    private final PrintWriter printWriter; // на SocketServer fromClient
    private final BufferedReader bufferedReader; // на SocketServer toClient

    public SocketClient(String host, int port) {
        try {
            client = new Socket(host, port);
            printWriter = new PrintWriter(client.getOutputStream(), true);
            bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void sendMessage(Message message) {
        try {
            String jsonMessage = new ObjectMapper().writeValueAsString(message);
            printWriter.println(jsonMessage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                String futureMessage = bufferedReader.readLine();
                ObjectMapper objectMapper = new ObjectMapper();
                Message message = objectMapper.readValue(futureMessage, Message.class);

                switch (message.getType()) {
                    case PLAYER_CONNECTED -> {
                        System.out.println(message.getType());
                        Platform.runLater(() -> controller.startGame());
                    }
                    case PLAYER_DRAWS -> {
                        System.out.println(message.getType());
                        String[] coordinates = message.getBody().split(",");
                        Platform.runLater(() -> controller.fillRect(coordinates[0], coordinates[1], coordinates[2], coordinates[3]));
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

    public PaintController getController() {
        return controller;
    }

    public void setController(PaintController controller) {
        this.controller = controller;
    }
}
