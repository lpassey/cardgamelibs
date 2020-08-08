package com.passkeysoft.poker;

import com.passkeysoft.Card;
import com.passkeysoft.cardgameserver.CardGame;
import poker.Poker;

import java.util.ArrayList;
import java.util.List;

import static poker.Tables.*;

@SuppressWarnings("UnusedReturnValue")
public class PokerGame<T extends PokerPlayer> extends CardGame<T>
{
    static int FOLDED = -1;

    // ------------- instance methods and variables for this implementation
    int round;  // 5 rounds of betting, each representing a card that is "up" plus "down and dirty"
    int pot = 0;
    int highBet = 0;
    int firstPlayer;
    String lastAction = "";

    /**
     * Constructor
     *
     * @param timeout   How long to wait in the run thread before proceeding without user action. If zero,
     *                  the thread will immediately exit upon startup, and no user timer will be used.
     * @param monitor   The Runnable class that will be notified after user action has occurred.
     */
    PokerGame( int timeout, PokerServer.Monitor monitor )
    {
        super( timeout, monitor );
        pause();
        deck = new PokerDeck();
        round = 0;
    }

    void restart()
    {
        super.restart( 0, false );  // sets roundOver to false and paused to true
        round = pot = highBet = 0;
        for (int i = 1; i <=  getNumPlayers(); i++)
        {
            dealNewHandToPlayer( i );
            PokerPlayer player = playerList.get( i );
            player.newGame();
            // collect ante
            --player.stake;
            ++pot;
        }
     }

    PokerDeck getDeck()
    {
        return (PokerDeck) deck;
    }

    boolean isGameOver()
    {
        return gameOver;
    }

    PokerGame setGameOver()
    {
        this.gameOver = true;
        return this;
    }

    List<Card> dealNewHandToPlayer( int player )
    {
        // game players are 1 to numPlayers + 1; the game reserves 0 for the deck itself
        if (0 == player)
            throw new IllegalArgumentException( "The player identifier must never be zero." );

        deck.dealNewHandToPlayer( player, 7 );
        return deck.getHandByOwner( player );
    }

    PokerPlayer finalizePlay()
    {
        return ((PokerServer.Monitor) monitor).moveOn();
    }

    /*
      Managing game play is the responsibility of this class. Managing the interface with the browser is
      the responsibility of the server class.
     */
    synchronized void bet( int playerNum, int bet )
    {
        try
        {
            // Just like calling play, except in a different thread. When it's done, it will notify our monitor.
            // playerNum will be the player making the bet.
            PokerPlayer player = playerList.get( playerNum );

            if (bet >=  highBet)
            {
                player.bet = bet;
                if (bet == highBet)
                    lastAction = String.format( "%s %s", player.getPlayerName(), bet == 0 ? "checked" : "called" );
                else
                    lastAction = String.format( "%s raised to %d", player.getPlayerName(), bet );
                highBet = bet;     // A player can never make a bet lower than the high bet.
            }
            else if (0 > bet)
            {
                // this player folded, but his existing bet is still good
                player.withdraw();
                // discard his hand
                List<Card> hand = deck.getHandByOwner( playerNum );
                for (Card card : hand )
                    deck.discard( card, 0 );
                lastAction = String.format("%s folded", player.getPlayerName());
            }
            // By setting the bet before calling getNextPlayer it won't return null if the bets aren't equal
            PokerPlayer nextPlayer = getNextPlayer( );
            if (null == nextPlayer)
            {
                // collect all the bets from all the players, add them to the pot, and reset all the bets to zero
                for ( PokerPlayer pp : playerList)
                {
                    if (null != pp)
                    {
                        pot += pp.bet;
                        pp.stake -= pp.bet;
                        pp.bet = 0;
                    }
                }
                round++;
                highBet = 0;
            }
        }
        catch( Exception ignor )
        {
            ignor.printStackTrace();
        }
    }

    /**
     *
     * @param ignored   Ignored in this implementation, typically null
     * @param player    the player making a bet.
     * @param bet       the amount of the bet. If less than 0, the player is folding.
     */
    @Override
    public void play( Object ignored, int player, int bet )
    {
        bet( player, bet );
    }

    @Override
    public int getScoreForPlayer( int playerId )
    {
        List<Card> bestHand = deck.getHandByOwner( playerId );
        if (7 == bestHand.size())
            bestHand = Poker.getBest5CardsOf7( bestHand );
        if (bestHand.size() > 0)
            return Poker.evaluate( bestHand );
        else
            return 8000;
    }


