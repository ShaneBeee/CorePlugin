package com.shanebeestudios.core.api.registry;

import com.shanebeestudios.core.plugin.CorePlugin;

/**
 * Holder of registries
 */
public class Registries {

    private final Warps warps;

    public Registries(CorePlugin plugin) {
        Enchantments.init();
        Ranks.init();
        this.warps = new Warps(plugin);
    }

    public void disable() {
        this.warps.getWarpsConfig().saveWarps();
    }

    public Warps getWarps() {
        return this.warps;
    }

}
