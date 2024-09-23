package com.shanebeestudios.core.plugin;

import com.shanebeestudios.core.api.registry.Registries;
import com.shanebeestudios.core.api.registry.Warps;
import com.shanebeestudios.core.api.util.Util;
import com.shanebeestudios.core.plugin.command.CommandManager;
import com.shanebeestudios.core.plugin.enchantment.EnchantmentManager;
import com.shanebeestudios.core.plugin.listener.ListenerManager;
import com.shanebeestudios.core.plugin.stats.StatsBiomeBar;
import com.shanebeestudios.core.plugin.stats.StatsRamBar;
import com.shanebeestudios.core.plugin.stats.StatsSidebar;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.exceptions.UnsupportedVersionException;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class CorePlugin extends JavaPlugin {

    static {
        ConfigurationSerialization.registerClass(Warps.Warp.class);
    }

    private static CorePlugin instance;

    private Registries registries;
    private StatsSidebar statsSidebar;
    private StatsBiomeBar statsBiomebar;
    private StatsRamBar statsRambar;

    @Override
    public void onLoad() {
        try {
            CommandAPI.onLoad(new CommandAPIBukkitConfig(this)
                .setNamespace("core")
                .verboseOutput(false)
                .silentLogs(true)
                .skipReloadDatapacks(true));
        } catch (UnsupportedVersionException ignore) {
        }
    }

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        instance = this;
        Util.log("Enabling plugin.");
        CommandAPI.onEnable();

        this.registries = new Registries(this);
        registerListeners();
        new CommandManager(this);

        long finish = System.currentTimeMillis() - start;
        Util.log("Finished enabling plugin in &b%s&7ms.", finish);
    }

    private void registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    private void registerListeners() {
        // Register stat listeners
        this.statsSidebar = new StatsSidebar(this);
        registerListener(this.statsSidebar);
        this.statsBiomebar = new StatsBiomeBar(this);
        registerListener(this.statsBiomebar);
        this.statsRambar = new StatsRamBar(this);
        registerListener(this.statsRambar);

        // Register core listeners
        new EnchantmentManager(this);
        new ListenerManager(this);
    }

    @Override
    public void onDisable() {
        Util.log("Disabling plugin.");
        this.registries.disable();
        CommandAPI.onDisable();
    }

    @SuppressWarnings("unused")
    public static CorePlugin getInstance() {
        return instance;
    }

    public Registries getRegistries() {
        return this.registries;
    }

    public StatsSidebar getStatsSidebar() {
        return this.statsSidebar;
    }

    public StatsBiomeBar getStatsBiomebar() {
        return this.statsBiomebar;
    }

    public StatsRamBar getStatsRambar() {
        return this.statsRambar;
    }

}
