/* Names: Jonathan Cheung, Ryan Pang
   Date: 2022-01-24
   Description: This class contains variables and methods relating to the Highscore object 
*/
public class Highscore implements Comparable <Highscore> {

	private String name;
	private int score;
	
	//Description: Constructor of the Highscore object
	//Parameters: A String to take a name and an int for a score
	//Return: Highscore object
	public Highscore (String name, int score) {
		this.name = name;
		this.score = score;
	}
	
	//Description: Compares two highscore objects to see which one is greater
	//Parameters: A highscore object to be compared
	//Return: An int that tells whether Highscore h is greater or lesser than the other highscore
	public int compareTo (Highscore h) {
		if (h.score-this.score == 0) {
			return this.name.compareToIgnoreCase(h.name);
		}
		return h.score-this.score;
	}
	
	//BELOW ARE GETTERS AND SETTERS
	public int getHighScore() {
		return this.score;
	}
	
	public String getName() {
		return this.name;
	}
	
	//Description: Tells whether two highscore objects are equal or not
	//Parameters: An object
	//Return: A boolean telling whether the highscores are equal
	public boolean equals (Object o) {
		Highscore h = (Highscore) o;
		return this.name.equalsIgnoreCase(h.name);	}
	
	//Description: Returns hashcode of the name
	//Parameters: N/A
	//Return: Integer hashcode
	public int hashCode() {
		return this.name.toLowerCase().hashCode();
	}
}
