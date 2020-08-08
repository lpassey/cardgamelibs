package com.passkeysoft.poker;

import com.passkeysoft.Card;
import com.passkeysoft.cardgameserver.CardPlayerData;
import org.glassfish.jersey.media.sse.OutboundEvent;

import javax.ws.rs.core.MediaType;
import java.util.List;

class PokerPlayer extends CardPlayerData
{
    PokerPlayer( String playerName, int gameId )
    {
        super( playerName, gameId );
        stake = 1000;
    }

    int stake, bet = 0;

    void sendEvent( OutboundEvent event )
    {
        if (null != getEventSink())
            getEventSink().send( event );
    }

    synchronized void sendHand( PokerGame theGame, List<Card> hand )
    {
        String jsonHand = theGame.buildHandAsJSON( "faces/", hand );
        final OutboundEvent event = new OutboundEvent.Builder()
            .name( "show-hand" )
            .data( String.class, jsonHand )
            .build();
        sendEvent( event );
    }

    /**
     * This method needs to send the pot (for pot limit), and the highest bet so far. TODO: Do I need my current bet as well?
     * @param highBet the highest bet placed so far. Call will match this bet for the player
     */
    synchronized void activate( int highBet )
    {
        StringBuilder sb = new StringBuilder( "{\"betData\": {" );
        sb.append( "\"highBet\":" ).append( highBet );
        sb.append( "}}" );

        OutboundEvent event = new OutboundEvent.Builder()
            .name( "activate" )
            .mediaType( MediaType.TEXT_PLAIN_TYPE )
            .comment( "activate player" )
            .data( String.class, sb.toString() )
            .build();
        sendEvent( event );     // protects against NPE
    }

    void withdraw()
    {
        withdrawn = true;
    }

    /**
     * Do whatever is necessary here to prepare the player for a new game.
     */
    void newGame()
    {
        withdrawn = false;
    }
}
