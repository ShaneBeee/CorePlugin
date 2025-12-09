package com.shanebeestudios.core.plugin.listener;

import com.shanebeestudios.core.api.registry.Ranks;
import com.shanebeestudios.core.plugin.CorePlugin;
import com.shanebeestudios.coreapi.util.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitScheduler;

public class PlayerListener implements Listener {

    private final CorePlugin plugin;
    private final BukkitScheduler scheduler = Bukkit.getScheduler();

    public PlayerListener(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Ranks.joinRank(event.getPlayer());
        Player player = event.getPlayer();
        Component mini = Utils.getMini("<gray>[<#1BF987>+<gray>] <#1BF9E8>" + player.getName());
        event.joinMessage(mini);
        closeTerrainLoadingScreen(player, false);
        if (player.isOp()) player.setGameMode(GameMode.CREATIVE);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Component mini = Utils.getMini("<gray>[<#F94D1B>-<gray>] <#1BF9E8>" + player.getName());
        event.quitMessage(mini);
    }

    @EventHandler
    private void onPlayerTrampleFarmland(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && clickedBlock.getType() == Material.FARMLAND) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void onWorldChange(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (event.getFrom().getWorld() != event.getTo().getWorld()) {
            closeTerrainLoadingScreen(player, true);
        }
    }

    @EventHandler
    private void onRespawn(PlayerRespawnEvent event) {
        closeTerrainLoadingScreen(event.getPlayer(), true);
    }

    private void closeTerrainLoadingScreen(Player player, boolean delay) {
        if (delay) {
            this.scheduler.runTaskLater(this.plugin, () -> player.closeInventory(), 0);
        } else {
            player.closeInventory();
        }
    }

}
