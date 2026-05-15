package com.uno.game;

import com.uno.model.Card;
import com.uno.util.CardUtils;
import java.util.ArrayList;
import java.util.List;

public class CardDeck {
    private List<Card> drawPile;
    private List<Card> discardPile;

    public CardDeck() {
        reset();
    }

    public void reset() {
        drawPile = CardUtils.createDeck();
        CardUtils.shuffle(drawPile);
        discardPile = new ArrayList<>();
    }

    public Card drawCard() {
        if (drawPile.isEmpty()) {
            if (discardPile.size() <= 1) {
                return null;
            }
            Card topCard = discardPile.remove(discardPile.size() - 1);
            drawPile.addAll(discardPile);
            discardPile.clear();
            discardPile.add(topCard);
            CardUtils.shuffle(drawPile);
        }
        return drawPile.remove(drawPile.size() - 1);
    }

    public List<Card> drawCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Card card = drawCard();
            if (card == null) break;
            cards.add(card);
        }
        return cards;
    }

    public void discard(Card card) {
        discardPile.add(card);
    }

    public Card getTopCard() {
        if (discardPile.isEmpty()) return null;
        return discardPile.get(discardPile.size() - 1);
    }

    public int getRemainingCards() {
        return drawPile.size();
    }
}
