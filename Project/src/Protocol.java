import java.net.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import  java.security.NoSuchAlgorithmException;

/**
 * Protocol and authentication written by Y-Uyen on 11/20/16
 */

public class Protocol {
    private static final int WAITING = 0;
    private static final int SENTUSERNAME = 1;
    private static final int SENTPASSWORD = 2;


    private int state = WAITING;
    private boolean found = false;       

    public Hash hash = new Hash();                          
    public String[] login;

    public String processInput(String theInput) {
        String checkUser[] = new String[1];     
   
        String theOutput = null;

        if (state == WAITING) {                                 
            theOutput = "Please enter your username:";
            state = SENTUSERNAME;
        } else if (state == SENTUSERNAME) {   
            try {
                String line;  
                BufferedReader reader = new BufferedReader(new FileReader("login.txt")); 
                while ((line = reader.readLine()) != null) {
                    //split the user and pw just so you can check the username
                    checkUser = line.split(",");
                    if(checkUser[0].equals(theInput)) {             //if username matches, store user and pw into a an array of size 2
                        login = line.split(",");
                        found = true;
                        break;
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(found) {
                theOutput = "Please enter your password.";
                state = SENTPASSWORD;
            } else {
                theOutput = "Invalid username. Please try again!";
                state = SENTUSERNAME;
            }       
        } else if (state == SENTPASSWORD) {  
            try {
                //create the salt
                String salt = "sodium";
                byte[] salted = salt.getBytes();                   
                
                //salt and hash pw the user entered
                String enteredPw = theInput;
                byte[] byteEnteredPw = enteredPw.getBytes();
                byte[] enteredHashedPw = hash.generatePasswordHash(salted, byteEnteredPw);

                //get corresponding pw from login.txt and then salt and hash it
                String password = login[1];                   
                byte[] bytePw = password.getBytes();
                byte[] hashedPw = hash.generatePasswordHash(salted, bytePw);

                //now check to see if the passwords match
                if (hash.compareHashes(hashedPw, enteredHashedPw)) {
                    theOutput = "You've been authenticated! Good bye!";     
                    state = WAITING;
                } else {
                    theOutput = "Invalid password. Please try again!";
                    state = SENTPASSWORD;
                }
            }
            catch (NoSuchAlgorithmException e) {

            }
            
        }
        return theOutput;
    }
}