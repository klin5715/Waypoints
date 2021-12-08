package com.klin.waypoints;

import org.bukkit.Location;

import java.util.Set;

public class Traveler {
    public Set<Integer> indexes;
    public String charge;
    public int points;

    public Location home;
    public Location camp;

    public boolean isTeleporting;

    public Traveler(Set<Integer> indexes, String charge, int points){
        this.indexes = indexes;
        this.charge = charge;
        this.points = points;

        this.isTeleporting = false;
    }
}
