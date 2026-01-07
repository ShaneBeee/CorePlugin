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
        new BiomeSampleCommand();
        new ChunkRefreshCommand();
        new ChunkVisualizeCommand();
        new ClearChatCommand();
        new ColorCommand();
        registerListener(new DistanceCommand());
        new DumpRegistryCommand();
        new FixCommand(this.plugin);
        new GameruleCommand();
        new HealCommand();
        new IgniteCommand();
        new ModifyItemCommand();
        registerListener(new NoMonstersCommand());
        registerListener(new PathCommand(this.plugin));
        new PrettyNBTCommand();
        new RandomTeleportCommand();
        new RemoveEntityCommand();
        new RepairCommand();
        new SetBiomeCommand();
        new StatsCommand(this.plugin);
        new TagsCommand();
        new TopCommand();
        new TreeCommand();
        new VehicleCommand();
        new WarpsCommand(this.plugin);
        new WorldCommand();

        //
        new TestCommand();
    }

}
