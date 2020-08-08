package com.passkeysoft.poker;

import com.passkeysoft.Card;
import com.passkeysoft.Deck;

import java.util.Arrays;
import java.util.List;

public class PokerDeck extends Deck
{
    // Suits
    static final int CLUBS = 0x8;
    static final int DIAMONDS = 0x4;
    static final int HEARTS = 0x2;
    static final int SPADES = 0x1;

    // Ranks
    public static final int DEUCE = 0;
    public static final int TREY = 1;
    public static final int FOUR = 2;
    public static final int FIVE = 3;
    public static final int SIX = 4;
    public static final int SEVEN = 5;
    public static final int EIGHT = 6;
    public static final int NINE = 7;
    public static final int TEN = 8;
    public static final int JACK = 9;
    public static final int QUEEN = 10;
    public static final int KING = 11;
    public static final int ACE = 12;


    static List suitNames = Arrays.asList( "Hearts", "Clubs", "Diamonds", "Spades" );

    private static int[] suits = {1, 2, 4, 8 };

    static Card back = new Card( 0, 13 );

    PokerDeck()
    {
        for (int suit = 0; suit < 4; suit++)
        {
            for (int value = 0; value < 13; value++)
            {
                Card newCard = new Card( suits[ suit ], value );

                StringBuilder sb = new StringBuilder( String.valueOf( value + 2 ));
                sb.append(
                    newCard.getSuit() == 1 ? "H" :
                        newCard.getSuit() == 2 ? "C" :
                            newCard.getSuit() == 4 ? "D" : "S" );
                sb.append( ".png" );

                // Face( image, description );
                addFace( newCard, new Face( sb.toString(), sb.toString() ));
                addCard( newCard );
            }
        }
        addFace( back, new Face( "gray_back.png", "Back of a card" ));
    }
}
