import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.HashMap;
import java.io.PrintWriter;

public class Monoplified {
	
	// declaration of variables and objects that will used in a lot of methods
	public static Scanner input = new Scanner(System.in); 
	public static String [] board = new String[32]; // the monopoly board
	public static int [] propPrice= new int[32]; // the price of each square with values
	public static int [] rentPrice = new int[32]; // the rent price of each property
	public static String [] propSet = new String[32]; // which property belongs in which set
	public static int [] money; // how much money each player has
	public static int [] playerPlace; //where on the board each players are at
	
	
	public static void main (String[] args) throws Exception {
		// additional board setup declaration
		int [] housePrice = new int[32]; // price of getting a house for each house
		Map <Integer, Integer> isOwned = new HashMap<>(); // which properties are owned and by who
		Map <Integer, String> playerIndex = new HashMap<>(); // what number each player is assigned to for array index 
		
		instructionsAndRule();
		
		//getting the number of players that will participate in the game
		System.out.print("Please enter the number of people playing (2 to 6): ");
		int playerNum = input.nextInt();
		while(playerNum<2 || playerNum>6) {
			System.out.print("Invalid... Please input a number from 2 to 6: ");
			playerNum = input.nextInt();
		}
		
		// inputting the nicknames and setting up the order that the player will go in for turns
		String [] nickname = new String[playerNum]; // the nicknames of each player
		input.nextLine();
		for(int i=0; i<playerNum; i++) {
			System.out.print("Enter player " + (i+1) + "'s nickname: ");
			nickname[i] = input.nextLine();
		}
		shuffleAndPrint(nickname, playerIndex); // shuffle nickname and print the order that everyone is going in
		
		// setting up each player's information
		money = new int[playerNum];
		Arrays.fill(money, 1500); // everyone starts with $1500
		playerPlace = new int[playerNum];
		int[] inJail = new int[playerNum]; // how long a player would need to stay in jail
		boolean bankrupt [] = new boolean[playerNum]; // whether or not a player is still in the game
		ArrayList<Integer> [] propCollection = new ArrayList[playerNum]; // the properties that each player owns
		ArrayList<String> [] fullSet = new ArrayList[playerNum]; // the fullSets that each player own
		
		setGame(housePrice, propCollection, fullSet);
		
		// each turn
		while(true) {
			for(int i=0; i<playerNum; i++) {
				if(bankrupt[i] == false) { // check if the player is still in game
					// output
					System.out.println(nickname[i].toUpperCase() + "'S TURN:");
					System.out.println("Money in bank: $" + money[i] + "\n");
					
					//check if the player is allowed to roll (out of jail)
					if(inJail[i] == 0) rollDie(i);
					else if(inJail[i] > 0) {
						System.out.println("Still in jail, you have " + inJail[i] + " turn(s) until you can roll again");
						inJail[i]--;
					}
					Thread.sleep(2000);
					
					// check if the player will go to jail
					if(playerPlace[i] == 24) {
						jailTime(i, inJail);
						Thread.sleep(2000);
					}
					
					buyProperty(i, fullSet, propCollection, isOwned); // buying a property
					buyHouse(i, housePrice, fullSet, propCollection); // buying a house
					Fee (i, bankrupt, fullSet, propCollection, isOwned, playerIndex); // paying money that a player owe and determining if they are bankrupted
					System.out.println("Money in bank: $" + money[i] + "\n");
					Thread.sleep(800);
					System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------\n");
					
					//finding a winner
					boolean oneLeft = false;
					int winner = -1;
					for(boolean temp: bankrupt) {
						winner++;
						if(!oneLeft && !temp) {
							oneLeft = true;
						} else if(oneLeft && !temp) {
							oneLeft = false; break;
						}
					}
					if(oneLeft) {
						System.out.println("Since everyone else is eliminated, " + playerIndex.get(winner) + " is the winner"); System.exit(0);
					}
					
				}
			} 
		}
		
	}
	
	
	public static void shuffleAndPrint (String [] nickname, Map <Integer, String> playerIndex) {
		// shuffleling using math.random() and swapping
		for(int i=0; i<nickname.length; i++) {
			int random = (int) (Math.random()*nickname.length);
			String temp = nickname[random];
			nickname[random] = nickname[i];
			nickname[i] = temp;
		}
		
		//output
		System.out.print("The order of the players will be: " + nickname[0]);
		playerIndex.put(0, nickname[0]);
		for(int i=1; i<nickname.length; i++) {
			System.out.print(", " + nickname[i]);
			playerIndex.put(i, nickname[i]);
		}
		System.out.println("\n");
	}
	
