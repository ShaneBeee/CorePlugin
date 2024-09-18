package com.shanebeestudios.core.listener;

import com.shanebeestudios.core.CorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public class ListenerManager {

    public ListenerManager(CorePlugin plugin) {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new EntityListener(), plugin);
        pluginManager.registerEvents(new PlayerListener(plugin), plugin);
        pluginManager.registerEvents(new WorldListener(), plugin);
    }

}
