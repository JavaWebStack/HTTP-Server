package org.javawebstack.httpserver.websocket;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;

import java.util.HashMap;
import java.util.Map;

@org.eclipse.jetty.websocket.api.annotations.WebSocket
public class InternalWebSocketAdapter {
    public static Map<String, WebSocket> webSockets = new HashMap<>();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        WebSocket socket = webSockets.get(session.getUpgradeResponse().getHeader("X-Server-WSID"));
        if (socket == null) {
            session.close(500, "Server Error");
            return;
        }
        socket.setSession(session);
        try {
            socket.getHandler().onConnect(socket);
        } catch (Throwable t) {
            socket.getExchange().getServer().getExceptionHandler().handle(socket.getExchange(), t);
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        WebSocket socket = webSockets.get(session.getUpgradeResponse().getHeader("X-Server-WSID"));
        if (socket == null) {
            session.close(500, "Server Error");
            return;
        }
        try {
            socket.getHandler().onMessage(socket, message);
        } catch (Throwable t) {
            socket.getExchange().getServer().getExceptionHandler().handle(socket.getExchange(), t);
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int code, String reason) {
        WebSocket socket = webSockets.get(session.getUpgradeResponse().getHeader("X-Server-WSID"));
        if (socket != null) {
            try {
                socket.getHandler().onClose(socket, code, reason);
            } catch (Throwable t) {
                socket.getExchange().getServer().getExceptionHandler().handle(socket.getExchange(), t);
            }
            webSockets.remove(session.getUpgradeResponse().getHeader("X-Server-WSID"));
        }
    }
}
