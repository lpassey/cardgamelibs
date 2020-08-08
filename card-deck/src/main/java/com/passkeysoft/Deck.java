package com.passkeysoft;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Deck
{
    public static final int DISCARD = 65535;

    protected List<Card> cardList = new ArrayList<>(  );

    public static class Face
    {
        public String image;
        public String description;

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

    protected Deck addCard( Card card )
    {
        cardList.add( card );
        return this;
    }

    protected Deck addFace( Card card, Face face )
    {
        TreeMap<Integer, Face> cardFaces = deckFaces.computeIfAbsent(
            card.getSuit(), k -> new TreeMap<>() );
        cardFaces.put( card.getValue(), face );
        return this;
    }

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

    public void shuffle()
    {
        // Now sort the list by 1) owner and 2) random number. This will have the effect of
        // shuffling all the cards in the deck, but those owned by the deck will be first in line
        shuffleCustom( Comparator.comparing( Card::getOwner ));
    }

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


    public Card dealCardTo( int owner )
    {
        for (Card toBeDealt : cardList)
        {
            if (0 == toBeDealt.getOwner())
            {
                toBeDealt.setOwner( owner );
                return toBeDealt;
            }
        }
        return null;    // No undealt cards remaining
    }

    public Card dealCardToPlayerBySuit( int owner, int suit )
    {
        for (Card toBeDealt : cardList)
        {
            if (0 == toBeDealt.getOwner() && toBeDealt.getSuit() == suit )
            {
                toBeDealt.setOwner( owner );
                return toBeDealt;
            }
        }
        return null;    // No undealt cards remaining

    }

    public Card discard( Card card, long position )
    {
        card.setOwner( DISCARD ).setRandom( position );
        return card;
    }

    public List<Card> dealNewHandToPlayer( int owner, int numCardsInHand )
    {
        // return all this players cards to the deck
        returnHandFromOwner( owner );
        for (int i = 0; i < numCardsInHand ; i++)
        {
            dealCardTo( owner );
        }
        return getHandByOwner( owner );
    }

    public List<Card> getHandByOwner( int owner )
    {
        return cardList.stream()
            .filter( card -> card.getOwner() == owner )
            .collect( Collectors.toList());
    }

    public void returnHandFromOwner( int owner )
    {
        for (Card card : cardList )
        {
            if (card.getOwner() == owner)
                card.setOwner( 0 );
        }
    }

    public String buildCardAsJSON( String path, Card card )
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
                .append( path )
                .append( face.image )
                .append( "\",\n\"description\":\"" )
                .append( face.description )
                .append( "\"" );
        }
        sb.append( "\n}");
        return sb.toString();
    }

    public String buildHandAsJSON( String path, List<Card> hand )
    {
        StringBuilder sb = new StringBuilder("{\n\"cards\":[\n");
        boolean first = true;
        for( Card card : hand )
        {
            if (!first)
                sb.append( ",\n" );
            sb.append( buildCardAsJSON( path, card ));
            first = false;
        }
        sb.append( "\n]\n}" );
        return sb.toString();
    }

    /**
     * Gets the card from a player's hand that matches the card presented.
     *
     * @param cardToMatch   // The card we are trying to match
     * @param playerNum     // The player whose hand we are searching
     * @return              // The (a) matching card from the player's hand, or null if a match cannot be found.
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
    public String toString()
    {
        return buildHandAsJSON( "", cardList );
    }

}
