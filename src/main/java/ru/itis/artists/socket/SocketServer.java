package ru.itis.artists.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocketServer {

    private final List<Client> clients = new ArrayList<>();

    private final int PLAYERS_NUMBER = 2;

    private static SocketServer socketServer;

    public static SocketServer getInstance() {
        if (socketServer == null) {
            socketServer = new SocketServer();
        }
        return socketServer;
    }

    public boolean isEnoughPlayers() {
        return clients.size() == PLAYERS_NUMBER;
    }

    public List<Client> getClients() {
        return clients;
    }

    public void start(int port) {
        new Thread() {
            ServerSocket serverSocket;
            {
                try {
                    serverSocket = new ServerSocket(port);

                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        Client client = new Client(clientSocket);
                        clients.add(client);

                        client.start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
