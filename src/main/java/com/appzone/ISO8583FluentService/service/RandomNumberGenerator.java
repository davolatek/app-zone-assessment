package com.appzone.ISO8583FluentService.service;

import java.util.Random;

public class RandomNumberGenerator {

    private Random random;

    public RandomNumberGenerator() {
        this.random = new Random();
    }

    public String generateRandomInteger(int min, int max) {
        int randomNumber =  random.nextInt(max - min + 1) + min;
        return String.valueOf(randomNumber);
    }
}
