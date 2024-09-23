package com.shanebeestudios.core.api.config;

import com.shanebeestudios.core.api.registry.Warps.Warp;
import com.shanebeestudios.core.plugin.CorePlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Config for saving {@link Warp warps}
 */
public class WarpsConfig {

    private final File warpsConfigFile;
    private final FileConfiguration warpsConfig;

    private final Map<String, Warp> queueForSaving = new HashMap<>();

    /**
     * @hidden
     */
    public WarpsConfig(CorePlugin plugin) {
        this.warpsConfigFile = new File(plugin.getDataFolder(), "warps.yml");
        if (!this.warpsConfigFile.exists()) {
            plugin.saveResource("warps.yml", false);
        }
        this.warpsConfig = YamlConfiguration.loadConfiguration(this.warpsConfigFile);
        startSaveTimer(plugin);
    }

    /**
     * Load all warps
     *
     * @return Map of all loaded warps
     */
    public Map<String, Warp> loadWarps() {
        Map<String, Warp> warpsMap = new HashMap<>();
        ConfigurationSection section = this.warpsConfig.getConfigurationSection("warps");
        if (section != null) {
            for (String key : section.getKeys(true)) {
                Object object = section.get(key);
                if (object instanceof Warp warp) {
                    warpsMap.put(key.toLowerCase(), warp);
                }
            }
        }
        return warpsMap;
    }

    private void startSaveTimer(CorePlugin plugin) {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();

        scheduler.runTaskTimer(plugin, () -> {
            if (this.queueForSaving.isEmpty()) return;

            processQueue();
            scheduler.runTaskAsynchronously(plugin, this::saveConfig);

        }, 6000, 6000); // 5 minutes
    }

    private void processQueue() {
        this.queueForSaving.forEach(this::saveWarp);
        this.queueForSaving.clear();
    }

    private void saveWarp(String key, @Nullable Warp warp) {
        this.warpsConfig.set("warps." + key.toLowerCase(), warp);
    }

    /**
     * Queue a warp for saving
     *
     * @param key  Key of warp to save
     * @param warp Warp to save
     */
    public void queueForSaving(String key, Warp warp) {
        this.queueForSaving.put(key.toLowerCase(), warp);
    }

    /**
     * Save all warps
     * <p>Should only be used when plugin disables</p>
     */
    public void saveWarps() {
        processQueue();
        saveConfig();
    }

    private void saveConfig() {
        try {
            this.warpsConfig.save(this.warpsConfigFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
