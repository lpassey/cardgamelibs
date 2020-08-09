package com.passkeysoft.cardgameserver;

public class CardGameData<T extends CardGame<S>, S extends CardPlayer>
{
    public T theGame;

    public boolean started;

    public boolean isStarted()
    {
        return started;
    }

    protected long created;

    public long whenCreated()
    {
        return created;
    }

    protected String adminName;

    public String getAdminName()
    {
        return adminName;
    }

    public CardGameData( String gameOwner )
    {
        clear( gameOwner );
        // theGame will be created when the game starts and we know how many players we have.
    }

    public void clear( String adminName )
    {
        theGame = null;
        created = System.currentTimeMillis();
        this.adminName = adminName;
        started = false;
    }

    public T getTheGame()
    {
        return theGame;
    }

    // Just because I seem to need it.
    public static int parseGameId( String gameId )
    {
        int gameNum = 0;

        if (null != gameId && !gameId.trim().isEmpty()) try
        {
            gameNum = Integer.parseInt( gameId.trim() );
        }
        catch (NumberFormatException ignore ) {}
        return gameNum;
    }
}
