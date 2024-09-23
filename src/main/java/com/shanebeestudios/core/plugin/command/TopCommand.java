package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.core.api.util.Permissions;
import dev.jorel.commandapi.CommandTree;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class TopCommand {

    public TopCommand() {
        registerCommand();
    }

    private void registerCommand() {
        CommandTree command = new CommandTree("top")
            .withPermission(Permissions.COMMANDS_TOP.get())
            .executesPlayer(info -> {
                Player player = info.sender();
                Location playerLocation = player.getLocation();
                World world = player.getWorld();
                Block highestBlock = world.getHighestBlockAt(playerLocation, HeightMap.MOTION_BLOCKING_NO_LEAVES);
                Location location = highestBlock.getLocation();
                if (location.getBlockY() <= playerLocation.getBlockY()) return;

                location.setYaw(playerLocation.getYaw());
                location.setPitch(playerLocation.getPitch());
                location = location.add(0.5, 1, 0.5);
                if (isSolid(location)) {
                    while (isSolid(location)) {
                        location = location.add(0,1,0);
                    }
                }
                player.teleport(location);
            });

        command.register();
    }

    private boolean isSolid(Location location) {
        return location.getBlock().isSolid() || location.getBlock().getRelative(BlockFace.UP).isSolid();
    }

}
