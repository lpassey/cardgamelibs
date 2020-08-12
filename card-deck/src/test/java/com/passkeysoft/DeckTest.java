package com.passkeysoft;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class DeckTest
{
    private Deck testDeck = new Deck();
    private List<Card> player1Hand;

    @Before
    public void setUp()
    {
        for (int suit = 0; suit < 4; suit++)
        {
            for (int value = 0; value < 13; value++)
            {
                Card newCard = new Card( suit, value );
                StringBuilder sb = new StringBuilder( String.valueOf( value + 2 ));
                sb.append(
                    newCard.getSuit() == 0 ? "H" :
                        newCard.getSuit() == 1 ? "C" :
                            newCard.getSuit() == 2 ? "D" : "S" );
                sb.append( ".png" );

                testDeck.addFace( newCard, new Deck.Face( sb.toString(), sb.toString() ));
                testDeck.cardList.add( newCard );
            }
        }

        player1Hand = testDeck.dealNewHandToPlayer( 1, 7 );
    }

    @After
    public void tearDown()
    {
    }

    @Test
    public void addCardTest()
    {
    }

    @Test
    public void addFaceTest()
    {
    }

    @Test
    public void getFaceTest()
    {
    }

    @Test
    public void shuffleTest()
    {
    }

    @Test
    public void shuffleCustom()
    {
    }

    @Test
    public void reset()
    {
        dealNewHandToPlayer();
        testDeck.reset();
        List<Card> hand = testDeck.getHandByOwner( 1 );
        assertEquals( 0, hand.size() );
        // Poker deck is 52 cards.
        hand = testDeck.getHandByOwner( 0 );
        assertEquals( 52, hand.size());
    }

    @Test
    public void dealCardToPlayerBySuit()
    {
        // deal 5 cards of the same suit to this player, then 7 random cards.
        testDeck.dealCardToPlayerBySuit( 2, 1 );
        testDeck.dealCardToPlayerBySuit( 2, 1 );
        testDeck.dealCardToPlayerBySuit( 2, 1 );
        testDeck.dealCardToPlayerBySuit( 2, 1 );
        testDeck.dealCardToPlayerBySuit( 2, 1 );
        testDeck.dealCardTo( 2 );
        testDeck.dealCardTo( 2 );
        List<Card> hand = testDeck.getHandByOwner( 2 );
        assertEquals( 7, hand.size() );
        // the resulting hand must have at least 5 clubs, although it may have more
        int numClubs = 0;
        for (Card card : hand )
        {
            if (card.getSuit() == 1)
                numClubs++;
        }
        assertThat( numClubs, org.hamcrest.Matchers.greaterThan( 4 ));
    }

    @Test
    public void discard()
    {
        List<Card> hand = testDeck.getHandByOwner( Deck.DISCARD );
        assertEquals( 0, hand.size());

        // discard 3 cards
        testDeck.discard( player1Hand.get( 0 ) );
        testDeck.discard( player1Hand.get( 1 ) );
        testDeck.discard( player1Hand.get( 2 ) );

        hand = testDeck.getHandByOwner( Deck.DISCARD );
        assertEquals( 3, hand.size());

        player1Hand = testDeck.getHandByOwner( 1 );
        assertEquals( 4, player1Hand.size() );

        // Three discarded, still 45 in the deck;
        hand = testDeck.getHandByOwner( 0 );
        assertEquals( 45, hand.size());
    }

    @Test
    public void dealNewHandToPlayer()
    {
        assertEquals( 7, player1Hand.size() );
        // Poker deck is 52 cards. If we've dealt 7 there ought to be 45 left.
        List<Card> hand = testDeck.getHandByOwner( 0 );
        assertEquals( 45, hand.size());

        discard();

        // This should return 4 cards to the deck, then deal out 7
        player1Hand = testDeck.dealNewHandToPlayer( 1, 7 );
        assertEquals( 7, player1Hand.size() );

        hand = testDeck.getHandByOwner( 0 );
        assertEquals( 42, hand.size());     // original 52 minus 7 just dealt and 3 in the discard
    }

    @Test
    public void buildCardAsJSON() throws JsonProcessingException
    {
        // build ace of spades
        Card card = new Card(3,12);
        String jsonCard = testDeck.buildCardAsJSON( card,"path/" );
        final ObjectMapper mapper = new ObjectMapper();
        mapper.readTree( jsonCard );

        assertEquals( "{\n\"suit\":3,\n" +
            "\"value\":12,\n" +
            "\"img\":\"path/14S.png\",\n" +
            "\"description\":\"14S.png\"\n" +
            "}", jsonCard );
    }

    @Test
    public void buildHandAsJSON() throws JsonProcessingException
    {
        // build a full house, queens over twos
        List<Card> hand = new ArrayList<>( 5 );
        // value of 0 is a two, 12 is an ace.
        hand.add( new Card( 0, 10 ));
        hand.add( new Card( 1, 10 ));
        hand.add( new Card( 3, 0 ));
        hand.add( new Card( 2, 10 ));
        hand.add( new Card( 2, 0 ));
        hand.sort( Comparator.comparing( Card::getValue ).reversed().thenComparing( Card::getSuit ));
        String jsonCard = testDeck.buildHandAsJSON( hand, "path/" );
        final ObjectMapper mapper = new ObjectMapper();
        mapper.readTree( jsonCard );

        assertThat( jsonCard, startsWith( "{\n\"cards\":[\n{\n" +
            "\"suit\":0,\n" +
            "\"value\":10,\n" +
            "\"img\":\"path/12H.png\",\n" +
            "\"description\":\"12H.png\"\n" +
            "}," ));
        assertThat( jsonCard, endsWith( "{\n" +
            "\"suit\":3,\n" +
            "\"value\":0,\n" +
            "\"img\":\"path/2S.png\",\n" +
            "\"description\":\"2S.png\"\n" +
            "}\n]\n}" ));
    }

    @Test
    public void getMatchingCard()
    {
        Card ownedCard = player1Hand.get(0);
        Card card = new Card( ownedCard.getSuit(),ownedCard.getValue());
        assertEquals( ownedCard, testDeck.getMatchingCard( card, 1 ));
    }
}