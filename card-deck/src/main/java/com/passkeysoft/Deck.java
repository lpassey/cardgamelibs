package com.passkeysoft;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * A class representing a deck of cards
 */
@SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
public class Deck
{
    /**
     * Used to indicate that a card has been discarded.
     */
    public static final int DISCARD = 65535;

    /**
     * A list of all the cards in this deck.
     */
    protected List<Card> cardList = new ArrayList<>(  );

    /**
     * a card Face object which includes an image file name and a description.
     */
    public static class Face
    {
        /**
         * An image file name associated with a card face.
         */
        public String image;

        /**
         * A human-readable description of the card face; e.g. "Ace of Spades" (Poker),
         * "Green Ten" (Uno) or "Year of Plenty" (Catan).
         */
        public String description;

        /**
         * Instantiates a new card Face.
         *
         * @param image       the image file name associated with this card face.
         * @param description the description of this card face.
         */
        public Face( String image, String description )
        {
            this.image = image;
            this.description = description;
        }

        @Override
        public String toString()
        {
            return image;
        }
    }

    private TreeMap<Integer, TreeMap<Integer, Face>> deckFaces = new TreeMap<>(  );
    private SecureRandom random = new SecureRandom();

    /**
     * Add a newly instantiated {@link Card} to this deck.
     *
     * @param suit  the suit of the new card
     * @param value the face value of the new card
     * @return this instance, suitable for fluent-style programming.
     */
    protected Deck addCard( int suit, int value )
    {
        cardList.add( new Card( suit, value ));
        return this;
    }

    /**
     * Adds a {@link Deck.Face} object associated with a specific card to the internal Face map.
     * Consistent with Java maps, if the reference already exists it will be replaced.
     * <p>
     * This method is protected, so only derived sub-classes can add a face reference.
     *
     * @param card the reference card
     * @param face the {@link Deck.Face} object to be associated with the reference card.
     * @return this instance, suitable for fluent-style programming.
     */
    protected Deck addFace( Card card, Face face )
    {
        TreeMap<Integer, Face> cardFaces = deckFaces.computeIfAbsent(
            card.getSuit(), k -> new TreeMap<>() );
        cardFaces.put( card.getValue(), face );
        return this;
    }

    /**
     * Gets the {@link Deck.Face} object associated with the reference card
     *
     * @param card the reference card
     * @return the associated {@link Deck.Face} object
     */
    public Face getFace( Card card )
    {
        TreeMap<Integer, Face> cardFaces = deckFaces.get( card.getSuit() );
        if (null != cardFaces)
        {
            return cardFaces.get( card.getValue() );
        }
        return null;
    }

    private void randomize()
    {
        for (Card card : cardList)
        {
            card.setRandom( random.nextLong());
        }
    }

    /**
     * Shuffles the deck by sorting the deck first by owner and then by assigned random number.
     * This will have the effect of shuffling all the cards in the deck, but those owned by
     * the deck (i.e. owned by 0) will be first in line and those which are discarded will
     * (probably) be at the end of the list. It acts by calling the {@link Deck#shuffleCustom(Comparator)}
     * method which has the effect of resetting all the random numbers before sorting.
     */
    public void shuffle()
    {
        // Now sort the list by 1) owner and 2) random number.
        shuffleCustom( Comparator.comparing( Card::getOwner ));
    }

    /**
     * Shuffles the deck in a custom manner. The caller provides a {@link Comparator} object that
     * will be applied before comparing the cards by their random number. Thus it is possible,
     * for example, to shuffle the deck but group all the cards by suit, or by owner (as
     * {@link Deck#shuffle()} does).
     *
     * @param comparator the custom Comparator
     */
    public void shuffleCustom( Comparator<Card> comparator )
    {
        randomize();    // create a new random number for every card in the deck
        // Now sort the list by the custom comparator, finishing by sorting by the random value;
        cardList.sort( comparator.thenComparing( Card::getRandom ) );
    }

    /**
     * resets the owner of all cards to 0 (the deck) and shuffles
     */
    public void reset()
    {
        for (Card card : cardList)
        {
            card.setOwner( 0 );
        }
        shuffle();
    }


    /**
     * Deal a card to a player
     *
     * @param playerNum a non-zero integer value representing a single player who will
     *                  receive the card
     * @return the {@link Card} dealt, or null if there are no remaining cards owned by the deck.
     */
    public Card dealCardTo( int playerNum )
    {
        for (Card toBeDealt : cardList)
        {
            if (0 == toBeDealt.getOwner())
            {
                toBeDealt.setOwner( playerNum );
                return toBeDealt;
            }
        }
        return null;    // No undealt cards remaining
    }

    /**
     * Deal a card to a player by suit.
     *
     * @param playerNum a non-zero integer value representing a single player                  who will receive the card
     * @param suit      the suit requested
     * @return the first undealt card from the card list which matches the requested suit.
     */
    public Card dealCardToPlayerBySuit( int playerNum, int suit )
    {
        for (Card toBeDealt : cardList)
        {
            if (0 == toBeDealt.getOwner() && toBeDealt.getSuit() == suit )
            {
                toBeDealt.setOwner( playerNum );
                return toBeDealt;
            }
        }
        return null;    // No undealt cards of the requested suit remaining
    }

