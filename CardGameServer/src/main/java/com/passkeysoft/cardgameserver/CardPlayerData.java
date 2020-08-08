package com.passkeysoft.cardgameserver;

import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

public class CardPlayerData
{
    // These two fields are set at construction time and cannot be altered?
    protected final int gameId;
    protected final String playerName;

    // How to talk to the browser that represents this player.
    protected SseEventSink eventSink;
    protected Sse sse;

    // whether this player is active in the current round or game.
    protected boolean withdrawn;

    public String getPlayerName()
    {
        return playerName;
    }

    public CardPlayerData( String playerName, int gameId )
    {
        this.gameId = gameId;
        this.playerName = playerName;
    }

    private CardPlayerData setSse( Sse sse )
    {
        this.sse = sse;
        return this;
    }

    public CardPlayerData setEventSink( SseEventSink eventSink )
    {
        this.eventSink = eventSink;
        return this;
    }

    public SseEventSink getEventSink()
    {
        return eventSink;
    }

    public boolean isWithdrawn()
    {
        return withdrawn;
    }
}
