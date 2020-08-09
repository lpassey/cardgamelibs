package com.passkeysoft.poker;

import poker.Poker;
import com.passkeysoft.Card;
import com.passkeysoft.cardgameserver.CardGameMetadata;
import com.passkeysoft.cardgameserver.CardPlayer;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Singleton;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.SseEventSink;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Singleton
/*
     Managing game play is the responsibility of the PokerGame class. Managing the interface with the browser is
    the responsibility of the this class.
 */
class PokerServer
{
    private static String[] noises = { "faces/silence.mp3", "faces/iMac_Startup_Chime.mp3",
        "faces/Itsyourturn.mp3", "faces/Lightsaber-On.mp3", "faces/Magic-Wand-Noise.mp3",
        "faces/Metronome.mp3", "faces/tardis.mp3", "faces/Game-Show-Wheel-Spin.mp3",
        "faces/Rudy_rooster_crowing.mp3", "faces/horned_owl.mp3", "faces/Hawk-Call.mp3",
        "faces/Metal_Gong.mp3"};

    private static final List<CardGameMetadata<PokerGame<PokerPlayer>, PokerPlayer>> gameList = new ArrayList<>(  );

    static List<CardGameMetadata<PokerGame<PokerPlayer>, PokerPlayer>> getGameList()
    {
        return gameList;
    }

    static CardGameMetadata<PokerGame<PokerPlayer>, PokerPlayer> getGameData( int gameNum )
    {
        CardGameMetadata<PokerGame<PokerPlayer>, PokerPlayer> game;
        try
        {
            game = gameList.get( gameNum );
        }
        catch( ArrayIndexOutOfBoundsException ex )
        {
            // Asked for an invalid game, give them the default game. TODO: or should we restart a new one?
            game = gameList.get( 0 );
        }
        return game;
    }

    /*
      This class will run as a separate thread, and will contain pretty much all the logic necessary to play a single card.
     */

    public class Monitor implements Runnable
    {
        private final CardGameMetadata<PokerGame<PokerPlayer>, PokerPlayer> gameMeta;

        Monitor( CardGameMetadata<PokerGame<PokerPlayer>, PokerPlayer> game )
        {
            this.gameMeta = game;
        }

        private void highlightHands( PokerGame<PokerPlayer> game )
        {
            String typeOfHand;

            StringBuilder sb = new StringBuilder( "{\"hands\": [" );
            boolean first = true;
            for (int i = 1; i < game.playerList.size(); i++)
            {
                List<Card> hand = null;
                PokerPlayer player = game.playerList.get( i );
                if (!first)
                    sb.append( "," );
                sb.append( "{\"id\":" )
                    .append( i )
                    .append( ",\"name\":\"" )
                    .append( player.getPlayerName() )
                    .append( "\",\"cards\":" );

                if (player.isWithdrawn())
                {
                    sb.append( "[]" );
                    typeOfHand = "folded";
                }
                else
                {
                    hand = game.getDeck().getHandByOwner( i );
                    // this produces the index in the full hand of every card not in the best hand
                    // we do this to minimize the unused cards
                    List<Card> bestHand = Poker.getBest5CardsOf7( hand );
                    List<Integer> outies = hand.stream()
                        .filter( o -> (bestHand.indexOf( o ) == -1) )
                        .map( hand::indexOf )
                        .collect( Collectors.toList() );

                    sb.append( outies.toString() );
                    typeOfHand = Poker.pokerTypes[Poker.typeOfHand( bestHand )];
                }

                sb.append( ", \"type\":\"" )
                    .append( typeOfHand )
                    .append( "\"}" );
                first = false;
            }
            sb.append( "]}" );
            OutboundEvent event = new OutboundEvent.Builder()
                .name( "best" )
                .mediaType( MediaType.TEXT_PLAIN_TYPE )
                .data( String.class, sb.toString() )
                .build();
            broadcast( game.playerList, event );
        }

