package com.shanebeestudios.core.plugin;

import com.shanebeestudios.core.plugin.command.DistanceCommand;
import com.shanebeestudios.core.plugin.command.RemoveEntityCommand;
import com.shanebeestudios.core.plugin.command.StatsCommand;
import com.shanebeestudios.core.plugin.command.TagsCommand;
import com.shanebeestudios.core.plugin.command.WorldCommand;
import com.shanebeestudios.core.plugin.listener.ListenerManager;
import com.shanebeestudios.core.plugin.stats.StatsBiomeBar;
import com.shanebeestudios.core.plugin.stats.StatsRamBar;
import com.shanebeestudios.core.plugin.stats.StatsSidebar;
import com.shanebeestudios.core.api.util.Util;
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
        long start = System.currentTimeMillis();
        instance = this;
        Util.log("Enabling plugin.");
        CommandAPI.onEnable();

        registerListeners();
        registerCommands();

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
        new ListenerManager(this);
    }

    private void registerCommands() {
        DistanceCommand distanceCommand = new DistanceCommand();
        registerListener(distanceCommand);
        new RemoveEntityCommand();
        new StatsCommand(this);
        new TagsCommand();
        new WorldCommand();
    }

    @Override
    public void onDisable() {
        Util.log("Disabling plugin.");
        CommandAPI.onDisable();
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
