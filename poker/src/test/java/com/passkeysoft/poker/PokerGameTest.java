package com.passkeysoft.poker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.passkeysoft.Card;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import poker.Poker;

import java.util.*;

import static poker.Poker.*;
import static com.passkeysoft.poker.PokerDeck.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PokerGameTest
{
    @Before
    public void setUp() throws Exception
    {
//        game.shuffle();
    }

    @After
    public void tearDown() throws Exception
    {
    }

    private List<Card> buildRoyalFlush()
    {
        ArrayList<Card> hand = new ArrayList<Card>();
        hand.add( new Card( CLUBS, KING ));
        hand.add( new Card( CLUBS, JACK ));
        hand.add( new Card( CLUBS, TEN ));
        hand.add( new Card( CLUBS, QUEEN ));
        hand.add( new Card( CLUBS, ACE ));
        return hand;
    }

    @Test
    public void royalFlushValue()
    {
        List<Card> hand = buildRoyalFlush();
        int result = Poker.evaluate( hand );
        assertEquals( 1, result );
    }

    @Test
    public void evaluateSevenHigh()
    {
        ArrayList<Card> hand = new ArrayList<Card>();
        hand.add( new Card( HEARTS, SEVEN ) );
        hand.add( new Card( CLUBS, FIVE)  );
        hand.add( new Card( DIAMONDS, FOUR ) );
        hand.add( new Card( SPADES, TREY ) );
        hand.add( new Card( HEARTS, DEUCE ) );

        assertEquals( 7462, Poker.evaluate( hand ) );
    }

    @Test
    public void evaluateLowPair()
    {
        ArrayList<Card> hand = new ArrayList<Card>();
        hand.add( new Card( HEARTS, DEUCE ) );
        hand.add( new Card( DIAMONDS, DEUCE ) );
        hand.add( new Card( CLUBS, TREY ) );
        hand.add( new Card( CLUBS, FOUR ) );
        hand.add( new Card( CLUBS, FIVE ) );
        assertEquals( 6185, Poker.evaluate( hand ) );
    }

    @Test
    public void compareFlushAndBoat()
    {
        // Build lowest possible flush
        List<Card> hand = new ArrayList<>();
        hand.add( new Card( CLUBS, DEUCE ) );
        hand.add( new Card( CLUBS, TREY ) );
        hand.add( new Card( CLUBS, FOUR ) );
        hand.add( new Card( CLUBS, FIVE ) );
        hand.add( new Card( CLUBS, SEVEN ) );
        int flush = Poker.evaluate( hand );
        assertEquals( "Flush", pokerTypes[Poker.typeOfHand( hand )] );

        // Build LOWEST possible full house
        List<Card> hand2 = new ArrayList<>();
        hand2.add( new Card( HEARTS, DEUCE ) );
        hand2.add( new Card( DIAMONDS, DEUCE ) );
        hand2.add( new Card( CLUBS, DEUCE ) );
        hand2.add( new Card( SPADES, TREY ) );
        hand2.add( new Card( CLUBS, TREY ) );
        int full = Poker.evaluate( hand2 );
        assertThat( full, Matchers.lessThan( flush ));
        assertEquals( "Full House",  pokerTypes[Poker.typeOfHand( hand2 )] );

        // Build LOWEST possible straight
        List<Card> hand3 = new ArrayList<>();
        hand3.add( new Card( HEARTS, ACE ) );
        hand3.add( new Card( DIAMONDS, DEUCE ) );
        hand3.add( new Card( CLUBS, TREY ) );
        hand3.add( new Card( SPADES, FOUR ) );
        hand3.add( new Card( CLUBS, FIVE ) );
        int straight = Poker.evaluate( hand3 );
        assertThat( full, Matchers.lessThan( straight ));
        assertEquals( "Straight",  pokerTypes[Poker.typeOfHand( hand3 )] );

        // build LOWEST possible three of a kind
        List<Card> hand4 = new ArrayList<>();
        hand4.add( new Card( HEARTS, DEUCE ) );
        hand4.add( new Card( DIAMONDS, DEUCE ) );
        hand4.add( new Card( CLUBS, FOUR ) );
        hand4.add( new Card( SPADES, TREY ) );
        hand4.add( new Card( CLUBS, DEUCE ) );
        int tok = Poker.evaluate( hand4 );
        assertEquals( "Three of a Kind",  pokerTypes[Poker.typeOfHand( hand4 )] );

        // build highest possible two PAIR
        hand4 = new ArrayList<>();
        hand4.add( new Card( HEARTS, ACE ) );
        hand4.add( new Card( DIAMONDS, ACE ) );
        hand4.add( new Card( CLUBS, KING ) );
        hand4.add( new Card( SPADES, KING ) );
        hand4.add( new Card( CLUBS, QUEEN ) );
        int high3 = Poker.evaluate( hand4 );
        assertEquals( "Two Pair",  pokerTypes[Poker.typeOfHand( hand4 )] );

        // build highest hand
        hand4 = new ArrayList<>();
        hand4.add( new Card( HEARTS, ACE ) );
        hand4.add( new Card( DIAMONDS, KING ) );
        hand4.add( new Card( CLUBS, QUEEN ) );
        hand4.add( new Card( SPADES, JACK ) );
        hand4.add( new Card( CLUBS, NINE ) );
        int low3 = Poker.evaluate( hand4 );
        assertThat( full, Matchers.lessThan( low3 ));
        assertEquals( "High Card",  pokerTypes[Poker.typeOfHand( hand4 )] );
    }

    @Test
    public void compareLowPairAndHighCard()
    {
        // Build a pair
        List<Card> hand = new ArrayList<>();
        hand.add( new Card( 4, KING ) );
        hand.add( new Card( 8, FIVE ) );
        hand.add( new Card( 2, DEUCE ) );
        hand.add( new Card( 8, DEUCE ) );
        hand.add( new Card( 1, QUEEN ) );
        assertEquals( "One Pair",  pokerTypes[Poker.typeOfHand( hand )] );

        // highest possible high card
        List<Card> hand2 = new ArrayList<>();
        hand2.add( new Card( 4, ACE ) );
        hand2.add( new Card( 8, KING ) );
        hand2.add( new Card( 4, QUEEN ) );
        hand2.add( new Card( 8, JACK ) );
        hand2.add( new Card( 1, NINE ) );
        assertEquals( "High Card",  pokerTypes[Poker.typeOfHand( hand2 )] );

        int pair = Poker.evaluate( hand );
        int high = Poker.evaluate( hand2 );
        assertThat( pair, Matchers.lessThan( high ));
    }

    @Test
    public void get5of7()
    {
        PokerGame<PokerPlayer> game = new PokerGame<>(600000, null );
        List<Card> hand = game.dealNewHandToPlayer( 1 );

        PokerDeck deck = game.getDeck();

        // Sort by suit then value; should have the effect of sorting all derived hands as well
        // ordinarily this wouldn't be done, but we're doing here for testing purposed
        hand.sort( Comparator.comparing( Card::getValue ).reversed().thenComparing( Card::getSuit ) );
        String myHand = deck.buildHandAsJSON( hand, "" );
        List<Card> bestHand = Poker.getBest5CardsOf7( hand );
        int value = Poker.evaluate( bestHand );
        myHand = deck.buildHandAsJSON( bestHand, "" );
    }

    @Test
    public void test7Stud() throws JsonProcessingException
    {
        PokerGame<PokerPlayer> game = new PokerGame<>(600000, null );
        List<List<Card>> hands = new ArrayList<>( 5 );
        PokerDeck deck = game.getDeck();

        // create 5 three cards hands to the player; third card is face up for betting.
        for (int i = 0; i < 5; i++ )
        {
            List<Card> hand = deck.dealNewHandToPlayer( i + 1, 3 );
            hands.add( hand );
        }

        // bet
        assertEquals( 3, deck.getHandByOwner( 1 ).size());

        // deal fourth card to each player
        for (int i = 0; i < 5; i++)
        {
            hands.get(i).add( deck.dealCardTo( i+1 ) );
        }

        // bet
        assertEquals( 4, deck.getHandByOwner( 2 ).size());


        // deal fifth card to each player
        for (int i = 0; i < 5; i++)
        {
            hands.get(i).add( deck.dealCardTo( i+1 ) );
        }

        // bet
        assertEquals( 5, deck.getHandByOwner( 3 ).size());


        // deal sixth card to each player
        for (int i = 0; i < 5; i++)
        {
            hands.get(i).add( deck.dealCardTo( i+1 ) );
        }

        // bet
        assertEquals( 6, deck.getHandByOwner( 4 ).size());

        // deal last card to player, down and dirty
        for (int i = 0; i < 5; i++)
        {
            hands.get(i).add( deck.dealCardTo( i+1 ) );
        }
        assertEquals( 7, deck.getHandByOwner( 5 ).size());

        // Find the best 5 hand from each 7
        List<List<Card>> handsOf5 =  new ArrayList<>(5);
        for (int i = 0; i < 5; i++)
        {
            handsOf5.add( Poker.getBest5CardsOf7( hands.get(i)));
        }

        // Sort the collection from best to worst
        handsOf5.sort( new Comparator<List<Card>>() {
            @Override
            public int compare( List<Card> o1, List<Card> o2 )
            {
                return Poker.evaluate( o1 ) - Poker.evaluate( o2 );
            }
        });

        // This is what it looks like as created
        StringBuilder sb = new StringBuilder( "{\"hands\" : [" );
        for (int i = 0; i < 5; i++)
        {
            String myHand = deck.buildHandAsJSON( hands.get(i), "" );
            sb.append( myHand );
            if (i < 4)
                sb.append( "," );
            sb.append( "\n" );
        }
        sb.append( "]}" );

        // throw an exception if not valid JSON
        ObjectMapper mapper = new ObjectMapper();
        mapper.readTree( sb.toString() );

        List<List<Card>> sortedHands = new ArrayList<>( hands );
        // TODO: Sort the old array to match the new array
        hands.sort( new Comparator<List<Card>>() {
            @Override
            public int compare( List<Card> o1, List<Card> o2 )
            {
                List<Card> best1 = Poker.getBest5CardsOf7( o1 );
                List<Card> best2 = Poker.getBest5CardsOf7( o2 );
                return Poker.evaluate( best1 ) - Poker.evaluate( best2 );
            }
        });

        sb = new StringBuilder( "{\"hands\" : [" );
        for (int i = 0; i < 5; i++)
        {
            List<Card> hand = sortedHands.get(i);
            hand.sort( Comparator.comparing( Card::getValue ).reversed());
            String myHand = deck.buildHandAsJSON( hand, "" );
            sb.append( myHand );
            if (i < 4)
                sb.append( "," );
            sb.append( "\n" );
        }
        sb.append( "]}" );
//        System.out.println(sb.toString());

        // throw an exception if not valid JSON
        mapper = new ObjectMapper();
        mapper.readTree( sb.toString() );

        int winner = hands.indexOf( sortedHands.get( 0 ) );

        sb = new StringBuilder( "{\"hands\" : [" );
        for (int i = 0; i < 5; i++)
        {
            List<Card> hand = handsOf5.get( i );
            hand.sort( Comparator.comparing( Card::getValue ).reversed());
            String myHand = deck.buildHandAsJSON( handsOf5.get( i ), "" );
            String type = pokerTypes[ Poker.typeOfHand( hand )];
            sb.append( "{\n\"type\" : \"" ).append( type ).append( "\"," );
            sb.append( myHand.substring( 1 ) );
            if (i < 4)
                sb.append( "," );
            sb.append( "\n" );
        }
        sb.append( "]}" );
        // throw an exception if not valid JSON
        mapper = new ObjectMapper();
        mapper.readTree( sb.toString() );
    }

    @Test
    public void getNextPlayer()
    {
        // the constructor automatically adds player 0, so no need to do it again
        PokerGame<PokerPlayer> theGame = new PokerGame<>(600000, null );

        theGame.playerList.add( new PokerPlayer( "One" ));
        theGame.playerList.add( new PokerPlayer( "Two" ));
        theGame.playerList.add( new PokerPlayer( "Three" ));
        theGame.playerList.add( new PokerPlayer( "Four" ));
        theGame.playerList.add( new PokerPlayer( "Five" ));
        theGame.shuffle();
        theGame.restart( 0, false );

        PokerDeck deck = theGame.getDeck();

        // generate a ton of random hands, for 5 players
        deck.dealNewHandToPlayer( 1, 3 );
        deck.dealNewHandToPlayer( 2, 3 );
        deck.dealNewHandToPlayer( 3, 3 );
        deck.dealNewHandToPlayer( 4, 3 );
        deck.dealNewHandToPlayer( 5, 3 );

//        List<Card> hand = deck.getHandByOwner( 1 );

        // call getNextPlayer and let's see what happens.
        PokerPlayer next = theGame.getNextPlayer( );

        // if we call it five more times we ought to have returned to zero
        next = theGame.getNextPlayer( );
        next = theGame.getNextPlayer( );
        next = theGame.getNextPlayer( );
        next = theGame.getNextPlayer( );
        next = theGame.getNextPlayer();
        assertNull( next ); // when current player becomes zero, this will return null

        deck.dealCardTo( 1 );
        deck.dealCardTo( 2 );
        deck.dealCardTo( 3 );
        deck.dealCardTo( 4 );
        deck.dealCardTo( 5 );

        theGame.round++;

        // each hand now has 2 cards visible
        next = theGame.getNextPlayer();

        // if we call it five more times we ought to have returned to zero
        next = theGame.getNextPlayer();
        next = theGame.getNextPlayer();
        next = theGame.getNextPlayer();
        next = theGame.getNextPlayer();
        next = theGame.getNextPlayer();
        assertNull( next ); // when current player becomes zero, this will return null

        deck.dealCardTo( 1 );
        deck.dealCardTo( 2 );
        deck.dealCardTo( 3 );
        deck.dealCardTo( 4 );
        deck.dealCardTo( 5 );

        // each hand now has 3 cards visible
        theGame.round++;

        next = theGame.getNextPlayer(  );

        // if we call it five more times we ought to have returned to zero
        next = theGame.getNextPlayer(  );
        next = theGame.getNextPlayer(  );
        next = theGame.getNextPlayer(  );
        next = theGame.getNextPlayer(  );
        next = theGame.getNextPlayer(  ); // when current player becomes zero, this will return null
        assertNull( next );

        deck.dealCardTo( 1 );
        deck.dealCardTo( 2 );
        deck.dealCardTo( 3 );
        deck.dealCardTo( 4 );
        deck.dealCardTo( 5 );

        // each hand now has 4 cards visible
        theGame.round++;
        next = theGame.getNextPlayer(  );

        // if we call it five more times we ought to have returned to zero
        next = theGame.getNextPlayer(  );
        next = theGame.getNextPlayer(  );
        next = theGame.getNextPlayer(  );
        next = theGame.getNextPlayer(  );
        next = theGame.getNextPlayer(  ); // when current player becomes zero, this will return null
        assertNull( next );
    }

    @Test
    public void threeTableHighPair()
    {
        List<Card> hand = new ArrayList<>(  );
        hand.add( new Card(CLUBS, ACE ) );
        hand.add( new Card( DIAMONDS, ACE ));
        hand.add( new Card( SPADES, KING ));

        int handValue = threeTable.get( Poker.computeIndex( hand ));
        assertEquals( 7977, 7810 + handValue );     // 7977 Highest pair value for three cards

        hand.add( new Card( HEARTS, QUEEN ));
        handValue = fourTable.get( Poker.computeIndex( hand ));
        assertEquals( 8835, 7810 + handValue );     // 8835 Highest pair value for four cards

        hand.clear();
        hand.add( new Card(CLUBS, QUEEN ) );
        hand.add( new Card( DIAMONDS, QUEEN ));
        hand.add( new Card( SPADES, TEN ));
        hand.add( new Card( HEARTS, SIX ));
        handValue = fourTable.get( Poker.computeIndex( hand ));
        assertEquals( 8642, 7810 + handValue );     // 8642 pair of queens with a ten kicker for four cards;

        hand.clear();
        hand.add( new Card(CLUBS, JACK ));
        hand.add( new Card( DIAMONDS, JACK ));
        hand.add( new Card( SPADES, JACK ));
        handValue = threeTable.get( Poker.computeIndex( hand ));
//        assertEquals( 7991, 7810 + handValue );     // 7991 Highest 3ofk value for three cards
        hand.add( new Card( HEARTS, DEUCE ));
        handValue = fourTable.get( Poker.computeIndex( hand ));
        assertEquals( 9047, 7810 + handValue );     // 9097 3ofk value for 3 jacks in four cards


        hand.clear();
        hand.add( new Card(CLUBS, DEUCE ) );
        hand.add( new Card( DIAMONDS, DEUCE ));
        hand.add( new Card( SPADES, DEUCE ));
        handValue = threeTable.get( Poker.computeIndex( hand ));
        assertEquals( 7979, 7810 + handValue );     // 7979 lowest 3ofk value for three cards
        hand.add( new Card( HEARTS, TREY ));
        handValue = fourTable.get( Poker.computeIndex( hand ));
        assertEquals( 8931, 7810 + handValue );     // 8931 lowest 3ofk value for four cards

        hand.clear();
        hand.add( new Card(CLUBS, TEN ) );
        hand.add( new Card( DIAMONDS, TEN ));
        hand.add( new Card( SPADES, TEN ));
        handValue = threeTable.get( Poker.computeIndex( hand ));
        assertEquals( 7987, 7810 + handValue );     // 7987 midrange 3ofk value for three cards

        hand.clear();
        hand.add( new Card(CLUBS, ACE ) );
        hand.add( new Card( DIAMONDS, ACE ));
        hand.add( new Card( SPADES, KING ));
        hand.add( new Card( HEARTS, KING ));
        handValue = fourTable.get( Poker.computeIndex( hand ));
        assertEquals( 8927, 7810 + handValue );     // 8927 Highest two pair value for four cards;

        hand.clear();
        hand.add( new Card(CLUBS, DEUCE ) );
        hand.add( new Card( DIAMONDS, DEUCE ));
        hand.add( new Card( SPADES, TREY ));
        hand.add( new Card( HEARTS, TREY ));
        handValue = fourTable.get( Poker.computeIndex( hand ));
        assertEquals( 8850, 7810 + handValue );     // 8850 lowest two pair value for four cards;

        hand.clear();
        hand.add( new Card(CLUBS, TEN ) );
        hand.add( new Card( DIAMONDS, TEN ));
        hand.add( new Card( SPADES, SIX ));
        hand.add( new Card( HEARTS, SIX ));
        handValue = fourTable.get( Poker.computeIndex( hand ));
        assertEquals( 8882, 7810 + handValue );     // 8882 two pair value 10s and 6s for four cards;
    }
}