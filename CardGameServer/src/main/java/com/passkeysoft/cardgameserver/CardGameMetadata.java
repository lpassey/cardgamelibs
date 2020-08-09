package com.passkeysoft.cardgameserver;

/**
 * A class to wrap a {@link CardGame} object with additional metadata. Probably a bad idea and the
 * meta should be merged into the {@link CardGame} object itself.
 * @param <T> extends {@link CardGame}. The CardGame derived class that this class wraps
 * @param <S> extends {@link CardPlayer}. The CardPlayer derived class the the {@link CardGame} uses.
 */
public class CardGameMetadata<T extends CardGame<S>, S extends CardPlayer>
{
    /**
     * The instance of the {@link CardGame} class being wrapped.
     */
    public T theGame;

    public boolean started;
    protected long created;
    protected String adminName;

    public CardGameMetadata( String gameOwner )
    {
        clear( gameOwner );
        // theGame will be created when the game starts and we know how many players we have.
    }

    public boolean isStarted()
    {
        return started;
    }

    public long whenCreated()
    {
        return created;
    }

    public String getAdminName()
    {
        return adminName;
    }

    /**
     * Clears this object of its metadata, including the wrapped class, which will have to be
     * reinserted before staring a new game.
     *
     * @param adminName The name of the administrator of the new game.
     */
    public void clear( String adminName )
    {
        theGame = null;
        created = System.currentTimeMillis();
        this.adminName = adminName;
        started = false;
    }

    /**
     * @return the wrapped instance of the {@link CardGame} class
     */
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
