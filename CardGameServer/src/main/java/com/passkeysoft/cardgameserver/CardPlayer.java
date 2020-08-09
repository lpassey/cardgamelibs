package com.passkeysoft.cardgameserver;

import javax.ws.rs.sse.SseEventSink;

public class CardPlayer
{
    /**
     *  player nickname, set at the time of construction, and cannot be changed thereafter.
     */
    private final String playerName;

    /**
     * How to talk to the browser that represents this player. If this property remains null the
     * business logic should detect it and not fail. For example, in the case of player robots
     * this value will typically be null and an alternative should be used to trigger the robot.
     */
    protected SseEventSink eventSink;

    /**
     *    Whether this player is active in the current round or game.
     */
    protected boolean withdrawn;

    /**
     * @param playerName The immutable nickname associated with this player for the duration of the game.
     */
    public CardPlayer( String playerName )
    {
        this.playerName = playerName;
    }

    /**
     * Sets the eventSink protected instance. This {@link SseEventSink} can be used to send
     * events unique to this player to the web browser associated with this player. This is
     * not the only mechanism that a {@link CardGame} can be used to communicate with a player
     * proxy; other mechanisms can be used in sub-classes.
     *
     * @param eventSink a {@link SseEventSink} which can be used to send events unique to this
     *                  player to the web browser associated with this player.
     * @return this instance, suitable for fluent-style programming.
     */
    public CardPlayer setEventSink( SseEventSink eventSink )
    {
        this.eventSink = eventSink;
        return this;
    }

    /**
     * @return the {@link SseEventSink} associated with this player; can be used to send
     * Server-Sent Events to the player's browser
     */
    public SseEventSink getEventSink()
    {
        return eventSink;
    }

    /**
     * @return the player's nickname
     */
    public String getPlayerName()
    {
        return playerName;
    }

    /**
     * @return true if the player has withdrawn from this game or round, false otherwise.
     */
    public boolean isWithdrawn()
    {
        return withdrawn;
    }
}
