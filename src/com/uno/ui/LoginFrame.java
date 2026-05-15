package com.uno.ui;

import com.uno.client.Client;
import com.uno.model.Message;
import com.uno.util.GameConstants;
import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField hostField;
    private JTextField portField;
    private JTextField nameField;
    private JButton connectButton;
    private JLabel statusLabel;
    private Client client;

    public LoginFrame() {
        setTitle("UNO - Login");
        setSize(350, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Server IP:"), gbc);
        gbc.gridx = 1;
        hostField = new JTextField("127.0.0.1", 15);
        panel.add(hostField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Port:"), gbc);
        gbc.gridx = 1;
        portField = new JTextField(String.valueOf(GameConstants.DEFAULT_PORT), 15);
        panel.add(portField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(15);
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        connectButton = new JButton("Connect");
        panel.add(connectButton, gbc);

        gbc.gridy = 4;
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        panel.add(statusLabel, gbc);

        add(panel);

        connectButton.addActionListener(e -> connect());
        getRootPane().setDefaultButton(connectButton);
    }

    private void connect() {
        String host = hostField.getText().trim();
        String portStr = portField.getText().trim();
        String name = nameField.getText().trim();

        if (host.isEmpty() || portStr.isEmpty() || name.isEmpty()) {
            statusLabel.setText("Please fill all fields.");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException ex) {
            statusLabel.setText("Invalid port.");
            return;
        }

        connectButton.setEnabled(false);
        statusLabel.setText("Connecting...");

        client = new Client(host, port, name);
        client.setMessageHandler(this::handleMessage);
        client.setLoginFrame(this);

        if (client.connect()) {
            statusLabel.setText("Connected. Waiting for server...");
        } else {
            statusLabel.setText("Connection failed.");
            connectButton.setEnabled(true);
        }
    }

    private void handleMessage(Message msg) {
        SwingUtilities.invokeLater(() -> {
            switch (msg.getType()) {
                case Message.LOGIN_SUCCESS:
                    statusLabel.setText("Login successful!");
                    RoomFrame roomFrame = new RoomFrame(client);
                    client.setRoomFrame(roomFrame);
                    roomFrame.setVisible(true);
                    dispose();
                    break;
                case Message.LOGIN_FAIL:
                    statusLabel.setText("Login failed: " + msg.getContent());
                    connectButton.setEnabled(true);
                    break;
                default:
                    break;
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
