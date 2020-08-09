package com.passkeysoft.cardgameserver;

import com.passkeysoft.Card;
import com.passkeysoft.Deck;

import java.util.ArrayList;
import java.util.List;

import static com.passkeysoft.Deck.DISCARD;

/**
 * An abstract class providing basic functionality for a card game.
 * @param <T>   The type of player that will participate in this game.
 */
public abstract class CardGame<T extends CardPlayer> implements Runnable
{
    /**
     * A list of players participating in this game.
     *
     * player zero is reserved for the deck. Actual players will begin with 1.
     */
    public final List<T> playerList = new ArrayList<>(  );

    /**
     * The card Deck containing a deck of all the cards used by this game.
     */
    protected Deck deck;

    /**
     * A {@link Runnable} object that will be used to monitor the play of this game. It is
     * provided by the class instantiating this class, but the Thread containing the <b>monitor</b>
     * will be started by this classes and a Thread reference will be maintained as an instance
     * variable in this instance.
     */
    protected final Runnable monitor;

    private Thread monitorThread = null;
    private int timeout;

    private boolean wasPaused = false;
    private boolean paused = false;

    private Object toBePlayed;

    // getters and setters for these values are the responsibility of the implementing class
    protected boolean gameOver = false;

    protected int currentPlayer;
    protected int nextPlayer;
    protected int targetPlayer;

    /**
     * A flag indicating whether a round of play for this game is complete. The meaning of a
     * <b>round</b> is dependant on the derived class. In a game like Poker a round could be
     * a round of betting, but in a game like Hearts a round may be when all tricks have been
     * played.
     */
    protected boolean isRoundOver = false;

    /**
     * {@link CardGame} Constructor.  This method is protected, so this object cannot be
     * instantiated directly, it can only be used by derived sub-classes.
     *
     * @param timeout    How long the {@link CardGame#run} method will wait before automatically playing
     *                   for the next player
     * @param monitor    A {@link Runnable} object that will be notified when play for the specified
     *                   player is completed.
     */
    protected CardGame( int timeout, Runnable monitor )
    {
        playerList.add( null );
        this.monitor = monitor;
        this.timeout = timeout;
    }

    // public getters for private/protected properties
    /**
     * @return true if the round is over.
     */
    public boolean isRoundOver()
    {
        return isRoundOver;
    }

    /**
     * @return the index of the current player in the list of players.
     */
    public int getCurrentPlayer()
    {
        return currentPlayer;
    }

    /**
     * @return  The number of actual players. It does not count the null player which is the first
     *          entry in the player list
     */
    public int getNumPlayers()
    {
        return playerList.size() - 1;
    }

    /**
     *
     * @return  The number of players in this game who have withdrawn.
     */
    public int getNumWithdrawn()
    {
        int numWithdrawn = 0;
        for (CardPlayer player : playerList)
        {
            if (null != player && player.isWithdrawn())
                numWithdrawn++;
        }
        return numWithdrawn;
    }

    /**
     * Sets instance values used by the {@link CardGame#play} method when called from inside
     * the {@link CardGame#run()} method. The contents of these instance values are consumed by
     * the {@link CardGame#run()} method in a different thread, so it is imperative that write
     * access to these instance values by synchronized on this instance.
     *
     * @param toBePlayed    The object to be played. Typically, in card shedding games or card war games
     *                      the toBePlayed object is a {@link Card} object.
     * @param playerIdx     The index into the player list of the player who is playing
     * @param targetIdx     The index into the player list of the player who is the target of the action (optional).
     */
    public synchronized void setObjectToBePlayed( Object toBePlayed, int playerIdx, int targetIdx )
    {
        this.toBePlayed = toBePlayed;
        currentPlayer = playerIdx;
        targetPlayer = targetIdx;
    }

     /**
      * Create a new {@link Thread} that will "Play" this game on behalf of a specified player
      * when notified. It also creates the monitor thread for the <b>monitor</b> object which was
      * furnished in the constructor. If the <b>monitor</b> property is null, no new {@link Thread}s
      * will be created.
      * <br/>
      * For details on the behavior of the "Play" thread see the documentation for the {@link CardGame#run()}
      * method.
      *
      * @return A reference to the new monitor thread, or null if no threads were created.
     */
    public Thread start()
    {
        if (null != monitor)
        {
            // Start the thread that monitors my activity
            monitorThread = new Thread( monitor );
            monitorThread.start();

            // Start up my play thread to kill this thread, set gameOver to true
            new Thread( this ).start();
            return monitorThread;
        }
        return null;
    }

