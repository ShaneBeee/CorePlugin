package com.shanebeestudios.core.api.registry;

/**
 * Holder of registries
 */
public class Registries {

    private Registries() {
    }

    public static void init() {
        Enchantments.init();
        Ranks.init();
    }

}
