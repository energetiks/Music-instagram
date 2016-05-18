/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.bystrov.musicinstagram.cryptography;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 *
 * @author defesteban
 */
public class PasswordHash {
    
    public String getSecurePassword(String passwordToHash, String salt) {
       String generatedPassword = null;
       try {
           // Create MessageDigest instance for MD5
           MessageDigest md = MessageDigest.getInstance("SHA-256");
           //Add password bytes to digest
           md.update(salt.getBytes());
           //Get the hash's bytes
           byte[] bytes = md.digest(passwordToHash.getBytes());
           //This bytes[] has bytes in decimal format;
           //Convert it to hexadecimal format
           StringBuilder sb = new StringBuilder();
           for (byte aByte : bytes) {
               sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
           }
           //Get complete hashed password in hex format
           generatedPassword = sb.toString();
       }
       catch (NoSuchAlgorithmException e) {
           e.printStackTrace();
       }
       return generatedPassword;
   }

   public String getSalt() throws NoSuchAlgorithmException
   {
       //Always use a SecureRandom generator
       SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
       //Create array for salt
       byte[] salt = new byte[16];
       //Get a random salt
       sr.nextBytes(salt);
       //return salt
       return salt.toString();
   }
}
