package com.passkeysoft.cardgameserver;

public class IllegalGamePlayException extends Exception
{
    public IllegalGamePlayException()
    {
        super();
    }

    public IllegalGamePlayException( String message )
    {
        super( message );
    }
}
