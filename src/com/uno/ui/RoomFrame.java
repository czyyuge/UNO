package com.uno.ui;

import com.uno.client.Client;
import com.uno.model.GameRoom;
import com.uno.model.Message;
import com.uno.model.Player;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

public class RoomFrame extends JFrame {
    private Client client;
    private JList<String> roomList;
    private DefaultListModel<String> roomListModel;
    private JList<String> playerList;
    private DefaultListModel<String> playerListModel;
    private JTextArea chatArea;
    private JTextField chatField;
    private JButton createRoomButton;
    private JButton joinRoomButton;
    private JButton readyButton;
    private JButton startButton;
    private JButton leaveButton;
    private JButton refreshButton;
    private String currentRoomId;
    private boolean inRoom;

    public RoomFrame(Client client) {
        this.client = client;
        setTitle("UNO - Rooms | " + client.getUsername());
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        client.setMessageHandler(this::handleMessage);
        client.send(new Message(Message.ROOM_LIST));
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Left: Room list
        JPanel roomPanel = new JPanel(new BorderLayout());
        roomPanel.setBorder(new TitledBorder("Rooms"));
        roomListModel = new DefaultListModel<>();
        roomList = new JList<>(roomListModel);
        roomPanel.add(new JScrollPane(roomList), BorderLayout.CENTER);

        JPanel roomButtonPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        refreshButton = new JButton("Refresh");
        createRoomButton = new JButton("Create Room");
        roomButtonPanel.add(refreshButton);
        roomButtonPanel.add(createRoomButton);
        roomPanel.add(roomButtonPanel, BorderLayout.SOUTH);

        add(roomPanel, BorderLayout.WEST);

        // Center: Player list in current room
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new TitledBorder("Current Room"));
        playerListModel = new DefaultListModel<>();
        playerList = new JList<>(playerListModel);
        centerPanel.add(new JScrollPane(playerList), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        joinRoomButton = new JButton("Join Room");
        readyButton = new JButton("Ready");
        startButton = new JButton("Start Game");
        leaveButton = new JButton("Leave Room");
        actionPanel.add(joinRoomButton);
        actionPanel.add(readyButton);
        actionPanel.add(startButton);
        actionPanel.add(leaveButton);
        centerPanel.add(actionPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        // Bottom: Chat
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBorder(new TitledBorder("Chat"));
        chatArea = new JTextArea(5, 30);
        chatArea.setEditable(false);
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        chatField = new JTextField();
        chatPanel.add(chatField, BorderLayout.SOUTH);
        add(chatPanel, BorderLayout.SOUTH);

        // Listeners
        refreshButton.addActionListener(e -> client.send(new Message(Message.ROOM_LIST)));
        createRoomButton.addActionListener(e -> createRoom());
        joinRoomButton.addActionListener(e -> joinRoom());
        readyButton.addActionListener(e -> {
            if (inRoom) client.send(new Message(Message.PLAYER_READY, client.getUsername(), null));
        });
        startButton.addActionListener(e -> {
            if (inRoom) client.send(new Message(Message.START_GAME));
        });
        leaveButton.addActionListener(e -> {
            if (inRoom) {
                client.send(new Message(Message.LEAVE_ROOM));
                inRoom = false;
                currentRoomId = null;
                playerListModel.clear();
            }
        });
        chatField.addActionListener(e -> {
            String text = chatField.getText().trim();
            if (!text.isEmpty()) {
                client.send(new Message(Message.CHAT, client.getUsername(), text));
                chatField.setText("");
            }
        });

        updateButtonStates();
    }

    private void createRoom() {
        String name = JOptionPane.showInputDialog(this, "Enter room name:");
        if (name != null && !name.trim().isEmpty()) {
            client.send(new Message(Message.CREATE_ROOM, null, name.trim()));
        }
    }

    private void joinRoom() {
        String selected = roomList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a room.");
            return;
        }
        String roomId = selected.split(" ")[0];
        currentRoomId = roomId;
        inRoom = true;
        client.send(new Message(Message.JOIN_ROOM, null, roomId));
        updateButtonStates();
    }

    private void updateButtonStates() {
        joinRoomButton.setEnabled(!inRoom);
        readyButton.setEnabled(inRoom);
        startButton.setEnabled(inRoom);
        leaveButton.setEnabled(inRoom);
    }

    @SuppressWarnings("unchecked")
    private void handleMessage(Message msg) {
        SwingUtilities.invokeLater(() -> {
            switch (msg.getType()) {
                case Message.ROOM_LIST:
                    roomListModel.clear();
                    List<GameRoom> rooms = (List<GameRoom>) msg.getData("rooms");
                    if (rooms != null) {
                        for (GameRoom room : rooms) {
                            roomListModel.addElement(room.getRoomId() + " - " + room.getRoomName()
                                    + " (" + room.getPlayerCount() + "/4)");
                        }
                    }
                    break;
                case Message.PLAYER_JOIN:
                    appendChat("[System] " + msg.getSender() + " joined the room.");
                    break;
                case Message.PLAYER_LEAVE:
                    appendChat("[System] " + msg.getSender() + " left the room.");
                    break;
                case Message.PLAYER_READY:
                    appendChat("[System] " + msg.getSender() + " is ready.");
                    break;
                case Message.UPDATE_ROOM:
                    playerListModel.clear();
                    List<Player> players = (List<Player>) msg.getData("players");
                    if (players != null) {
                        for (Player p : players) {
                            String status = p.isReady() ? " [Ready]" : "";
                            playerListModel.addElement(p.getName() + " (" + p.getCardCount() + " cards)" + status);
                        }
                    }
                    break;
                case Message.CHAT:
                    appendChat(msg.getSender() + ": " + msg.getContent());
                    break;
                case Message.GAME_START:
                    GameFrame gameFrame = new GameFrame(client);
                    client.setGameFrame(gameFrame);
                    gameFrame.setVisible(true);
                    dispose();
                    break;
                case Message.ERROR:
                    JOptionPane.showMessageDialog(this, msg.getContent());
                    break;
                default:
                    break;
            }
        });
    }

    private void appendChat(String text) {
        chatArea.append(text + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
}
