package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.core.plugin.CorePlugin;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

public class CommandManager {

    private final CorePlugin plugin;
    private final PluginManager manager;

    public CommandManager(CorePlugin plugin) {
        this.plugin = plugin;
        this.manager = plugin.getServer().getPluginManager();
        registerCommands();
    }

    private void registerListener(Listener listener) {
        this.manager.registerEvents(listener, this.plugin);
    }

    private void registerCommands() {
        new ClearChatCommand();
        registerListener(new DistanceCommand());
        new FixCommand(this.plugin);
        new HealCommand();
        new IgniteCommand();
        registerListener(new NoMonstersCommand());
        registerListener(new PathCommand(this.plugin));
        new PrettyNBTCommand();
        new RemoveEntityCommand();
        new RepairCommand();
        new SetBiomeCommand();
        new StatsCommand(this.plugin);
        new TagsCommand();
        new TopCommand();
        new TreeCommand();
        new WarpsCommand(this.plugin);
        new WorldCommand();
        new WorkbenchCommand();
    }

}
