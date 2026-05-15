package com.uno.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private List<Card> hand;
    private boolean saidUno;
    private boolean connected;
    private boolean ready;

    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
        this.saidUno = false;
        this.connected = true;
        this.ready = false;
    }

    public String getName() {
        return name;
    }

    public List<Card> getHand() {
        return hand;
    }

    public void addCard(Card card) {
        hand.add(card);
        saidUno = false;
    }

    public boolean removeCard(Card card) {
        return hand.remove(card);
    }

    public int getCardCount() {
        return hand.size();
    }

    public boolean hasUno() {
        return hand.size() == 1;
    }

    public boolean hasWon() {
        return hand.isEmpty();
    }

    public boolean isSaidUno() {
        return saidUno;
    }

    public void setSaidUno(boolean saidUno) {
        this.saidUno = saidUno;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean hasPlayableCard(Card topCard) {
        for (Card card : hand) {
            if (card.canPlayOn(topCard)) {
                return true;
            }
        }
        return false;
    }
}