    List<Card> getCardsForRound( int robotIndex, int startCard )
    {
        List<Card> fullHand = getHandByOwner( robotIndex );
        // build a hand for evaluation, which starts at 2, and has "round" elements
        List<Card> hand = new ArrayList<>( round );
        for (int j = startCard; j < round + 3; j++)
            hand.add( fullHand.get( j ));
        return hand;
    }

    /**
     *
     * @return the player with the highest visible hand.
     */
    public PokerPlayer getNextPlayer()
    {
        if (1 == getNumPlayers() - getNumWithdrawn())
        {
            currentPlayer = 0;
            isRoundOver = true;
            return null;
        }
        if (0 == currentPlayer)
        {
            firstPlayer = 0;
            int bestScore = -1;
            // zero is never a valid player number, so start with one
            for (int i = 1; i < playerList.size() ; i++)
            {
                PokerPlayer player = playerList.get( i );
                if (!player.isWithdrawn())
                {
                    List<Card> hand = getCardsForRound( i, 2 );
                    int handValue = Poker.createBitmap( hand );

                    // count the number of bits set
                    int count = 0;
                    for (int j = 0; j < 13; j++)
                    {
                        int test = handValue >> j;
                        test &= 1;
                        if (0 != test)
                            count++;

                    }
                    if (count != hand.size())
                    {
                        if (2 == hand.size())
                        {
                            // a pair. just add the card value to the base
                            handValue = 7808 + hand.get( 0 ).getValue();
                        }
                        else
                        {
                            // do the prime multiplication thing.
                            int product = 1;
                            for (Card card : hand)
                            {
                                product *= PRIMES[card.getValue()];
                            }
                            if (3 == hand.size())
                            {
                                handValue = 7808 + Poker.threeTable.get( product );
                            }
                            if (4 == hand.size())
                            {
                                handValue = 7808 + Poker.fourTable.get( product );
                            }
                        }
                    }
                    if (handValue > bestScore)
                    {
                        firstPlayer = i;
                        bestScore = handValue;
                    }
                }
            }
            currentPlayer = firstPlayer;
        }
        else
        {
            boolean isWithdrawn = true;
            while( isWithdrawn)
            {
                currentPlayer++;
                if (currentPlayer >= playerList.size())
                    currentPlayer = 1;
                PokerPlayer pokerPlayer = playerList.get( currentPlayer );
                isWithdrawn = pokerPlayer.isWithdrawn();
            }
             // check that every player has equal bets or has withdrawn. If so, set
             // current player to zero and return null;
             boolean betsEqual = true;
             int lastBet = -1;
             for (PokerPlayer player : playerList)
             {
                 if (null != player && !player.isWithdrawn())
                 {
                     if (-1 == lastBet)
                         lastBet = player.bet;
                     else if (lastBet != player.bet)
                     {
                         betsEqual = false;
                         break;
                     }
                 }
             }
             if (betsEqual)
             {
                 if (firstPlayer == currentPlayer || 0 != lastBet)
                 {
                     currentPlayer = 0;
                 }
             }
        }
        return playerList.get( currentPlayer );     // returns null when currentPlayer == 0
    }

    void shuffle()
    {
        deck.shuffle();
    }

    void resetCurrentPlayer()
    {
        currentPlayer = firstPlayer;
    }

    PokerPlayer getWinner()     // TODO: deal with ties.
    {
        int winner = 0;
        int bestHand = 10000;
        for (int i = 1; i < playerList.size(); i++)
        {
            int score = getScoreForPlayer( i );
            // in this case lower is better
            if (score < bestHand)
            {
                winner = i;
                bestHand = score;
            }
        }
        PokerPlayer player = playerList.get( winner );
        // put the pot into the winner's stake.
        player.stake += pot;
        isRoundOver = true;
        return player;
    }

    /**
     * go through all the players (not counting the current player) and find all the visible cards that match
     * the candidate card.
     *
     * @param whosAsking    The player who's looking for liveness
     * @param cardValue     The card value we're checking.
     * @return The number of visible cards that
     */
    int getLiveness( PokerPlayer whosAsking, int cardValue )
    {
        int count = 0;
        for (PokerPlayer player : playerList)
        {
            if (null != player && whosAsking != player && !player.isWithdrawn())
            {
                List<Card> hand = deck.getHandByOwner( playerList.indexOf( player ) );
                for (int i = 2; i < round + 3; i++)
                {
                    if (cardValue == hand.get( i ).getValue())
                        count++;
                }
            }
        }
        return count;
    }
}
