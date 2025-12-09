package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.core.api.command.CustomArguments;
import dev.jorel.commandapi.CommandTree;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.Random;

public class TreeCommand {

    private final Random random = new Random();

    public TreeCommand() {
        registerCommand();
    }

    private void registerCommand() {
        CommandTree command = new CommandTree("tree")
            .then(CustomArguments.getTreeTypeArgument("type")
                .executesPlayer(info -> {
                    TreeType type = info.args().getByClassOrDefault("type", TreeType.class, TreeType.TREE);
                    Player player = info.sender();
                    Block targetBlock = player.getTargetBlockExact(100);
                    if (targetBlock == null) return;

                    if (targetBlock.isSolid()) targetBlock = targetBlock.getRelative(BlockFace.UP);
                    player.getWorld().generateTree(targetBlock.getLocation(), this.random, type);
                }));

        command.register();
    }

}
