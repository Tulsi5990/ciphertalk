package com.av.avmessenger;

// Import necessary classes for cryptographic operations

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

// This class provides utility methods for AES encryption and decryption
public class AESHelper {
    // Constants used for AES encryption
    private static final String AES = "AES"; // Algorithm name: Advanced Encryption Standard
    private static final String AES_MODE = "AES/CBC/PKCS5Padding"; // Encryption mode: CBC with PKCS5 padding
    private static final int KEY_SIZE = 256; // Key size in bits for AES-256 encryption

    // Method to generate a new random AES secret key
    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(AES); // Create a KeyGenerator for AES
        keyGenerator.init(KEY_SIZE, new SecureRandom()); // Initialize the KeyGenerator with a key size and a secure random number generator
        return keyGenerator.generateKey(); // Generate and return the secret key
    }

    // Method to generate a random initialization vector (IV) for encryption
    public static byte[] generateIV() {
        byte[] iv = new byte[16]; // Create a byte array of size 16 (AES block size)
        new SecureRandom().nextBytes(iv); // Fill the array with random bytes
        return iv; // Return the generated IV
    }

    // Method to create a SecretKey object from raw key bytes
    public static SecretKey getSecretKey(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, "AES"); // Create a SecretKeySpec for AES using the provided byte array
    }

    // Method to encrypt a plaintext string using AES with a secret key and an IV
    public static String encrypt(String plainText, SecretKey key, byte[] iv) throws Exception {
        // Create a SecretKeySpec from the provided secret key
        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), AES);

        // Get a Cipher instance for the specified AES mode
        Cipher cipher = Cipher.getInstance(AES_MODE);

        // Create an IvParameterSpec using the provided IV
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Initialize the Cipher in ENCRYPT_MODE with the secret key and IV
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        // Encrypt the plaintext and obtain the encrypted bytes
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());

        // Encode the encrypted bytes to a Base64 string for easy storage and transmission
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Method to decrypt a Base64-encoded ciphertext using AES with a secret key and an IV
    public static String decrypt(String encryptedText, SecretKey key, byte[] iv) throws Exception {
        // Create a SecretKeySpec from the provided secret key
        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), AES);

        // Get a Cipher instance for the specified AES mode
        Cipher cipher = Cipher.getInstance(AES_MODE);

        // Create an IvParameterSpec using the provided IV
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Initialize the Cipher in DECRYPT_MODE with the secret key and IV
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        // Decode the Base64-encoded ciphertext into raw encrypted bytes
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);

        // Decrypt the encrypted bytes to obtain the original plaintext
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);

        // Convert the decrypted bytes to a string and return the plaintext
        return new String(decryptedBytes);
    }
}
