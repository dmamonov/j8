package org.xgame.context.impl;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.function.Function;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Iterables.transform;

/**
 * @author dmitry.mamonov
 *         Created: 2014-08-19 12:07 AM
 */
public class Server extends WebSocketServer {
    private final Function<Long, String> stateProvider;
    private volatile ImmutableSet<Integer> pressedKeys = ImmutableSet.of();

    public ImmutableSet<Integer> getPressedKeys() {
        return pressedKeys;
    }

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

    private String lastState = "{}";

    @Override
    public void onMessage(final WebSocket webSocket, final String data) {
        //System.out.println("Reply: " + data);
        if (data != null && data.startsWith("{")) {
            final JsonObject pressedJson = new JsonParser().parse(data).getAsJsonObject();
            this.pressedKeys = copyOf(transform(pressedJson.entrySet(), e -> Integer.parseInt(e.getKey())));
        }
        final String newState = stateProvider.apply(System.currentTimeMillis());
        if (!lastState.equals(newState)) {
            webSocket.send(newState);
        }
    }

    @Override
    public void onError(final WebSocket webSocket, final Exception e) {
        e.printStackTrace();
    }

    public static Server create(final Function<Long, String> stateProvider){
        try {
            return new Server(stateProvider);
        } catch (final UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

}
