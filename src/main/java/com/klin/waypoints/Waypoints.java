package com.klin.waypoints;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Waypoints extends JavaPlugin {
    private static Waypoints instance;

    @Override
    public void onEnable(){
        instance = this;
        Config.setUp("config");
        Config.setUp("waypoints");
        Config.setUp("travelers");

        Manager manager = new Manager();
        getServer().getPluginManager().registerEvents(manager, this);
        getCommand("waypoints").setExecutor(manager);

        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Waypoints [ON]");
    }

    @Override
    public void onDisable(){
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "Waypoints [OFF]");
    }

    public static Waypoints getInstance() {
        return instance;
    }
}
