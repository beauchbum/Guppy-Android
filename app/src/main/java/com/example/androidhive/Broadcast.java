package com.example.androidhive;

/**
 * Created by Jake on 4/26/2016.
 */
public class Broadcast {

    public String username;
    public int listeners;
    public String song;
    public String artist;
    public String image_title;

    public Broadcast(String n, int l, String s, String a, String i)
    {
        this.username = n;
        this.listeners = l;
        this.song = s;
        this.artist = a;
        this.image_title = i;
    }

}
