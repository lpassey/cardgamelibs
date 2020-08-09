A simple Java framework for creating playing cards and decks of playing cards.

### Card

A ***Card*** object has a ***suit***, which can be any integer value including zero, and a ***value*** which can also be any integer including zero. It also holds an integer ***owner*** property and a long random value. These last two properties have special uses.
 
 For example, a simple poker deck can be created with bitmap suits where Hearts are 1, Clubs are 2, Diamonds are 4, Spades are 8 and Jokers are 15. A simple binary AND across any number of suit values will indicate that all the cards of the same suit if the result is non-zero. Values representing the card faces from Deuce to Ace (or Ace to King) could be represented by the values 0 to 12, or 1 to 13, or even 2 to 14 if you wanted the value to match the face value.
 
 Likewise, an Uno specialty deck could be created with suits of 0 to 5, where 0 is a wild card, 1 is blue, 2 is green, 3 is red, and 4 is yellow. The card values would range from 0 to 12 where 0 to 9 matches the face value of the card, 10 is a "Reverse", 11 is a "Skip" and 12 is a "Draw Two".
 
 A ***Card*** is essentially a Java Passive Data Structure; it defines a limited set of properties and provides getters and setters for those properties, but provides no other business logic.
 
 ### Deck
 
 A Deck object represents a deck of playing cards. Unlike the Card object, the Deck object only contains two properties, a **protected** list of ***Card***s and a **private** list of card ***Face***s, but it possesses a number of methods use to manipulate those two lists.
 
 The protected ***cardList*** property is a list of Card objects that make up the deck. There is no requirement that the list have unique cards, nor is there any requirement of parity among suits and values. That is, a deck may contain 5 cards representing the Ace of Spades, and there may be 13 Hearts but only two jokers.
 
 ***cardList*** is a protected property, so only sub-classes can add cards to a deck.
 
 A private ***Face*** (Deck.Face) list property also exists in the ***Deck*** object. It is used to map an image and a description to a specific card, based upon its suit and value. Methods in the Deck class allow a sub-class to set this data for its cards, and any class may retrieve the Face object for a specific card. The use of card ***Face***s is optional.
 
 The ***Deck*** class has methods to shuffle the cards. This is done by simply iterating through the list of cards and assigning each card a random number. The list is then sorted by the random numbers effectively shuffling the order of the cards in the list.
 
 The ***Deck*** class has methods to deal cards to players, to retrieve a player's hand, and to remove cards from play (discard). These methods do not remove cards from the ***cardList***, but simply change the ownership of a card. The integer 0 is reserved for ***Deck*** ownership, and 65535 (0xFFFF) is typically used for discarded or burned cards.
 
 Shuffling the deck does not change the ownership of cards, it simply rearranges them. To return all cards from players to the deck, use the ***reset***() method.
 
 To get details of the methods and properties of these two classes, consult the (evolving) Javadocs.