    /**
     * Discard a card. Simply sets the owner of the card to {@link Deck#DISCARD} and sets
     * the random value to the current time in milliseconds. Because the random value is reset
     * on every discard, the most recently discarded card can be retrieved by getting all the
     * cards owned by {@link Deck#DISCARD}, sorting the list in reverse by {@link Card#getRandom()},
     * and taking the first card in the list.
     *
     * @param card the card being discarded.
     * @return the Deck object, suitable for fluent-style programming.
     */
    public Deck discard( Card card )
    {
        card.setOwner( DISCARD ).setRandom( System.currentTimeMillis() );
        try
        {
            // Don't discard cards too fast.
            Thread.sleep(1);
        }
        catch( InterruptedException ignore ) {}
        return this;
    }

    /**
     * Discards the first available card on the deck.
     *
     * @return the ={@link Card} burned, or null if there are not any undealt cards.
     */
    public Card burnCard()
    {
        for (Card card : cardList)
        {
            if (0 == card.getOwner())
            {
                discard( card );
                return card;
            }
        }
        return null;
    }

    /**
     * @return a {@link List} of all "discarded" cards in the deck, sorted in reverse chronological order
     */
    public List<Card> getDiscards()
    {
        List<Card> hand = getHandByOwner( DISCARD );
        hand.sort( Comparator.comparing( Card::getRandom ).reversed() );
        return hand;
    }

    /**
     * Gets hand by owner.
     *
     * @param owner the owner
     * @return the hand by owner
     */
    public List<Card> getHandByOwner( int owner )
    {
        return cardList.stream()
            .filter( card -> card.getOwner() == owner )
            .collect( Collectors.toList());
    }

    /**
     * Deal a new hand to a player, by simply finding the first numCardsInHand number of cards
     * in the card list owned by the deck and changing the owner to the new player.
     * <p>
     * It is not anticipated that the player would have an existing hand, but in that case
     * the existing cards would have to be replaced and not added to. So we first cache the
     * players existing hand, then create the new hand, and finally return the cached hand
     * to the deck. The net effect of this is that the player will not have any of his previous
     * cards in the new hand.
     *
     * @param playerNum      the integer reference to the player who will "own" the new hand.
     * @param numCardsInHand the number of cards to be in the new hand.
     * @return a {@link List} of cards constituting the new hand.
     */
    public List<Card> dealNewHandToPlayer( int playerNum, int numCardsInHand )
    {
        // temporarily save off this players hand, and reassign them to a temporary owner
        returnHandFromOwner( playerNum, DISCARD - 1 );

        // deal a new hand to the player
        for (int i = 0; i < numCardsInHand ; i++)
        {
            dealCardTo( playerNum );
        }

        // return the temporary hand to the deck.
        returnHandFromOwner( DISCARD - 1, 0 );
        return getHandByOwner( playerNum );
    }

    /**
     * Intended for use in returning a player's hand to the deck (when newOwner == 0)
     * but can also be used to change the owner of a hand to a new, arbitrary owner.
     *
     * @param oldOwner the owner of the cards whose ownership is being changed.
     * @param newOwner the new owner of the designated cards.
     */
    public void returnHandFromOwner( int oldOwner, int newOwner )
    {
        for (Card card : cardList )
        {
            if (card.getOwner() == oldOwner)
                card.setOwner( newOwner );
        }
    }

    /**
     * Create a JSON string describing a single {@link Card}.
     *
     * @param card the card being described as JSON
     * @param path the URL path where the {@link Deck.Face} images can be found.
     * @return the JSON string
     */
    public String buildCardAsJSON( Card card,  String path )
    {
        Face face = getFace( card );

        // suit and value are numeric values
        StringBuilder sb = new StringBuilder( "{\n" )
            .append("\"suit\":")
            .append( card.getSuit() )
            .append( ",\n\"value\":")
            .append( card.getValue() );

        if (null != face)
        {
            sb.append( ",\n\"img\":\"" )
                .append( null == path ? "" : path )
                .append( face.image )
                .append( "\",\n\"description\":\"" )
                .append( face.description )
                .append( "\"" );
        }
        sb.append( "\n}");
        return sb.toString();
    }

    /**
     * Creates a JSON string describing a hand of cards.
     *
     * @param hand a {@link List} of {@link Card}s constituting a single hand.
     * @param path the URL path where the {@link Deck.Face} images can be found.
     * @return the JSON string
     */
    public String buildHandAsJSON(List<Card> hand, String path )
    {
        StringBuilder sb = new StringBuilder("{\n\"cards\":[\n");
        boolean first = true;
        for( Card card : hand )
        {
            if (!first)
                sb.append( ",\n" );
            sb.append( buildCardAsJSON( card, path ));
            first = false;
        }
        sb.append( "\n]\n}" );
        return sb.toString();
    }

    /**
     * Gets the card from a player's hand that matches the card presented.
     *
     * @param cardToMatch The card we are trying to match
     * @param playerNum   The player whose hand we are searching
     * @return The (a) matching card from the player's hand, or null if a match cannot be found.
     */
    public Card getMatchingCard( Card cardToMatch, int playerNum )
    {
        if (null != cardToMatch)
        {
            List<Card> hand = getHandByOwner( playerNum );
            for (Card card : hand)
            {
                if (card.getValue() == cardToMatch.getValue()
                    && card.getSuit() == cardToMatch.getSuit())
                {
                    return card;
                }
            }
        }
        return null;
    }

    @Override
    /**
     * Returns a JSON representation of the entire deck. Use with caution.
     */
    public String toString()
    {
        return buildHandAsJSON( cardList, "" );
    }

}
