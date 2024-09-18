package com.shanebeestudios.core.stats;

import com.shanebeestudios.core.CorePlugin;
import com.shanebeestudios.core.util.Permissions;
import com.shanebeestudios.core.util.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StatsBiomeBar implements Listener, Stats {

    private final BukkitScheduler scheduler = Bukkit.getScheduler();
    private final List<UUID> playerList = new ArrayList<>();

    public StatsBiomeBar(CorePlugin plugin) {
        startPlayerTimer(plugin);
    }

    public void startPlayerTimer(CorePlugin plugin) {
        this.scheduler.runTaskTimer(plugin, () -> this.playerList.forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;

            String key = player.getLocation().getBlock().getBiome().getKey().toString();
            Component action = Util.getMini("<grey>Biome: " + formatBiome(key));
            player.sendActionBar(action);
        }), 5, 5);
    }

    private String formatBiome(String key) {
        key = key.replace("minecraft:", "<aqua>minecraft<reset>:<yellow>");
        key = key.replace("wythers:", "<green>wythers<reset>:<yellow>");
        key = key.replace("skbee:", "<#FFA533>skbee<reset>:<yellow>");
        key = key.replace("terralith:", "<#D01ED8>terralith<reset>:<yellow>");
        return key;
    }

    public void enable(Player player) {
        UUID uuid = player.getUniqueId();
        if (this.playerList.contains(uuid)) return;
        this.playerList.add(uuid);
    }

    public void disable(Player player) {
        UUID uuid = player.getUniqueId();
        if (this.playerList.contains(uuid)) {
            this.playerList.remove(uuid);
            player.sendActionBar(Component.text());
        }
    }

    public void toggle(Player player) {
        if (this.playerList.contains(player.getUniqueId())) {
            disable(player);
        } else {
            enable(player);
        }
    }

    // Listeners
    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        if (Permissions.STATS_BIOMEBAR.hasPermission(event.getPlayer())) {
            enable(event.getPlayer());
        }
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        disable(event.getPlayer());
    }

}
