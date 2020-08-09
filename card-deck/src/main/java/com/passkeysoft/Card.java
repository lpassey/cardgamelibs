package com.passkeysoft;

/**
 * The type Card.
 */
public class Card
{
    private int suit;
    private int value;
    private int owner;      // 0 is deck-owner, MAX_INT is discarded
    private long randomized;

    /**
     * Instantiates a new Card.
     *
     * @param suit  the suit
     * @param value the value
     */
    public Card( int suit, int value )
    {
        this.suit = suit;
        this.value = value;
    }


    /**
     * Gets owner.
     *
     * @return the owner
     */
    public int getOwner()
    {
        return owner;
    }

    /**
     * Sets owner.
     *
     * @param owner the owner
     * @return the owner
     */
    public Card setOwner( int owner )
    {
        this.owner = owner;
        return this;
    }

    /**
     * Gets random.
     *
     * @return the random
     */
    public long getRandom( )
    {
        return randomized;
    }

    /**
     * Sets random.
     *
     * @param notRandom the not random
     * @return the random
     */
    public Card setRandom( long notRandom )
    {
        this.randomized = notRandom;
        return this;
    }

    /**
     * Gets suit.
     *
     * @return the suit
     */
    public int getSuit()
    {
        return suit;
    }

    /**
     * Sets suit.
     *
     * @param newSuit the new suit
     * @return the suit
     */
    public Card setSuit( int newSuit )
    {
        // TODO: Normally, this should not be allowed. We might need to find some way to protect it.
        suit = newSuit;
        return this;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    public int getValue()
    {
        return value;
    }
}
