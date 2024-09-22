package com.shanebeestudios.core.plugin.enchantment;

import com.shanebeestudios.core.api.registry.Enchantments;
import com.shanebeestudios.core.plugin.CorePlugin;
import org.bukkit.event.Listener;

public class EnchantmentManager {

    private final CorePlugin plugin;

    public EnchantmentManager(CorePlugin plugin) {
        this.plugin = plugin;
        if (Enchantments.isBeerEnabled()) {
            registerListener(new AutoSmeltEnchantment());
            registerListener(new ExplosiveEnchantment());
            registerListener(new FellerEnchantment(plugin));
            registerListener(new GreenThumbEnchantment(plugin));
        }
    }

    private void registerListener(Listener listener) {
        this.plugin.getServer().getPluginManager().registerEvents(listener, this.plugin);
    }

}
