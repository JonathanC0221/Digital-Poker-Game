/* Names: Jonathan Cheung, Ryan Pang
   Date: 2022-01-24
   Description: This class is the driver class of our poker game, where the player can play a poker duel against an AI.
*/
import java.util.*;
import java.awt.*;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;

import java.io.*;

import java.awt.event.*;
import java.math.*;
import java.net.URISyntaxException;
import java.net.URL;

public class driver implements ActionListener {
	
	public static void openWebpage(String urlString) {
	        try {
				Desktop.getDesktop().browse(new URL(urlString).toURI());
			} catch (IOException | URISyntaxException e) {
				JOptionPane.showMessageDialog(null, "Error loading webpage");
			}
	}
	
	//Description: This method updates the graphics that show the number of chips each player has.
	//Parameters: N/A
	//Return: N/A
	public static void updateChips () {
		currentPlayerChips.replaceRange("Chips: " + playerChip, 0, currentPlayerChips.getText().length());
		currentAIChips.replaceRange("Chips: " + AIChip, 0, currentAIChips.getText().length());
	}
	
	//Description: This method updates a graphical text that tells the player what happened.
	//Parameters: String for what text to update
	//Return: N/A
	public static void updateGameStatus (String status) {
		String s = "Game status: " + status;
		gameInfo.replaceRange(s, 0, gameInfo.getText().length());
	}
	
	//Description: This method resets the game necessary variables to allow the player to start a new game.
	//Parameters: N/A
	//Return: N/A
	public static void newGame() {
		//graphics
		c1.setIcon(cardBack);
		c2.setIcon(cardBack);
		c3.setIcon(cardBack);
		c4.setIcon(cardBack);
		c5.setIcon(cardBack);
		ai1.setIcon(cardBack);
		ai2.setIcon(cardBack);
		pc1.setIcon(cardBack);
		pc2.setIcon(cardBack);
		burn.setIcon(feedme);
		
		//initialize vars
		gamer = new Player (playerChip);
		opponent = new AI (AIChip);
		roundNum = 1;
		roundsWon = 0;
		betPool = 0;
		blind = 2;
		consecBet = 0;
		AIBet = false;
		AIBet2 = false;
		shuffled = false;
		deck = new ArrayList <> ();
		dealtDeck = new ArrayList <> ();
		burnDeck = new ArrayList <> ();
		center = new ArrayList <> ();
		checkClicked = false;
		
		gamePanel.remove(continueButt);
		gamePanel.remove(reset);
		
		roundText = String.format("%03d", roundNum);
		roundInfo.replaceRange(roundText, 7, roundInfo.getText().length());
		updateGameStatus("");
		//betting pool and stat info graphic update
		frame.pack();
		updatePool();
	}
	
	//Description: This method updates the game's leaderboard. The leaderboard is based off how many rounds they won in the game against the AI.
	//Parameters: N/A
	//Return: N/A
	public static void updateLeaderboard () {
		Map <Highscore, Integer> leaderboard = new HashMap <Highscore, Integer> ();
		boolean Blank = true;
		String name = "";
		Highscore newScore = null;
		boolean changeBoard = false;

		//read in name
		try {
			while (Blank) {
				name = JOptionPane.showInputDialog("Please enter your name");
				if (name.length() == 0) {
					JOptionPane.showMessageDialog(null, "Name cannot be blank"); 
				} else {
					newScore = new Highscore(name, roundsWon);
					Blank = false;
				}
			}
			
			//read from existing leaderboard file
			try {
				BufferedReader in = new BufferedReader (new FileReader("leaderboard.txt"));
				String line = "";
				while ((line = in.readLine()) != null) {
					line.trim();
					String boardName = line.substring(0, line.lastIndexOf(" "));
					//add leaderboard to map if possible
					try {
						int score = Integer.parseInt(line.substring(line.lastIndexOf(" ") + 1));
						Highscore hs = new Highscore(boardName, score);
						leaderboard.put(hs, score);
					} catch (NumberFormatException e) {
						JOptionPane.showMessageDialog(null, "Invalid leaderboard entry");
					}

				}
			} catch (FileNotFoundException e) {
				JOptionPane.showMessageDialog(null, "File Not Found");
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Reading error");
			}
			
			//check if name entered already exists
			if (leaderboard.get(newScore) != null) {
				//add score if it beats oldd scsore
				if (leaderboard.get(newScore) < roundsWon) {
					newScore = new Highscore (name, roundsWon);
					leaderboard.remove(newScore);
					leaderboard.put(newScore, roundsWon);
					changeBoard = true;
					//humiliate player for not beating their old score
				} else {
					JOptionPane.showMessageDialog(null, "pathetic, can't beat ur old score");
				}
				//enter name if it doesn't exist
			} else {
				leaderboard.put(newScore, roundsWon);
				changeBoard = true;
			}

			//update textfile if needed
			if (changeBoard == true) {
				PrintWriter out = null;
				//add to learderboard
				try {
					out = new PrintWriter(new FileWriter("leaderboard.txt"));
				} catch (IOException e) {JOptionPane.showMessageDialog(null, "Writing error");}
				Set <Highscore> sortedBoard = new TreeSet <> (leaderboard.keySet());
				Iterator <Highscore> iter = sortedBoard.iterator();
				while (iter.hasNext()) {
					Highscore iterScore = iter.next();
					out.println(iterScore.getName() + " " + iterScore.getHighScore());
				}
				out.close();
			}
		} catch (NullPointerException e) {}
	}
	
	//Description: This method resets the game's variables to start a new round of betting and play. Occurs mid-game.
	//Parameters: N/A
	//Return: N/A
	public static void resetGame () {
		//reset everything to begin the next round
		//reset graphics
		c1.setIcon(cardBack);
		c2.setIcon(cardBack);
		c3.setIcon(cardBack);
		c4.setIcon(cardBack);
		c5.setIcon(cardBack);
		ai1.setIcon(cardBack);
		ai2.setIcon(cardBack);
		pc1.setIcon(cardBack);
		pc2.setIcon(cardBack);
		burn.setIcon(feedme);

		//move values in arrayLists to dealtDeck + clear arrayLists	
		dealtDeck.add(gamer.getHand().remove(1));
		dealtDeck.add(gamer.getHand().remove(0));
		dealtDeck.add(opponent.getHand().remove(1));
		dealtDeck.add(opponent.getHand().remove(0));

		while (center.size() > 0) {
			dealtDeck.add(center.remove(0));
		}

		while (burnDeck.size() > 0) {
			dealtDeck.add(burnDeck.remove(0));
		}

		gamePanel.remove(reset);
		checkClicked = false;

		roundText = String.format("%03d", roundNum);
		roundInfo.replaceRange(roundText, 7, roundInfo.getText().length());
		updateGameStatus("");
		//betting pool and stat info graphic update
		frame.pack();
		updatePool();
	}

