package com.uno.server;

import com.uno.model.GameRoom;
import com.uno.model.Message;
import com.uno.model.Player;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private Server server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String playerName;
    private GameRoom currentRoom;
    private boolean running;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.running = true;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            while (running) {
                Message msg = (Message) in.readObject();
                handleMessage(msg);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client disconnected: " + playerName);
            disconnect();
        }
    }

    private void handleMessage(Message msg) {
        switch (msg.getType()) {
            case Message.LOGIN:
                playerName = msg.getSender();
                if (RoomManager.getInstance().isNameTaken(playerName)) {
                    send(new Message(Message.LOGIN_FAIL, null, "Name already taken"));
                } else {
                    RoomManager.getInstance().registerPlayer(playerName, this);
                    send(new Message(Message.LOGIN_SUCCESS, null, "Welcome " + playerName));
                }
                break;
            case Message.ROOM_LIST:
                send(RoomManager.getInstance().getRoomListMessage());
                break;
            case Message.CREATE_ROOM:
                String roomId = RoomManager.getInstance().createRoom(msg.getContent());
                send(new Message(Message.CREATE_ROOM, null, roomId));
                send(RoomManager.getInstance().getRoomListMessage());
                break;
            case Message.JOIN_ROOM:
                currentRoom = RoomManager.getInstance().joinRoom(msg.getContent(), playerName, this);
                if (currentRoom != null) {
                    broadcastToRoom(currentRoom, new Message(Message.PLAYER_JOIN, playerName, null));
                    sendRoomUpdate(currentRoom);
                } else {
                    send(new Message(Message.ERROR, null, "Cannot join room"));
                }
                break;
            case Message.LEAVE_ROOM:
                if (currentRoom != null) {
                    RoomManager.getInstance().leaveRoom(currentRoom.getRoomId(), playerName);
                    broadcastToRoom(currentRoom, new Message(Message.PLAYER_LEAVE, playerName, null));
                    if (currentRoom.isEmpty()) {
                        RoomManager.getInstance().removeRoom(currentRoom.getRoomId());
                    }
                    currentRoom = null;
                }
                break;
            case Message.PLAYER_READY:
                if (currentRoom != null) {
                    Player p = currentRoom.getPlayer(playerName);
                    if (p != null) {
                        p.setReady(!p.isReady());
                        broadcastToRoom(currentRoom, new Message(Message.PLAYER_READY, playerName, null));
                        sendRoomUpdate(currentRoom);
                        if (currentRoom.allReady()) {
                            RoomManager.getInstance().startGame(currentRoom);
                        }
                    }
                }
                break;
            case Message.CHAT:
                if (currentRoom != null) {
                    broadcastToRoom(currentRoom, msg);
                }
                break;
            case Message.PLAY_CARD:
            case Message.DRAW_CARD:
            case Message.CALL_UNO:
            case Message.COLOR_PICKED:
                if (currentRoom != null && currentRoom.isInGame()) {
                    RoomManager.getInstance().handleGameAction(currentRoom, msg);
                }
                break;
            case Message.START_GAME:
                if (currentRoom != null && currentRoom.getPlayers().get(0).getName().equals(playerName)) {
                    if (currentRoom.getPlayerCount() >= 2) {
                        RoomManager.getInstance().startGame(currentRoom);
                    }
                }
                break;
            default:
                break;
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

    public void broadcastToRoom(GameRoom room, Message msg) {
        for (Player player : room.getPlayers()) {
            ClientHandler handler = RoomManager.getInstance().getHandler(player.getName());
            if (handler != null) {
                handler.send(msg);
            }
        }
    }

    private void sendRoomUpdate(GameRoom room) {
        Message update = new Message(Message.UPDATE_ROOM);
        update.putData("players", room.getPlayers());
        update.putData("inGame", room.isInGame());
        broadcastToRoom(room, update);
    }

    private void disconnect() {
        running = false;
        if (currentRoom != null) {
            RoomManager.getInstance().leaveRoom(currentRoom.getRoomId(), playerName);
            broadcastToRoom(currentRoom, new Message(Message.PLAYER_LEAVE, playerName, null));
        }
        RoomManager.getInstance().unregisterPlayer(playerName);
        server.removeClient(this);
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPlayerName() {
        return playerName;
    }
}