	public static void rollDie (int player) throws InterruptedException {
		int rollDie = (int) Math.floor((((Math.random()*6)+1) + ((Math.random()*6)+1)));
		boolean passedGo = false;
		//check whether or not the player reaches the end of the "board" and passes "GO"
		if(playerPlace[player] + rollDie >= board.length) {
			playerPlace[player] =  rollDie - (board.length - playerPlace[player]);
			money[player] += 200;
			passedGo = true;
		} else {
			playerPlace[player] += rollDie;
		}
		
		//output
		System.out.println("Rolling die . . . ");
		Thread.sleep(1200);
		System.out.println("You rolled a " + rollDie + " and landed on \"" + board[playerPlace[player]] + "\"");
		if(passedGo) System.out.println("\nYou have passed GO" + "\nMoney in Bank: $" + money[player]);
	}
	
	public static void jailTime(int player, int[] inJail) {
		//output
		System.out.println("\nSince you landed on \"GO TO JAIL\", you have the option of:");
		System.out.println("\t1. Paying $100 and moving next round");
		System.out.println("\t2. Waiting 4 rounds in jail and going after 4 rounds\n");
		System.out.print("Enter 1 or 2 to select your action:");
		
		// input player's choice
		String jailOption = input.next();
		while (!jailOption.equals("1") && !jailOption.equals("2")) {
			System.out.print("Invalid... Please input 1 or 2: ");
			jailOption = input.next();
		}
		
		// action
		playerPlace[player] = 8; // move player to "IN JAIL" square
		if(jailOption.equals("1")) {
			 money[player] -= 100;
		} else{
			inJail[player] = 4;
		}
	}
	
	public static void buyProperty(int player, ArrayList<String> [] fullSet, ArrayList<Integer> [] propCollection, Map<Integer, Integer> isOwned) throws Exception {
		System.out.println();
		int playerLoc = playerPlace[player]; // where the player is on the "board"
		if(isOwned.containsKey(playerLoc) && isOwned.get(playerLoc)== player) {
			System.out.println("This is your own property, so there's not much to do");
			Thread.sleep(2000);
			System.out.println();
			
		} else if(rentPrice[playerLoc]!=0 && !isOwned.containsKey(playerLoc)) { // the player landed on an unowned property square
			// output the properties the player already owns and the set they belong to
			if(!propCollection[player].isEmpty()) {
				System.out.println("Properties: ");
				for(int i= 0; i<propCollection[player].size(); i++) {
					int propIndex = propCollection[player].get(i);
					System.out.println("� " + board[propIndex] + " -> " + propSet[propIndex]);
				}
			}
			
			// output and input action the player wants to carry out
			System.out.print("Would you like to purchase \""+ board[playerLoc] + "\" -> $"+ propPrice[playerLoc]+" -> " + propSet[playerLoc]+" (y/n): ");
			String tempAction = input.next().toLowerCase().substring(0,1);
			while (!tempAction.equals("y") && !tempAction.equals("n")) {
				System.out.print("Invalid... Please input \"y\" to purchase property or \"n\" to not: ");
				tempAction = input.next().toLowerCase().substring(0,1);
			}
			
			// carry out action
			if(tempAction.equals("y")&& money[player] > propPrice[playerLoc]) { // check if the player has enough money to buy the property
				isOwned.put(playerLoc, player);
				money[player] -= propPrice[playerLoc];
				propCollection[player].add(playerLoc);
				System.out.println("You now own \""+ board[playerLoc] +"\"");
				
				if(playerLoc!=4 && playerLoc!=12 && playerLoc!=20 && playerLoc!=28 && checkFullSet(player, playerLoc, propCollection)) { // check if the recently bought property completes a full set and is not a company or railroad
					fullSet[player].add(propSet[playerLoc]);
				}
				Thread.sleep(2000);
				
			}  else if (tempAction.equals("y")&& money[player] < propPrice[playerLoc]){
				System.out.println("Sadly you don't have enough money to buy this property :(");
				Thread.sleep(2000);
			}
			System.out.println();
		}
	}
	
