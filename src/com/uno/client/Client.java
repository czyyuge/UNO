package com.uno.client;

import com.uno.model.Message;
import com.uno.ui.GameFrame;
import com.uno.ui.LoginFrame;
import com.uno.ui.RoomFrame;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;

public class Client {
    private String host;
    private int port;
    private String username;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ClientListener listener;
    private Thread listenerThread;
    private Consumer<Message> messageHandler;
    private LoginFrame loginFrame;
    private RoomFrame roomFrame;
    private GameFrame gameFrame;

    public Client(String host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = username;
    }

    public boolean connect() {
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            listener = new ClientListener(in, this);
            listenerThread = new Thread(listener);
            listenerThread.start();
            send(new Message(Message.LOGIN, username, null));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void send(Message msg) {
        try {
            if (out != null) {
                out.reset();
                out.writeObject(msg);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            send(new Message(Message.DISCONNECT, username, null));
            if (listener != null) listener.stop();
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setMessageHandler(Consumer<Message> handler) {
        this.messageHandler = handler;
    }

    public void onMessageReceived(Message msg) {
        if (messageHandler != null) {
            messageHandler.accept(msg);
        }
    }

    public String getUsername() {
        return username;
    }

    public LoginFrame getLoginFrame() {
        return loginFrame;
    }

    public void setLoginFrame(LoginFrame loginFrame) {
        this.loginFrame = loginFrame;
    }

    public RoomFrame getRoomFrame() {
        return roomFrame;
    }

    public void setRoomFrame(RoomFrame roomFrame) {
        this.roomFrame = roomFrame;
    }

    public GameFrame getGameFrame() {
        return gameFrame;
    }

    public void setGameFrame(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
    }
}
