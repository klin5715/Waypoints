package com.klin.waypoints;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.Set;
import java.util.UUID;

public class Waypoint {
    public int index;
    public String name;
    public Location loc;
    public Set<UUID> contributors;
    public boolean isComplete;

    public Banner banner;
    public ItemStack shield;

    public Waypoint(int index, String name, Location loc, Set<UUID> contributors, boolean isComplete, Banner banner){
        this.index = index;
        this.name = name;
        this.loc = loc;
        this.contributors = contributors;
        this.isComplete = isComplete;

        this.banner = banner;
        ItemStack shield = new ItemStack(Material.SHIELD);
        BlockStateMeta meta = (BlockStateMeta) shield.getItemMeta();
        meta.setBlockState(banner);
        shield.setItemMeta(meta);
        this.shield = shield;
    }
}