	public static boolean checkFullSet(int player, int propertyBought, ArrayList<Integer> [] propCollection) { //return true or false
		boolean full = true;
		int start = -1; 
		int end = -1;
		//which index does each set starts and ends at on the the "board"
		if(propSet[propertyBought].equals("Red")) {
			start = 1; end = 2;
		} else if(propSet[propertyBought].equals("Orange")) {
			start = 5; end = 7;
		} else if(propSet[propertyBought].equals("Yellow")) {
			start = 9; end = 11;
		} else if(propSet[propertyBought].equals("Green")) {
			start = 13; end = 15;
		} else if(propSet[propertyBought].equals("Blue")) {
			start = 17; end = 19;
		} else if(propSet[propertyBought].equals("Purple")) {
			start = 21; end = 23;
		} else if(propSet[propertyBought].equals("Pink")) {
			start = 25; end = 27;
		} else if(propSet[propertyBought].equals("Gray")) {
			start = 29; end = 31;
		}
		
		// check for full set
		if(start != -1 && end != -1) {
			for(int i=start; i<=end; i++) {
				if(rentPrice[i] != 0 && !propCollection[player].contains(i)) { // if it's a property and the player doesn't have the property
					full = false; // the newly bought square does not complete a set
					break;
				}
				
			}
		}
		return full;
	}
	
	public static void buyHouse(int player, int[] housePrice, ArrayList<String> [] fullSet, ArrayList<Integer> [] propCollection) throws Exception {
		if(!fullSet[player].isEmpty()) { // if the player has any fullSets
			ArrayList <Integer> fullSetProp = new ArrayList<>(); // the index of the properties that are in a full set
			
			// output properties that are in full sets
			System.out.println("\nHere are the houses you have that are in a full set: ");
			for(int i=0; i < propCollection[player].size(); i++) {
				int property = propCollection[player].get(i); // a property that the player owns
				if(fullSet[player].contains(propSet[property])) { // check if the property is a part of a set in the player's fullSet collection
					System.out.println(i +" - " + board[property] + " (house price: " + housePrice[property] + ") ");
					fullSetProp.add(i);
				}
			}
			
			// output and input for user's actions
			System.out.println("Would you like to buy a house for any of these properties? (y/n)");
			String tempAction = input.next().toLowerCase().substring(0,1);
			while (!tempAction.equals("y") && !tempAction.equals("n")) {
				System.out.print("Invalid... Please input \"y\" to buy a house or \"n\" to not: ");
				tempAction = input.next().toLowerCase().substring(0,1);
			}
			
			// output and input for more user's actions
			if(tempAction.equals("y")) {
				System.out.println("Please enter a number for which property you would like to buy a house for (numbers in the list above or enter -2 to skip)");
				System.out.print("Enter an integer: ");
				int houseBuy = input.nextInt();
				while (fullSetProp.contains(houseBuy) && houseBuy != -2) {
					System.out.print("Invalid... Please input an integer on the previous list to pick a property: ");
					houseBuy = input.nextInt();
				}
				
				// carry out action
				if(houseBuy == -2) {
					return;
				} else if(money[player]< housePrice[propCollection[player].get(houseBuy)]) {
					System.out.println("Sadly you don't have enough money to buy a house for this property :(");
				} else {
					int propHouse = propCollection[player].get(houseBuy); // the property index that the player bought a house for
					System.out.println("You have bought a house for \"" + board[propHouse] + "\"" );
					rentPrice[propHouse] += (int) (propPrice[propHouse]/20); // rent increases by 0.05% of original property price
					money[player] -= housePrice[propHouse]; 
				}
			}
			System.out.println();
			Thread.sleep(2000);
		}
	}
	
