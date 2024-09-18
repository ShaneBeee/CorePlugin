package com.shanebeestudios.core;

import com.shanebeestudios.core.command.DistanceCommand;
import com.shanebeestudios.core.command.StatsCommand;
import com.shanebeestudios.core.listener.ListenerManager;
import com.shanebeestudios.core.stats.StatsBiomeBar;
import com.shanebeestudios.core.stats.StatsRamBar;
import com.shanebeestudios.core.stats.StatsSidebar;
import com.shanebeestudios.core.util.Util;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.exceptions.UnsupportedVersionException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class CorePlugin extends JavaPlugin {

    private static CorePlugin instance;

    private StatsSidebar statsSidebar;
    private StatsBiomeBar statsBiomebar;
    private StatsRamBar statsRambar;

    @Override
    public void onLoad() {
        try {
            CommandAPI.onLoad(new CommandAPIBukkitConfig(this)
                .verboseOutput(false).silentLogs(true).skipReloadDatapacks(true));
        } catch (UnsupportedVersionException ignore) {
        }
    }

    @Override
    public void onEnable() {
        instance = this;
        Util.log("Enabling plugin.");
        CommandAPI.onEnable();

        registerListeners();
        registerCommands();

        Util.log("Finished enabling plugin.");
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
        new ListenerManager(this);
    }

    private void registerCommands() {
        new StatsCommand(this);
        DistanceCommand distanceCommand = new DistanceCommand();
        registerListener(distanceCommand);
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
        Util.log("Disabling plugin.");
    }

    public static CorePlugin getInstance() {
        return instance;
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
