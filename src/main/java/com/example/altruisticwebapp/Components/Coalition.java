package com.example.altruisticwebapp.Components;

import java.util.HashSet;

public class Coalition extends HashSet<Player> {

    private String name;
    private final int scope = 10000000;
    double id;

    public Coalition (){
        this.id=scope*Math.random();
    }
    public Coalition (String name){
        this.name = name;
        this.id=scope*Math.random();
    }

    public void setName(String name){
        this.name = name;
    }



    public void addPlayerSet(PlayerSet ps){
        for (int i = 0; i < ps.size(); i++){
            this.add(ps.get(i));
        }
    }

    public String getName(){
        return name;
    }
    public int numberOfFriends(Player p, NetworkOfFriends nw)  {
        int friends = 0;
        for (Player q : this){
            if (p.areFriends(q, nw)) {
                friends++;
            }
        }
        return friends;
    }

    public int numberOfEnemies(Player p, NetworkOfFriends nw) {
        int enemies = 0;
        for (Player q : this){
            if (p.areEnemies(q, nw)) enemies++;
        }
        return enemies;
    }

    public int value(Player p, NetworkOfFriends nw) {
        return nw.getSize() * numberOfFriends(p, nw) - numberOfEnemies(p, nw);
    }

    public Coalition duplicate(){
        Coalition a = new Coalition();
        a.addAll(this);
        return a;
    }

    public double avg(Player p, NetworkOfFriends nw){ //average friend oriented valuation of p's friends in coalition

        /*
        avg = {sum of all friends a of i in coalition} value that a gives coalition / amount of friends in coalition
         */

        double sum = 0;
        for (Player q : this){
            if (p.areFriends(q, nw)){
                sum = sum + this.value(q, nw);
            }
        }

        return sum / numberOfFriends(p, nw);
    }

    public double avgPlus(Player p, NetworkOfFriends nw){ //average friend oriented valuation of p AND p's friends in coalition
        double sum = this.value(p, nw);
        int numberOfFriends = 0;
        for (Player q : this){
            if (p.areFriends(q, nw)){
                if (this.contains(q)){
                    numberOfFriends++;
                    sum = sum + this.value(q, nw);
                }
            }
        }
        sum = sum / (numberOfFriends + 1);
        return sum;
    }

    public double min(Player p, NetworkOfFriends nw){
        double minVal = 1000;
        for (Player q : this){
            if (p.areFriends(q, nw)) {
                int val = value(q, nw);
                if (val < minVal){
                    minVal = val;
                }
            }
        }
        return minVal;
    }

    public double minPlus(Player p, NetworkOfFriends nw){
        double minVal = min(p, nw);
        if (value(p, nw) < minVal) minVal = value(p, nw);
        return minVal;
    }
}