        PokerPlayer moveOn()
        {
            PokerPlayer nextPlayer;
            PokerGame<PokerPlayer> game = gameMeta.getTheGame();
            if (0 == game.getCurrentPlayer())
            {
                // the bet is over. Tell everyone to update their hands
                if (game.round > 3)
                {
                    if (5 == game.round)
                    {
                        game.pause();
                        // showdown. show all hands with best 5 highlighted.
                        // Send every player the remaining cards. The player already knows what he has
                        sendOpponentsHands( game );
                            highlightHands( game );

                        // pick the winner. Collects the pot into the winner's stake and sets roundOver to true
                        PokerPlayer winner = game.getWinner();

                        OutboundEvent event = new OutboundEvent.Builder()
                            .name( "round-over" )
                            .mediaType( MediaType.TEXT_PLAIN_TYPE )
                            .data( String.class, winner.getPlayerName() )
                            .build();
                        broadcast( game.playerList, event );

                        // We're waiting for the button click to clear the game and restart.
                        return null;
                    }
                    else
                    {
                        // This is the player we started with in the round. Use him again for last bet.
                        // we need to set the currentPlayer to this as well so everyone gets a chance to bet.
                        game.resetCurrentPlayer();
                        nextPlayer = game.playerList.get( game.firstPlayer );
                        game.lastAction = game.lastAction + "<br /><br />Last card down and dirty";
                    }
                }
                else    // This gets skipped for the "down and dirty" bet
                {
                    nextPlayer = game.getNextPlayer();
                    game.lastAction = game.lastAction + "<br /><br />Dealing next card";
                }
                // Send every player the remaining cards of his/her hand
                populateAllHands( game );

                // Then send everyone the remainder of the hands, with the first two cards obscured.
                // Show one more card to everyone, then get the high player, and activate him.
                try
                {
                    sendOpponentsHands( game );
                }
                catch( Exception ex )
                {
                    ex.printStackTrace();
                }
            }
            else
                // betting is not over, get the next player.
                nextPlayer = game.playerList.get( game.getCurrentPlayer() );

            return nextPlayer;
        }

        @Override
        public void run()
        {
            // We'll get notified when the play is over.
            while (null != gameMeta.getTheGame() && !gameMeta.getTheGame().isGameOver()) try
            {
                PokerPlayer player = null;
                synchronized (this)
                {
                    wait( 600000 );
                    player = moveOn();
                }
                if (null != player)
                {
                    PokerGame<PokerPlayer> game = gameMeta.getTheGame();
                    announceActivePlayer( game, player.getPlayerName(), game.lastAction );
                    activatePlayer( player, game );
                }
            }
            catch( InterruptedException ignore )
            {
                ignore.printStackTrace();
                // When we get interrupted, that means the game is over and we can stop this thread.
                if (null != gameMeta.getTheGame() && gameMeta.getTheGame().isGameOver())
                    gameMeta.getTheGame().setGameOver();
//                return;
            }
        }
    }


    private synchronized void broadcast( List<PokerPlayer> playerList, OutboundEvent event )
    {
        for (CardPlayer player : playerList) try
        {
            if (null != player)
            {
                ((PokerPlayer) player).sendEvent( event );
                Thread.sleep( 10 );
            }
        }
        catch( InterruptedException ignore ) {}
    }

