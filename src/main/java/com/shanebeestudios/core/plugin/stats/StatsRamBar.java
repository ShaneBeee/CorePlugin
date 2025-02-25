package com.shanebeestudios.core.plugin.stats;

import com.shanebeestudios.core.api.util.Permissions;
import com.shanebeestudios.coreapi.util.TaskUtils;
import com.shanebeestudios.coreapi.util.Utils;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.stream.StreamSupport;

public class StatsRamBar implements Listener, Stats {

    private final BossBar bossbar;

    public StatsRamBar() {
        this.bossbar = BossBar.bossBar(Utils.getMini("RamBar"), 1.0f, BossBar.Color.WHITE, BossBar.Overlay.NOTCHED_20);
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (Permissions.STATS_RAMBAR.hasPermission(player)) {
                enable(player);
            }
        });
        startTimer();
    }

    private void startTimer() {
        Runtime runtime = Runtime.getRuntime();
        TaskUtils.runTaskTimerAsynchronously(() -> {
            long max = runtime.maxMemory() / 1024 / 1024;
            long free = runtime.freeMemory() / 1024 / 1024;
            double used = Math.floor(max - free);
            String title = String.format("<grey>Ram: <aqua>%s<grey>mb / <aqua>%s<grey>mb", used, max);
            this.bossbar.name(Utils.getMini(title));
            float progress = (float) (used / max);
            this.bossbar.progress(progress);
            if (progress > 0.9) this.bossbar.color(BossBar.Color.RED);
            else if (progress > 0.75) this.bossbar.color(BossBar.Color.YELLOW);
            else this.bossbar.color(BossBar.Color.GREEN);

        }, 5, 5);
    }

    public void enable(Player player) {
        this.bossbar.addViewer(player);
    }

    public void disable(Player player) {
        this.bossbar.removeViewer(player);
    }

    public void toggle(Player player) {
        boolean hasPlayer = StreamSupport.stream(this.bossbar.viewers().spliterator(), false)
            .anyMatch(viewer -> viewer.equals(player));
        if (hasPlayer) {
            disable(player);
        } else {
            enable(player);
        }
    }

    public void unload() {
        // Doesn't need anything
    }

    // Listeners
    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        if (Permissions.STATS_RAMBAR.hasPermission(event.getPlayer())) {
            enable(event.getPlayer());
        }
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        disable(event.getPlayer());
    }

}
