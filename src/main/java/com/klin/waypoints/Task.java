package com.klin.waypoints;

import com.klin.waypoints.Waypoints;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class Task implements Runnable{
    private final int taskId;

    public Task(int arg1, int arg2) {
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Waypoints.getInstance(), this, arg1, arg2);
    }

    public void cancel(){
        Bukkit.getScheduler().cancelTask(taskId);
    }
}
