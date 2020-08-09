The **card-game-server** is a framework for building client-server card games using **card-deck** objects. The framework consists of two primary classes, ***CardPlayer*** and ***CardGame***.

### CardPlayer

The ***CardPlayer*** object represents a player in a card game. It is a simple Java Passive Data Structure with a property for a player's game nickname and a flag indicating whether the player has withdrawn from a game or round.

The ***CardPlayer*** object was designed to be a proxy for a remote human player using a web browser. Communication with a specific user's browser is done via Server-Sent Events, so the ***CardPlayer*** object also contains a property called ***eventSink*** that holds a *javax.ws.rs.sse.SseEventSink* object that can be used to send events to a specific player's browser.

### CardGame

 