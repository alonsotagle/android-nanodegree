package com.leonelmendez.jokes.backend.model;

/**
 * Created by leonelmendez on 08/07/15.
 */
public class Joke {

    private String joker;
    private String joke;
    private String country;

    public String getJoker() {
        return joker;
    }

    public void setJoker(String joker) {
        this.joker = joker;
    }

    public String getJoke() {
        return joke;
    }

    public void setJoke(String joke) {
        this.joke = joke;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
