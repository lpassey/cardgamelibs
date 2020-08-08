package com.passkeysoft.poker;

import com.passkeysoft.cardgameserver.CardGameData;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.sse.SseEventSink;

@Path("/")
@Singleton
/*
 * The main JAX-RS server code. Only server endpoints are here. All of the logic will be
 * in the associated PokerServer file
 */
public class PokerResource
{
    @Inject
    PokerServer pokerServer;    //  = new PokerServer();

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @Path( "/test" ) @GET
    @ Produces(MediaType.TEXT_PLAIN)
    public String getIt()
    {
        return "Got it!";
    }


    @SuppressWarnings("VoidMethodAnnotatedWithGET")     // expected for Server Sent Events
    @GET
    @Path("events")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    /**
     * This gets called after poker.html is loaded, and when that page is registering for events. Each
     * time it is called it sends an alert to all participants to update the list of player names.
     *
     * @param eventSink Where to send messages for this game
     * @param playerId  A string representing the integer id of the player registering
     * @param gameId    A string representing the integer id of the game this player wants to join
     * @param noise     A an integer offset into the table of alert sounds
     */
    public void registerForEvents( @Context SseEventSink eventSink, // @Context Sse sse,
        @CookieParam("playerId") String playerId, @CookieParam("gameId") String gameId,
        @CookieParam("noise") String noise )
    {
         pokerServer.registerPlayer( eventSink, playerId, gameId, Integer.valueOf( noise ) );
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String start( @CookieParam("name") String playerName, @CookieParam("gameId") String gameId )
    {
        pokerServer.start( CardGameData.parseGameId( gameId ));
        return "OK";
    }

    @Path( "play" ) @POST
    @Produces( MediaType.TEXT_PLAIN)
    public Response playCard(
        @CookieParam( "playerId" ) String playerId, @CookieParam( "gameId" ) String gameId,
        @FormParam("bet") int bet )
    {
        int playerNum = Integer.parseInt( playerId );
        // get the game that this action is supposed to apply to
        int gameNum = CardGameData.parseGameId( gameId );
        // set these value where the play() method can get them
        CardGameData<PokerGame<PokerPlayer>, PokerPlayer> gameWrapper = PokerServer.getGameData( gameNum );
        synchronized (gameWrapper.theGame)
        {
            //  For testing, we will use the current player no matter what the browser said
            playerNum = gameWrapper.theGame.getCurrentPlayer();    // TODO: for development I need to override this.

            // notify the play thread to do so. Only used for timer threads
            gameWrapper.theGame.setCardToBePlayed( null, playerNum, bet );
            gameWrapper.theGame.notify();
//            gameWrapper.getTheGame().play( null, playerNum, bet );
//            PokerPlayer player = gameWrapper.getTheGame().finalizePlay();
//            if (null != player)
//                pokerServer.activatePlayer(player, gameWrapper.theGame );
        }
        return Response.status(200).entity("OK").build();
    }
}