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
        Utils.sendMiniTo(sender, "Teleporting <aqua>%s <grey>to a random location in <aqua>%s<grey>.", player.getName(), world.getName());
        Utils.sendMiniTo(player, "<aqua>%s <grey>is teleporting you to a random location in <aqua>%s<grey>.", sender.getName(), world.getName());
    }

}
