package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.core.api.util.RandomTeleporter;
import com.shanebeestudios.coreapi.util.Utils;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.WorldArgument;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class RandomTeleportCommand {

    public RandomTeleportCommand() {
        registerCommand(new RandomTeleporter());
    }

    private void registerCommand(RandomTeleporter teleporter) {
        new CommandTree("randomteleport")
            .withAliases("rtp")
            .executesPlayer(info -> {
                Player sender = info.sender();
                teleporter.rtp(sender, sender.getWorld());
            })
            .then(new WorldArgument("world")
                .executesPlayer(info -> {
                    Player sender = info.sender();
                    World world = (World) info.args().get("world");
                    teleporter.rtp(sender, world);
                }))
            .then(new EntitySelectorArgument.ManyPlayers("players")
                .then(new WorldArgument("world")
                    .setOptional(true)
                    .executes((sender, args) -> {

                        @SuppressWarnings("unchecked")
                        Collection<Player> players = (Collection<Player>) args.get("players");
                        assert players != null;
                        players.forEach(player -> {
                            World world = (World) args.getOrDefault("world", player.getWorld());
                            if (player != sender) sendOther(player, sender, world);
                            teleporter.rtp(player, world);
                        });
                    })))

            .register();
    }

    private void sendOther(Player player, CommandSender sender, @NotNull World world) {
        Utils.sendTo(sender, "Teleporting &b%s &7to a random location in &b%s&7.", player.getName(), world.getName());
        Utils.sendTo(player, "&b%s &7is teleporting you to a random location in &b%s&7.", sender.getName(), world.getName());
    }

}
