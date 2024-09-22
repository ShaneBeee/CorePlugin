package com.shanebeestudios.core.plugin.listener;

import com.shanebeestudios.core.plugin.CorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public class ListenerManager {

    public ListenerManager(CorePlugin plugin) {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new ChatListener(), plugin);
        pluginManager.registerEvents(new EntityListener(), plugin);
        pluginManager.registerEvents(new PlayerListener(plugin), plugin);
        pluginManager.registerEvents(new WorldListener(), plugin);
    }

}
