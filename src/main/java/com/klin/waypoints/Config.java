package com.klin.waypoints;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Config {
    private static File configFile;
    private static FileConfiguration config;
    private static File waypointsFile;
    private static FileConfiguration waypoints;
    private static File travelersFile;
    private static FileConfiguration travelers;

    public static void save(String name){
        try {
            switch (name) {
                case "config":
                    config.save(configFile);
                    break;
                case "waypoints":
                    waypoints.save(waypointsFile);
                    break;
                case "travelers":
                    travelers.save(travelersFile);
                    break;
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void setUp(String name) {
        switch(name) {
            case "config":
                configFile = new File(Waypoints.getInstance().getDataFolder(), name+".yml");
                config = new YamlConfiguration();
                setUpProcedure(configFile, config);

                config.set("required", "8");
                config.set("quickTravel", "CREATIVE SPECTATOR");
                Config.save("config");
                break;
            case "waypoints":
                waypointsFile = new File(Waypoints.getInstance().getDataFolder(), name+".yml");
                waypoints = new YamlConfiguration();
                setUpProcedure(waypointsFile, waypoints);
                break;
            case "travelers":
                travelersFile = new File(Waypoints.getInstance().getDataFolder(), name+".yml");
                travelers = new YamlConfiguration();
                setUpProcedure(travelersFile, travelers);
                break;
        }
    }

    private static void setUpProcedure(File file, FileConfiguration fileConfig){
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
        try {
            fileConfig.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static FileConfiguration get(String name) {
        switch (name) {
            case "config":
                return config;
            case "waypoints":
                return waypoints;
            case "travelers":
                return travelers;
            default:
                return null;
        }
    }
}
