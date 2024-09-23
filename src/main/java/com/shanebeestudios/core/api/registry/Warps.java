package com.shanebeestudios.core.api.registry;

import com.shanebeestudios.core.api.config.WarpsConfig;
import com.shanebeestudios.core.api.util.PlayerUtils;
import com.shanebeestudios.core.plugin.CorePlugin;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Collection of {@link Warp warps}
 */
public class Warps {

    /**
     * Represents a warp a player can teleport to
     * <p>To create a warp use {@link Warps#addWarp(String, Location)}
     */
    public static final class Warp implements ConfigurationSerializable {

        private final Location location;

        /**
         * @param location Location of warp
         */
        private Warp(Location location) {
            this.location = location;
        }

        /**
         * Get the location of this warp
         *
         * @return Location of this warp
         */
        public Location getLocation() {
            return location;
        }

        /**
         * Teleport a player to this warp
         *
         * @param player Player to teleport
         */
        public void teleport(Player player) {
            PlayerUtils.teleportWithoutWarning(player, this.location);
        }

        /**
         * @hidden
         */
        @Override
        public @NotNull Map<String, Object> serialize() {
            Map<String, Object> map = new HashMap<>();
            map.put("location", this.location);
            return map;
        }

        /**
         * @hidden
         */
        @SuppressWarnings("unused")
        public static Warp deserialize(Map<String, Object> map) {
            Location location = (Location) map.get("location");
            return new Warp(location);
        }

    }

    private final Map<String, Warp> warps;
    private final WarpsConfig warpsConfig;

    /**
     * @hidden
     */
    public Warps(CorePlugin plugin) {
        this.warpsConfig = new WarpsConfig(plugin);
        this.warps = warpsConfig.loadWarps();
    }

    public WarpsConfig getWarpsConfig() {
        return this.warpsConfig;
    }

    /**
     * Check if a warp exists
     *
     * @param key Key to check
     * @return True if warp exists else false
     */
    public boolean warpExists(String key) {
        return this.warps.containsKey(key);
    }

    /**
     * Get a warp by key
     *
     * @param key Key of warp
     * @return Warp if available
     */
    @Nullable
    public Warp getWarp(String key) {
        return this.warps.get(key);
    }

    /**
     * Get a map of all registered warps
     *
     * @return All warps
     */
    public Map<String, Warp> getAllWarps() {
        return this.warps;
    }

    /**
     * Create a new Warp
     *
     * @param key      Key of warp
     * @param location Location of warp
     */
    public void addWarp(String key, Location location) {
        if (this.warps.containsKey(key)) return;
        Warp warp = new Warp(location);
        this.warps.put(key, warp);
        this.warpsConfig.queueForSaving(key, warp);
    }

    /**
     * Remove a warp
     *
     * @param key Key of warp to remove
     */
    public void removeWarp(String key) {
        if (!this.warps.containsKey(key)) return;
        this.warps.remove(key);
        this.warpsConfig.queueForSaving(key, null);
    }

}
