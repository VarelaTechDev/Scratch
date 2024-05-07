package org.example.util;

import java.security.SecureRandom;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SecretKeyGenerator {

    public static SecretKey generateSecretKey() {
        // Define the length of the secret key in bytes
        int keyLength = 32; // You can adjust this length as needed for your security requirements

        // Generate a secure random byte array
        byte[] keyBytes = new byte[keyLength];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(keyBytes);

        // Create a SecretKeySpec from the random bytes
        return new SecretKeySpec(keyBytes, "AES"); // Using AES algorithm for key generation
    }

    public static void main(String[] args) {
        SecretKey secretKey = generateSecretKey();
        byte[] encodedKey = secretKey.getEncoded();
        System.out.println("Generated Secret Key: " + bytesToHex(encodedKey));
    }

    // Helper method to convert byte array to hex string
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
