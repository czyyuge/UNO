package com.uno.game;

import com.uno.model.Card;
import com.uno.model.Player;
import com.uno.util.GameConstants;
import java.util.List;

public class GameLogic {
    private List<Player> players;
    private CardDeck deck;
    private int currentPlayerIndex;
    private int direction;
    private boolean gameOver;
    private String winner;
    private int cardsToDraw;
    private boolean pendingDraw;

    public GameLogic(List<Player> players) {
        this.players = players;
        this.deck = new CardDeck();
        this.currentPlayerIndex = 0;
        this.direction = 1;
        this.gameOver = false;
        this.cardsToDraw = 0;
        this.pendingDraw = false;
        initGame();
    }

    private void initGame() {
        for (Player player : players) {
            player.getHand().clear();
            player.setReady(false);
            player.setSaidUno(false);
            for (int i = 0; i < GameConstants.INITIAL_CARDS; i++) {
                player.addCard(deck.drawCard());
            }
        }
        Card firstCard = deck.drawCard();
        while (GameConstants.isWildCard(firstCard.getType())) {
            deck.discard(firstCard);
            firstCard = deck.drawCard();
        }
        deck.discard(firstCard);
    }

    public boolean playCard(Player player, Card card, String chosenColor) {
        if (gameOver) return false;
        if (!getCurrentPlayer().equals(player)) return false;
        Card topCard = deck.getTopCard();
        if (!card.canPlayOn(topCard)) return false;
        if (!player.getHand().contains(card)) return false;

        player.removeCard(card);
        if (GameConstants.isWildCard(card.getType()) && chosenColor != null) {
            card.setColor(chosenColor);
        }
        deck.discard(card);

        if (player.hasWon()) {
            gameOver = true;
            winner = player.getName();
            return true;
        }

        applyCardEffect(card);
        return true;
    }

    private void applyCardEffect(Card card) {
        String type = card.getType();
        switch (type) {
            case GameConstants.TYPE_SKIP:
                nextPlayer();
                break;
            case GameConstants.TYPE_REVERSE:
                direction *= -1;
                if (players.size() == 2) {
                    nextPlayer();
                }
                break;
            case GameConstants.TYPE_DRAW_TWO:
                cardsToDraw += 2;
                pendingDraw = true;
                break;
            case GameConstants.TYPE_WILD_DRAW_FOUR:
                cardsToDraw += 4;
                pendingDraw = true;
                break;
        }
        nextPlayer();
    }

    public Card drawCard(Player player) {
        if (gameOver) return null;
        if (!getCurrentPlayer().equals(player)) return null;

        if (pendingDraw && cardsToDraw > 0) {
            List<Card> cards = deck.drawCards(cardsToDraw);
            for (Card c : cards) player.addCard(c);
            cardsToDraw = 0;
            pendingDraw = false;
            nextPlayer();
            return cards.isEmpty() ? null : cards.get(0);
        }

        Card card = deck.drawCard();
        if (card != null) {
            player.addCard(card);
        }
        if (!player.hasPlayableCard(deck.getTopCard())) {
            nextPlayer();
        }
        return card;
    }

    public void callUno(Player player) {
        player.setSaidUno(true);
    }

    public void checkUnoPenalty(Player player) {
        if (player.hasUno() && !player.isSaidUno()) {
            List<Card> penalty = deck.drawCards(2);
            for (Card c : penalty) player.addCard(c);
        }
    }

    private void nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + direction + players.size()) % players.size();
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public Card getTopCard() {
        return deck.getTopCard();
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public String getWinner() {
        return winner;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public String getDirection() {
        return direction == 1 ? "CLOCKWISE" : "COUNTER_CLOCKWISE";
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }
}