	public static void Fee (int player, boolean[] bankrupt, ArrayList<String> [] fullSet, ArrayList<Integer> [] propCollection,  Map <Integer, Integer> isOwned, Map <Integer, String> playerIndex) throws Exception {
		if(playerPlace[player] == 3) { // if a player lands on a tax area
			System.out.println("Tax paying day! :P You owe the bank $200\n");
			
			if(money[player]<200) { // if a player doesn't have enough money and will go bankrupt
				System.out.println("UH OH! You don't have enough money to pay tax! You will have to leave the game.");
				bankBankrupt (player, bankrupt, propCollection, isOwned); // returning things to the bank and eliminating the player
			} else {
				money[player] -= 200;
			}
			Thread.sleep(2000);
			
		} else if (playerPlace[player] == 30) { // if a player lands on a luxury tax area
			System.out.println("Luxury tax paying day! :P You owe the bank $100\n");
			
			if(money[player]<100) { // if a player doesn't have enough money and will go bankrupt
				System.out.println("UH OH! You don't have enough money to pay tax! You will have to leave the game.");
				bankBankrupt (player, bankrupt, propCollection, isOwned); // returning things to the bank and eliminating the player
			} else {
				money[player] -= 100;
			}
			Thread.sleep(2000);
			
		} else if(isOwned.containsKey(playerPlace[player]) && isOwned.get(playerPlace[player]) != player) { // if a player lands on another player's property
			String name = playerIndex.get(isOwned.get(playerPlace[player])); // the owner of the property the player landed on
			int oweAmount = rentPrice[playerPlace[player]]; // the rent price of the property
			System.out.println("You landed on " + name + "'s property, so you have to pay them $" + oweAmount +"\n");
			
			if(money[player]<oweAmount) { // not enough money to pay the rent
				System.out.println("UH OH! You don't have enough money to pay the rent! You will have to leave the game.");
				System.out.println(name + " will have all your remaining money, properties and houses");
				playerBankrupt (player, isOwned.get(playerPlace[player]), bankrupt,  fullSet, propCollection, isOwned); // giving everything to the other player and eliminating the poor player
			} else {
				money[isOwned.get(playerPlace[player])] += oweAmount;
				money[player] -= oweAmount;
			}
			Thread.sleep(2000);
		}
	}
	
	public static void bankBankrupt (int player, boolean[] bankrupt, ArrayList<Integer> [] propCollection, Map <Integer, Integer> isOwned) {
		bankrupt[player] = true; 
		for(int i=0; i<propCollection[player].size(); i++) { // loop through all the properties the player has
			int propReturn = propCollection[player].get(i); // the properties that are being returned
			rentPrice[propReturn] = propPrice[propReturn]/10; // reset rent price
			isOwned.remove(propReturn); // the property is no longer owned by anyone
		}
		
	}
	
	public static void playerBankrupt (int player, int newOwner, boolean[] bankrupt, ArrayList<String> [] fullSet, ArrayList<Integer> [] propCollection, Map <Integer, Integer> isOwned) {
		bankrupt[player] = true;
		for(int i=0; i<propCollection[player].size(); i++) { // loop through all the properties the player has
			int propReturn = propCollection[player].get(i);// the properties that are being given away
			isOwned.put(propReturn, newOwner); // change ownership of the property
		}
		for(int i= 0; i<fullSet[player].size(); i++) { // loop through all the full sets the player has
			// change ownership of the full sets
			fullSet[newOwner].add(fullSet[player].get(i)); 
			fullSet[player].remove(i);
		}
		
		//transfer money
		money[newOwner] += money[player];
		money[player]=0;
	}
	
	
	
