package com.shanebeestudios.core.plugin.stats;

import com.shanebeestudios.core.plugin.CorePlugin;
import com.shanebeestudios.core.api.util.Permissions;
import com.shanebeestudios.core.api.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;

public class StatsRamBar implements Listener, Stats {

    private final BukkitScheduler scheduler = Bukkit.getScheduler();
    private final BossBar bossbar;

    public StatsRamBar(CorePlugin plugin) {
        this.bossbar = Bukkit.createBossBar("RamBar", BarColor.BLUE, BarStyle.SEGMENTED_20);
        startTimer(plugin);
    }

    private void startTimer(CorePlugin plugin) {
        Runtime runtime = Runtime.getRuntime();
        this.scheduler.runTaskTimer(plugin, () -> {
            long max = runtime.maxMemory() / 1024 / 1024;
            long free = runtime.freeMemory() / 1024 / 1024;
            double used = Math.floor(max - free);
            String title = String.format("&7Ram: &b%s&7mb / &b%s&7mb", used, max);
            this.bossbar.setTitle(Util.getColString(title));
            double progress = (used / max);
            this.bossbar.setProgress(progress);
            if (progress > 0.9) this.bossbar.setColor(BarColor.RED);
            else if (progress > 0.75) this.bossbar.setColor(BarColor.YELLOW);
            else this.bossbar.setColor(BarColor.GREEN);

        }, 5, 5);
    }

    public void enable(Player player) {
        this.bossbar.addPlayer(player);
    }

    public void disable(Player player) {
        this.bossbar.removePlayer(player);
    }

    public void toggle(Player player) {
        if (this.bossbar.getPlayers().contains(player)) {
            disable(player);
        } else {
            enable(player);
        }
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
