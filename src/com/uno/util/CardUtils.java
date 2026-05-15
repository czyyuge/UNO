package com.uno.util;

import com.uno.model.Card;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardUtils {
    public static List<Card> createDeck() {
        List<Card> deck = new ArrayList<>();
        for (String color : GameConstants.COLORS) {
            deck.add(new Card(color, GameConstants.TYPE_NUMBER, 0));
            for (int i = 1; i <= 9; i++) {
                deck.add(new Card(color, GameConstants.TYPE_NUMBER, i));
                deck.add(new Card(color, GameConstants.TYPE_NUMBER, i));
            }
            for (String type : GameConstants.SPECIAL_TYPES) {
                deck.add(new Card(color, type, -1));
                deck.add(new Card(color, type, -1));
            }
        }
        for (int i = 0; i < 4; i++) {
            deck.add(new Card(GameConstants.COLOR_WILD, GameConstants.TYPE_WILD, -1));
            deck.add(new Card(GameConstants.COLOR_WILD, GameConstants.TYPE_WILD_DRAW_FOUR, -1));
        }
        return deck;
    }

    public static void shuffle(List<Card> deck) {
        Collections.shuffle(deck);
    }

    public static String getDisplayText(Card card) {
        return card.toString();
    }

    public static java.awt.Color getSwingColor(String color) {
        switch (color) {
            case GameConstants.COLOR_RED: return java.awt.Color.RED;
            case GameConstants.COLOR_GREEN: return java.awt.Color.GREEN;
            case GameConstants.COLOR_BLUE: return java.awt.Color.BLUE;
            case GameConstants.COLOR_YELLOW: return java.awt.Color.YELLOW;
            default: return java.awt.Color.DARK_GRAY;
        }
    }
}