 	public static void instructionsAndRule () throws Exception {
		//intro
		System.out.println("Welcome to MONOPLIFIED, a simplified version of Monopoly!\n");
		Thread.sleep(1500);
		System.out.println("This game requires 2 to 6 people to play with, so grab your friends and family!\n"
				+ "Feel free to play multiple turns with yourself if nobody is currently available to play with you :)\n");
		Thread.sleep(4500);
		
		//rules
		System.out.println("The rules of Monoplified is quite similar to Monopoly, but please take a few seconds to read it since there are a few changes.");
		System.out.print("Have you read the rules already? (y/n): ");
		String skipRule = input.next().toLowerCase().substring(0, 1);
		while(!skipRule.equals("y") && !skipRule.equals("n")) {
			System.out.println("Invalid... Please input \"y\" or \"n\": ");
			skipRule = input.next().toLowerCase().substring(0, 1);
		}
		System.out.println("");

		Thread.sleep(500);
		if(skipRule.equals("n")) {
			System.out.println("Rules:");
			System.out.println("1. Each turn starts off with the player rolling 2 die");
			System.out.println("2. Each time a player lands on or passes over *GO*, they will be paid a $200 salary :D");
			System.out.println( "3. Jail: \n"
					+ "\t• If a player lands on *GO TO JAIL*, they will go directly to jail without getting any sort of 'passing go' payment \n"
					+ "\t• Houses and rent can still be bought and collected when the player in is jail\n"
					+ "\t• To get out of jail, the criminal can choose between paying a fine of $100 and going the next round, or waiting 4 rounds");
			System.out.println("4. Buying Properties: \n"
					+ "\t• A player can only buy a property if it's unowned and they land on it\n"
					+ "\t• The price of the property is written in the file and will also be printed out when a player land on that property");
			System.out.println("5. Buying Houses:\n"
					+ "\t• A player can only buy houses for properties in a full colour set owned by them \n"
					+ "\t\t• ex: Jane can only buy a house for Ottawa if she also has Seoul and Rome (green set)\n"
					+ "\t• The prices of houses for each propery is in the file and will also be printed when the player is ready to buy a house");
			System.out.println("6. Rent (PAY ATTENTION TO THIS):\n"
					+ "\t• When a player lands on a property owned by another player, the owner collects rent\n"
					+ "\t• Rent Price:\n"
					+ "\t\t• Rent for railroads, the electric company, and waterworks is 20% of the original property price\n"
					+ "\t\t• Rent for the rest of the properties is 10% of the original property price\n"
					+ "\t\t• Rent price doubles once a player obtain all the properties in a colour set\n"
					+ "\t\t• Rent increases by 5% of the original property price (rounded) for each house on the square\n"
					+ "\t\t\t• ex:  Delhi ($60) with 3 houses --> rent: $6(2) + $3 + $3 + $3 = $21");
			System.out.println("7. If a player owes more money than they can pay, they must give everything they have to the person they own money to (bank/player) \n"
					+ "   and retire from the game :(");
			System.out.println();
			Thread.sleep(5000);
		}
		
	}
	
