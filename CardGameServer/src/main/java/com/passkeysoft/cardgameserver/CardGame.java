package com.passkeysoft.cardgameserver;

import com.passkeysoft.Card;
import com.passkeysoft.Deck;

import java.util.ArrayList;
import java.util.List;


import static com.passkeysoft.Deck.DISCARD;

public abstract class CardGame<T extends CardPlayerData> implements Runnable
{
    public final List<T> playerList = new ArrayList<>(  );
    // player zero is reserved for the deck. Actual players will begin with 1.

    protected final Runnable monitor;
    private Thread monitorThread = null;

    private boolean wasPaused = false;
    private boolean paused = false;
    protected boolean isRoundOver = false;
    protected int currentPlayer;

    protected Deck deck;
    protected int timeout;
    protected Card toBePlayed;

    // getters and setters for these values are the responsibility of the implementing class
    protected boolean gameOver = false;
    protected int nextPlayer;
    protected int targetPlayer;

    // getters only
    public boolean isRoundOver()
    {
        return isRoundOver;
    }
    public int getCurrentPlayer()
    {
        return currentPlayer;
    }

    /**
     * getter only
     *
     * @return The number of actual players. It does not count the null player which is the first
     *          entry in the player list
     */
    public int getNumPlayers()
    {
        return playerList.size() - 1;
    }

    public int getNumWithdrawn()
    {
        int numWithdrawn = 0;
        for (CardPlayerData player : playerList)
        {
            if (null != player && player.isWithdrawn())
                numWithdrawn++;
        }
        return numWithdrawn;
    }

    // setter only
    public synchronized void setCardToBePlayed( Card toBePlayed, int playerId, int targetId )
    {
        this.toBePlayed = toBePlayed;
        currentPlayer = playerId;
        targetPlayer = targetId;
    }

    /**
     * Constructor
     * @param timeout    How long the "play" method will wait before automatically playing for this player
     * @param monitor    A "runnable" that will be notified when play for a specific player is completed.
     */
    protected CardGame( int timeout, Runnable monitor )
    {
        playerList.add( null );
        this.monitor = monitor;
        this.timeout = timeout;
    }

    public Thread start()
    {
        // Start the thread that monitors my activity
        monitorThread = new Thread( monitor );
        monitorThread.start();

        // Start up my play thread to kill this thread, set gameOver to true
        new Thread( this ).start();
        return monitorThread;
    }

    public int restart( int firstPlayer, boolean withDiscard )
    {
        deck.reset();   // resets the owner of all cards to 0 (the deck) and shuffles
        isRoundOver = false;
        gameOver = false;
        pause();    // The game won't respond until unpaused!
        if (withDiscard)
        {
            deck.dealCardTo( DISCARD ).setRandom( 0L );
        }
        currentPlayer = firstPlayer;
        return firstPlayer;
    }

    /**
     * Generally, plays a specific card by a specific player. Returns void, so sub-classes are responsible for setting
     * any required return value in a thread-protected instance variable. Typically this method is responsible for
     * setting the "nextPlayer" field so it can be picked up by the monitoring class.
     *
     * @param cardPlayed The card to be played
     * @param player    the player playing the card.
     * @param target    the player who is the target of the play. If the value is 65535 then the card
     *                  is being discarded. If the value is zero then the target is (typically) ignored.
     */
    abstract public void play( Object cardPlayed, int player, int target )
        throws IllegalGamePlayException;

    abstract public int getScoreForPlayer( int playerId );

    /**
     * Indicates whether a specific card can be played by a specific player against a specific targeted player
     * The default implementation returns true if the card is in the player's hand, false otherwise
     *
     * @param cardPlayed    The card intended to be played
     * @param player        The player intending to play the card
     * @param target        The player who is the recipient of the card
     * @return              true if the card is playable, false otherwise
     */
    protected boolean isPlayable( Card cardPlayed, int player, int target )
    {
        return null != deck.getMatchingCard( cardPlayed, player );
    }

    /**
     * Started in a new thread, when triggered this method calls the abstract "{@link this.play())" method,  which
     * is game type specific. If the trigger is expiration of the wait timeout, it probably means that the play
     * method should be called as a proxy for a real player who is taking too long.
     *
     * The play() method will not be called if play is paused. When {@link this.unpause()} is called the timeout
     * will be reset and play will resume.
     *
     * This thread will terminate when {@link this.gameOver} becomes true (and the timeout expires). If timeout is
     * zero the thread will immediately terminate (in those cases where timers are not needed).
     *
     * When the thread ends it will also interrupt the monitor thread, if any.
     */
    public void run()
    {
        while (!gameOver && 0 != timeout)
        {
            synchronized (this)
            {
                try
                {
                    wait( timeout );
                    if (paused)
                    {
                        if (wasPaused)
                        {
                            paused = false;
                            wasPaused = false;
                        }
                    }
                    else if (!isRoundOver && !gameOver)
                    {
                        try
                        {
                            // play the currently set card
                            play( toBePlayed, currentPlayer, targetPlayer );
                            setCardToBePlayed( null, currentPlayer, 0 );  // reset for autoplay if not overridden
                            if (null != monitor) synchronized (monitor)
                            {
                                monitor.notify();
                            }
                        }
                        catch( IllegalGamePlayException | IllegalArgumentException ignore )
                        {
                        }
                    }
                    else
                    {
                        System.out.println( "Round is over" );
                    }
                }
                catch( InterruptedException ie )
                {
                    ie.printStackTrace();
                }
            }
        }
        // This thread is shutting down; shut down my monitor as well
        if (null != monitorThread)
            monitorThread.interrupt();
    }

    public boolean isPaused()
    {
        return paused;
    }

    public synchronized void pause()
    {
        wasPaused = false;
        paused = true;
    }

    public synchronized void unPause()
    {
        paused = true;
        wasPaused = true;
        synchronized (this)
        {
            notify();   // This should reset the timer as wait will expire.
            Thread.yield(); // give it a chance.
        }
    }

    /*-----------------------------
     * Proxy methods to protect access to the deck member.
     */
    public String buildHandAsJSON( String pathToFaceImage, List<Card> hand )
    {
        return deck.buildHandAsJSON( pathToFaceImage, hand );
    }

    public List<Card> getHandByOwner( int playerId )
    {
        return deck.getHandByOwner( playerId );
    }

    public String buildCardAsJSON( String pathToFaceImage, Card card )
    {
        return deck.buildCardAsJSON( pathToFaceImage, card );
    }

    public Card drawCard( int player )
    {
        return deck.dealCardTo( player );
    }
}
