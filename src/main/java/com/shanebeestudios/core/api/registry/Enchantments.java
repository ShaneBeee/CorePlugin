package com.shanebeestudios.core.api.registry;

import io.papermc.paper.datapack.Datapack;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

/**
 * Custom enchantments
 */
@SuppressWarnings("unused")
public class Enchantments {

    private static boolean BEER_ENABLED = false;

    private Enchantments() {
    }

    // Beer datapack
    public static Enchantment BEER_FELLER;
    public static Enchantment BEER_WITHER;
    public static Enchantment BEER_GREEN_THUMB;
    public static Enchantment BEER_AUTO_SMELT;
    public static Enchantment BEER_TELEKINESIS;
    public static Enchantment BEER_EXPLOSIVE;
    public static Enchantment BEER_LEVITATION;
    public static Enchantment BEER_CLIMBING;
    public static Enchantment BEER_REACH;

    static void init() {
        for (Datapack enabledPack : Bukkit.getDatapackManager().getEnabledPacks()) {
            if (enabledPack.getName().toLowerCase().contains("beer")) {
                BEER_ENABLED = true;
            }
        }
        if (BEER_ENABLED) {
            BEER_FELLER = getBeer("feller");
            BEER_WITHER = getBeer("wither");
            BEER_GREEN_THUMB = getBeer("green_thumb");
            BEER_AUTO_SMELT = getBeer("auto_smelt");
            BEER_TELEKINESIS = getBeer("telekinesis");
            BEER_EXPLOSIVE = getBeer("explosive");
            BEER_LEVITATION = getBeer("levitation");
            BEER_CLIMBING = getBeer("climbing");
            BEER_REACH = getBeer("reach");
        }
    }

    private static Enchantment getBeer(String key) {
        if (BEER_ENABLED) {
            return get("beer:" + key);
        }
        throw new IllegalArgumentException("Beer DataPack is not enabled.");
    }

    private static Enchantment get(String key) {
        NamespacedKey namespacedKey = NamespacedKey.fromString(key);
        if (namespacedKey == null) {
            throw new IllegalArgumentException("Unknown enchantment key: " + key);
        }
        Enchantment enchantment = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(namespacedKey);
        if (enchantment == null) {
            throw new IllegalArgumentException("Unknown enchantment: " + key);
        }
        return enchantment;
    }

    public static boolean isBeerEnabled() {
        return BEER_ENABLED;
    }

}