    /**
     * This gets called after poker.html is loaded, and when that page is registering for events.
     * @param eventSink Where to send messages for this game
     * @param playerId  A string representing the integer id of the player registering
     * @param gameId    A string representing the integer id of the game this player wants to join
     */
    synchronized void registerPlayer( SseEventSink eventSink, String playerId, String gameId, int noise )
    {
        int playerNum = Integer.parseInt( playerId );
        CardGameMetadata<PokerGame<PokerPlayer>, PokerPlayer> gameWrapper = getGameData( CardGameMetadata.parseGameId( gameId ));
        List<PokerPlayer> players = gameWrapper.getTheGame().playerList;
        PokerPlayer player;
        try
        {
            player = players.get( playerNum );
            if (null == player)
                throw new IndexOutOfBoundsException(  );
        }
        catch( IndexOutOfBoundsException ex)
        {
            throw new NotFoundException( "Player number " + playerId + " was not registered for this game" );
        }
        player.setEventSink( eventSink );

        // Send the registrant his player id so he can save it as a variable, not a cookie.
        OutboundEvent event = new OutboundEvent.Builder()
            .name( "whoami" )
            .mediaType( MediaType.TEXT_PLAIN_TYPE )
            .data( String.class, String.format( "{\"name\":\"%s\",\n\"playerId\":%d,\n\"noise\":\"%s\"}",
                player.getPlayerName(), gameWrapper.getTheGame().playerList.indexOf( player ), noises[noise] ) )
            .build();
        player.sendEvent( event );

        // Send a list of all the players to all the players so they can show that list in the UI.
        boolean first = true;
        StringBuilder sb = new StringBuilder( "{\n\"players\":[" );
        for (int i = 1;  i < players.size(); i++ )
        {
            if (!first)
                sb.append( ",\n" );
            sb.append( "{\"id\":" )
                .append( i )
                .append( ",\n\"name\":\"" )
                .append( players.get( i ).getPlayerName())
                .append( "\",\n\"stake\":")
                .append( players.get( i ).stake )
                .append( "}" );
            first = false;
        }
        sb.append( "]\n}" );
        event = new OutboundEvent.Builder()
            .name( "participants" )
            .mediaType( MediaType.TEXT_PLAIN_TYPE )
            .data( String.class, sb.toString() )
            .build();
        broadcast( gameWrapper.getTheGame().playerList, event );
    }

    // --------- private methods that support start and play methods
    private void announceActivePlayer( PokerGame<PokerPlayer> game, String player, String lastAction )
    {
        StringBuilder sb = new StringBuilder( "{\"action\":{\"name\":\"" );
        sb.append( player )
            .append( "\",\"last\":\"" )
            .append( lastAction )
            .append( "\", \"pot\" : " )
            .append( game.pot )
            .append( "}}" );
        OutboundEvent event = new OutboundEvent.Builder()
            .name("player")
            .mediaType(MediaType.TEXT_PLAIN_TYPE)
            .data(String.class, sb.toString() )
            .build();
        broadcast( game.playerList, event);
        // TODO: if player has no event sink that means it is a robot. We need to trigger autoplay.
    }

    //---------- private methods that support the start resource
    private List<Card> getCardsForRound( PokerPlayer player, PokerGame<PokerPlayer> game  )
    {
        List<Card> hand = game.getHandByOwner( game.playerList.indexOf( player ) );
        List<Card> partial = new ArrayList<>( 3 + game.round );
        if (hand.size() > 1 + game.round)
        {
            for (int i = 0; i < 3 + game.round; i++)
                partial.add( hand.get( i ) );
        }
        return partial;
    }

    private synchronized void populateAllHands( PokerGame<PokerPlayer> game )
    {
        List<PokerPlayer> players = game.playerList;

        for (PokerPlayer player : players )
        {
            if (null != player)
            {
                // we send hands to folded players just to clear their displays
                player.sendHand( game, getCardsForRound( player, game ));
            }
        }
    }

