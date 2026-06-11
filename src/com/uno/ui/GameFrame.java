package com.uno.ui;

import com.uno.client.Client;
import com.uno.model.Card;
import com.uno.model.Message;
import com.uno.model.Player;
import com.uno.util.CardUtils;
import com.uno.util.GameConstants;
import com.uno.util.MessageUtils;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

public class GameFrame extends JFrame {
    private Client client;
    private JPanel opponentsPanel;
    private JButton topCardButton;
    private JPanel handPanel;
    private JButton drawButton;
    private JButton unoButton;
    private JTextArea chatArea;
    private JTextField chatField;
    private JLabel statusLabel;
    private JLabel directionLabel;
    private java.util.List<Player> players;
    private java.util.List<Card> myHand;
    private Card topCard;
    private String currentPlayerName;
    private String myName;
    private String direction;

    public GameFrame(Client client) {
        this.client = client;
        this.myName = client.getUsername();
        setTitle("UNO - Game | " + myName);
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        client.setMessageHandler(this::handleMessage);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Top: Opponents and status
        JPanel northPanel = new JPanel(new BorderLayout());
        opponentsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        opponentsPanel.setBorder(new TitledBorder("Players"));
        northPanel.add(opponentsPanel, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(new FlowLayout());
        statusLabel = new JLabel("Waiting for game state...");
        directionLabel = new JLabel("Direction: -");
        infoPanel.add(statusLabel);
        infoPanel.add(directionLabel);
        northPanel.add(infoPanel, BorderLayout.SOUTH);

        add(northPanel, BorderLayout.NORTH);

        // Center: Table (discard pile)
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(0, 100, 0));
        topCardButton = new JButton("UNO");
        topCardButton.setPreferredSize(new Dimension(140, 180));
        topCardButton.setFont(new Font("Arial", Font.BOLD, 14));
        topCardButton.setMargin(new Insets(2, 2, 2, 2));
        topCardButton.setEnabled(false);
        centerPanel.add(topCardButton);
        add(centerPanel, BorderLayout.CENTER);

        // East: Chat
        JPanel eastPanel = new JPanel(new BorderLayout());
        eastPanel.setPreferredSize(new Dimension(250, 0));
        eastPanel.setBorder(new TitledBorder("Chat"));
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        eastPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        chatField = new JTextField();
        eastPanel.add(chatField, BorderLayout.SOUTH);
        add(eastPanel, BorderLayout.EAST);

        // South: My hand and actions
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(new TitledBorder("Your Hand"));
        handPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JScrollPane handScroll = new JScrollPane(handPanel);
        handScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        handScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        handScroll.setPreferredSize(new Dimension(0, 160));
        southPanel.add(handScroll, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout());
        drawButton = new JButton("Draw Card");
        unoButton = new JButton("Call UNO!");
        actionPanel.add(drawButton);
        actionPanel.add(unoButton);
        southPanel.add(actionPanel, BorderLayout.SOUTH);

        add(southPanel, BorderLayout.SOUTH);

        // Listeners
        drawButton.addActionListener(e -> client.send(MessageUtils.createDrawCardMessage(myName)));
        unoButton.addActionListener(e -> client.send(MessageUtils.createCallUnoMessage(myName)));
        chatField.addActionListener(e -> {
            String text = chatField.getText().trim();
            if (!text.isEmpty()) {
                client.send(MessageUtils.createChatMessage(myName, text));
                chatField.setText("");
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void handleMessage(Message msg) {
        SwingUtilities.invokeLater(() -> {
            switch (msg.getType()) {
                case Message.GAME_STATE:
                    currentPlayerName = (String) msg.getData("currentPlayer");
                    topCard = (Card) msg.getData("topCard");
                    players = (List<Player>) msg.getData("players");
                    direction = (String) msg.getData("direction");
                    updateUI();
                    break;
                case Message.TURN_CHANGE:
                    currentPlayerName = (String) msg.getData("currentPlayer");
                    updateStatus();
                    break;
                case Message.CHAT:
                    appendChat(msg.getSender() + ": " + msg.getContent());
                    break;
                case Message.CALL_UNO:
                    appendChat("[System] " + msg.getSender() + " called UNO!");
                    break;
                case Message.GAME_OVER:
                    String winner = (String) msg.getData("winner");
                    JOptionPane.showMessageDialog(this, "Game Over! Winner: " + winner);
                    break;
                default:
                    break;
            }
        });
    }

    private void updateUI() {
        if (players == null) return;

        // Update opponents
        opponentsPanel.removeAll();
        for (Player p : players) {
            if (!p.getName().equals(myName)) {
                JLabel label = new JLabel(p.getName() + ": " + p.getCardCount() + " cards");
                if (p.getName().equals(currentPlayerName)) {
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                    label.setForeground(Color.RED);
                }
                opponentsPanel.add(label);
            } else {
                myHand = p.getHand();
            }
        }
        opponentsPanel.revalidate();
        opponentsPanel.repaint();

        // Update top card
        if (topCard != null) {
            topCardButton.setText(topCard.toString());
            topCardButton.setBackground(CardUtils.getSwingColor(topCard.getColor()));
            topCardButton.setForeground(
                    GameConstants.COLOR_YELLOW.equals(topCard.getColor()) ? Color.BLACK : Color.WHITE);
        }

        // Update hand
        handPanel.removeAll();
        boolean myTurn = myName.equals(currentPlayerName);
        if (myHand != null) {
            for (Card card : myHand) {
                JButton cardBtn = createCardButton(card, myTurn);
                handPanel.add(cardBtn);
            }
        }
        handPanel.revalidate();
        handPanel.repaint();

        updateStatus();
        directionLabel.setText("Direction: " + direction);
    }

    private JButton createCardButton(Card card, boolean myTurn) {
        JButton btn = new JButton(card.toString());
        btn.setPreferredSize(new Dimension(110, 130));
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(CardUtils.getSwingColor(card.getColor()));
        btn.setForeground(GameConstants.COLOR_YELLOW.equals(card.getColor()) ? Color.BLACK : Color.WHITE);
        btn.setMargin(new Insets(2, 2, 2, 2));
        btn.setEnabled(myTurn);
        btn.addActionListener(e -> {
            if (!myTurn) return;
            if (GameConstants.isWildCard(card.getType())) {
                String color = chooseColor();
                if (color != null) {
                    client.send(MessageUtils.createPlayCardMessage(myName, card, color));
                }
            } else {
                client.send(MessageUtils.createPlayCardMessage(myName, card, null));
            }
        });
        return btn;
    }

    private void updateStatus() {
        if (myName.equals(currentPlayerName)) {
            statusLabel.setText("Your turn!");
            statusLabel.setForeground(Color.RED);
        } else {
            statusLabel.setText("Waiting for " + currentPlayerName);
            statusLabel.setForeground(Color.BLACK);
        }
        drawButton.setEnabled(myName.equals(currentPlayerName));
        unoButton.setEnabled(myName.equals(currentPlayerName) && myHand != null && myHand.size() == 2);
    }

    private String chooseColor() {
        Object[] options = {GameConstants.COLOR_RED, GameConstants.COLOR_GREEN,
                GameConstants.COLOR_BLUE, GameConstants.COLOR_YELLOW};
        int choice = JOptionPane.showOptionDialog(this, "Choose a color:", "Wild Card",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (choice >= 0 && choice < options.length) {
            return (String) options[choice];
        }
        return null;
    }

    private void appendChat(String text) {
        chatArea.append(text + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
}
