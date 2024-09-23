package com.shanebeestudios.core.api.registry;

import com.shanebeestudios.core.api.config.WarpsConfig;
import com.shanebeestudios.core.plugin.CorePlugin;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Warps {

    public record Warp(String key, Location location) implements ConfigurationSerializable {
        public void teleport(Entity entity) {
            entity.teleportAsync(this.location);
        }

        @Override
        public @NotNull Map<String, Object> serialize() {
            Map<String, Object> map = new HashMap<>();
            map.put("key", this.key);
            map.put("location", this.location);
            return map;
        }

        @SuppressWarnings("unused")
        public static Warp deserialize(Map<String, Object> map) {
            String name = (String) map.get("key");
            Location location = (Location) map.get("location");
            return new Warp(name, location);
        }
    }

    private final Map<String, Warp> warps;
    private final WarpsConfig warpsConfig;

    public Warps(CorePlugin plugin) {
        this.warpsConfig = new WarpsConfig(plugin);
        this.warps = warpsConfig.loadWarps();
    }

    public WarpsConfig getWarpsConfig() {
        return this.warpsConfig;
    }

    public boolean warpExists(String key) {
        return this.warps.containsKey(key);
    }

    @Nullable
    public Warp getWarp(String key) {
        return this.warps.get(key);
    }

    public Map<String, Warp> getAllWarps() {
        return this.warps;
    }

    /**
     * Create a new Warp
     *
     * @param key     Name of warp
     * @param location Location of warp
     */
    public void addWarp(String key, Location location) {
        if (this.warps.containsKey(key)) return;
        Warp warp = new Warp(key, location);
        this.warps.put(key, warp);
        this.warpsConfig.queueForSaving(key, warp);
    }

    public void removeWarp(String key) {
        if (!this.warps.containsKey(key)) return;
        this.warps.remove(key);
        this.warpsConfig.queueForSaving(key, null);
    }

}
