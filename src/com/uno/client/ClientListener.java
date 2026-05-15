package com.uno.client;

import com.uno.model.Message;
import java.io.IOException;
import java.io.ObjectInputStream;

public class ClientListener implements Runnable {
    private ObjectInputStream in;
    private Client client;
    private boolean running;

    public ClientListener(ObjectInputStream in, Client client) {
        this.in = in;
        this.client = client;
        this.running = true;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Message msg = (Message) in.readObject();
                if (msg != null) {
                    client.onMessageReceived(msg);
                }
            } catch (IOException | ClassNotFoundException e) {
                if (running) {
                    System.out.println("Connection lost.");
                    running = false;
                }
            }
        }
    }

    public void stop() {
        running = false;
    }
}