    /**
     * Restarts a round of play. Resets properties to their initial values, resets the owner of
     * all cards in the {@link CardGame#deck} to 0 (the deck) and shuffles it, and sets the state
     * of the round to paused.
     *
     * @param firstPlayer The index of the player in the {@link CardGame#playerList} who will be the
     *                    first to play the new round.
     * @param withDiscard If true, the first card in the newly shuffled deck will be discarded to
     *                    start the discard stack.
     * @return  The index of the player to start the game.
     */
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
     * Generally, plays a specific card by a specific player. Returns void, so sub-classes are
     * responsible for setting any required return value in a thread-protected instance variable.
     * Typically this method is responsible for setting the "nextPlayer" field so it can be picked
     * up by the monitoring class.
     *
     * @param objectPlayed The object to be played. Typically in shedding style card games
     *                     (e.g. Hearts) or "war" style card games (e.g. <i>Milles Bornes</i>
     *                     the objectPlayed is an instance of {@link Card}.
     * @param player    the index of the player playing the card.
     * @param target    the index of the player who is the target of the play. If the value is
     *                  65535 then the card is being discarded. If the value is zero then
     *                  the target is (typically) ignored.
     */
    abstract public void play( Object objectPlayed, int player, int target )
        throws IllegalGamePlayException;

    /**
     * Gets the current score for the specified player. This may be a game score or it may be a
     * round score, depending on the needs of the implementation.
     *
     * @param playerId The index of the player whose score is being sought.
     * @return  The player's score.
     */
    abstract public int getScoreForPlayer( int playerId );

    /**
     * Indicates whether a specific card can be played by a specific player against a specific
     * targeted player. The default implementation simply returns true if the card is in the
     * player's hand, false otherwise
     *
     * @param cardPlayed    The card intended to be played
     * @param player        The index of the player intending to play the card
     * @param target        The index of the player who is the recipient of the card, if any
     * @return              true if the card is playable, false otherwise
     */
    protected boolean isPlayable( Card cardPlayed, int player, int target )
    {
        return null != deck.getMatchingCard( cardPlayed, player );
    }

    /**
     * Started in a new thread, when triggered this method calls the abstract {@link CardGame#play(Object, int, int)}
     * method, which is specific to the derived sub-class. If the trigger is the expiration of the
     * wait timeout, it probably means that the play method should be called as a proxy for a real
     * player who is taking too long.
     *
     * The {@link CardGame#play(Object, int, int)} method will not be called if play is paused.
     * When {@link CardGame#unPause()} is called the timeout will be reset and play will resume.
     *
     * This thread will terminate when {@link CardGame#gameOver} becomes true (and the timeout expires).
     * If {@link CardGame#timeout} is zero when this method is first called the thread will immediately
     * terminate (in those cases where timers are not needed).
     *
     * When this method ends it will also interrupt the monitor thread, if any.
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
                            setObjectToBePlayed( null, currentPlayer, 0 );  // reset for autoplay if not overridden
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

    /**
     * @return true if the current round is paused, false otherwise.
     */
    public boolean isPaused()
    {
        return paused;
    }

    /**
     * Pause the current round.
     */
    public synchronized void pause()
    {
        wasPaused = false;
        paused = true;
    }

    /**
     * Unpause the current round. Will have the effect of resetting the play timer so the next
     * player after play resumes will have the full time available.
     */
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
     * Proxy methods to protect access to the {@link CardGame#deck} protected variable.
     */

    /**
     * Proxy method to protect access to the {@link CardGame#deck} protected variable.
     */
    public String buildHandAsJSON( String pathToFaceImage, List<Card> hand )
    {
        return deck.buildHandAsJSON( hand, pathToFaceImage );
    }

    /**
     * Proxy method to protect access to the {@link CardGame#deck} protected variable.
     */
    public List<Card> getHandByOwner( int playerId )
    {
        return deck.getHandByOwner( playerId );
    }

    /**
     * Proxy method to protect access to the {@link CardGame#deck} protected variable.
     */
    public String buildCardAsJSON( String pathToFaceImage, Card card )
    {
        return deck.buildCardAsJSON( card, pathToFaceImage );
    }

    /**
     * Proxy method to protect access to the {@link CardGame#deck} protected variable.
     */
    public Card drawCard( int player )
    {
        return deck.dealCardTo( player );
    }
}