	public static void setGame(int[] housePrice, ArrayList<Integer> [] collection, ArrayList<String> [] set ) throws Exception {
		// game board --> square names
		board[0] = "GO"; board[1] = "Rio"; board[2] = "Delhi"; board[3] = "Income Tax"; board[4] = "RailRoad";
		board[5] = "Bangkok"; board[6] = "Cairo"; board[7] = "Madrid"; board[8] = "IN JAIL/PASS BY"; board[9] = "Jakarta";
		board[10] = "Berlin"; board[11] = "Moscow"; board[12] = "Electric Company"; board[13] = "Ottawa"; board[14] = "Seoul";
		board[15] = "Rome"; board[16] = "FREE PARKING"; board[17] = "Zurich"; board[18] = "Riyadh"; board[19] = "Copenhagen";
		board[20] = "RailRoad"; board[21] = "BeiJing"; board[22] = "Dubai"; board[23] = "Canberra"; board[24] = "GO TO JAIL";
		board[25] = "Paris"; board[26] = "HongKong"; board[27] = "Tokyo"; board[28] = "Water Works"; board[29] = "London";
		board[30] = "Luxury Tax"; board[31] = "New York";
		
		// value of squares on the board
		propPrice[1] = 60; propPrice[2] = 60; propPrice[3] = 200; propPrice[4] = 200;
		propPrice[5] = 100; propPrice[6] = 100; propPrice[7] = 120; propPrice[9] = 140;
		propPrice[10] = 140; propPrice[11] = 160; propPrice[12] = 150; propPrice[13] = 180; propPrice[14] = 180;
		propPrice[15] = 200; propPrice[17] = 220; propPrice[18] = 220; propPrice[19] = 240;
		propPrice[20] = 200; propPrice[21] = 260; propPrice[22] = 260; propPrice[23] = 280;
		propPrice[25] = 300; propPrice[26] = 300; propPrice[27] = 320; propPrice[28] = 150; propPrice[29] = 350;
		propPrice[30] = 100; propPrice[31] = 400;
		
		// rent price of properties
		for(int i=0; i<32; i++) {
			rentPrice[i] = propPrice[i]/10;
		}
		rentPrice[3]= 0; rentPrice[4] = 40; rentPrice[12] = 30; rentPrice[20] = 40; rentPrice[28] = 30; rentPrice[30]=0 ;
		
		// house price of properties
		housePrice[1] = 50; housePrice[2] = 50; housePrice[5] = 50; housePrice[6] = 50; housePrice[7] = 50; 
		housePrice[9] = 100; housePrice[10] = 100; housePrice[11] = 100; housePrice[13] = 100; housePrice[14] = 100; housePrice[15] = 100;
		housePrice[17] = 150; housePrice[18] = 150; housePrice[19] = 150;  housePrice[21] = 150; housePrice[22] = 150; housePrice[23] = 150;
		housePrice[25] = 200; housePrice[26] = 200; housePrice[27] = 200; housePrice[29] = 200; housePrice[31] = 200;
		
		// which property belongs to which set
		propSet[1]= "Red"; propSet[2] = "Red";
		propSet[5]= "Orange"; propSet[6] = "Orange"; propSet[7] = "Orange";
		propSet [9] = "Yellow"; propSet[10] = "Yellow"; propSet[11] = "Yellow";
		propSet[13] = "Green"; propSet[14] = "Green"; propSet[15] = "Green";
		propSet[17] = "Blue"; propSet[18] = "Blue"; propSet[19] = "Blue";
		propSet[21] = "Purple"; propSet[22] = "Purple"; propSet[23] = "Purple";
		propSet[25] = "Pink"; propSet[26] = "Pink"; propSet[27] = "Pink";
		propSet[29] = "Gray"; propSet[31] = "Gray";
		propSet[4] = "No Set"; propSet[12] = "No Set"; propSet[20] = "No Set"; propSet[28] = "No Set";
		
		//initializing user's personal collections
		for (int i = 0; i < collection.length ; i++) { 
			collection[i] = new ArrayList<Integer>(); 
        } 
		
		for (int i = 0; i < set.length ; i++) { 
			set[i] = new ArrayList<String>(); 
        } 
		
		filePrint(); // printing info onto a file
		
	}
	
