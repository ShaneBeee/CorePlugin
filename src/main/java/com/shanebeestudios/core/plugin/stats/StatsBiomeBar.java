package com.shanebeestudios.core.plugin.stats;

import com.shanebeestudios.core.api.util.Permissions;
import com.shanebeestudios.coreapi.util.TaskUtils;
import com.shanebeestudios.coreapi.util.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StatsBiomeBar implements Listener, Stats {

    private final List<UUID> playerList = new ArrayList<>();

    public StatsBiomeBar() {
        // Add online players to list in case of reload
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (Permissions.STATS_BIOMEBAR.hasPermission(player)) {
                enable(player);
            }
        });
        startPlayerTimer();
    }

    public void startPlayerTimer() {
        TaskUtils.runTaskTimer(() -> this.playerList.forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;

            Component action = Utils.getMini("<grey>Biome: " + getFormattedBiome(player.getLocation()));
            player.sendActionBar(action);
        }), 5, 5);
    }

    private String getFormattedBiome(Location location) {
        String key = location.getWorld().getBiome(location).getKey().toString();
        key = key.replace("minecraft:", "<aqua>minecraft<reset>:<yellow>");
        key = key.replace("wythers:", "<green>wythers<reset>:<yellow>");
        key = key.replace("skbee:", "<#FFA533>skbee<reset>:<yellow>");
        key = key.replace("beer:", "<#1BF89B>beer<reset>:<yellow>");
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
