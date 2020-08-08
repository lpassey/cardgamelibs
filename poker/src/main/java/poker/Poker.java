package poker;

import com.passkeysoft.Card;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static poker.Tables.PRIMES;
import static poker.Tables.hash;
import static com.passkeysoft.poker.PokerDeck.*;
import static com.passkeysoft.poker.PokerDeck.ACE;

public class Poker
{
    public final static int[] bitmaps = { 1, 1 << TREY,  1 << FOUR,  1 << FIVE,  1 << SIX,
        1 << SEVEN, 1 << EIGHT,  1 << NINE,  1 << TEN,  1 << JACK,  1 << QUEEN,  1 << KING,  1 << ACE };

    public static String[] pokerTypes = {"Straight Flush", "Four of a Kind", "Full House", "Flush",
        "Straight", "Three of a Kind", "Two Pair", "One Pair", "High Card" };


    //------------ Static methods that apply to poker games in general ------------------
    /*
       Table of hand values for hands of three cards containing at least one pair. Higher values are better.
       Highest hand is three aces, lowest hand is 2, 2, 3 (0, 0, 2)
     */
    public static final HashMap<Integer, Integer> threeTable;
    static
    {
        threeTable = new HashMap<>();
        for (int i = 0; i < 13; i++)
        {
            int base = PRIMES[i] * PRIMES[i];
            for (int j = 0; j < 13; j++)
            {
                int result = base * PRIMES[j];
                int rank;
                if (j != i)
                {
                    rank = i * 13 + j;
                }
                else
                    rank = 13 * 13 + j;
                threeTable.put( result, rank );
            }
        }
    }
   /*
      Table of hand values for hands of four cards containing at least one pair. Higher values are better.
       Highest hand is four aces, next category is tree aces, next is two pair (aces and kings), and lowest hand
      is a pair of deuces with 3 and 4 kickers( 0, 0, 1, 2  Note that flushes and straights are not applicable
     */

    public static final HashMap<Integer, Integer> fourTable;
    static
    {
        fourTable = new HashMap<>(  );
        for (int i = 0; i < 13; i++)
        {
            int base = PRIMES[i] * PRIMES[i];
            for (int j = 0; j < 13; j++)
            {
                for (int k = 0; k <= j; k++)
                {
                    int rank = 0;
                    if (j == i || k == i)
                    {
                        // three of a kind at least
                        if (k == j)
                        {
                            // four of a kind
                            rank = (1288) + i;
                        }
                        else
                        {
                            rank = (14 * 80) + (i * 12) + j + k;
                        }
                    }
                    else if (j != k)
                    {
                        // just a pair
                        rank = (i * 80) + ((j * (j - 1)) /2) + k;
                    }
                    else if (i > k)
                    {
                        // two pair
                        int temp = (i * (i - 1)) / 2;

                        rank = (13 * 80) + temp + k;
                    }
                    if (0 < rank)
                    {
                        int index = base * PRIMES[j] * PRIMES[k];
                        fourTable.put( index, rank );
                    }
                }
            }
        }
    }

    public static int computeIndex( List<Card> hand )
    {
        int index = 1;
        for (Card card : hand)
        {
            index *= PRIMES[ card.getValue()];
        }
        return index;
    }

    public static boolean isFlush( List<Card> hand )
    {
        int flushy = 0x0F;
        for (Card card : hand)
        {
            flushy &= card.getSuit();
        }
        return flushy != 0;
    }

    public static int createBitmap( List<Card> hand )
    {
        int seq = 0;
        for ( Card card : hand)
        {
            seq |= bitmaps[ card.getValue()];
        }
        return seq;
    }

    public static int isStraight( List<Card> hand )
    {
        int seq = createBitmap( hand );

        // I now have a bitmap of all the cards. shift right until the rightmost bit is set, counting as we go.
        int count = 1;
        while (0 == (seq & 0x1))
        {
            seq >>= 1;
            count++;
        }
        if (hand.size() == 3 && seq == 7)
        {
            return  count + 3;
        }
        else if (hand.size() == 4 && seq == 0xF)
        {
            return count + 4;
        }
        return 0;
    }

    /**
     * Evaluates the given hand and returns its value as an integer.
     * Based on Kevin Suffecool's 5-card hand evaluator and with Paul Senzee's pre-computed hash.
     * @param hand a hand of cards to evaluate
     * @return the value of the hand as an integer between 1 and 7462
     */
    public static int evaluate( List<Card> hand) {
        // Only 5-card hands are supported
        if (hand == null || hand.size() != 5) {
            throw new IllegalArgumentException("Exactly 5 cards are required.");
        }

        // Calculate index into the flushes/unique table
        int index = createBitmap( hand );

        // Flushes, including straight flushes
        if (isFlush( hand ))
        {
            return Flushes.table[index];
        }

        // Straight and high card hands
        final int value = NoPairs.table[index];
        if (value != 0) {
            return value;
        }

        // Remaining hands that contain at least one pair
        final int product = computeIndex( hand );
        int h = hash(product);
        return Tables.Values.TABLE[ h ];
    }

    public static List<Card> getBestHand( List<List<Card>> allHands )
    {
        // Seven high (7, 5, 4, 3, 2 off suit) is the worst possible hand, with a score of 7462
        int best = 0, score = 7500;
        for (int i = 0; i < allHands.size(); i++ )
        {
            int temp = evaluate( allHands.get( i ) );
            if ( temp < score )
            {
                // best hand so far.
                best = i;
                score = temp;
            }
        }
        return allHands.get( best );
    }

    public static List<Card> get5CardsList( List<Card> hand)
    {
        int handSize = hand.size();
        List<List<Card>> allHands = new ArrayList<>(  );

        for ( Card firstCard : hand )
        {
            List<Card> handOf5 = new ArrayList<>( 5 );
            // every card that is not the first added to the hand
            for( Card card : hand )
            {
                if (card != firstCard)
                {
                    handOf5.add( card );
                }
            }
            allHands.add( handOf5 );
        }
        return getBestHand( allHands );
    }

    public static List<Card> getBest5CardsOf7( List<Card> hand )
    {
        List<List<Card>> allHands = new ArrayList<>(  );

        // select first card not to be in the hand
        for (int firstCard = 6; firstCard > 0; firstCard--)
        {
            // select second card not to be in the hand
            for(int secondCard = firstCard - 1; secondCard > 0; secondCard--)
            {
                List<Card> handOf5 = new ArrayList<>( 5 );

                // every card that is not the first or second will added to the hand
                for(int i = 0; i < 7; i++)
                {
                    if (i != firstCard && i != secondCard){
                        handOf5.add( hand.get( i ));
                    }
                }
                allHands.add( handOf5 );
            }
        }
        return getBestHand( allHands );
    }



    public static int typeOfHand( List<Card> hand )
    {
        int score = evaluate( hand );
        if (11 > score)
            return (0);
        else if (167 > score)
            return (1);
        else if (323 > score)
            return (2);
        else if (1600 > score)
            return (3);
        else if (1610 > score)
            return (4);
        else if (2468 > score)
            return (5);
        else if (3326 > score)
            return (6);
        else if (6186 > score)
            return (7);
        return 8;
    }

    public static int countBits( int handValue )
    {
        int count = 0;
        for (int j = 0; j < 13; j++)
        {
            int test = handValue >> j;
            test &= 1;
            if (0 != test )
                count++;
        }
        return count;
    }
}
