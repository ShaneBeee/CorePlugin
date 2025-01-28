package com.shanebeestudios.core.plugin.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.event.Listener;

public class WorldListener implements Listener {

    public WorldListener() {
        onLoad();
    }

    public void onLoad() {
        Bukkit.getWorlds().forEach(world -> {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);

            //world.setTime(6000); // TODO might not keep this
            world.setViewDistance(5);
            world.setSimulationDistance(3);
        });
    }

}
