package ru.itis.artists.socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.itis.artists.protocol.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client extends Thread {

    private final BufferedReader fromServer;

    private final PrintWriter toServer;

    private final Socket socket;

    private final SocketServer socketServer;

    private boolean running = true;


    public Client(Socket socket) {
        try {
            this.socket = socket;
            socketServer = SocketServer.getInstance();

            toServer = new PrintWriter(socket.getOutputStream(), true);
            fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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

    private void sendMessageToOthers(Message message, boolean isForAll) {
        for (Client other: socketServer.getClients()) {
            if (isForAll || !this.equals(other)){
                other.sendMessage(message);
            }
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                String futureMessage = fromServer.readLine();
                ObjectMapper objectMapper = new ObjectMapper();
                Message message = objectMapper.readValue(futureMessage, Message.class);

                switch (message.getType()){
                    case PLAYER_CONNECTED:{
                        if (socketServer.isEnoughPlayers())
                            sendMessageToOthers(message, true);
                        break;
                    }
                    case PLAYER_DRAWS:{
                        sendMessageToOthers(message, false);
                        break;
                    }
                    case STOP:{
                        running = false;
                        socketServer.getClients().remove(this);
                        sendMessageToOthers(message,false);
                        break;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}