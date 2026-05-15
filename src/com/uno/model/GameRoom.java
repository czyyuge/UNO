package com.uno.model;

import com.uno.game.GameLogic;
import com.uno.util.GameConstants;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameRoom implements Serializable {
    private static final long serialVersionUID = 1L;
    private String roomId;
    private String roomName;
    private List<Player> players;
    private GameLogic gameLogic;
    private boolean inGame;
    private int maxPlayers;

    public GameRoom(String roomId, String roomName) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.players = new ArrayList<>();
        this.inGame = false;
        this.maxPlayers = GameConstants.MAX_PLAYERS;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public boolean addPlayer(Player player) {
        if (players.size() >= maxPlayers || inGame) return false;
        players.add(player);
        return true;
    }

    public boolean removePlayer(String playerName) {
        return players.removeIf(p -> p.getName().equals(playerName));
    }

    public boolean isFull() {
        return players.size() >= maxPlayers;
    }

    public boolean isEmpty() {
        return players.isEmpty();
    }

    public boolean isInGame() {
        return inGame;
    }

    public void startGame() {
        inGame = true;
        gameLogic = new GameLogic(players);
    }

    public GameLogic getGameLogic() {
        return gameLogic;
    }

    public int getPlayerCount() {
        return players.size();
    }

    public boolean allReady() {
        if (players.size() < 2) return false;
        for (Player p : players) {
            if (!p.isReady()) return false;
        }
        return true;
    }

    public Player getPlayer(String name) {
        for (Player p : players) {
            if (p.getName().equals(name)) return p;
        }
        return null;
    }
}
