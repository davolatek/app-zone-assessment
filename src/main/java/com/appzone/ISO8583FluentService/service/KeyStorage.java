package com.appzone.ISO8583FluentService.service;

import java.util.concurrent.ConcurrentHashMap;

public class KeyStorage {

    // Thread-safe map to store the keys in memory
    private static final ConcurrentHashMap<String, String> keyStore = new ConcurrentHashMap<>();

    // Method to store the key
    public static void storeKey(String identifier, String encryptedKey) {
        keyStore.put(identifier, encryptedKey);
    }

    // Method to retrieve the key
    public static String retrieveKey(String identifier) {
        return keyStore.get(identifier);
    }

    // Example usage
    public static void main(String[] args) {
        // Storing the key
        storeKey("transaction-123", "DE53EncryptedKeyExample");

        // Retrieving the key later
        String key = retrieveKey("transaction-123");
        System.out.println("Retrieved Key: " + key);
    }
}

