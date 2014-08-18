package org.xgame.context.impl;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Random;

/**
 * @author dmitry.mamonov
 *         Created: 2014-08-19 12:07 AM
 */
public class Server extends WebSocketServer {
    private static final String structure = "{\"x\":%f,\"y\":%f}";
    private static final Random random = new Random();
    private static String generatePoint() {
        return String.format(structure, random.nextDouble() * 640, random.nextDouble() * 480);
    }

    public Server() throws UnknownHostException {
        super(new InetSocketAddress(8081));
    }

    @Override
    public void onOpen(final WebSocket webSocket, final ClientHandshake clientHandshake) {
        System.out.println("Open");
        webSocket.send(generatePoint());
    }

    @Override
    public void onClose(final WebSocket webSocket, final int i, final String s, final boolean b) {
        System.out.println("Closed");
    }

    @Override
    public void onMessage(final WebSocket webSocket, final String s) {
        System.out.println("Reply: "+s);
        webSocket.send(generatePoint());
    }

    @Override
    public void onError(final WebSocket webSocket, final Exception e) {
        e.printStackTrace();
    }

    public static void main(final String[] args) throws UnknownHostException, InterruptedException {
        new Server().start();
        while (true) {
            Thread.sleep(1000);
        }
    }
}
