/*
 * file name: Acey.java
 * this program is for playing Acey Deucey game
 * we connect to the dealer via Internet and the computer will play
 * @author: Paria Akhtarmoghaddam
 */

import java.util.*;
import java.io.*;
import java.net.Socket;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Acey {
    //create class objects and variable in order to use them in the methods later
    Socket socket;
    DataInputStream dis;
    DataOutputStream dos;
    private int all = 0;
    private int same = 0;
    Scanner scan;
    boolean testMode = false;

    public Acey(String fileName) throws FileNotFoundException {
        scan = new Scanner(new File(fileName));
        testMode = true;
    }

    //test
    private void write(String s) throws IOException {
        if (!testMode) {
            dos.writeUTF(s);
            System.out.println("Writing " + s);
            dos.flush();
        } else {
            System.out.println("WRITE: " + s);
        }
    }

    private String read() throws IOException {
        if (!testMode) {
            return dis.readUTF();
        } else {
            String theLine = scan.nextLine();
            //System.out.println(theLine);
            return theLine;
        }
    }

    //end test
    //creating a constructor to initialize objects and catch their errors
    Acey(String IpAddress, String IpPort) {
        try {
            socket = new Socket(IpAddress, Integer.parseInt(IpPort));
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException exp1) {
            exp1.printStackTrace();
        }
    }


    //create the write method to write our response to the dealer
	private void write(String s) throws IOException {
		dos.writeUTF(s);
		dos.flush();
	}



	//create the read method to read the data from the dealer
	private String read() throws IOException { 
		return dis.readUTF();
	}

    //create a method to get the value of the cards and return it
    int cardValue(String card) {
        int cardVal = 0;
        //check for numbers from 2 - 10
        if (Character.isDigit(card.charAt(0))) {
            // check for 10
            if (Character.isDigit(card.charAt(1))) {
                cardVal = 10;
            }
            //check for numbers from 2-9
            else {
                cardVal = Integer.parseInt(card.charAt(0) + "");
            }
        }
        //check for letter {j,q,k,a}
        else {
            switch (card.charAt(0)) {
                case ('J'):
                    cardVal = 11;
                    break;
                case ('Q'):
                    cardVal = 12;
                    break;
                case ('K'):
                    cardVal = 13;
                    break;
                case ('A'):
                    cardVal = 14;
                    break;
            }
        }
        return cardVal;
    }

    /*create a method to go through cards that have been dealt so far
     *count the cards that are in the middle of the edge cards
     *start checking cards from the sixth index of the commend that were converted to a list, because the dealt cards start form the sixth index
     *also we count the cards that are the same as the edges cards
     */
    int[] calculateDealtCardsMid(String[] myls, int len, int firstVal, int secondVal) {
        int mid = 0;
        int eachCardVal = 0;
        int all = len - 6;

        for (int i = 6; i < len - 2; i++) {
            eachCardVal = cardValue(myls[i]);
            if (eachCardVal < firstVal && eachCardVal > secondVal || eachCardVal < secondVal && eachCardVal > firstVal) {
                mid++;
            } else if (eachCardVal == firstVal || eachCardVal == secondVal) {
                same++;
            }

        }
        int[] num = new int[2];
        num[0] = mid;
        num[1] = all;

        return num;
    }

    /*create a method to go through cards that have been dealt so far
     *count the cards that are higher or lower then the edge card
     *start checking cards from the sixth index of the commend that were converted to a list, because the dealt status starts form the sixth index
     *also we count the number of cards that are the same as the cards
     */
    int[] calculateDealtCardsSam(String[] myls, int len, int cardVal) {
        int high = 0;
        int low = 0;
        int eachCardVal = 0;
        all = len - 6;
        for (int i = 6; i < len - 2; i++) {
            eachCardVal = cardValue(myls[i]);
            if (eachCardVal > cardVal) {
                high++;
            } else if (eachCardVal < cardVal) {
                low++;
            } else {
                same++;
            }

        }
        int[] count = new int[3];
        count[0] = low;
        count[1] = high;
        count[2] = all;
        System.out.println("low: " + count[0]);
        System.out.println("high: " + count[1]);
        System.out.println("all: " + count[2]);
        System.out.println("same: " + same);

        return count;

    }

    /*create a method to calculate the chance of getting a card between the edges
     *first we calculate the difference between two cards and subtract them by one in order not to include the edges
     *then multiply this number by 4 because there are 4 cards (4 suits) of each value in each deck and multiply it by 7 because there are 7 decks of cards in this game
     *calculate the cards between the edges by calling the "calculateDealtCardsMid" method and subtract the difference by the number of cards that have been dealt and are between the edges
     *then subtract the probability of getting a card between the edges by the number of whole cards in 7 decks minus the number of cards that have been dealt so far
     *finally multiply the total number by 100 to get the percentage
     */
    double advantaegMid(String[] myls, int len, int val1, int val2) {
        int difference = Math.abs(val2 - val1) - 1;
        int[] count = calculateDealtCardsMid(myls, len, val1, val2);
        int denum = (difference * 4 * 7) - count[0];
        double total = (double) denum / (364 - count[1]) * 100;
        return total;
    }

    /*create a method to calculate the chance of getting a card with higher or lower value if two cards are the same
     *first we calculate the difference between the card and edges, if the value is more than 8 then we subtract it by 2 to calculate the chances of getting a lower card, but if the value is more than 8 then we subtract 14 by the card value to calculate the chance of getting a card less than the card value
     *then multiply this number by 4 because there are 4 cards of each value in each deck(there are 4 suit in each deck) and multiply it by 7 because there are 7 decks of cards in this game
     *calculate the cards between the edges by calling the "calculateDealtCardsSam" method and subtract the difference by this the number that is returned form this method	, if we are calculating the chance of getting a higher value then we use the second value of the list that is returned by the method whose index is 1, which is the number of higher cards that have been dealt so far
     *but if we are calculating the chance of getting a lower value then we use the first value of the list that is returned by the method whose index is 0, which is the number of lower cards that have been dealt so far
     *then subtract the probability of getting a card less than or higher than the card we have by the number of whole cards in 7 decks
     *finally multiply the total number by 100 to get the percentage
     */

    double advantageSame(String[] myls, int len, int val1) {
        int[] count = calculateDealtCardsSam(myls, len, val1);
        if (val1 > 8) {//bet on low values
            int difference = Math.abs(val1 - 2);
            double chance = (double) ((difference * 4 * 7) - count[0]) / (364 - all);
            return chance * 100;
        } else if (val1 < 8) {//bet on high values
            int difference = Math.abs(14 - val1);
            double chance = (double) ((difference * 4 * 7) - count[1]) / (364 - all);
            return chance * 100;
        } else {// if the value of cards is 8
            return 50;
        }
    }

    /*
     * here we calculate the advantage of getting the same third card as the edges
     * if the edges are different then we have 28 cars that are the same as the first edge card and 28 cards that are the same as the second card. so we have 56 cards in general.
     * then we subtract then number of the same cards as the edges form 56 and then subtract this number by 2 because we don't want the edges to be included.
     */
    int advantageThirdMid() {
        return 56 - same - 2;
    }

    /*
     * here we calculate the advantage of getting the same third card as the edges
     * since the edges are the same, then we have 28 cards in 7 decks that have the same value, so we subtract 28 by the number of cards that have been dealt so far and are the same as the edges.
     *  then subtract this number by 2 because we don't want the edges to be included.
     */
    int advantageThirdSame() {
        return 28 - same - 2;
    }

    /*kelly fraction is a formula for calculating the amount of betting for card games
     * advantage is the chance of winning
     *q is the chance of losing
     *k is the percentage of your chips that you should bet
     */
    double kellyFraction(double advantage) {
        double q = 100 - advantage;
        double k = advantage - q;
        return k;
    }


    /*
     * in this method we calculate the amount of betting if the edges are not equal
     * if the number of cards that have been dealt so far were less than the half which is 28 but i have decided to choose 27 to reduce the risk) then it is risky and i have decided to divide the amount of betting by 3, because if I get the same card as one of the edges, then i will lose two times of the amount that i have bet. (I should have divide the betting amount by 2 but i divided by 3 to reduce the risk)
     * also if the number of cards that have been dealt so far were more than the half which is kind of safer, i have decided to divide the amount of betting by 2, because i don't want to bet too much and i want to stay longer.
     */
    int betMid(int advantage, int my, int kelly) {
        int bets = 0;
        if (advantage > 27) {
            //divide by two because of the third card was equal to one of them then twice the bet will be taken
            bets = (my * kelly / 100) / 3;
        } else if (advantage <= 27) {
            bets = (my * kelly / 100) / 2;
        }

        return bets;
    }

    /*
     * in this method we calculate the amount of betting if the edges are equal
     * if the number of cards that have been dealt so far were less than the half which is 14 (but i have decided to choose 13 to reduce the risk) then it is risky and i have decided to divide the amount of betting by 4, because if I get the same card as one of the edges, then i will lose three times of the amount that i have bet.(I should have divide the betting amount by 3 but i divided by 4 to reduce the risk)
     * also if the number of cards that have been dealt so far were more than the half which is kind of safer, i have decided to divide the amount of betting by 2, because i don't want to bet too much and i want to stay longer.
     */
    int betSame(int advantage, int my, int kelly) {
        int bets = 0;
        if (advantage > 13) {
            bets = (my * kelly / 100) / 4;
        } else if (advantage <= 13) {
            bets = (my * kelly / 100) / 2;
        }
        return bets;
    }

    /*
     * this method is for writting the respons to the dealer if two cards were the same
     * if the cards were less than the half(8), then i will bet of the higher values
     * else i will bet on the lower values (include 8)
     */
    String writtingSame(int firstCard) {
        String write = "";
        if (firstCard < 8) {
            write = "high:";
        } else {
            write = "low:";
        }
        return write;
    }
    /* in this method we read the commends from the dealer
     *create try catch statement to handle IO exceptions that might happen
     *as long as the commends does not start with "done" we read the commend every time
     *we read commends by the read method from the dealer and turn it to a list by separate it by the colons and then check for the first word
     *if the first word is login then we login by the GitHub account and choose an avatar name and write our response to the dealer by the write method
     *if the first word is "play" then we print it out which shoes the player chips, pot chips, two cards that are dealt for the player and cards that have been dealt so far
     *then convert the pot chips and the player chips form string to integers and get the value of cards by calling the "cardValue" method
     *then we check if two cards are equal or not
     *then call the functions to calculate the percentage of our money that we should bet and then write our response to the dealer
     */

    void plays() throws IOException {
        String answer = "";
        try {

            String commend = "";
            while (!commend.startsWith("done")) {
                commend = read();
                String[] myls = commend.split(":");
                answer = myls[0];
                if (answer.equals("login")) {
                    String ID = "paria03";
                    String Avatar = "Star";
                    String login = ID + ":" + Avatar;
                    write(login);
                } else if (answer.equals("play")) {
                    System.out.println(commend);
                    int potChips = Integer.parseInt(myls[1]);
                    int myChips = Integer.parseInt(myls[2]);
                    String firstCard = myls[3];
                    String secondCard = myls[4];
                    int len = (myls.length);
                    int firstValue = cardValue(firstCard);
                    int secondValue = cardValue(secondCard);
                    //if cards are not equal:
                    if (potChips != 0 && myChips != 0) {

                        if (firstValue != secondValue) {
                            int advantage = (int) advantaegMid(myls, len, firstValue, secondValue);
                            int kelly = (int) kellyFraction(advantage);

                            int aTM = advantageThirdMid();
                            int bets = betMid(aTM, myChips, kelly);

                            //we cannot bet more than the pot chips so we should bet the whole pot 
                            if (bets > potChips) {
                                bets = potChips;
                                write("mid:" + bets);
                            }
                            //if the bet amount was negative then bet zero
                            else if (bets <= 0) {
                                write("mid:0");
                            }
                            //else write the bet amount to the dealer
                            else {
                                write("mid:" + bets);
                            }
                        }
                        //if two cards are equal
                        else {
                            int ad = (int) advantageSame(myls, len, firstValue);
                            int kelly = (int) kellyFraction(ad);

                            int aTS = advantageThirdSame();
                            int bets = betSame(aTS, myChips, kelly);
                            String write = writtingSame(firstValue);

                            //we cannot bet more than the pot chips so we should bet the whole pot 
                            if (bets > potChips) {
                                bets = potChips;
                                write(write + bets);
                                //then we check for the value of cards to determine whether bet on high values or low values
                                //if the value is less than 8 then we bet on high values
                            }
                            //if we the amount of betting is less than pot chips and if it is negative then we bet zero
                            else if (bets <= 0) {
                                write("low:0");
                            }
                            //else if the betting value is positive and less than the pot value then we write the calculated amount of the bet
                            else {
                                write(write + bets);
                            }
                        }
                    }

                }

                //if the first word of the commend is "status" then print the whole commend to get the status and understand whether we lost or won
                else if (answer.equals("status")) {
                    System.out.println(commend);
                }
            }
            //when the first word of the commend is "done" then it will terminate the while loop and print the reason that the game is over for the player and close the socket
            if (answer.equals("done")) {
                System.out.println(commend);
                Socket socket = new Socket();
                socket.close();
            }
        }
        //catch IO exception errors and print them for the user
        catch (IOException exp1) {
            exp1.printStackTrace();
        }
    }

    /*
     * in this method we start the program by providing ip address and port address
     * we create an object of type Acey class in order to use its methods and variables
     * and if there were an IO exception error then we will catch it and show it to the user
     */
	public static void main(String[] args) {
		try {
			Acey thePlayer = new Acey(args[0], args[1]);
			thePlayer.plays();
		}
		catch (IOException ioe) {
			System.err.println("ERROR: got IO exception");
			ioe.printStackTrace();
		}
	}
//    public static void main(String[] args){
//        if (args.length == 0) { // Test Mode
//            try {
//                Acey thePlayer = new Acey("src/tests.txt");
//                thePlayer.plays();
//                return;
//            } catch (IOException ioe) {
//                ioe.printStackTrace();
//                return;
//            }
//        }else {
//
//            try {
//                Acey thePlayer = new Acey(args[0], args[1]);
//                thePlayer.plays();
//            } catch (IOException ioe) {
//                System.err.println("ERROR: got IO exception");
//                ioe.printStackTrace();
//            }
//        }
//    }
}