    private synchronized void sendOpponentsHands( PokerGame<PokerPlayer> theGame )
    {
        List<PokerPlayer> players = theGame.playerList;

        boolean first = true;
        StringBuilder sb = new StringBuilder("{\"hands\":[");
        for (PokerPlayer player : players)
        {
            if (null != player)
            {
                int playerIdx = players.indexOf( player );
                // for folded players this hand should be empty
                List<Card> hand = theGame.getHandByOwner( playerIdx );
                List<Card> partial = new ArrayList<>( 3 + theGame.round );
                if (3 < hand.size())
                {
                    if (8 > 3 + theGame.round)
                    {
                        partial.add( PokerDeck.back );
                        partial.add( PokerDeck.back );
                    }
                    else
                    {
                        partial.add( hand.get( 0 ) );
                        partial.add( hand.get( 1 ) );
                    }
                    int limit = (theGame.round < 4 ? 3 + theGame.round : 6);
                    for (int i = 2; i < limit; i++)
                        partial.add( hand.get( i ) );
                    if (4 == theGame.round)
                        partial.add( PokerDeck.back );
                    else if (5 == theGame.round)
                        partial.add( hand.get( 6 ) );
                }
                else
                {
                    partial.add(  PokerDeck.back  );
                }
                if (!first)
                    sb.append( "," );
                sb.append( "{\"name\":\"" )
                    .append( player.getPlayerName() )
                    .append( "\",\"id\":" )
                    .append( playerIdx )
                    .append( ",\"stake\":" )
                    .append( player.stake )
                    .append( ",\"hand\":" )
                    .append( theGame.buildHandAsJSON( "faces/", partial ));
                sb.append( "}" );
                first = false;
            }
        }
        sb.append( "]}" );
        final OutboundEvent event = new OutboundEvent.Builder()
            .name( "show-all")
            .data( String.class, sb.toString() )
            .build();
        broadcast( theGame.playerList, event );
    }

    /**
     * Start the game
     */
    void start( int gameId )
    {
        // initialize the deck and restart the game.
        CardGameMetadata<PokerGame<PokerPlayer>, PokerPlayer> gameWrapper = getGameData( gameId );
        PokerGame<PokerPlayer> theGame = gameWrapper.getTheGame();

        if (!gameWrapper.isStarted())
        {
            // Add some players for testing -- Gort, Robby, Wall-E, Hal, Data, Rosie, Marvin, etc
            PokerPlayer playerData = new PokerPlayer( "R2D2" );
            theGame.playerList.add( playerData );
            playerData = new PokerPlayer( "Marvin" );
            theGame.playerList.add( playerData );
            playerData = new PokerPlayer( "Wall-E" );
            theGame.playerList.add( playerData );
            //         playerData = new PokerPlayer( "Data", gameId );
            //         game.playerList.add( playerData );
            // Only needed if we want to use timer threads.
            gameWrapper.getTheGame().start();
            gameWrapper.started = true;    // No one else can join now. TODO: Do we want this?
        }
        gameWrapper.getTheGame().restart();

        // send every registered player a restart event with the gameId -- current unused
//        OutboundEvent event = new OutboundEvent.Builder()
//            .name( "restart" )
//            .mediaType( MediaType.TEXT_PLAIN_TYPE )
//            .data( String.class, String.valueOf( gameList.indexOf( game ) ) )
//            .build();
//        broadcast( game.playerList, event );    // broadcast() protects against null players.

        // Send every player the first three cards of his/her hand
        populateAllHands( gameWrapper.getTheGame() );

        // Then send everyone the remainder of the hands, with the first two cards obscured.
        sendOpponentsHands( gameWrapper.getTheGame() );

        // Tell the first player to activate
        PokerPlayer player = gameWrapper.getTheGame().getNextPlayer();
        announceActivePlayer( gameWrapper.getTheGame(), player.getPlayerName(), "New deal" );
//        player = gameWrapper.theGame.playerList.get( 1 );    // For testing, use me.
        gameWrapper.getTheGame().unPause();
        activatePlayer( player, gameWrapper.getTheGame() );
    }

    void activatePlayer( PokerPlayer player, PokerGame<PokerPlayer> game )
    {
        if (null == player.getEventSink())
        {
            // robot player
            int bet = Poker7Robot.actLikeARobot( player, game );
            synchronized (game)
            {
                game.setObjectToBePlayed( null, game.playerList.indexOf( player ), bet );
                game.notify();
            }
        }
        else
            player.activate( game.highBet );
    }
}
