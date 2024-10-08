package com.shanebeestudios.core.plugin.stats;

import com.shanebeestudios.core.api.util.Permissions;
import com.shanebeestudios.core.plugin.CorePlugin;
import com.shanebeestudios.coreapi.util.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.UnsafeValues;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class StatsBiomeBar implements Listener, Stats {

    private final BukkitScheduler scheduler = Bukkit.getScheduler();
    private final UnsafeValues unsafeValues = Bukkit.getUnsafe();
    private final List<UUID> playerList = new ArrayList<>();

    public StatsBiomeBar(CorePlugin plugin) {
        startPlayerTimer(plugin);
    }

    public void startPlayerTimer(CorePlugin plugin) {
        this.scheduler.runTaskTimer(plugin, () -> this.playerList.forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;

            Component action = Utils.getMini("<grey>Biome: " + getFormattedBiome(player.getLocation()));
            player.sendActionBar(action);
        }), 5, 5);
    }

    private String getFormattedBiome(Location location) {
        String key = this.unsafeValues.getBiomeKey(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ()).toString();
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
