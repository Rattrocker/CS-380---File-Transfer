import java.net.*;
import java.io.*;

/**
 * Written by Y-Uyen on 11/20/16
 */

public class Protocol {
    private static final int WAITING = 0;
    private static final int SENTUSERNAME = 1;
    private static final int SENTPASSWORD = 2;


    private int state = WAITING;

    private String username = "yuyen";                      //feel free to change the username and pw to whatever when testing!
    private String password = "wutwut";

    private byte[] bytePw = password.getBytes();            //converted pw to byte array in order to use it with the Hash class written by Zach

    public Hash hash = new Hash();                          //creates Hash object


    public String processInput(String theInput) {
        byte[] hashedPw = hash.generateCheckSum(bytePw);       //creates the checksum for the pw that is stored 
   
        String theOutput = null;

        if (state == WAITING) {                                 //this is the first message that gets print out from the server to the client.
            theOutput = "Please enter your username:";
            state = SENTUSERNAME;
        } else if (state == SENTUSERNAME) {                     //this else if block just keeps checking to see if the username is valid
            if (theInput.equals("yuyen")) {
                theOutput = "Please enter your password.";
                state = SENTPASSWORD;
            } else {
                theOutput = "Invalid username. Please try again!";
                state = SENTUSERNAME;
            }       
        } else if (state == SENTPASSWORD) {                     //this else if block just keeps checking to see if the password is valid
            String enteredPw = theInput;
            theOutput = theInput + "password woo";
            byte[] byteEnteredPw = enteredPw.getBytes();
            byte[] enteredHashedPw = hash.generateCheckSum(byteEnteredPw);


            if (hash.compareHashes(hashedPw, enteredHashedPw)) {
                theOutput = "You've been authenticated! Good bye!";     //if valid, it sends out this message and then the client and server close the connection
                state = WAITING;
            } else {
                theOutput = "Invalid password. Please try again!";
                state = SENTPASSWORD;
            }
        }
        return theOutput;
    }
}