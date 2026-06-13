package com.uno.util;

import com.uno.model.Card;
import com.uno.model.Message;
import com.uno.model.Player;
import java.util.List;

public class MessageUtils {
    public static Message createGameStateMessage(String currentPlayer, Card topCard, List<Player> players, String direction) {
        Message msg = new Message(Message.GAME_STATE);
        msg.putData("currentPlayer", currentPlayer);
        msg.putData("topCard", topCard);
        msg.putData("players", players);
        msg.putData("direction", direction);
        return msg;
    }

    public static Message createTurnChangeMessage(String currentPlayer) {
        Message msg = new Message(Message.TURN_CHANGE);
        msg.putData("currentPlayer", currentPlayer);
        return msg;
    }

    public static Message createPlayCardMessage(String sender, Card card, String chosenColor) {
        Message msg = new Message(Message.PLAY_CARD, sender, null);
        msg.putData("card", card);
        msg.putData("chosenColor", chosenColor);
        return msg;
    }

    public static Message createDrawCardMessage(String sender) {
        return new Message(Message.DRAW_CARD, sender, null);
    }

    public static Message createCallUnoMessage(String sender) {
        return new Message(Message.CALL_UNO, sender, null);
    }

    public static Message createChatMessage(String sender, String text) {
        return new Message(Message.CHAT, sender, text);
    }

    public static Message createQuickPlayMessage(String sender, Card card, String chosenColor) {
        Message msg = new Message(Message.QUICK_PLAY, sender, null);
        msg.putData("card", card);
        msg.putData("chosenColor", chosenColor);
        return msg;
    }

    public static Message createGameOverMessage(String winner) {
        Message msg = new Message(Message.GAME_OVER);
        msg.putData("winner", winner);
        return msg;
    }
}
