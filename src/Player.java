/* Names: Jonathan Cheung, Ryan Pang
   Date: 2022-01-24
   Description: This class contains all the variables and methods relating to the Player object
*/
import java.util.*;

public class Player {
	protected int chips;
	protected ArrayList <Cards> playerHand = new ArrayList <> ();
	protected boolean check = false;
	protected boolean allIn = false;
	protected boolean fold = false;
	protected int handScore;
	
	//Description: Constructor for a player object
	//Parameters: # of starting chips
	//Return: Player object.
	public Player (int chips) {
		this.chips = chips;
	}

	//BELOW ARE GETTERS AND SETTERS
	public int getHandScore() {
		return handScore;
	}

	public void setHandScore(int handScore) {
		this.handScore = handScore;
	}
	
	public int getChips() {
		return chips;
	}
	
	public boolean getCheck() {
		return check;
	}
	
	public boolean getAllIn() {
		return allIn;
	}
	
	public void setAllIn(boolean allIn) {
		this.allIn = allIn;
	}
	
	public void setCheck(boolean check) {
		this.check = check;
	}
	
	public void setChips(int chips) {
		this.chips = chips;
	}
	
	public ArrayList <Cards> getHand() {
		return playerHand;
	}
	
	public Cards getFirstCard() {
		return playerHand.get(0);
	}
	
	public Cards getLastCard() {
		return playerHand.get(1);
	}
	
	public boolean getFold() {
		return fold;
	}
	
	public void setFold (boolean fold) {
		this.fold = fold;
	}
}