	//Description: Gives a scoring obj. that describes the hand's score, highest card in hand, and highest card in the player's hand.
	//Parameters: ArrayList for the center set of cards, ArrayList for the player's hand.
	//Return: Scoring object
	public static Scoring analyzeHand (ArrayList <Cards> center, ArrayList <Cards> player) {
		ArrayList <Cards> c = new ArrayList <> (player);
		c.addAll(center);
		Collections.sort(c);
		ArrayList <Cards> [] dupeStraight = new ArrayList [1];
		Map <Integer, Integer> suiteMap = new HashMap <> ();
		int size = c.size();
		Cards flushStraight = null;
		
		Cards highest;
		Collections.sort(player);
		highest = player.get(0);
		
		//checking straight with duplicate cards
		LinkedList <int[]> dupeIndex = new LinkedList <int[]> ();
		for (int i = 0; i < c.size()-1; i++) {
			if (c.get(i).getStrength() == c.get(i+1).getStrength()) {
				if (dupeIndex.size() > 0 && dupeIndex.peekLast()[1] == i+1) { //updates previous last index to current last index
					  int[] newBound = {dupeIndex.peekLast()[0], i + 2};
                      dupeIndex.removeLast();
                      dupeIndex.addLast(newBound);
				}
				 else { //adding new duplicates
                     int[] newBound = {i, i+2};
                     dupeIndex.addLast(newBound);
                 }
			}
		}
		
		//adding removed duplicate cards separately to a different arraylist with the rest of the deck
		if (dupeIndex.size() == 0) { //no dupes
			dupeStraight [0] = c;
		} else if (dupeIndex.size() == 2) {
			dupeStraight = new ArrayList [4]; //2 pairs (4 combinations)
			for (int i = 0; i < 4; i++) {
				ArrayList <Cards> dupeList = new ArrayList <Cards>();
				for (int j = 0; j < size; j++) {
					int done = 0;
					if (c.get(j).getStrength() == c.get((j + 1) % size).getStrength()) {
						if ((done == 0 && i / 2 == 0) || (done == 1 && i % 2 == 0)) {
							dupeList.add(c.get(j++));
						}
						else {
							dupeList.add(c.get(j++ + 1));
						}
					}
					else {
						dupeList.add(c.get(j));
					}
					dupeStraight[i] = dupeList;
				}
			}
		} else if (dupeIndex.size() == 1) { //triple or double
			dupeStraight = new ArrayList [dupeIndex.peekLast()[1] - dupeIndex.peekLast()[0]];
            for (int i = 0; i < dupeStraight.length; i++) {
                ArrayList <Cards> dupeList = new ArrayList <Cards>();
                for (int j = 0; j < size; j++) {
                    if (c.get(j).getStrength() == c.get((j+dupeStraight.length-1)%size).getStrength()) {
                        dupeList.add(c.get(j+i));
                        j += dupeStraight.length - 1;
                    } else {
                        dupeList.add(c.get(j));
                    }
                }
                dupeStraight[i] = dupeList;
            }
		} else if (dupeIndex.size() == 3) { //three pairs or triple and two doubles
			ListIterator <int []> dupeIter = dupeIndex.listIterator(); //iterator for list
			while(dupeIter.hasNext()) {
				int [] index = dupeIter.next();
				if (index[1] - index[0] == 3) { //fullhouse, only combination of 3 with 2 pairs is possible
					return new Scoring (60, c.get(index[0]), highest);
				}
			}
			return new Scoring (20, c.get(dupeIndex.getFirst()[0]), highest);
		}
		
		//check straight first
		int firstIndex = 0;
		boolean ace = false;
		Cards handCard = null;
		boolean straight = false;
		boolean suiteCheck = false;
		int straightCount = 1;
		int [] score = new int [dupeStraight.length];
		int flushTrack = 0;
		
		//just straight
		for (int k = 0; k < dupeStraight.length; k++) {
			for (int i = 0; i < dupeStraight[k].size()+4; i++) {
				//checking overflow aces (a,2,3,4,5 etc.)
				if (dupeStraight[k].get(i%dupeStraight[k].size()).getStrength() == 0 && dupeStraight[k].get((i + 1) % dupeStraight[k].size()).getStrength() == 12) {
					ace = true; //return ace
					straightCount++;
				//regular check
				} else if (dupeStraight[k].get(i%dupeStraight[k].size()).getStrength() - 1 == (dupeStraight[k].get((i + 1) % dupeStraight[k].size()).getStrength() % 12)) {
					straightCount++;
				}
				else {
					straightCount = 1;
				}
				if (straightCount == 5) {
					straight = true;
					firstIndex = (i-3+dupeStraight[k].size())%dupeStraight[k].size();
					break;
				}
			}
			
			//checking diff kinds of straights
			if (straight) {
				suiteCheck = false;
				suiteMap = new HashMap <> ();
				suiteMap.put(0, 0); //adding diamonds
				suiteMap.put(1, 0); //adding clubs
				suiteMap.put(2, 0); //adding hearts
				suiteMap.put(3, 0); //adding spades
				for (int i = firstIndex; i <= firstIndex + 4; i++) {
					suiteMap.put(dupeStraight[k].get(i % dupeStraight[k].size()).getSuite(), suiteMap.get(dupeStraight[k].get(i % dupeStraight[k].size()).getSuite())+1);
				}
				
				//checking straight for straight flush
				for (int i = 0; i < 4; i++) {
					if (suiteMap.get(i) == 5) {
						suiteCheck = true;
					}
				}

				//getting highest and lowest card from straight deck for royal check
				int highestCard = dupeStraight[k].get(firstIndex).getStrength();
				int lowest = dupeStraight[k].get(firstIndex).getStrength();
				for (int i = firstIndex; i < firstIndex+4; i++) {
					highestCard = Math.max(highestCard, dupeStraight[k].get((i + 1) % dupeStraight[k].size()).getStrength());
					lowest = Math.min(lowest, dupeStraight[k].get((i + 1) % dupeStraight[k].size()).getStrength());
				}

				//checking royal flush
				if (lowest == 8 && highestCard == 12 && suiteCheck) {
					return new Scoring (90, dupeStraight[k].get(firstIndex), highest);
				} else if (suiteCheck) { //checking straight flush
					score [k] = 80;
					flushTrack = k; //remembering the index of k to retrieve proper card for straight flush
				} else {
					score [k] = 40;
					
				}
				if (ace) { //return highest ace or ace from straight flush
					handCard = dupeStraight[0].get(0);
					flushStraight = dupeStraight[flushTrack].get(0);
				} else { //return highest card (will be at first combination of cards unless it is a straight flush)
					handCard = dupeStraight[0].get(firstIndex);
					flushStraight = dupeStraight[flushTrack].get(firstIndex);
				}
			}
		
		}
		
		//sort array to find highest straight combination
		Arrays.sort(score);
		
		//return straight flush
		if (straight && score[score.length-1] == 80) {
			return new Scoring (80, flushStraight, highest);
		}
		
		//duplicate check
		int count = 1;
		int count2 = 1;
		int countIndex = 0;
		boolean endReached = false;
		
		//first set of matching cards
		for (int i = 0; i < c.size()-1; i++) {
			if (c.get(i).getStrength() == c.get(i+1).getStrength()) {
				count++;
			} else if (count > 1) {
				countIndex = i+1;
				break;
			}
			if (i == c.size()-2) {
				countIndex = i+1;
				endReached = true;
			}
		}
		
		//second set of matching;
		if (!endReached) {
			for (int i = countIndex; i < c.size()-1; i++) {
				if (c.get(i).getStrength() == c.get(i+1).getStrength()) {
					count2++;
				}
				else if (c.get(i).getStrength() != c.get(i+1).getStrength() && count2 > 1) {
					break;
				}
			}
		}
		
		//flush check
		suiteMap = new HashMap <> ();
		suiteMap.put(0, 0); //adding diamonds
		suiteMap.put(1, 0); //adding clubs
		suiteMap.put(2, 0); //adding hearts
		suiteMap.put(3, 0); //adding spades

		suiteCheck = false;
		int flushSuite = 0;
		int flushIndex = 0;
		
		//adding cards to map based on suite
		for (int i = 0; i < c.size(); i++) {
			suiteMap.put(c.get(i).getSuite(), suiteMap.get(c.get(i).getSuite())+1);
		}
		
		//check if flush exists
		for (int i = 0; i < 4; i++) {
			if (suiteMap.get(i) >= 5) {
				flushSuite = i;
				suiteCheck = true;
			}
		}

		//return highest card from flush
		if (suiteCheck) {
			for (int i = 0; i < c.size(); i++) {
				if (c.get(i).getSuite() == flushSuite) {
					flushIndex = i;
					break;
				}
			}
		}
		
		//pair analysis
		if (count == 4 || count2 == 4) { //four of a kind
			if (count == 4) {
				return new Scoring (70, c.get(countIndex-count), highest);
			} else {
				return new Scoring (70, c.get(countIndex), highest); //if four of a kind is found after another set of dupes
			}
		} else if (count == 3 && count2 == 2 || count == 2 && count2 == 3) { //fullhouse
			if (count == 3) {
				return new Scoring (60, c.get(countIndex-count), highest);
			} else {
				return new Scoring (60, c.get(countIndex), highest); //if triple from fh is found at the back of the hand
			}
		} else if (suiteCheck) { //flush
			return new Scoring (50, c.get(flushIndex),highest);
		} else if (straight) { //straight
			return new Scoring (40, handCard, highest);
		} else if (count == 3 || count2 == 3) { //three of a kind
			if (count == 3) {
				if (endReached) { //if three of a kind is at the back of deck
					return new Scoring (30, c.get(countIndex-2), highest); 
				} else {
					return new Scoring (30, c.get(countIndex-3), highest); //if found by first dupe counter
				}
			} else {
				return new Scoring (30, c.get(countIndex), highest); //if found by second
			}
		} else if (count == 2 && count2 == 2) { //double pairs
			return new Scoring (20, c.get(countIndex-2), highest);
		}else if (count == 2) { //single pair
			if (endReached) { //if single pair is found at end of deck
				return new Scoring(10, c.get(countIndex-1), highest);
			}
			return new Scoring (10, c.get(countIndex-2),highest);
		}
		
//		//highest card only
		return new Scoring (0, highest, highest);
		
	}

	//Description: Makes text big and gold and unable to be edited 
	//Parameters: JTextArea to change properties of
	//Return: N/A
	public static void textProperties (JTextArea name) {
		name.setForeground(gold);
		name.setBackground(darkGreen);
		name.setEditable(false);
		name.setFont(name.getFont().deriveFont(20f));
	}

	//Description: Updates text for the blinds and betting pool
	//Parameters: blind amount integer
	//Return: N/A
	public static void updateBlindText(int blind) {
		blindText = String.format("%03d", blind);
		blindPool.replaceRange(blindText, 12, blindPool.getText().length());
	}

	//Description: Adds points from bets graphically to the betpool
	//Parameters: Integer to update
	//Return: N/A
	public static void updateBetText(int betPool) {
		betText = String.format("%03d", betPool);
		bettingPool.replaceRange(betText, 14, bettingPool.getText().length());
	}

	//Description: Graphically updates the amount of chips that the player and AI have
	//Parameters: gamer's chips and AI's chips
	//Return: N/A
	public static void updatePoints(int gamerChip, int opponentChip) {
		playerText = String.format("%03d", gamerChip);
		AIText = String.format("%03d", opponentChip);
		playerPoints.replaceRange(playerText, 13, playerPoints.getText().length());
		aiPoints.replaceRange(AIText, 4, aiPoints.getText().length());
	}

