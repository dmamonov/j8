package org.xgame.context.impl;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.function.Function;

/**
 * @author dmitry.mamonov
 *         Created: 2014-08-19 12:07 AM
 */
public class Server extends WebSocketServer {
    private final Function<Long, String> stateProvider;

    public Server(final Function<Long, String> stateProvider) throws UnknownHostException {
        super(new InetSocketAddress(8081));
        this.stateProvider = stateProvider;
    }

    @Override
    public void onOpen(final WebSocket webSocket, final ClientHandshake clientHandshake) {
        System.out.println("Open");
        webSocket.send(stateProvider.apply(System.currentTimeMillis()));
    }

    @Override
    public void onClose(final WebSocket webSocket, final int i, final String s, final boolean b) {
        System.out.println("Closed");
        //System.exit(0);
    }

    @Override
    public void onMessage(final WebSocket webSocket, final String s) {
        System.out.println("Reply: " + s);
        webSocket.send(stateProvider.apply(System.currentTimeMillis()));
    }

    @Override
    public void onError(final WebSocket webSocket, final Exception e) {
        e.printStackTrace();
    }

    public static void demo(final String json) {
        try {
            new Server(version -> json).start();
        } catch (final UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            Desktop.getDesktop().open(new File("context/src/main/resources/frontend.html"));
        } catch (final IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {

                Thread.sleep(1000L);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
