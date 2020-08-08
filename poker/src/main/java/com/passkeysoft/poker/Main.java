package com.passkeysoft.poker;

import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * Main class.
 *
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    static final String BASE_URI = "http://0.0.0.0:8082/poker/";

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    static HttpServer startServer() throws IOException
    {
        // create a resource config that scans for JAX-RS resources and providers
        // in com.passkeysoft package
        final ResourceConfig rc = new ResourceConfig()
            .packages("com.passkeysoft.poker")
            .register(new AbstractBinder() {
            @Override
            protected void configure() {
//                bind(new PokerServer()).to(PokerServer.class);
                bind( PokerServer.class ).to( PokerServer.class ).in( Singleton.class );
            }
        });

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer( URI.create( BASE_URI ), rc );
        CLStaticHttpHandler httpHandler = new CLStaticHttpHandler( Main.class.getClassLoader(),
            "/" );
//            "src/main/resources/" );
        server.getServerConfiguration().addHttpHandler( httpHandler, "/" );
        server.start();
        return server;
//        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
        System.in.read();
//        server.stop();
    }
}

