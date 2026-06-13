package com.uno.server;

import com.uno.game.GameLogic;
import com.uno.model.*;
import com.uno.util.GameConstants;
import com.uno.util.MessageUtils;
import java.util.*;

public class RoomManager {
    private static RoomManager instance;
    private Map<String, GameRoom> rooms;
    private Map<String, ClientHandler> playerHandlers;
    private int roomCounter;

    private RoomManager() {
        rooms = new HashMap<>();
        playerHandlers = new HashMap<>();
        roomCounter = 1;
    }

    public static synchronized RoomManager getInstance() {
        if (instance == null) instance = new RoomManager();
        return instance;
    }

    public boolean isNameTaken(String name) {
        return playerHandlers.containsKey(name);
    }

    public void registerPlayer(String name, ClientHandler handler) {
        playerHandlers.put(name, handler);
    }

    public void unregisterPlayer(String name) {
        playerHandlers.remove(name);
    }

    public ClientHandler getHandler(String name) {
        return playerHandlers.get(name);
    }

    public synchronized String createRoom(String roomName) {
        String roomId = "ROOM_" + (roomCounter++);
        rooms.put(roomId, new GameRoom(roomId, roomName));
        return roomId;
    }

    public synchronized GameRoom joinRoom(String roomId, String playerName, ClientHandler handler) {
        GameRoom room = rooms.get(roomId);
        if (room == null || room.isFull() || room.isInGame()) return null;
        Player player = new Player(playerName);
        if (room.addPlayer(player)) {
            return room;
        }
        return null;
    }

    public synchronized void leaveRoom(String roomId, String playerName) {
        GameRoom room = rooms.get(roomId);
        if (room != null) {
            room.removePlayer(playerName);
            if (room.isEmpty()) {
                rooms.remove(roomId);
            }
        }
    }

    public synchronized void removeRoom(String roomId) {
        rooms.remove(roomId);
    }

    public Message getRoomListMessage() {
        Message msg = new Message(Message.ROOM_LIST);
        msg.putData("rooms", new ArrayList<>(rooms.values()));
        return msg;
    }

    public void startGame(GameRoom room) {
        room.startGame();
        for (Player player : room.getPlayers()) {
            ClientHandler handler = playerHandlers.get(player.getName());
            if (handler != null) {
                handler.send(new Message(Message.GAME_START));
            }
        }
        broadcastGameState(room);
    }

    public void handleGameAction(GameRoom room, Message msg) {
        GameLogic logic = room.getGameLogic();
        if (logic == null || logic.isGameOver()) return;
        Player player = room.getPlayer(msg.getSender());
        if (player == null) return;

        switch (msg.getType()) {
            case Message.PLAY_CARD:
                checkAndApplyUnoPenalty(room, logic);
                Card card = (Card) msg.getData("card");
                String chosenColor = (String) msg.getData("chosenColor");
                if (card != null && logic.playCard(player, card, chosenColor)) {
                    if (logic.isGameOver()) {
                        broadcastToRoom(room, MessageUtils.createGameOverMessage(logic.getWinner()));
                        return;
                    }
                    broadcastGameState(room);
                }
                break;
            case Message.DRAW_CARD:
                checkAndApplyUnoPenalty(room, logic);
                logic.drawCard(player);
                broadcastGameState(room);
                break;
            case Message.CALL_UNO:
                logic.callUno(player);
                broadcastToRoom(room, new Message(Message.CALL_UNO, player.getName(), null));
                break;
            case Message.QUICK_PLAY:
                checkAndApplyUnoPenalty(room, logic);
                Card quickCard = (Card) msg.getData("card");
                String quickColor = (String) msg.getData("chosenColor");
                if (quickCard != null && logic.quickPlayCard(player, quickCard, quickColor)) {
                    if (logic.isGameOver()) {
                        broadcastToRoom(room, MessageUtils.createGameOverMessage(logic.getWinner()));
                        return;
                    }
                    broadcastGameState(room);
                }
                break;
            case Message.COLOR_PICKED:
                Card top = logic.getTopCard();
                if (top != null && GameConstants.isWildCard(top.getType())) {
                    top.setColor((String) msg.getData("color"));
                    broadcastGameState(room);
                }
                break;
        }
    }

    private void checkAndApplyUnoPenalty(GameRoom room, GameLogic logic) {
        String offender = logic.checkAndClearUnoOffender();
        if (offender != null) {
            broadcastToRoom(room, new Message(Message.CHAT, null,
                    "[System] " + offender + " forgot to call UNO! Draws 2 penalty cards."));
            broadcastGameState(room);
        }
    }

    private void broadcastGameState(GameRoom room) {
        GameLogic logic = room.getGameLogic();
        if (logic == null) return;
        Message state = MessageUtils.createGameStateMessage(
                logic.getCurrentPlayer().getName(),
                logic.getTopCard(),
                room.getPlayers(),
                logic.getDirection()
        );
        broadcastToRoom(room, state);
    }

    private void broadcastToRoom(GameRoom room, Message msg) {
        for (Player player : room.getPlayers()) {
            ClientHandler handler = playerHandlers.get(player.getName());
            if (handler != null) handler.send(msg);
        }
    }
}
