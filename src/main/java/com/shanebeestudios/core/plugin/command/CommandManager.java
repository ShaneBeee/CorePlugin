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
        new PrettyNBTCommand();
        new RemoveEntityCommand();
        new SetBiomeCommand();
        new StatsCommand(this.plugin);
        new TagsCommand();
        new WorldCommand();
    }

}
