package com.shanebeestudios.core.plugin.listener;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityInteractEvent;

public class EntityListener implements Listener {

    @EventHandler
    private void onEntityTrampleFarmland(EntityInteractEvent event) {
        if (event.getBlock().getType() == Material.FARMLAND) {
            event.setCancelled(true);
        }
    }

}
