package com.jokes;

import java.util.Random;

public class JokesManager {

    private String [] jokeList;

    public JokesManager(){

        jokeList = new String[]{
            "why does irish dogs have flat faces? because of her chasing parked cars!"
            ," Where do you find a cow with no legs? Right where you left it."
            ,"Have I told you this deja vu joke before? "
            ,"What do you call a magic dog? A Labracadabrador"
            ,"I used to be addicted to soap, but I'm clean now"
            ,"My grandad has the heart of a lion and a life time ban from the San Diego Zoo"
            ,"What's orange and sounds like a parrot? A carrot"
            ,"This is your captain speaking, AND THIS IS YOUR CAPTAIN SHOUTING"
            ,"A Buddhist walks up to a hotdog stand and says, Make me one with everything"
        };
    }

    public String getAJoke(){
        return jokeList[getRandomNumber()];
    }

    private int getRandomNumber(){
        Random random = new Random();
        long range = jokeList.length - 1;
        long fraction = (long)(range * random.nextDouble());
        return (int)(fraction +1);
    }
}
