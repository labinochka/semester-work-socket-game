package ru.itis.artists.application;

import ru.itis.artists.socket.SocketServer;

public class Server {
    public static void main(String[] args) {
        SocketServer server = SocketServer.getInstance();

        server.start(71);
    }
}
