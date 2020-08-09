package com.passkeysoft;

/**
 * A java object representing a single instance of a playing card
 */
public class Card
{
    private int suit;
    private int value;
    private int owner;      // 0 is deck-owner, {@link Deck#DISCARD} is discarded
    private long randomized;

    /**
     * Instantiates a new Card.
     *
     * @param suit  An arbitrary integer value representing the suit of this card. All cards
     *              sharing this value will be considered to be of the same suit.
     * @param value An arbitrary integer value representing the face value of this card.
     */
    public Card( int suit, int value )
    {
        this.suit = suit;
        this.value = value;
    }

    /**
     * Sets the owner of this card, as an integer value.
     *
     * @param owner the new owner of the card, as an integer value
     * @return the {@link Card} object affected, suitable for fluent-style programming.
     */
    public Card setOwner( int owner )
    {
        this.owner = owner;
        return this;
    }

    /**
     * Gets The owner value of this card.
     *
     * @return The owner of this card. 0 indicates that the card belongs to the deck,
     * and by convention {@link Deck#DISCARD} (65535, 0xFFFF) indicates that the card
     * has been discarded.
     */
    public int getOwner()
    {
        return owner;
    }

    /**
     * Sets a random number for this card. Used by the {@link Deck} object to shuffle a deck
     *
     * @param randomNumber the random number to be assigned to this card. This method should
     *                     only be used by the owning {@link Deck} instance, but I can't figure
     *                     out how to make the compiler enforce this rule without making
     *                     this class an inner class of {@link Deck}.
     * @return the {@link Card} object affected, suitable for fluent-style programming.
     */
    public Card setRandom( long randomNumber )
    {
        this.randomized = randomNumber;
        return this;
    }

    /**
     * Gets the random number assigned to this card. Probably not useful to anything other
     * than the {@link Deck} class.
     *
     * @return the random number assigned to this card
     */
    public long getRandom( )
    {
        return randomized;
    }

    /**
     * Sets the suit.  TODO: Normally, this should not be allowed. We might need to find some way to protect it.
     *
     * @param newSuit the new suit for the card
     * @return the {@link Card} object affected, suitable for fluent-style programming.
     */
    public Card setSuit( int newSuit )
    {
        suit = newSuit;
        return this;
    }

    /**
     * Gets an integer value representing the suit of this card
     *
     * @return the integer value representing the suit of this card
     */
    public int getSuit()
    {
        return suit;
    }

    /**
     * Gets an integer representing the card's face value. Note that there is no setter for this
     * property, it must be set exclusively in the constructor.
     *
     * @return an integer representing the card's face value.
     */
    public int getValue()
    {
        return value;
    }
}
