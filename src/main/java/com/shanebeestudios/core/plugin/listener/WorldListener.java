package com.shanebeestudios.core.plugin.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldListener implements Listener {

    public WorldListener() {
        onLoad();
    }

    private void onLoad() {
        Bukkit.getWorlds().forEach(this::worldGamerules);
    }

    @EventHandler
    private void onWorldLoad(WorldLoadEvent event) {
        worldGamerules(event.getWorld());
    }

    private void worldGamerules(World world) {
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
    }

}
