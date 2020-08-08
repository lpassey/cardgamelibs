package com.passkeysoft.cardgameserver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CardGameTest
{
    CardGame<CardPlayerData> theGame;
    Runnable monitor = new Runnable()
    {
        @Override
        public void run()
        {

        }
    };

    @Before
    public void setUp() throws Exception
    {
//        theGame = new PokerGame();
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void isRoundOver()
    {
    }

    @Test
    public void getCurrentPlayer()
    {
    }

    @Test
    public void getNumPlayers()
    {
    }

    @Test
    public void setCardToBePlayed()
    {
    }

    @Test
    public void buildHandAsJSON()
    {
    }

    @Test
    public void getHandByOwner()
    {
    }

    @Test
    public void buildCardAsJSON()
    {
    }

    @Test
    public void drawCard()
    {
    }

    @Test
    public void restart()
    {
    }

    @Test
    public void play()
    {
    }

    @Test
    public void getScoreForPlayer()
    {
    }

    @Test
    public void isPlayable()
    {
    }

    @Test
    public void run()
    {
    }

    @Test
    public void isPaused()
    {
    }

    @Test
    public void pause()
    {
    }

    @Test
    public void unPause()
    {
    }
}