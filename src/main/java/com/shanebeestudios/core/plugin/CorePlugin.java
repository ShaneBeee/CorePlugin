package com.shanebeestudios.core.plugin;

import com.shanebeestudios.core.api.registry.Registries;
import com.shanebeestudios.core.api.registry.Warps;
import com.shanebeestudios.core.plugin.command.CommandManager;
import com.shanebeestudios.core.plugin.listener.ListenerManager;
import com.shanebeestudios.core.plugin.stats.StatsBiomeBar;
import com.shanebeestudios.core.plugin.stats.StatsRamBar;
import com.shanebeestudios.core.plugin.stats.StatsSidebar;
import com.shanebeestudios.coreapi.util.TaskUtils;
import com.shanebeestudios.coreapi.util.Utils;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIPaperConfig;
import dev.jorel.commandapi.exceptions.UnsupportedVersionException;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class CorePlugin extends JavaPlugin {

    static {
        ConfigurationSerialization.registerClass(Warps.Warp.class);
    }

    private static CorePlugin instance;

    private boolean canLoad = false;
    private Registries registries;
    private StatsSidebar statsSidebar;
    private StatsBiomeBar statsBiomebar;
    private StatsRamBar statsRambar;

    @Override
    public void onLoad() {
        Utils.setMiniPrefix("<grey>[<aqua>Core<grey>] ");
        TaskUtils.init(this);
        try {
            Utils.logMini("Loading CommandAPI...");
            CommandAPI.onLoad(new CommandAPIPaperConfig(this)
                .setNamespace("core")
                .verboseOutput(false)
                //.useLatestNMSVersion(true)
                .silentLogs(true));
                //.skipReloadDatapacks(true));
            canLoad = true;
            Utils.logMini("<green>Loaded CommandAPI!");
        } catch (UnsupportedVersionException ex) {
            Utils.logMini("<red>Failed to load CommandAPI: <grey>%s", ex.getMessage());
        }
    }

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        instance = this;
        Utils.logMini("Enabling plugin.");

        this.registries = new Registries(this);
        registerListeners();
        if (canLoad) {
            Utils.logMini("Loading commands...");
            CommandAPI.onEnable();
            new CommandManager(this);
            Utils.logMini("<green>Loaded commands!");
        } else {
            Utils.logMini("<red>Cannot load commands.");
        }

        long finish = System.currentTimeMillis() - start;
        Utils.logMini("Finished enabling plugin in <aqua>%s<grey>ms.", finish);
    }

    private void registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    private void registerListeners() {
        // Register stat listeners
        this.statsSidebar = new StatsSidebar();
        registerListener(this.statsSidebar);
        this.statsBiomebar = new StatsBiomeBar();
        registerListener(this.statsBiomebar);
        this.statsRambar = new StatsRamBar();
        registerListener(this.statsRambar);

        // Register core listeners
        new ListenerManager(this);
    }

    @Override
    public void onDisable() {
        Utils.logMini("Disabling plugin.");
        Bukkit.getScheduler().cancelTasks(this);
        this.statsRambar.unload();
        this.statsSidebar.unload();
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
