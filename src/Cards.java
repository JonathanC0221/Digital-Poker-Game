/* Names: Jonathan Cheung, Ryan Pang
   Date: 2022-01-24
   Description: Contains the variables and methods relating to the Card object 
*/
public class Cards implements Comparable <Cards> {

	private int ID;
	private int strength;
	private int suite;
	
	//Description: Constructor for the Cards object
	//Parameters: An ID number
	//Return: A card object	
	public Cards (int ID) {
		this.ID = ID;
		strength = ID / 4;
		suite = ID % 4;
	}
	
	//BELOW ARE GETTERS AND SETTERS
	public int getID() {
		return ID;
	}
	
	public int getSuite() {
		return suite;
	}
	
	public int getStrength() {
		return strength;
	}
	
	//Description: Compares the IDs of two cards
	//Parameters: A card object to be compared
	//Return: An int telling whether the card object has a greater or lower ID
	public int compareTo (Cards c) {
		return c.ID-this.ID;
	}
}
