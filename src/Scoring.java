/* Names: Jonathan Cheung, Ryan Pang
   Date: 2022-01-24
   Description: This is the class for scoring objects that weigh how strong a player's hand is.
*/
public class Scoring {

	private int score;
	private Cards card;
	private Cards handCard;
	
	//Description: First constructor of a scoring object
	//Parameters: A score # and a card object
	//Return: A Scoring object
	public Scoring (int score, Cards card) {
		this.score = score;
		this.card = card;
	}
	
	//Description: Second constructor of a scoring object
	//Parameters: A score # and two card objects
	//Return: A Scoring object
	public Scoring (int score, Cards handCard, Cards card) {
		this.score = score;
		this.handCard = handCard;
		this.card = card;
	}
	
	//BELOW IS GETTERS AND SETTERS
	public int getScore () {
		return this.score;
	}
	
	public Cards getHighest () {
		return this.card;
	}
	
	public Cards getHandCard () {
		return this.handCard;
	}
}
