package com.passkeysoft;

public class Card
{
    private int suit;
    private int value;
    private int owner;      // 0 is deck-owner, MAX_INT is discarded
    private long randomized;

    public Card( int suit, int value )
    {
        this.suit = suit;
        this.value = value;
    }


    public int getOwner()
    {
        return owner;
    }

    public Card setOwner( int owner )
    {
        this.owner = owner;
        return this;
    }

    public long getRandom( )
    {
        return randomized;
    }

    public Card setRandom( long notRandom )
    {
        this.randomized = notRandom;
        return this;
    }

    public int getSuit()
    {
        return suit;
    }

    public Card setSuit( int newSuit )
    {
        // Normally, this should not be allowed. We might need to find some way to protect it.
        suit = newSuit;
        return this;
    }

    public int getValue()
    {
        return value;
    }
}
