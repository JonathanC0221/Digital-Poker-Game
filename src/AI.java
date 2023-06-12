/* Names: Jonathan Cheung, Ryan Pang
   Date: 2022-01-24
   Description: This class extends the Player class and contains exclusive variables and methods for the AI
*/
public class AI extends Player {
	
	private int difficulty = 1;
	
	//Description: Constructor of an AI object
	//Parameters: Starting # of chips
	//Return: AI object
	public AI(int chips) {
		super(chips);
	}

	//BELOW ARE GETTERS AND SETTERS
	public int getDifficulty() {
		return difficulty;
	}
	
	public void setDifficulty(int difficulty) {
		this.difficulty = difficulty;
	}

}