	//  Graphical Variables //
	static JFrame frame;
	static JPanel gamePanel;
	static JPanel menuPanel;
	static JPanel winScreen;
	static JPanel loseScreen;
	static JPanel instructionScreen;
	static JPanel leaderScreen;
	JPanel settingPanel;
	JButton deal, check, bet, fold, playGame, instructions, exit, back, changeMyChips, changeAIChips, changeAIType, quitGame, winButton, loseButton, instructionButton, instructionBack, leaderButton;
	static JButton reset, continueButt;
	static JLabel pc1, pc2, ai1,ai2, c1, c2, c3, c4, c5, burn;
	static JTextArea aiPoints, playerPoints, currentPlayerChips, currentAIChips, gameInfo, god, bettingPool, blindPool, roundInfo, winText, loseText, leaderText;
	SpringLayout menuSpring, settingSpring, gameSpring, winSpring, loseSpring, instructionSpring;
	static ImageIcon cardBack = new ImageIcon (new ImageIcon("back.png").getImage().getScaledInstance(107, 144, Image.SCALE_DEFAULT));
	static ImageIcon feedme = new ImageIcon (new ImageIcon("feedme.jpg").getImage().getScaledInstance(107, 144, Image.SCALE_DEFAULT));
	ImageIcon yes = new ImageIcon(new ImageIcon("god.png").getImage().getScaledInstance(1000, 700, Image.SCALE_DEFAULT));
	ImageIcon title = new ImageIcon("yesn't.png");
	ImageIcon cards [] = new ImageIcon[52];
	ImageIcon moist = new ImageIcon(new ImageIcon("winscreen.png").getImage().getScaledInstance(1000, 700, Image.SCALE_DEFAULT));
	ImageIcon sus = new ImageIcon(new ImageIcon("losescreen.png").getImage().getScaledInstance(1000, 700, Image.SCALE_DEFAULT));
	ImageIcon instructionImage = new ImageIcon(new ImageIcon("instructions.jpg").getImage().getScaledInstance(1000, 700, Image.SCALE_DEFAULT));
	ImageIcon leaderBg = new ImageIcon(new ImageIcon("leaderBg.png").getImage().getScaledInstance(1000, 700, Image.SCALE_DEFAULT));
	JLabel jonathan = new JLabel(yes);
	JLabel titleBg = new JLabel(title);
	JLabel woo = new JLabel(moist);
	JLabel ree = new JLabel(sus);
	JLabel instructionLabel = new JLabel(instructionImage);
	JLabel leaderBackground = new JLabel(leaderBg);
	static String betText, blindText, playerText, AIText, roundText;
	String playerName;
	static int roundsWon = 0;
	static boolean checkClicked = false;
	static Clip bgm, betSound, changeSound, checkSound, dealSound, foldSound, instructionSound, loseSound, playSound, winSound, continueSound, resetSound, backSound, instructionScreenSound, diffSound, leaderSound;
	
	// Global Variables //
	public static int screenID = 0;
	public static int roundNum = 1;
	public static int betPool = 0;
	public static int blind = 2;
	public static Color darkGreen = new Color(0,100,0);
	public static Color gold = new Color(255,215,0);
	public static int playerChip = 100;
	public static int AIChip = 100;
	public double chance;
	public static int consecBet = 0;
	public static boolean AIBet = false;
	public static boolean AIBet2 = false;
	public static boolean counted = false;
	public static boolean counted2 = false;
	public static int allInt = 0;

	// Card Variables //
	public static ArrayList <Cards> deck = new ArrayList <> ();
	public static ArrayList <Cards> dealtDeck = new ArrayList <> ();
	public static ArrayList <Cards> burnDeck = new ArrayList <> ();
	public static ArrayList <Cards> center = new ArrayList <> ();

	public static boolean shuffled = false;

	// Player + AI //
	static Player gamer = new Player(playerChip);
	static AI opponent = new AI(AIChip);

