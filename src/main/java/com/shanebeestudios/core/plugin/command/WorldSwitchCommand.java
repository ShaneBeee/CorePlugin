package com.shanebeestudios.core.plugin.command;

import dev.jorel.commandapi.CommandTree;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

public class WorldSwitchCommand {

    private World mainWorld;
    private World beerWorld;

    public WorldSwitchCommand() {
        List<World> worlds = Bukkit.getWorlds();
        if (worlds.size() > 1) {
            this.mainWorld = worlds.getFirst();
            this.beerWorld = worlds.get(1);
            register();
        }
    }

    private void register() {
        CommandTree command = new CommandTree("worldswitch")
            .executesPlayer(info -> {
                Player sender = info.sender();

                Location location = sender.getLocation();

                if (sender.getWorld() == this.mainWorld) {
                    location.setWorld(this.beerWorld);
                } else {
                    location.setWorld(this.mainWorld);
                }
                sender.teleportAsync(location);
            });

        command.register();
    }
}