	public static void filePrint () throws Exception {
		PrintWriter output = new PrintWriter("Gameboard and Rules.txt");
		//board
		output.println("BOARD: \n");
		//upper row of board
		for(int i=0; i<=8; i++) { // square name
			if(board[i].length()>7) {
				output.print(board[i] + "\t");
			} else {
				output.print(board[i] + "\t\t");
			}
		}
		output.println();
		
		for(int i=0; i<=8; i++) { // the value or pricing of the square
			if(propPrice[i] != 0) output.print("$" + propPrice[i] + "\t\t");
			else output.print("\t\t");
		}
		output.println("");
		
		for(int i=0; i<=8; i++) { // what set the properties belong to
			if(propSet[i] != null && !propSet[i].isEmpty()) output.print(propSet[i] + "\t\t");
			else output.print("\t\t");
		}
		output.println("\n");
		
		//columns of board
		for(int i=0; i<7; i++) {
			// square name
			int space = 16;
			if(board[31-i].length()>7) space --;
			
			output.print(board[31-i]);
			for(int j=0; j<space; j++) output.print("\t");
			output.println(board[9+i]);
			
			// the value or pricing of the square
			space = 16;
			if(propPrice[31-i] == 0) space -= 2;
			
			if(propPrice[31-i] != 0) output.print("$" + propPrice[31-i]);
			else output.print("\t\t");
			for(int j=0; j<space; j++) output.print("\t");
			if(propPrice[9+i] != 0) output.println("$" + propPrice[9+i]);
			else output.println("\t\t");
			
			// what set the properties belong to
			space = 16;
			if(propSet[31-i] == null) space -=2;
			else if( propSet[31-i] != null  && propSet[31-i].length()>7) space ++;
			else if(propSet[31-i].length()<4) space --;
			
			if(propSet[31-i] != null && !propSet[31-i].isEmpty()) output.print(propSet[31-i]);
			else output.print("\t\t");
			for(int j=0; j<space; j++) output.print("\t");
			if(propSet[9+i] != null && !propSet[9+i].isEmpty()) output.print(propSet[9+i] + "\t\t");
			else output.print("\t\t");
			
			output.println("\n");
		}
		
		//last row
		for(int i=0; i<=8; i++) { // square name
			if(board[31-7-i].length()>7) {
				output.print(board[31-7-i] + "\t");
			} else {
				output.print(board[31-7-i] + "\t\t");
			}
		}
		output.println();
		
		for(int i=0; i<=8; i++) { // the value or pricing of the square
			if(propPrice[31-7-i] != 0) output.print("$" + propPrice[31-7-i] + "\t\t");
			else output.print("\t\t");
		}
		output.println("");
		
		for(int i=0; i<=8; i++) { // what set the properties belong to
			if(propSet[31-7-i] != null && !propSet[31-7-i].isEmpty()) output.print(propSet[31-7-i] + "\t\t");
			else output.print("\t\t");
		}
		output.println("\n\n\n");
		
		
		//rules
		output.println("RULES: ");
		output.println("1. Each turn starts off with the player rolling 2 die");
		output.println("2. Each time a player lands on or passes over *GO*, they will be paid a $200 salary :D");
		output.println( "3. Jail: \n"
				+ "\t• If a player lands on *GO TO JAIL*, they will go directly to jail without getting any sort of 'passing go' payment \n"
				+ "\t• Houses and rent can still be bought and collected when the player in is jail\n"
				+ "\t• To get out of jail, the criminal can choose between paying a fine of $100 and going the next round, or waiting 4 rounds");
		output.println("4. Buying Properties: \n"
				+ "\t• A player can only buy a property if it's unowned and they land on it\n"
				+ "\t• The price of the property is written in the file and will also be printed out when a player land on that property");
		output.println("5. Buying Houses:\n"
				+ "\t• A player can only buy houses for properties in a full colour set owned by them \n"
				+ "\t\t• ex: Jane can only buy a house for Ottawa if she also has Seoul and Rome (green set)\n"
				+ "\t• The prices of houses for each property is in the file and will also be printed when the player is ready to buy a house");
		output.println("6. Rent (PAY ATTENTION TO THIS):\n"
				+ "\t• When a player lands on a property owned by another player, the owner collects rent\n"
				+ "\t• Rent Price:\n"
				+ "\t\t• Rent for railroads, the electric company, and waterworks is 20% of the original property price\n"
				+ "\t\t• Rent for the rest of the properties is 10% of the original property price\n"
				+ "\t\t• Rent price doubles once a player obtain all the properties in a colour set\n"
				+ "\t\t• Rent increases by 5% of the original property price (rounded) for each house on the square\n"
				+ "\t\t\t• ex:  Delhi ($60) with 3 houses --> rent: $6(2) + $3 + $3 + $3 = $21");
		output.println("7. If a player owes more money than they can pay, they must give everything they have to the person they own money to (bank/player) \n"
				+ "   and retire from the game :(");
		
		output.close();
	}
}