	//Description: Constructor of the driver class and initializes graphics
	//Parameters: N/A
	//Return: N/A
	public driver () {
		//adding cards to arraylist
		for (int i = 0; i <= 51; i++) {
			String name = i+".gif";
			cards[i] = new ImageIcon(new ImageIcon(name).getImage().getScaledInstance(107, 144, Image.SCALE_DEFAULT));
		}

		//initialize graphics
		frame = new JFrame ("Texas Hold'em Poker'");
		frame.setPreferredSize(new Dimension(1000,700));
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gameSpring = new SpringLayout();
		settingSpring = new SpringLayout();
		menuSpring = new SpringLayout();
		instructionSpring = new SpringLayout();
		winSpring = new SpringLayout();
		loseSpring = new SpringLayout();
		
		menuPanel = new JPanel();
		settingPanel = new JPanel();
		gamePanel = new JPanel();
		winScreen = new JPanel();
		loseScreen = new JPanel();
		instructionScreen = new JPanel();
		leaderScreen = new JPanel();
		

		//create buttons and add actionlistener for menuPanel
		playGame = new JButton("PLAY GAME");
		instructions = new JButton("INSTRUCTIONS AND SETTINGS");
		exit = new JButton("EXIT GAME");
		leaderButton = new JButton ("Show Leaderboard");
		
		playGame.addActionListener(this);
		instructions.addActionListener(this);
		exit.addActionListener(this);
		leaderButton.addActionListener(this);

		//add graphics to menuPanel
		menuPanel.setLayout(menuSpring);

		menuPanel.add(playGame);
		menuPanel.add(instructions);
		menuPanel.add(leaderButton);
		menuPanel.add(exit);
		menuPanel.add(titleBg);
		menuSpring.putConstraint(SpringLayout.HORIZONTAL_CENTER, playGame, 0, SpringLayout.HORIZONTAL_CENTER, menuPanel);
		menuSpring .putConstraint(SpringLayout.NORTH, playGame, 400, SpringLayout.NORTH, menuPanel);
		menuSpring.putConstraint(SpringLayout.HORIZONTAL_CENTER, instructions, 0, SpringLayout.HORIZONTAL_CENTER, menuPanel);
		menuSpring.putConstraint(SpringLayout.NORTH, instructions, 30, SpringLayout.SOUTH, playGame);
		menuSpring.putConstraint(SpringLayout.NORTH, leaderButton, 30, SpringLayout.SOUTH, instructions);
		menuSpring.putConstraint(SpringLayout.HORIZONTAL_CENTER, leaderButton, 0, SpringLayout.HORIZONTAL_CENTER, menuPanel);
		menuSpring.putConstraint(SpringLayout.HORIZONTAL_CENTER, exit, 0, SpringLayout.HORIZONTAL_CENTER, menuPanel);
		menuSpring.putConstraint(SpringLayout.NORTH, exit, 30, SpringLayout.SOUTH, leaderButton);
		//create buttons and add action listener for settingsPanel
		changeMyChips = new JButton("CHANGE YOUR CHIPS");
		changeAIChips = new JButton("CHANGE AI CHIPS");
		changeAIType = new JButton("CHANGE AI DIFFICULTY");
		back = new JButton ("RETURN TO TITLE MENU");
		instructionButton = new JButton ("Show Instructions");

		changeMyChips.addActionListener(this);
		changeAIChips.addActionListener(this);
		changeAIType.addActionListener(this);
		back.addActionListener(this);
		instructionButton.addActionListener(this);

		//settingPanel text
		currentPlayerChips = new JTextArea();
		currentAIChips = new JTextArea();
		god = new JTextArea();
		currentPlayerChips.setEditable(false);
		currentAIChips.setEditable(false);
		currentPlayerChips.append("Chips: " + playerChip);
		currentAIChips.append("Chips: " + AIChip);
		god.append("Made by: Jonathan and Ryan");
		god.setEditable(false);

		//add graphics to settingPanel
		settingPanel.setLayout(settingSpring);

		settingPanel.add(changeAIChips);
		settingPanel.add(changeMyChips);
		settingPanel.add(changeAIType);
		settingPanel.add(back);
		settingPanel.add(currentAIChips);
		settingPanel.add(currentPlayerChips);
		settingPanel.add(god);
		settingPanel.add(instructionButton);
		settingPanel.add(jonathan);
		settingSpring.putConstraint(SpringLayout.NORTH, changeAIChips, 50, SpringLayout.NORTH, settingPanel);
		settingSpring.putConstraint(SpringLayout.WEST, changeAIChips, 800, SpringLayout.WEST, settingPanel);
		settingSpring.putConstraint(SpringLayout.NORTH, currentAIChips, 15, SpringLayout.SOUTH, changeAIChips);
		settingSpring.putConstraint(SpringLayout.WEST, currentAIChips, 800, SpringLayout.WEST, settingPanel);
		settingSpring.putConstraint(SpringLayout.NORTH, changeMyChips, 150, SpringLayout.SOUTH, currentAIChips);
		settingSpring.putConstraint(SpringLayout.WEST, changeMyChips, 800, SpringLayout.WEST, settingPanel);
		settingSpring.putConstraint(SpringLayout.NORTH, currentPlayerChips, 15, SpringLayout.SOUTH, changeMyChips);
		settingSpring.putConstraint(SpringLayout.WEST, currentPlayerChips, 800, SpringLayout.WEST, settingPanel);
		settingSpring.putConstraint(SpringLayout.WEST, changeAIType, 800, SpringLayout.WEST, settingPanel);
		settingSpring.putConstraint(SpringLayout.NORTH, changeAIType, 100, SpringLayout.SOUTH, currentPlayerChips);
		settingSpring.putConstraint(SpringLayout.NORTH, god, 100, SpringLayout.SOUTH, changeAIType);
		settingSpring.putConstraint(SpringLayout.WEST, god, 800, SpringLayout.WEST, settingPanel);
		settingSpring.putConstraint(SpringLayout.WEST, back, 800, SpringLayout.WEST, settingPanel);
		settingSpring.putConstraint(SpringLayout.NORTH, back, 15, SpringLayout.SOUTH, god);
		settingSpring.putConstraint(SpringLayout.NORTH, instructionButton, 50, SpringLayout.NORTH, settingPanel);
		settingSpring.putConstraint(SpringLayout.WEST, instructionButton, 15, SpringLayout.WEST, settingPanel);

		//Game panel graphics
		gamePanel.setBackground(darkGreen);
		gamePanel.setLayout(gameSpring);

		aiPoints = new JTextArea();
		textProperties(aiPoints);
		aiPoints.append("AI: ");
		AIText = String.format("%03d", AIChip);
		aiPoints.append(AIText);
		playerPoints = new JTextArea();
		textProperties(playerPoints);
		playerPoints.append("Your points: ");
		playerText = String.format("%03d", playerChip);
		playerPoints.append(playerText);
		textProperties(playerPoints);

		bettingPool = new JTextArea();
		textProperties(bettingPool);
		betText = String.format("%03d", betPool);
		blindText = String.format("%03d", blind);
		bettingPool.append("Betting Pool: ");
		bettingPool.append(betText);
		blindPool = new JTextArea();
		textProperties(blindPool);
		blindPool.append("Blind Pool: ");
		blindPool.append(blindText);
		roundInfo = new JTextArea();
		textProperties(roundInfo);
		roundText = String.format("%03d", roundNum);
		roundInfo.append("Round #");
		roundInfo.append(roundText);
		
		gameInfo = new JTextArea();
		textProperties(gameInfo);
		gameInfo.append("Game Status: ");

		ai1 = new JLabel(cardBack);
		ai2 = new JLabel(cardBack);
		c1 = new JLabel(cardBack);
		c2 = new JLabel(cardBack);
		c3 = new JLabel(cardBack);
		c4 = new JLabel(cardBack);
		c5 = new JLabel(cardBack);
		pc1 = new JLabel(cardBack);
		pc2 = new JLabel(cardBack);
		burn = new JLabel(feedme);

		deal = new JButton("Deal Cards");
		check = new JButton("Check");
		bet = new JButton("Bet");
		fold = new JButton("Fold");
		reset = new JButton("Reset Round");
		quitGame = new JButton("Quit Game");
		continueButt = new JButton("ContinueButt");
		deal.addActionListener(this);
		check.addActionListener(this);
		bet.addActionListener(this);
		fold.addActionListener(this);
		reset.addActionListener(this);
		quitGame.addActionListener(this);
		continueButt.addActionListener(this);

		gamePanel.add(aiPoints);
		gamePanel.add(ai1);
		gamePanel.add(ai2);
		gamePanel.add(quitGame);
		gamePanel.add(roundInfo);
		gamePanel.add(gameInfo);
		gamePanel.add(c1);
		gamePanel.add(c2);
		gamePanel.add(c3);
		gamePanel.add(c4);
		gamePanel.add(c5);
		gamePanel.add(burn);
		gamePanel.add(bettingPool);
		gamePanel.add(blindPool);
		gamePanel.add(playerPoints);
		gamePanel.add(deal);
		gamePanel.add(pc1);
		gamePanel.add(pc2);
		gamePanel.add(check);
		gamePanel.add(bet);
		gamePanel.add(fold);
		gameSpring.putConstraint(SpringLayout.NORTH, aiPoints, 50, SpringLayout.NORTH, gamePanel);
		gameSpring.putConstraint(SpringLayout.WEST, aiPoints, 100, SpringLayout.WEST, gamePanel);
		gameSpring.putConstraint(SpringLayout.NORTH, ai1, 27, SpringLayout.NORTH, gamePanel);
		gameSpring.putConstraint(SpringLayout.WEST, ai1, 175, SpringLayout.EAST, aiPoints);
		gameSpring.putConstraint(SpringLayout.NORTH, ai2, 27, SpringLayout.NORTH, gamePanel);
		gameSpring.putConstraint(SpringLayout.WEST, ai2, 50, SpringLayout.EAST, ai1);
		gameSpring.putConstraint(SpringLayout.NORTH, roundInfo, 50, SpringLayout.SOUTH, ai1);
		gameSpring.putConstraint(SpringLayout.WEST, roundInfo, 200, SpringLayout.WEST, gamePanel);
		gameSpring.putConstraint(SpringLayout.NORTH, gameInfo, 50, SpringLayout.SOUTH, ai1);
		gameSpring.putConstraint(SpringLayout.WEST, gameInfo, 150, SpringLayout.EAST, roundInfo);
		gameSpring.putConstraint(SpringLayout.NORTH, c1, 30, SpringLayout.SOUTH, gameInfo);
		gameSpring.putConstraint(SpringLayout.WEST, c1, 50, SpringLayout.WEST, gamePanel);
		gameSpring.putConstraint(SpringLayout.NORTH, c2, 30, SpringLayout.SOUTH, gameInfo);
		gameSpring.putConstraint(SpringLayout.WEST, c2, 15, SpringLayout.EAST, c1);
		gameSpring.putConstraint(SpringLayout.NORTH, c3, 30, SpringLayout.SOUTH, gameInfo);
		gameSpring.putConstraint(SpringLayout.WEST, c3, 15, SpringLayout.EAST, c2);
		gameSpring.putConstraint(SpringLayout.NORTH, c4, 30, SpringLayout.SOUTH, gameInfo);
		gameSpring.putConstraint(SpringLayout.WEST, c4, 15, SpringLayout.EAST, c3);
		gameSpring.putConstraint(SpringLayout.NORTH, c5, 30, SpringLayout.SOUTH, gameInfo);
		gameSpring.putConstraint(SpringLayout.WEST, c5, 15, SpringLayout.EAST, c4);
		gameSpring.putConstraint(SpringLayout.NORTH, burn, 30, SpringLayout.SOUTH, gameInfo);
		gameSpring.putConstraint(SpringLayout.WEST, burn, 50, SpringLayout.EAST, c5);
		gameSpring.putConstraint(SpringLayout.NORTH, bettingPool, 30, SpringLayout.SOUTH, c1);
		gameSpring.putConstraint(SpringLayout.WEST, bettingPool, 200, SpringLayout.WEST, gamePanel);
		gameSpring.putConstraint(SpringLayout.NORTH, blindPool, 0, SpringLayout.NORTH, bettingPool);
		gameSpring.putConstraint(SpringLayout.WEST, blindPool, 20, SpringLayout.EAST, bettingPool);
		gameSpring.putConstraint(SpringLayout.NORTH, playerPoints, 20, SpringLayout.SOUTH, bettingPool);
		gameSpring.putConstraint(SpringLayout.WEST, playerPoints, 0, SpringLayout.WEST, aiPoints);
		gameSpring.putConstraint(SpringLayout.NORTH, deal, 50, SpringLayout.SOUTH, playerPoints);
		gameSpring.putConstraint(SpringLayout.WEST, deal, 0, SpringLayout.WEST, playerPoints);
		gameSpring.putConstraint(SpringLayout.NORTH, pc1, 20, SpringLayout.SOUTH, bettingPool);
		gameSpring.putConstraint(SpringLayout.WEST, pc1, 0, SpringLayout.WEST, ai1);
		gameSpring.putConstraint(SpringLayout.NORTH, pc2, 20, SpringLayout.SOUTH, bettingPool);
		gameSpring.putConstraint(SpringLayout.WEST, pc2, 50, SpringLayout.EAST, pc1);
		gameSpring.putConstraint(SpringLayout.NORTH, check, 20, SpringLayout.SOUTH, bettingPool);
		gameSpring.putConstraint(SpringLayout.EAST, check, 0, SpringLayout.EAST, burn);
		gameSpring.putConstraint(SpringLayout.NORTH, bet, 15, SpringLayout.SOUTH, check);
		gameSpring.putConstraint(SpringLayout.EAST, bet, 0, SpringLayout.EAST, check);
		gameSpring.putConstraint(SpringLayout.NORTH, fold, 15, SpringLayout.SOUTH, bet);
		gameSpring.putConstraint(SpringLayout.EAST, fold, 0, SpringLayout.EAST, bet);
		gameSpring.putConstraint(SpringLayout.EAST, quitGame, 0, SpringLayout.EAST, fold);
		gameSpring.putConstraint(SpringLayout.NORTH, quitGame, 0, SpringLayout.NORTH, ai2);

		//winscreen
		winSpring = new SpringLayout();
		winButton = new JButton ("Return to Main Menu");
		winButton.addActionListener(this);
		
		winScreen.setLayout(winSpring);

		winScreen.add(winButton);
		winScreen.add(woo);
		winSpring.putConstraint(SpringLayout.HORIZONTAL_CENTER, winButton, 0, SpringLayout.HORIZONTAL_CENTER, winScreen);
		winSpring.putConstraint(SpringLayout.VERTICAL_CENTER, winButton, 0, SpringLayout.VERTICAL_CENTER, winScreen);

		
		//losescreen
		loseSpring = new SpringLayout();
		loseButton = new JButton ("Return to Main Menu");
		loseButton.addActionListener(this);
		
		loseScreen.setLayout(loseSpring);
		
		loseScreen.add(loseButton);
		loseScreen.add(ree);
		loseSpring.putConstraint(SpringLayout.HORIZONTAL_CENTER, loseButton, 0, SpringLayout.HORIZONTAL_CENTER, loseScreen);
		loseSpring.putConstraint(SpringLayout.VERTICAL_CENTER, loseButton, 0, SpringLayout.VERTICAL_CENTER, loseScreen);
		
		//instruction
		instructionSpring = new SpringLayout();
		instructionBack = new JButton ("Return to Setting Screen");
		instructionBack.addActionListener(this);
		JButton instructionInfo = new JButton ("click here for more information");
		instructionInfo.addActionListener(this);
		
		instructionScreen.setLayout(instructionSpring);
		
		instructionScreen.add(instructionBack);
		instructionScreen.add(instructionInfo);
		instructionScreen.add(instructionLabel);
		instructionSpring.putConstraint(SpringLayout.NORTH, instructionBack, 600, SpringLayout.NORTH, instructionScreen);
		instructionSpring.putConstraint(SpringLayout.WEST, instructionBack, 650, SpringLayout.WEST, instructionScreen);
		instructionSpring.putConstraint(SpringLayout.NORTH, instructionInfo, 565, SpringLayout.NORTH, instructionScreen);
		instructionSpring.putConstraint(SpringLayout.WEST, instructionInfo, 80, SpringLayout.WEST, instructionScreen);
		
		//leaderboard
		leaderText = new JTextArea();
		leaderText.setEditable(false);
		JScrollPane leaderScroll = new JScrollPane(leaderText);
		JButton leaderBack = new JButton ("Return to Main Menu");
		leaderBack.addActionListener(this);
		
		SpringLayout leaderSpring = new SpringLayout();
		
		//displaying file contents
		try {
			Reader myReader = new BufferedReader(new FileReader("leaderboard.txt"));
			leaderText.read(myReader, "leaderboard.txt");
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "File not found, play a round or add a file");
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Reading error");
		}
		leaderScreen.add(leaderBack);
		leaderScreen.add(leaderScroll);
		leaderScreen.add(leaderBackground);
		
