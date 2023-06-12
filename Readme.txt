Responsibilities: 
------------------------------
Jonathan C: Worked on game features (graphics, sounds)  leaderboard, and scoring system (evaluate hands)

Ryan P: Worked on game logic (check, bet, shuffle, fold) and AI (actions and responses).

-------------------------------------------------------------------------------------------------------------------------------
Missing Functionalities:
------------------------------
AI Personalities - Removed due to time constraints. 

Card Counting - We were unsure how to implement such a functionality to the AI due to its complexity.

-------------------------------------------------------------------------------------------------------------------------------
Extra Functionalities:
------------------------------
AI Difficulty - AI aggressiveness can be controlled by a setting. Added in place of AI Personalities

Highscores - How many rounds the player has won in a game against the AI is recorded here.

Voice Acting - Spices up the game’s buttons with a nice accent. 

50/50 instructions - Clicking the button in the instructions has a 50/50 chance to take you to additional information or a special surprise.

-------------------------------------------------------------------------------------------------------------------------------
Known Bugs: 
------------------------------
Scoring Logic Bug - Game doesn’t check for alternative hands; it only checks for the highest hand and the highest card. 
Eg: This can create problems when the center 5 cards are a flush of 6, 7, 8, Q, A. P1 has a 6 and a 4. P2 has a 9 and a J. In this scenario, P2 would win by high card despite P1 also having a pair.

-------------------------------------------------------------------------------------------------------------------------------
Other Info: 
------------------------------
Kye Electriciteh voice acted on most of the buttons in the game.

For checking the same kind of hand (eg: both players have a pair), the game will check both the strength and the highest suit of the hand. For example, a 9D and 9H pair will lose to a 9H and 9S pair. A 10D and 10C pair will win against a 4S and 4H pair.

S - spades
H - hearts
C - clubs
D - diamonds

-------------------------------------------------------------------------------------------------------------------------------
Hints:
------------------------------
You might be able to win faster if you just go all in on the first round, though you could lose faster too. But it brings you to the game’s end a lot faster than playing it through.
