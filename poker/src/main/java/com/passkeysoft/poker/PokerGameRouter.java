package com.passkeysoft.poker;

import com.passkeysoft.cardgameserver.CardGameData;
import org.glassfish.jersey.media.sse.OutboundEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Path("/")
@Singleton
public class PokerGameRouter
{
    @Inject
    private PokerServer pokerServer;

    private SseBroadcaster sseBroadcaster;

    PokerGameRouter( @Context Sse sse )
    {
        sseBroadcaster = sse.newBroadcaster();
    }

    /**
     * Manage the list of games
     */
    private synchronized void updateGameList()
    {
        List<CardGameData<PokerGame<PokerPlayer>, PokerPlayer>> gameList = PokerServer.getGameList();
        if (0 < gameList.size())
        {
            CardGameData gameData;
            int listSize = gameList.size();
            StringBuilder sb = new StringBuilder( "{\n\"games\":[" );
            boolean first = true;
            for (int i = 0; i < listSize; i++)
            {
                gameData = gameList.get( i );
                if (gameData.getAdminName() != null && !gameData.isStarted())
                {
                    // if this game was created more than 5 minutes ago, and still is
                    // not started, do not list it.
                    long now = System.currentTimeMillis();
                    if (300000 > (now - gameData.whenCreated()) || gameData.isStarted())
                    {
                        if (!first)
                            sb.append( ",\n" );
                        sb.append( "{\"name\":\"" )
                            .append( gameData.getAdminName() )
                            .append( "\", \"number\": " )
                            .append( i )
                            .append( "}" );
                        first = false;
                    }
                }
            }
            sb.append( "]\n}" );
            OutboundEvent event = new OutboundEvent.Builder()
                .name( "games" )
                .mediaType( MediaType.TEXT_PLAIN_TYPE )
                .data( String.class, sb.toString() )
                .build();
            sseBroadcaster.broadcast( event );
        }
    }

    @SuppressWarnings("VoidMethodAnnotatedWithGET")
    @Path( "/games" ) @GET
    @Produces( MediaType.SERVER_SENT_EVENTS)
    public void gameLister( @Context SseEventSink eventSink )
    {
        sseBroadcaster.register( eventSink );
        updateGameList();       // This is just necessary so the newly registering browser gets the list.
    }

    /**
     * Join a game
     */
    @POST
    public Response joinGame( @FormParam("name") String playerName, @FormParam("gameId") String gameId,
        @FormParam( "noise" ) String noise )
        throws URISyntaxException
    {
        final List<CardGameData<PokerGame<PokerPlayer>, PokerPlayer>> gameList = PokerServer.getGameList();

        // gameId is -1 for a new game, or the number of the game to join otherwise
        synchronized (gameList)     // Anyone else wanting to join this game will have to wait until I'm done.
        {
            CardGameData<PokerGame<PokerPlayer>, PokerPlayer> game = null;
            int gameNum = CardGameData.parseGameId( gameId );

            if (0 > gameNum || gameList.size() <= gameNum)
            {
                // asking for a new game
                // run through the game list and see if there is an abandoned game; if so, use it.
                for (CardGameData<PokerGame<PokerPlayer>, PokerPlayer> gameIter : gameList )
                {
                    if (!gameIter.isStarted() && 30000 < (System.currentTimeMillis() - gameIter.whenCreated()))
                    {
                        // this game was started more than 5 minutes ago and is not started. Recycle it.
                        game = gameIter;
                        game.clear( playerName );
                        break;
                    }
                }
                if (null == game)
                {
                    game = new CardGameData<>( playerName );
                    PokerServer.Monitor monitor = new PokerServer().new Monitor( game );
                    game.theGame = new PokerGame<>( 60000, monitor );
                    gameList.add( game );
                }
                updateGameList();
            }
            else
            {
                game = pokerServer.getGameData( gameNum );
            }
            gameNum = gameList.indexOf( game );

            /*
               Add the player to the game list, and set cookies for the player. We will then redirect to the game
                page, and all further server requests will go to {@Link PokerResource }.java
            */
            PokerPlayer playerData = new PokerPlayer( playerName, gameNum );
            game.theGame.playerList.add( playerData );

            NewCookie identity = new NewCookie( "playerId",
                String.valueOf( game.theGame.playerList.indexOf( playerData )));
            NewCookie gameCookie = new NewCookie( "gameId", String.valueOf( gameNum ));
            NewCookie noiseCookie = new NewCookie( "noise", noise );
            return Response
                .seeOther( new URI( "/poker.html" ))
                .cookie( identity )
                .cookie( gameCookie )
                .cookie( noiseCookie )
                .build();
        }
    }
 }
