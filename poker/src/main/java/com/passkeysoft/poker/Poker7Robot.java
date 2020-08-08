package com.passkeysoft.poker;

import com.passkeysoft.Card;
import poker.Poker;

import java.util.List;

import static com.passkeysoft.poker.PokerDeck.*;
import static com.passkeysoft.poker.PokerDeck.ACE;
import static com.passkeysoft.poker.PokerGame.FOLDED;

class Poker7Robot
{
    static int actLikeARobot( PokerPlayer robot, PokerGame<PokerPlayer> game )
    {
        try
        {
            Thread.sleep( 2000 );
        }
        catch( InterruptedException ignore ) {}
        // get my hand for the round, evaluate it then check, bet or fold depending on the evaluation.
        List<Card> myHand = game.getCardsForRound( game.playerList.indexOf( robot ), 0);
        int handValue = Poker.createBitmap( myHand );
        // count the number of bits set
        int uniqueCardValues = Poker.countBits( handValue );
        int myBet = game.highBet;
        int rank = handValue;
        boolean isFlushable = Poker.isFlush( myHand );

        // should return the highest card in the possible straight.
        int straight = Poker.isStraight( myHand );
        // for 5 cards the lowest possible ace high is 4111, lowest possible king high is 2063, lowest possible queen
        // high is 1039.

        switch( game.round )
        {
            case 0: // three cards visible. Highest possible no pair hand is 7808
                // We probably want hands with more than one high card.
                if (uniqueCardValues < 3 )
                {
                    rank = 7810 + Poker.threeTable.get( Poker.computeIndex( myHand ) );
                }
                // 7991 Highest 3ofk value for three cards
                if (rank > 7978)
                {   // 7991 Highest 3ofk value for three cards
                    // 7979 Lowest  3ofk value for three cards. This hand will probably win. Bet a low pair to win
                    // now, but slow play a high pair to encourage pot growth
                    if (rank < 7987)    // 3 tens
                    {
                        myBet += 2;
                    }
                }
                else if (rank > 7810)
                {
                    // a pair or better. We probably want to bet a high pair and call a low pair.
                    // 7977 Highest pair value for three cards
//                    if (myBet == game.highBet)      // How is this not always true?
                    {
                        if (rank > 7895 && 0 == game.highBet)
                        {
                            // don't raise if someone ahead of me has already raised; could lead to an infinite loop
                            myBet += 2;
                        }
                    }
                }
                else if (myBet > 0)
                {
                    if (0 != straight || isFlushable)
                    {
                        // check other player's hands for live cards. The fewer the better.
                    }
                    else if (rank < 832)
                    {
                        // Anything lower than J-10 - 8 should be folded when faced with a bet of any kind.
                        myBet = FOLDED;
                    }
                    else
                    {
                        int high, kicker;
                        // At least a jack high. Check for liveness of my hand, and fold if my highest card isn't live
                        if (rank < 1027)    // lowest possible queen high (3 cards)
                        {
                            high = JACK;    // 832 is highest possible jack high not straighty.
                            kicker = rank;
                        }
                        else if (rank < 2051)   // lowest possible king high (3 cards)
                        {
                            high = QUEEN;       // 1281 is Q 10
                            kicker = 1281;
                        }
                        else if (rank < 4099)   // lowest possible ace high (3 cards)
                        {
                            high = KING;       // 2305 is K 10
                            kicker = 2305;
                        }
                        else                    // 6656 is the highest possible ace high that is not straighty
                        {
                            high = ACE;         // 4353 is A 10
                            kicker = 4353;
                        }
                        int howLive = game.getLiveness( robot, high );
                        if (1 == howLive)
                        {
                            // only one outstanding match. fold if my kicker isn't very good.
                            if (rank < kicker)
                                myBet = FOLDED;
                        }
                        else if (1 < howLive)
                        {
                            // too many live cards, just fold
                            myBet = FOLDED;
                        }
                    }
                }
                break;
            case 1: // four cards visible
                if (uniqueCardValues < 4)
                    rank = 7810 + Poker.fourTable.get( Poker.computeIndex( myHand ) );
                // 8642 pair of queens with a ten kicker for four cards;
                // 8835 Highest pair value for four cards
                // 8850 lowest two pair value for four cards;
                // 8882 two pair value 10s and 6s for four cards;
                // 8927 Highest two pair value;
                // 8931 lowest 3ofk value for four cards
                // 9047 trip jacks and a deuce.
                // 9097 Highest 3ofk value for four cards
                if (rank > 9046)
                {
                    // trip jacks or higher, always bet; low trips never fold
                    if (0 == game.highBet)
                        myBet += 2;
                }
                else if (rank > 8882)
                {
                    // high two pair or low trips; go ahead and bet, but just call lower two pair
                    if (0 == game.highBet)
                        myBet += 2;
                }
                else if (myBet > 0)
                {
                    if (rank < 7900)
                    {
                        // no pair and a bet; just fold
                        myBet = FOLDED;
                    }
                    else
                    {
                        // we must have a single pair. check for live cards and go ahead and call if the pair is live.
                        // find the pair in my hand
                        int high = -1;
                        for (int i = 0; i < myHand.size(); i++ )
                        {
                            Card card = myHand.get( i );
                            for (int j = i + 1; j < myHand.size(); j++ )
                            {
                                if (card.getValue() == myHand.get( j ).getValue())
                                {
                                    high = card.getValue();
                                    break;
                                }
                            }
                            if (high > -1)
                                break;
                        }
                        int howLive = game.getLiveness( robot, high );
                        if (1 == howLive)
                        {
                            // call a big pair, fold anything else
                            if (8 > high)   // pair of tens or less
                                myBet = FOLDED;
                        }
                        else if (1 < howLive)
                        {
                            // too many live cards, just fold
                            myBet = FOLDED;
                        }
                    }
                }
                System.out.println( robot.getPlayerName() + "'s rank for 4 cards is " + rank );
                break;
            case 2: // five cards
                // 9109 is the highest hand by my previous method. For evaluate(), lower is better so normalize it
                // by subtracting from a higher value.
                rank = 13000 - Poker.evaluate( myHand ); // Royal flush will result in 13000 ranking.
                // we have a full hand. evaluate my chances given everone else's possible hands,
                // and compare that to pot odds.
                System.out.println( robot.getPlayerName() + "'s rank for 5 cards of type "
                    + Poker.pokerTypes[ Poker.typeOfHand( myHand )]
                    + " is " + rank );
                break;
            case 3: // six cards
                List<Card> hand = Poker.get5CardsList( myHand );
                rank = 13000 - Poker.evaluate( hand );
                System.out.println( robot.getPlayerName() + "'s rank for 6 cards of type "
                    + Poker.pokerTypes[ Poker.typeOfHand( hand )]
                    + " is " + rank );
                break;
            default:    // five cards or more visible, evaluate the best 5 of seven
                hand = Poker.getBest5CardsOf7( myHand );
                rank = 13000 - Poker.evaluate( hand );
                System.out.println( robot.getPlayerName() + "'s rank for 7 cards of type "
                    + Poker.pokerTypes[ Poker.typeOfHand( hand )]
                    + " is " + rank );
                // What to do, What to do?
        }
        return myBet;
    }
}
