package com.uno.model;

import com.uno.util.GameConstants;
import java.io.Serializable;

public class Card implements Serializable {
    private static final long serialVersionUID = 1L;
    private String color;
    private String type;
    private int number;

    public Card(String color, String type, int number) {
        this.color = color;
        this.type = type;
        this.number = number;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getType() {
        return type;
    }

    public int getNumber() {
        return number;
    }

    public boolean canPlayOn(Card topCard) {
        if (topCard == null) return true;
        if (GameConstants.isWildCard(this.type)) return true;
        if (this.color.equals(topCard.getColor())) return true;
        if (this.type.equals(topCard.getType())) {
            if (GameConstants.TYPE_NUMBER.equals(this.type)) {
                return this.number == topCard.getNumber();
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        if (GameConstants.TYPE_NUMBER.equals(type)) {
            return color + " " + number;
        }
        if (GameConstants.TYPE_WILD.equals(type)) {
            return "WILD";
        }
        if (GameConstants.TYPE_WILD_DRAW_FOUR.equals(type)) {
            return "WILD +4";
        }
        switch (type) {
            case GameConstants.TYPE_SKIP: return color + " SKP";
            case GameConstants.TYPE_REVERSE: return color + " REV";
            case GameConstants.TYPE_DRAW_TWO: return color + " +2";
            default: return color + " " + type.substring(0, 3);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Card)) return false;
        Card other = (Card) obj;
        return this.number == other.number
                && this.type.equals(other.type)
                && this.color.equals(other.color);
    }
}
