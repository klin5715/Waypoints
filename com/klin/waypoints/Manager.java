package com.klin.waypoints;

import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Manager implements Listener, CommandExecutor {
    public static FileConfiguration waypointsYML;
    public static FileConfiguration travelersYML;
    
    public static Map<Player, Traveler> travelers;
    public static Map<Integer, Waypoint> waypoints;

    public static int requirement;
    public static Set<GameMode> quickTravel;

    private final static NamespacedKey key =
            new NamespacedKey(Waypoints.getInstance(), "waypoints");
    private final static NamespacedKey name =
            new NamespacedKey(Waypoints.getInstance(), "name");

    private final static Set<Material> banners = new HashSet<Material>(){{
        add(Material.BLACK_BANNER);
        add(Material.BLUE_BANNER);
        add(Material.BROWN_BANNER);
        add(Material.CYAN_BANNER);
        add(Material.GRAY_BANNER);
        add(Material.GREEN_BANNER);
        add(Material.LIGHT_BLUE_BANNER);
        add(Material.LIGHT_GRAY_BANNER);
        add(Material.LIME_BANNER);
        add(Material.MAGENTA_BANNER);
        add(Material.ORANGE_BANNER);
        add(Material.PINK_BANNER);
        add(Material.PURPLE_BANNER);
        add(Material.RED_BANNER);
        add(Material.WHITE_BANNER);
        add(Material.YELLOW_BANNER);
    }};

    public Manager(){
        FileConfiguration configYML = Config.get("config");
        waypointsYML = Config.get("waypoints");
        travelersYML = Config.get("travelers");
        
        travelers = new HashMap<>();
        ConfigurationSection section = waypointsYML.getConfigurationSection("");
        waypoints = new HashMap<>();
        for (String key : section.getKeys(false)) {
            String[] cords = waypointsYML.getString(key+".location").split(" ");
            World world = Bukkit.getWorld(cords[3]);
            Location loc = new Location(
                    world,
                    Double.parseDouble(cords[0]),
                    Double.parseDouble(cords[1]),
                    Double.parseDouble(cords[2]));

            String tributes = waypointsYML.getString(key+".contributors");
            Set<UUID> contributors = new HashSet<>();
            if(tributes!=null && !tributes.isEmpty()) {
                for (String tribute : tributes.split(" "))
                    contributors.add(UUID.fromString(tribute));
            }

            if(!world.getChunkAt(loc).isLoaded())
                world.getChunkAt(loc).load();
            Block block = world.getBlockAt(loc);
            if(!(block.getState() instanceof Banner))
                continue;
            Banner banner = (Banner) block.getState();

            int index = Integer.parseInt(key);
            waypoints.put(index, new Waypoint(
                    index, waypointsYML.getString(key+".name"), loc, contributors,
                    waypointsYML.getString(key+".isComplete").equals("1"), banner));
        }

        String required = configYML.getString("requirement");
        if(required!=null)
            requirement = Integer.parseInt(required);
        else
            requirement = 8;

       quickTravel = new HashSet<>();
        String gameModes = configYML.getString("quickTravel");
        if(gameModes!=null) {
            for (String gameMode : gameModes.split(" "))
                quickTravel.add(GameMode.valueOf(gameMode));
        }
    }

    @EventHandler
    public static void playerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        if(travelers.containsKey(player))
            return;

        String uuid = player.getUniqueId().toString();
        Set<Integer> indexes = new HashSet<>();
        if(travelersYML.getString(uuid)==null) {
            travelersYML.set(uuid + ".waypoints", "");

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy MM dd HH mm");
            LocalDateTime ldt = LocalDateTime.now();
            String charge = dtf.format(ldt);
            charge = charge.substring(0, 8) + (Integer.parseInt(charge.substring(8, 10)) - 0.25) + charge.substring(10);
            travelersYML.set(uuid + ".charge", charge);
            travelersYML.set(uuid + ".points", "1");
            travelersYML.set(uuid + ".home", "");
            travelersYML.set(uuid + ".camp", "");

            Config.save("travelers");
            travelers.put(player, new Traveler(indexes, charge, 1));
            return;
        }

        String unlocked = travelersYML.getString(uuid + ".waypoints");
        if (unlocked != null && !unlocked.isEmpty()) {
            for (String index : unlocked.split(" "))
                indexes.add(Integer.parseInt(index));
        }
        travelers.put(player, new Traveler(indexes,
                travelersYML.getString(uuid + ".charge"),
                Integer.parseInt(travelersYML.getString(uuid + ".points"))));
    }

    public static Traveler get(Player player){
        return travelers.get(player);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player) || !cmd.getName().equals("waypoint"))
            return true;
        Player player = (Player) sender;

        switch (args.length>0 ? args[0] : "") {
            case "":
                Traveler traveler = travelers.get(player);
                if(traveler==null)
                    return true;
                if(traveler.isTeleporting){
                    player.sendMessage("Finish teleporting first");
                    return true;
                }

                Inventory menu = Bukkit.createInventory(null, (traveler.indexes.size()/9+1)*9);
                for(Integer index : traveler.indexes)
                    menu.addItem(waypoints.get(index).shield);
                player.openInventory(menu);
                return true;

            case "help":

            //converts either elytra distance or time to points
            case "redeem":
        }
        return true;
    }

    @EventHandler
    public static void travel(InventoryClickEvent event){
        if(event.isCancelled())
            return;
        InventoryView view = event.getView();
        Inventory top = view.getTopInventory();
        if(top.getHolder()!=null || !view.getTitle().equals("Waypoints"))
            return;
        ItemStack item = event.getCurrentItem();
        if(item==null)
            return;
        event.setCancelled(true);

        for(Waypoint waypoint : waypoints.values()){
            if(waypoint.shield.equals(item))
                teleport((Player) event.getWhoClicked(), waypoint.loc, "Welcome to "+waypoint.name);
        }
    }

    @EventHandler
    public static void contribute(PlayerInteractEvent event){
        Block block = event.getClickedBlock();
        if (block == null ||
                event.getAction() != Action.RIGHT_CLICK_BLOCK ||
                event.getHand() != EquipmentSlot.HAND ||
                !banners.contains(block.getType()))
            return;

        Banner banner = (Banner) block.getState();
        PersistentDataContainer container = banner.getPersistentDataContainer();
        Integer index = container.get(key, PersistentDataType.INTEGER);
        Player player = event.getPlayer();
        Waypoint waypoint;
        if(index==null) {
            if(travelers.get(player).points<=0){
                player.sendMessage("No points");
                return;
            }
            waypoint = newWaypoint(container.get(name, PersistentDataType.STRING), banner);
            banner.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, waypoint.index);
            if(waypoint.name!=null)
                banner.getPersistentDataContainer().set(name, PersistentDataType.STRING, waypoint.name);
            banner.update();

            new BukkitRunnable() {
                public void run() {
                    addContributor(waypoint, player);
                }
            }.runTask(Waypoints.getInstance());
            return;
        }
        else
            waypoint = waypoints.get(index);

        if(waypoint.contributors.contains(player.getUniqueId())) {
            //change pattern instead of refunding if player is holding a banner
            ItemStack item = event.getItem();
            if(item!=null && banners.contains(item.getType())){
                BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
                Banner update = (Banner) meta.getBlockState();
                waypoint.banner.setPatterns(update.getPatterns());
                waypoint.banner.update();
                return;
            }

            refundContributor(waypoint, player);
        }
        else {
            if(travelers.get(player).points<=0){
                player.sendMessage("No points");
                return;
            }
            addContributor(waypoint, player);
        }
    }

    @EventHandler
    public static void name(BlockPlaceEvent event){
        if(event.isCancelled())
            return;
        Block block = event.getBlockPlaced();
        if (!banners.contains(block.getType()))
            return;
        ItemStack item = event.getItemInHand();
        if(item.getItemMeta()!=null) {
            Banner banner = (Banner) block.getState();
            banner.getPersistentDataContainer().
                    set(name, PersistentDataType.STRING, item.getItemMeta().getDisplayName());
            banner.update();

            new BukkitRunnable(){
                public void run(){
                    if(banner.getPersistentDataContainer().get(key, PersistentDataType.INTEGER)==null) {
                        banner.getPersistentDataContainer().remove(name);
                        banner.update();
                    }
                }
            }.runTaskLater(Waypoints.getInstance(), 100);
        }
    }

    private static Waypoint newWaypoint(String name, Banner banner){
        int index = waypoints.size();
        Location loc = banner.getLocation().add(0.5, 0.5, 0.5);
        Waypoint waypoint = new Waypoint(
                index, name, loc, new HashSet<>(), false, banner);
        waypoints.put(index, waypoint);

        ArmorStand stand = banner.getWorld().spawn(loc.clone().add(0, 1.1, 0), ArmorStand.class);
        stand.setMarker(true);
        stand.setCustomName(name + " 0/"+ requirement);
        stand.setInvisible(true);
        stand.setGravity(false);
        stand.setBasePlate(false);
        stand.setCustomNameVisible(true);

        waypointsYML.set(index + ".name", name);
        waypointsYML.set(index + ".location", loc.getX() +" "+ loc.getY() +" "+ loc.getZ() +" "+ loc.getWorld().getName());
        waypointsYML.set(index + ".contributors", "");
        waypointsYML.set(index + ".isComplete", "0");

        Config.save("waypoints");
        return waypoint;
    }

    private static void refundContributor(Waypoint waypoint, Player player){
        UUID uuid = player.getUniqueId();
        waypoint.contributors.remove(uuid);
        String tributes = "";
        for(UUID tribute : waypoint.contributors)
            tributes += tribute.toString();
        waypointsYML.set(waypoint.index+".contributors", tributes);
        Config.save("waypoints");

        get(player).points += 1;
        travelersYML.set(uuid.toString()+".points",
                Integer.parseInt(travelersYML.getString(uuid.toString()+".points"))+1);
        Config.save("travelers");

        ArmorStand stand = (ArmorStand) player.getWorld().getNearbyEntities(
                waypoint.loc.clone().add(0, 1.1, 0), 0.1, 0.1, 0.1).iterator().next();
        if(waypoint.contributors.size()==requirement) {
            stand.setCustomName("§6" + waypoint.name);
            player.sendMessage("The world opens itself before those with noble hearts");
        }
        else
            stand.setCustomName(waypoint.name + " " + waypoint.contributors.size() + "/" + requirement);

        player.sendMessage("Refunded");
    }

    private static void addContributor(Waypoint waypoint, Player player){
        UUID uuid = player.getUniqueId();
        waypoint.contributors.add(uuid);
        String tributes = "";
        for(UUID tribute : waypoint.contributors)
            tributes += tribute.toString();
        waypointsYML.set(waypoint.index+".contributors", tributes);
        Config.save("waypoints");

        get(player).points -= 1;
        travelersYML.set(uuid.toString()+".points",
                Integer.parseInt(travelersYML.getString(uuid.toString()+".points"))-1);
        Config.save("travelers");

        ArmorStand stand = (ArmorStand) player.getWorld().getNearbyEntities(
                waypoint.loc.clone().add(0, 1.1, 0), 0.1, 0.1, 0.1).iterator().next();
        stand.setCustomName(waypoint.name + " " + waypoint.contributors.size() + "/" + requirement);

        player.sendMessage("Contributed");
    }

    private static void teleport(Player player, Location loc, String message){
        if(quickTravel.contains(player.getGameMode())){
            transport(player, loc, message);
            return;
        }

        Traveler traveler = get(player);
        traveler.isTeleporting = true;

        BossBar bossbar = Bukkit.createBossBar(key, "teleporting. . .",
                BarColor.GREEN, BarStyle.SEGMENTED_20);
        bossbar.addPlayer(player);
        bossbar.setProgress(1);

        final double health = player.getHealth();
        final Location location = player.getLocation();

        new Task(0, 7) {
            @Override
            public void run() {
                if (player.getHealth() < health || !player.getLocation().equals(location)) {
                    bossbar.removePlayer(player);
                    Bukkit.removeBossBar(key);
                    traveler.isTeleporting = false;

                    player.sendMessage("Cancelling teleportation");
                    cancel();
                    return;
                }

                if (bossbar.getProgress() > 0.05)
                    bossbar.setProgress(bossbar.getProgress()-0.05);
                else {
                    bossbar.removePlayer(player);
                    Bukkit.removeBossBar(key);
                    traveler.isTeleporting = false;

                    transport(player, loc, message);
                    cancel();
                }
            }
        };
    }

    private static void transport(Player player, Location loc, String message){
        Location playerLoc = player.getLocation();
        loc.add(0.5, 0, 0.5);
        loc.setYaw(playerLoc.getYaw());
        loc.setPitch(playerLoc.getPitch());
        player.teleport(loc);
        player.sendMessage(message);
    }
}