		leaderScreen.setLayout(leaderSpring);
		leaderSpring.putConstraint(SpringLayout.HORIZONTAL_CENTER, leaderBack, 0, SpringLayout.HORIZONTAL_CENTER, leaderScreen);
		leaderSpring.putConstraint(SpringLayout.HORIZONTAL_CENTER, leaderScroll, 0, SpringLayout.HORIZONTAL_CENTER, leaderScreen);
		leaderSpring.putConstraint(SpringLayout.NORTH, leaderScroll, 20, SpringLayout.SOUTH, leaderBack);
		
		//display screen
		frame.add(menuPanel);
		frame.pack();
		frame.setVisible(true);
	}
	
	//Description: Updates betting pool graphics
	//Parameters: N/A
	//Return: N/A
	public static void updatePool() {
		//update betting pool graphics
		updateBlindText(blind);
		updateBetText(betPool);
		updatePoints(gamer.getChips(), opponent.getChips());
		frame.repaint();
	}

	//Description: Adds cards to the deck and shuffles them.
	//Parameters: N/A
	//Return: N/A
	public void firstShuffle() {
		for (int i = 0; i < 52; i++) {
			deck.add(new Cards(i));
		}
		Collections.shuffle(deck);
		shuffled = true;

	}

	//Description: Moves cards from dealtDeck to deck and shuffles them.
	//Parameters: N/A
	//Return: N/A
	public void shuffle() {
		while(!dealtDeck.isEmpty()) {
			deck.add(dealtDeck.remove(0));
		}
		Collections.shuffle(deck);
	}

	//Description: Puts AI's bet to the pool and sets check status of both players to false
	//Parameters: # of points to be bet
	//Return: N/A
	public void AIBet(int points) {
		//AI does stuff here
		if (points <= opponent.getChips()) {
			betPool += points;
			opponent.setChips(opponent.getChips() - points);
		} else {
			betPool += opponent.getChips();
			opponent.setChips(0);
		}
		gamer.setCheck(false);
		opponent.setCheck(false);
		updatePool();
	}

	//Description: Resets graphics and transfers betPool points to gamer
	//Parameters: N/A
	//Return: N/A
	public void AIFold() {
		//AI gives up here
		//inform user
		JOptionPane.showMessageDialog(null, "AI have folded. You will get " + betPool + " chips.");

		//reset graphics
		c1.setIcon(cardBack);
		c2.setIcon(cardBack);
		c3.setIcon(cardBack);
		c4.setIcon(cardBack);
		c5.setIcon(cardBack);
		pc1.setIcon(cardBack);
		pc2.setIcon(cardBack);
		burn.setIcon(feedme);

		//move values in arrayLists to dealtDeck + clear arrayLists	
		dealtDeck.add(gamer.getHand().remove(1));
		dealtDeck.add(gamer.getHand().remove(0));
		dealtDeck.add(opponent.getHand().remove(1));
		dealtDeck.add(opponent.getHand().remove(0));

		while (center.size() > 0) {
			dealtDeck.add(center.remove(0));
		}

		while (burnDeck.size() > 0) {
			dealtDeck.add(burnDeck.remove(0));
		}

		//give betting pool points to player + reset betPool
		gamer.setChips(gamer.getChips() + betPool);
		betPool = 0;
		roundNum++;

		//betting pool and stat info graphic update
		//update betting pool graphics
		updatePool();
		gamer.setAllIn(false);
	}

	//Description: Starts off the round by dealing 2 cards to both players
	//Parameters: N/A
	//Return: N/A
	public void dealCards() {
		//shuffle for the first time.
		if (!shuffled) {
			firstShuffle();
		}

		//deal cards to player and AI (4). shuffle deck if it makes the deck empty.
		if (deck.isEmpty()) {
			shuffle();
		}
		gamer.getHand().add(deck.remove(deck.size()-1));
		if (deck.isEmpty()) {
			shuffle();
		}

		opponent.getHand().add(deck.remove(deck.size()-1));
		if (deck.isEmpty()) {
			shuffle();
		}
		gamer.getHand().add(deck.remove(deck.size()-1));
		if (deck.isEmpty()) {
			shuffle();
		}	
		opponent.getHand().add(deck.remove(deck.size()-1));

		//graphics stuff below
		pc1.setIcon(cards[gamer.getFirstCard().getID()]);
		pc2.setIcon(cards[gamer.getLastCard().getID()]);
		frame.repaint();
	}

	//Description: Deals three cards to the center area and updates graphics for them.
	//Parameters: N/A
	//Return: N?A
	public void drawThree() {
		//get 3 cards from deck and move them to center. Move one card from deck to burn.
		if (deck.isEmpty()) {
			shuffle();
		}
		center.add(deck.remove(0));
		if (deck.isEmpty()) {
			shuffle();
		}
		center.add(deck.remove(0));
		if (deck.isEmpty()) {
			shuffle();
		}
		burnDeck.add(deck.remove(0));
		if (deck.isEmpty()) {
			shuffle();
		}
		center.add(deck.remove(0));

		//graphics
		c1.setIcon(cards[center.get(0).getID()]);
		c2.setIcon(cards[center.get(1).getID()]);
		c3.setIcon(cards[center.get(2).getID()]);
		burn.setIcon(cards[burnDeck.get(0).getID()]);
		frame.repaint();
	}

	//Description: Moves one card to the center and updates its graphics
	//Parameters: N/A
	//Return: N/A
	public void drawOne() {
		//one card drawn, one card burned
		if (deck.isEmpty()) {
			shuffle();
		}
		center.add(deck.remove(0));
		if (deck.isEmpty()) {
			shuffle();
		}
		burnDeck.add(deck.remove(0));

		//graphics
		if (center.size() == 4) {
			c4.setIcon(cards[center.get(3).getID()]);
		} else if (center.size() == 5) {
			c5.setIcon(cards[center.get(4).getID()]);
		}

		if (burnDeck.size() == 2) {
			burn.setIcon(cards[burnDeck.get(1).getID()]);
		} else if (burnDeck.size() == 3) {
			burn.setIcon(cards[burnDeck.get(2).getID()]);
		}
	}

	//Description: Logic for everything caused by clicking a button.
	//Parameters: ActionEvent variable to get input
	//Return: N/A
	public void actionPerformed(ActionEvent ae) {
		String event = ae.getActionCommand();
		if (screenID == 0) {
			//events from the menuPanel
			if (event.equals("PLAY GAME")) {
				playSound.setFramePosition(0);
				playSound.start();
				screenID = 1;
				newGame();
				frame.getContentPane().removeAll();
				frame.add(gamePanel);
				frame.revalidate();
				frame.repaint();
			} else if (event.equals("INSTRUCTIONS AND SETTINGS")) {
				instructionSound.setFramePosition(0);
				instructionSound.start();
				screenID = 2;
				frame.getContentPane().removeAll();
				frame.add(settingPanel);
				frame.revalidate();
				frame.repaint();
			} else if (event.equalsIgnoreCase("show leaderboard")) {
				leaderSound.setFramePosition(0);
				leaderSound.start();
				screenID = 6;
				try {
					Reader myReader = new BufferedReader(new FileReader("leaderboard.txt"));
					leaderText.read(myReader, "leaderboard.txt");
				} catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(null, "File not found, play a round or add a file");
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "Reading error");
				}
				frame.getContentPane().removeAll();
				frame.add(leaderScreen);
				frame.revalidate();
				frame.repaint();
			} else if (event.equals("EXIT GAME")) {
				System.exit(0);
			}
		} else if (screenID == 1) {
			
			if (event.equalsIgnoreCase("deal cards")) {
				if (gamer.getHand().isEmpty()) {
					dealSound.setFramePosition(0);
					dealSound.start();
					//subtract blinds. If blind exceeds chips, go "all in".
					//player blinds
					if (blind < gamer.getChips()) {
						betPool += blind;
						gamer.setChips(gamer.getChips() - blind);
					} else {
						gamer.setAllIn(true);
						betPool += gamer.getChips();
						gamer.setChips(0);
					}

					//AI blinds
					if (blind < opponent.getChips()) {
						betPool += blind;
						opponent.setChips(opponent.getChips() - blind);
					} else {
						opponent.setAllIn(true);
						betPool += opponent.getChips();
						opponent.setChips(0);
					}

					blind++;
					blindText = String.format("%03d", blind);
					//give player and opponent their cards.
					dealCards();

					//update betting pool graphics
					updatePool();
				}
			} else if (event.equalsIgnoreCase("Check")) { 
				checkSound.setFramePosition(0);
				checkSound.start();
				gamer.setCheck(true);
				//phase 1
				if (center.size() == 0 && gamer.getHand().size() > 0 && betPool > 0) {

					//respond to AIBet
					if (AIBet && gamer.getChips() > AIChip / 10) {
						betPool += AIChip / 10;
						gamer.setChips(gamer.getChips() - AIChip / 10);
						updatePool();
					} else if (AIBet && gamer.getChips() <= AIChip / 10) {
						betPool += gamer.getChips();
						gamer.setChips(0);
						gamer.setAllIn(true);
						updatePool();
					}
					//respond to AIBet2
					if (AIBet2 && gamer.getChips() > allInt) {
						betPool += allInt;
						gamer.setChips(gamer.getChips() - allInt);
						updatePool();
					} else if (AIBet2 && gamer.getChips() <= allInt) {
						betPool += gamer.getChips();
						gamer.setChips(0);
						gamer.setAllIn(true);
						updatePool();
					}

					//AI raises AGAIN for no reason on a chance basis
					chance = Math.random();
					if (opponent.getFirstCard().getStrength() == opponent.getLastCard().getStrength() && !counted) {
							if (AIChip / 10 > blind && opponent.getChips() > AIChip / 10) {
								AIBet(AIChip / 10);
								AIBet = true;
								gamer.setCheck(false);
								updateGameStatus("AI bets " + AIChip/10);
								updatePool();
								counted = true;
							} else {
								updateGameStatus("AI checks");
								opponent.setCheck(true);
							}
					} else if (chance < 0.4 + 0.1 * opponent.getDifficulty() && !gamer.getAllIn() && !opponent.getAllIn() && !opponent.check) {
						if (opponent.getChips() > AIChip / 10 && AIChip / 10 > blind) {
							AIBet(AIChip / 10);
							AIBet = true;
							gamer.setCheck(false); 
							updateGameStatus("AI bets " + AIChip/10);
							updatePool();
						} else if (opponent.getFirstCard().getStrength() == opponent.getLastCard().getStrength() && AIChip / 10 > blind && !opponent.allIn && !gamer.allIn && !opponent.check) { 
							AIBet(AIChip / 10);
							AIBet = true;
							gamer.setCheck(false);
							updateGameStatus("AI bets " + AIChip/10);
							updatePool();
						} else if (!opponent.allIn && !gamer.allIn && !opponent.check){
							//AI goes all in if it has less than 10% of its starting chips
							allInt = opponent.getChips();
							AIBet(opponent.getChips());
							updateGameStatus("AI goes all in");
							opponent.setAllIn(true);
							opponent.setCheck(true);
							gamer.setCheck(false);
						} else {
							updateGameStatus("AI checks");
							opponent.setCheck(true);
						}
					} else {
						updateGameStatus("AI checks");
						opponent.setCheck(true);
					}
					
					//if both players have checked proceed to next phase
					if (opponent.getCheck() && gamer.getCheck()) {
						drawThree();
						AIBet = false;
						opponent.setCheck(false);
						gamer.setCheck(false);
					}
					//phase 2
				} else if (center.size() == 3 && betPool > 0) {
					//respond to AIBet
					if (AIBet && gamer.getChips() > AIChip / 5) {
						betPool += AIChip / 5;
						gamer.setChips(gamer.getChips() - AIChip / 5);
						updatePool();
					} else if (AIBet && gamer.getChips() <= AIChip / 5) {
						betPool += gamer.getChips();
						gamer.setChips(0);
						gamer.setAllIn(true);
						gamer.setCheck(true);
						updatePool();
					}

					//respond to AIBet2
					if (AIBet2 && gamer.getChips() > AIChip / 20) {
						betPool += AIChip / 20;
						gamer.setChips(gamer.getChips() - AIChip / 20);
						updatePool();
					} else if (AIBet2 && gamer.getChips() <= AIChip / 20) {
						betPool += gamer.getChips();
						gamer.setChips(0);
						gamer.setAllIn(true);
						gamer.setCheck(true);
						updatePool();
					}
					
					//hand analysis and betting
					chance = Math.random();
					if (analyzeHand(center, opponent.getHand()).getScore() >= 10 && !counted && !opponent.allIn && !gamer.allIn) {
						if (AIChip / 5 > opponent.getChips() && opponent.getChips() >= AIChip / 5 && AIChip / 5 >= blind) {
							AIBet(AIChip / 5);
							AIBet = true;
							gamer.setCheck(false);
							counted = true;
							updateGameStatus("AI bets " + AIChip/5);
						} else if (AIChip / 20 > opponent.getChips() && opponent.getChips() >= AIChip / 20 && AIChip / 20 >= blind) {
							AIBet(AIChip / 20);
							AIBet2 = true;
							gamer.setCheck(false);
							counted = true;
							updateGameStatus("AI bets " + AIChip/20);						
						} else {
							updateGameStatus("AI checks");
							opponent.setCheck(true);
						}
					} else if (!opponent.getAllIn() && !gamer.getAllIn()) {
						if (chance < 0.4 + 0.05 * opponent.getDifficulty() && AIChip / 20 < opponent.getChips() && AIChip / 20 >= blind) {
							AIBet(AIChip / 20);
							AIBet2 = true;
							updateGameStatus("AI bets " + AIChip/20);
							gamer.setCheck(false);
						} else {
							updateGameStatus("AI checks");
							opponent.setCheck(true);
						}
					} else {
						updateGameStatus("AI checks");
						opponent.setCheck(true);
					}
					 
					//draw a card and proceed
					if (opponent.getCheck() && gamer.getCheck()) {
						drawOne();
						consecBet = 0;
						counted = false;
						AIBet = false;
						AIBet2 = false;
						opponent.setCheck(false);
						gamer.setCheck(false);
					}
				//phase 3
				} else if (center.size() == 4 && betPool > 0) {

					//respond to AIBet2
					if (AIBet2 && gamer.getChips() > blind + 1) {
						betPool += blind+1;
						gamer.setChips(gamer.getChips() - (blind + 1));
						updatePool();
					} else if (AIBet2 && gamer.getChips() <= (blind + 1)) {
						betPool += gamer.getChips();
						gamer.setChips(0);
						gamer.setAllIn(true);
						updatePool();
					}
					//respond to AIBet
					if (AIBet && gamer.getChips() > AIChip / 10) {
						betPool += AIChip / 10;
						gamer.setChips(gamer.getChips() - AIChip / 10);
						updatePool();
					} else if (AIBet && gamer.getChips() <= AIChip / 10) {
						betPool += gamer.getChips();
						gamer.setChips(0);
						gamer.setAllIn(true);
						updatePool();
					}

					//hand analysis and betting
					chance = Math.random();
					if (analyzeHand(center, opponent.getHand()).getScore() >= 10 && !counted && !opponent.allIn && !gamer.allIn) {
						if (AIChip / 10 > opponent.getChips() && opponent.getChips() >= AIChip / 10 && AIChip / 10 >= blind) {
							AIBet(AIChip / 10);
							AIBet = true;
							gamer.setCheck(false);
							counted = true;
							updateGameStatus("AI bets " + AIChip/5);
						} else if ((blind + 1) > opponent.getChips() && opponent.getChips() >= (blind + 1)) {
							AIBet((blind + 1));
							AIBet2 = true;
							gamer.setCheck(false);
							counted = true;
							updateGameStatus("AI bets " + (blind + 1));						
						} else {
							updateGameStatus("AI checks");
							opponent.setCheck(true);
						}
					} else if (!opponent.getAllIn() && !gamer.getAllIn()) {
						if (chance < 0.5 + 0.1 * opponent.getDifficulty() && (blind + 1) < opponent.getChips()) {
							AIBet((blind + 1));
							AIBet2 = true;
							updateGameStatus("AI bets " + (blind + 1));
							gamer.setCheck(false);
						} else {
							updateGameStatus("AI checks");
							opponent.setCheck(true);
						}
					} else {
						updateGameStatus("AI checks");
						opponent.setCheck(true);
					}

					//draw a card and proceed
					if (opponent.getCheck() && gamer.getCheck()) {
						drawOne();
						AIBet = false;
						opponent.setCheck(false);
						gamer.setCheck(false);
					}

					//final phase
				} else if (center.size() == 5 && betPool > 0) {
					//respond to AIBet
					if (AIBet && gamer.getChips() > AIChip / 5) {
						betPool += AIChip / 5;
						gamer.setChips(gamer.getChips() - AIChip / 5);
						updatePool();
					} else if (AIBet && gamer.getChips() <= AIChip / 5) {
						betPool += gamer.getChips();
						gamer.setChips(0);
						gamer.setAllIn(true);
						updatePool();
					}

					//respond to AIBet2
					if (AIBet2 && gamer.getChips() > AIChip / 10) {
						betPool += AIChip / 10;
						gamer.setChips(gamer.getChips() - AIChip / 10);
						updatePool();
					} else if (AIBet2 && gamer.getChips() <= AIChip / 10) {
						betPool += gamer.getChips();
						gamer.setChips(0);
						gamer.setAllIn(true);
						updatePool();
					}
					
					chance = Math.random();
					//count cards and assess risk
					if (analyzeHand(center, opponent.getHand()).getScore() >= 20 && !counted && !opponent.allIn && !gamer.allIn) {
						if (AIChip / 5 > opponent.getChips() && opponent.getChips() >= AIChip / 5 && AIChip / 5 >= blind) {
							AIBet(AIChip / 5);
							AIBet = true;
							gamer.setCheck(false);
							counted = true;
							updateGameStatus("AI bets " + AIChip/5);
						} else if (AIChip / 10 > opponent.getChips() && opponent.getChips() >= AIChip / 10 && AIChip / 10 >= blind) {
							AIBet(AIChip / 10);
							AIBet2 = true;
							gamer.setCheck(false);
							counted = true;
							updateGameStatus("AI bets " + AIChip/10);
						} else {
							updateGameStatus("AI checks");
							opponent.setCheck(true);
						}
					} else if (!opponent.getAllIn() && !gamer.getAllIn()) {
						if (chance < 0.4 + 0.05 * opponent.getDifficulty() && AIChip / 10 < opponent.getChips() && AIChip / 10 >= blind) {
							AIBet(AIChip / 10);
							AIBet2 = true;
							updateGameStatus("AI bets " + AIChip/10);
							gamer.setCheck(false);
						} else {
							opponent.setCheck(true);
							updateGameStatus("AI checks");
						}
					} else {
						opponent.setCheck(true);
						updateGameStatus("AI checks");

					}
					//show AI Cards and compare hands
					if (opponent.getCheck() && gamer.getCheck()) {
						AIBet = false;
						AIBet2 = false;
						counted = false;

						//prevent clicking checked to mess with button
					
						ai1.setIcon(cards[opponent.getFirstCard().getID()]);
						ai2.setIcon(cards[opponent.getLastCard().getID()]);

						//win check method + compare
						if (analyzeHand(center, gamer.getHand()).getScore() == analyzeHand(center, opponent.getHand()).getScore()) {
							if (analyzeHand(center, gamer.getHand()).getHandCard().getID() == analyzeHand(center, opponent.getHand()).getHandCard().getID()) {
								//if same hand and same highest card in hand
								if (analyzeHand(center, gamer.getHand()).getHighest().getID() > analyzeHand(center, opponent.getHand()).getHighest().getID()) {
									//give betting pool points to gamer + reset betpool
									gamer.setChips(gamer.getChips() + betPool);
									betPool = 0;
									opponent.setAllIn(false);
									gamer.setAllIn(false);
									roundsWon++;
									roundNum++;
									updateGameStatus("You win.");
								} else {
									//give betting pool points to AI + reset betpool
									opponent.setChips(opponent.getChips() + betPool);
									betPool = 0;
									opponent.setAllIn(false);
									gamer.setAllIn(false);
									roundNum++;
									updateGameStatus("AI wins.");
								}
							} else if (analyzeHand(center, gamer.getHand()).getHandCard().getID() > analyzeHand(center, opponent.getHand()).getHandCard().getID()) {
								//if same hand but different highest card in hand
								gamer.setChips(gamer.getChips() + betPool);
								betPool = 0;
								opponent.setAllIn(false);
								gamer.setAllIn(false);
								roundsWon++;
								roundNum++;
								updateGameStatus("You win.");
							} else {
								//give betting pool points to AI + reset betpool
								opponent.setChips(opponent.getChips() + betPool);
								betPool = 0;
								opponent.setAllIn(false);
								gamer.setAllIn(false);
								roundNum++;
								updateGameStatus("AI win.");
							}
						} else if (analyzeHand(center, gamer.getHand()).getScore() > analyzeHand(center, opponent.getHand()).getScore()) {
							//if better hand
							//give betting pool points to gamer + reset betpool
							gamer.setChips(gamer.getChips() + betPool);
							betPool =  0;
							opponent.setAllIn(false);
							gamer.setAllIn(false);
							roundsWon++;
							roundNum++;
							updateGameStatus("You win.");
						} else {
							//give betting pool points to AI + reset betpool
							opponent.setChips(opponent.getChips() + betPool);
							betPool = 0;
							opponent.setAllIn(false);
							gamer.setAllIn(false);
							roundNum++;
							updateGameStatus("AI win.");
						}
						
						//show reset
						if (!checkClicked && gamer.chips > 0 && opponent.chips > 0) {
							//prevent clicking check again to mess with stuff
							gameSpring.putConstraint(SpringLayout.NORTH, reset, 15, SpringLayout.SOUTH, fold);
							gameSpring.putConstraint(SpringLayout.EAST, reset, 0, SpringLayout.EAST, fold);
							gamePanel.add(reset);
							frame.pack();
							frame.repaint();
							checkClicked = true;
						}
						
						//take player to different screen if win/lose
						else if (!checkClicked && (gamer.chips <= 0 || opponent.chips <= 0)){
							//prevent clicking check again to mess with stuff
							gameSpring.putConstraint(SpringLayout.NORTH, continueButt, 15, SpringLayout.SOUTH, fold);
							gameSpring.putConstraint(SpringLayout.EAST, continueButt, 0, SpringLayout.EAST, fold);
							gamePanel.add(continueButt);
							frame.pack();
							frame.repaint();
							checkClicked = true;
						}
					}
				}
			} else if (event.equalsIgnoreCase("bet")) {
				betSound.setFramePosition(0);
				betSound.start();
				if (gamer.getChips() > blind && gamer.getHand().size() > 0 && !opponent.getAllIn() && !gamer.getAllIn() && betPool != 0) {
					try {
						//get an input
						int playerBet = Integer.parseInt(JOptionPane.showInputDialog("Please the amount you'd like to bet (between blind and number of chips you have)"));
						if (playerBet >= blind && playerBet <= gamer.getChips()) {
							//change AI points to entered value and tell user
							gamer.setChips(gamer.getChips()-playerBet);
							betPool += playerBet;
							updatePool();
							JOptionPane.showMessageDialog(null, "Bet successfully placed");

							//AI Behaviours
							if (center.size() == 0) {
								//Starting hand evaluation
								if (opponent.getFirstCard().getStrength() == opponent.getLastCard().getStrength() || opponent.getFirstCard().getSuite() == opponent.getLastCard().getSuite()) {
									if (playerBet >= opponent.getChips() && !opponent.allIn && !gamer.allIn) {
										AIBet(opponent.getChips());
										opponent.setAllIn(true);
										opponent.setCheck(true);
									} else if (!opponent.allIn){
										AIBet(playerBet);
									}
								} else {
									//chance to fold if high starter bet
									if (playerBet >= opponent.getChips() * 0.45 && playerBet < opponent.getChips()) {
										chance = Math.random();
										if (chance > 0.75 - opponent.getDifficulty() * 0.1) {
											AIFold();
										} else {
											if (playerBet >= opponent.getChips() && !opponent.allIn && !gamer.allIn) {
												AIBet(opponent.getChips());
												opponent.setAllIn(true);
											} else if (!opponent.allIn){
												AIBet(playerBet);
											}
										}
									}

									//fold if bad starting hand
									if (opponent.getFirstCard().getStrength() < 7 && opponent.getLastCard().getStrength() < 7 && opponent.getFirstCard().getStrength() != opponent.getLastCard().getStrength()) {
										AIFold();
									} else {
										AIBet(playerBet);
									}
								}
							} else if (center.size() == 3) {
								//Card counting
								//go in hard if AI has at least a two pair
								if (playerBet >= opponent.getChips() * 0.5) {
									if (analyzeHand(center, opponent.getHand()).getScore() >= 20) {
										if (playerBet >= opponent.getChips()) {
											AIBet(opponent.getChips());
											opponent.setAllIn(true);
											updatePool();
										} else if (!opponent.getAllIn()){
											AIBet(playerBet);
										}
									} else {
										//bluff or fold
										chance = Math.random();
										if (chance < 0.4 + 0.05 * opponent.getDifficulty()) {
											if (playerBet >= opponent.getChips()) {
												AIBet(opponent.getChips());
												opponent.setCheck(true);
												opponent.setAllIn(true);
												updatePool();
											} else {
												AIBet(playerBet);
											}
										} else {
											AIFold();
										}
									}
								} else {
									//if player doesn't go hard, AI will also go light
									//if AI has an ok hand it bets
									if (analyzeHand(center, opponent.getHand()).getScore() >= 10) {
										AIBet(playerBet);
									} else {
										//if AI doesn't have a good hand it might fold
										chance = Math.random();
										if (chance < 0.4 + 0.1 * opponent.getDifficulty()) {
											AIBet(playerBet);
										} else {
											AIFold();
										}
									}
								}
							} else if (center.size() == 4) {
								//Passive Response or Aggressive Response
								chance = Math.random();

								//if bet is too high
								if (playerBet >= opponent.getChips() * 0.45) {
									//Hand analysis
									if (analyzeHand(center, opponent.getHand()).getScore() >= 20) {
										chance = Math.random();
										if (chance > 0.7 - 0.1 * opponent.getDifficulty() && playerBet >= opponent.getChips() && !opponent.allIn) {
											AIBet(opponent.getChips());
											opponent.setAllIn(true);
											opponent.setCheck(true);	
										} else if (!opponent.getAllIn()){
											AIBet(playerBet);
										}
									} else {
										AIFold();
									}
								} else {
									//small bets
									chance = Math.random();
									if (analyzeHand(center, opponent.getHand()).getScore() >= 10 || chance < 0.3 + 0.05 * opponent.getDifficulty()) {
										AIBet(playerBet);
									} else {
										AIFold();
									}
								}
							} else if (center.size() == 5) {
								//analyze hand
								if (playerBet >= opponent.getChips()) {
									//player goes all in or greater than the AI's hand.
									chance = Math.random();
									if (analyzeHand(center, opponent.getHand()).getScore() >= 30 || opponent.getChips() < AIChip / 10 || chance < 0.25 + 0.05 * opponent.getDifficulty()) {
										AIBet(opponent.getChips());
										opponent.setCheck(true);
										opponent.setAllIn(true);
									} else {
										AIFold();
									}
								} else if (playerBet >= opponent.getChips() * 0.45) {
									//high risk betting. Might fold.
									if (analyzeHand(center, opponent.getHand()).getScore() >= 30) {
										AIBet(playerBet);
									} else {
										//bluff
										chance = Math.random();
										if (chance < 0.4 + 0.05 * opponent.getDifficulty() || betPool > blind * 2 + 10) {
											AIBet(playerBet);
										} else {
											AIFold();
										}
									}
								} else if (playerBet >= opponent.getChips() * 0.2) {
									//mid risk betting. Might fold.
									if (analyzeHand(center, opponent.getHand()).getScore() >= 20) {
										AIBet(playerBet);
									} else {
										//bluff. but bigger chance to bluff
										chance = Math.random();
										if (chance < 0.5 + 0.1 * opponent.getDifficulty() || betPool > blind * 2 + 10) {
											AIBet(playerBet);
										} else {
											AIFold();
										}
									}
								} else {
									//low risk betting. Just checks for high cards or lots of points in the betting pool
									if (opponent.getFirstCard().getStrength() > 8 || opponent.getLastCard().getStrength() > 8 || betPool >= blind * 2 + 10) {
										AIBet(playerBet);
									} else {
										AIFold();
									}
								}
							}
						} else {
							//tell user their value falls out of bounds
							JOptionPane.showMessageDialog(null, "Invalid number of chips. Please try again.");
						}	
					} catch (NumberFormatException e) {
						//tell user their value is invalid
						JOptionPane.showMessageDialog(null, "Invalid entry. Please try again.");
					}
				} else if (gamer.getChips() == blind) {
					JOptionPane.showMessageDialog(null, "All in. Good Luck!");
					betPool += gamer.getChips();
					gamer.setChips(0);
				} else {
					JOptionPane.showMessageDialog(null, "Not enough chips to bet");
				}
			} else if (event.equalsIgnoreCase("fold")) {
				foldSound.setFramePosition(0);
				foldSound.start();
				//player gives up
				if (gamer.getHand().size() > 0 && betPool != 0) {
					//inform user
					JOptionPane.showMessageDialog(null, "You have folded. " + betPool + " chips will go to the opponent.");

					//reset graphics
					c1.setIcon(cardBack);
					c2.setIcon(cardBack);
					c3.setIcon(cardBack);
					c4.setIcon(cardBack);
					c5.setIcon(cardBack);
					pc1.setIcon(cardBack);
					pc2.setIcon(cardBack);
					burn.setIcon(feedme);

					//move values in arrayLists to dealtDeck + clear arrayLists	
					dealtDeck.add(gamer.getHand().remove(1));
					dealtDeck.add(gamer.getHand().remove(0));
					dealtDeck.add(opponent.getHand().remove(1));
					dealtDeck.add(opponent.getHand().remove(0));

					while (center.size() > 0) {
						dealtDeck.add(center.remove(0));
					}

					while (burnDeck.size() > 0) {
						dealtDeck.add(burnDeck.remove(0));
					}

					//give betting pool points to AI + reset betPool
					opponent.setChips(opponent.getChips() + betPool);
					betPool = 0;
					roundNum++;
					opponent.setAllIn(false);
					gamer.setAllIn(false);
					
					
					//betting pool and stat info graphic update
					//update betting pool graphics
					if (betPool > playerChip + AIChip) {
						if (gamer.getChips() < 0) {
							gamer.setChips(0);
							betPool = playerChip + AIChip;
						} else if (opponent.getChips() < 0) {
							opponent.setChips(0);
							betPool = playerChip + AIChip;
						}
					}
					updatePool();
				}

			} else if (event.equalsIgnoreCase("reset round")) {
				resetSound.setFramePosition(0);
				resetSound.start();
				resetGame();
			} else if (event.equalsIgnoreCase("Quit Game")) {
				updateLeaderboard();
				screenID = 0;
				frame.getContentPane().removeAll();
				frame.add(menuPanel);
				frame.revalidate();
				frame.repaint();
			} else if (event.equalsIgnoreCase("Continuebutt")) {
				continueSound.setFramePosition(0);
				continueSound.start();
				//if player wins
				if (opponent.chips <= 0) {
					screenID = 3;
					roundsWon *= 10;
					updateLeaderboard();
					winSound.setFramePosition(0);
					winSound.start();
					gamePanel.remove(continueButt);
					frame.getContentPane().removeAll();
					frame.add(winScreen);
					frame.revalidate();
					frame.repaint();
				} else if (gamer.chips <= 0) { //if player loses
					screenID = 4;
					loseSound.setFramePosition(0);
					loseSound.start();
					gamePanel.remove(continueButt);
					frame.getContentPane().removeAll();
					frame.add(loseScreen);
					frame.revalidate();
					frame.repaint();
				}
			}

		} else if (screenID == 2) {
			//events in the settings panel//
			//change player's starting point value
			if (event.equals("CHANGE YOUR CHIPS")) {
				changeSound.setFramePosition(0);
				changeSound.start();
				try {
					//get an input
					int chipChange = Integer.parseInt(JOptionPane.showInputDialog("Please enter a number of chips between 50 and 500"));
					if (chipChange >= 50 && chipChange <= 500) {
						//change chip value to entered value and tell user
						playerChip = chipChange;
						gamer.setChips(chipChange);
						updatePoints(gamer.getChips(), opponent.getChips());
						JOptionPane.showMessageDialog(null, "Your starting chips have been set to " + playerChip + " chips.");
						updateChips();
					} else {
						//tell user their input is out of bounds
						JOptionPane.showMessageDialog(null, "Invalid number of chips. Please try again."); 
					}
				} catch (NumberFormatException e) {
					//tell user their input is not valid
					JOptionPane.showMessageDialog(null, "Invalid entry. Please try again.");
				}
				//change AI's starting point value
			} else if (event.equals("CHANGE AI CHIPS")) {
				changeSound.setFramePosition(0);
				changeSound.start();
				try {
					//get an input
					int AIChipChange = Integer.parseInt(JOptionPane.showInputDialog("Please enter a number of chips between 50 and 500"));
					if (AIChipChange >= 50 && AIChipChange <= 500) {
						//change AI points to entered value and tell user
						AIChip = AIChipChange;
						opponent.setChips(AIChipChange);
						updatePoints(gamer.getChips(), opponent.getChips());
						JOptionPane.showMessageDialog(null, "AI starting chips have been set to " + AIChip + " chips.");
						updateChips();
					} else {
						//tell user their value falls out of bounds
						JOptionPane.showMessageDialog(null, "Invalid number of chips. Please try again.");
					}	
				} catch (NumberFormatException e) {
					//tell user their value is invalid
					JOptionPane.showMessageDialog(null, "Invalid entry. Please try again.");
				}
				//change AI's difficulty variable
			} else if (event.equals("CHANGE AI DIFFICULTY")) {
				diffSound.setFramePosition(0);
				diffSound.start();
				try {
					//get input from user
					int diff = Integer.parseInt(JOptionPane.showInputDialog("Please enter an integer between 0 and 2 inclusive."));
					if (diff >= 0 && diff <= 2) {
						//change AI's difficulty and tell user
						opponent.setDifficulty(diff);
						JOptionPane.showMessageDialog(null, "AI's difficulty has been changed to " + opponent.getDifficulty());
					} else {
						//tell user their input falls out of bounds
						JOptionPane.showMessageDialog(null, "Difficulty variable out of range. Please try again.");
					}	
				} catch (NumberFormatException e) {
					//tell user their input is invalid
					JOptionPane.showMessageDialog(null, "Invalid input. Please try again.");
				}
				//go back to the title screen
			} else if (event.equalsIgnoreCase("Show Instructions")) {
				instructionScreenSound.setFramePosition(0);
				instructionScreenSound.start();
				screenID = 5;
				frame.getContentPane().removeAll();
				frame.add(instructionScreen);
				frame.revalidate();
				frame.repaint();
			} else if (event.equals("RETURN TO TITLE MENU")) {
				backSound.setFramePosition(0);
				backSound.start();
				screenID = 0;
				frame.getContentPane().removeAll();
				frame.add(menuPanel);
				frame.revalidate();
				frame.repaint();
			}
		//WIN SCREEN
		} else if (screenID == 3) {
			if (event.equalsIgnoreCase("return to main menu")) {
				backSound.setFramePosition(0);
				backSound.start();
				screenID = 0;
				frame.getContentPane().removeAll();
				frame.add(menuPanel);
				frame.revalidate();
				frame.repaint();
			}
		//LOSE SCREEN
		} else if (screenID == 4) {
			if (event.equalsIgnoreCase("return to main menu")) {
				backSound.setFramePosition(0);
				backSound.start();
				screenID = 0;
				frame.getContentPane().removeAll();
				frame.add(menuPanel);
				frame.revalidate();
				frame.repaint();
			}
		//instruction screen
		} else if (screenID == 5) {
			if (event.equalsIgnoreCase("return to setting screen")) {
				backSound.setFramePosition(0);
				backSound.start();
				screenID = 2;
				frame.getContentPane().removeAll();
				frame.add(settingPanel);
				frame.revalidate();
				frame.repaint();
			} else if (event.equalsIgnoreCase("click here for more information")) {
				double rick = Math.random();
				if (rick <= 0.49) {
					openWebpage("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
				} else {
					openWebpage("https://ca.pokernews.com/poker-rules/texas-holdem.htm ");
				}
			}
		//leaderboard
		} else if (screenID == 6) {
			if (event.equalsIgnoreCase("return to main menu")) {
				backSound.setFramePosition(0);
				backSound.start();
				screenID = 0; 
				frame.getContentPane().removeAll();
				frame.add(menuPanel);
				frame.revalidate();
				frame.repaint();
			}
		}
	}
	
	public static Clip addAudio(String fileName) {
		Clip audio = null;
		try {
			AudioInputStream sound = AudioSystem.getAudioInputStream(new File (fileName));
			audio = AudioSystem.getClip();
			audio.open(sound);
		} catch (IOException e){
			System.out.println("1" + fileName);
		} catch (LineUnavailableException e) {
			System.out.println("2" + fileName);
		} catch (UnsupportedAudioFileException e) {
			System.out.println("3" + fileName);
		}
		return audio;
	}
	
	public static void main(String[] args) {	
		//initialize audio
			bgm = addAudio("bgm.wav");
			betSound = addAudio("bet.wav");
			changeSound = addAudio("change_chips.wav");
			checkSound = addAudio("check.wav");
			dealSound = addAudio("deal cards.wav");
			foldSound = addAudio("fold.wav");
			instructionSound = addAudio("instruction.wav");
			playSound = addAudio("play.wav");
			winSound = addAudio("woo.wav");
			loseSound = addAudio("loseSound.wav");
			continueSound = addAudio("continueSound.wav");
			resetSound = addAudio("resetSound.wav");
			backSound  = addAudio("backSound.wav");
			instructionScreenSound = addAudio("instructionScreenSound.wav");
			diffSound = addAudio("diffSound.wav");
			leaderSound = addAudio("leaderSound.wav");
			bgm.start();
			bgm.loop(Clip.LOOP_CONTINUOUSLY);
			
			//initialize driver
			new driver();
	}
}
