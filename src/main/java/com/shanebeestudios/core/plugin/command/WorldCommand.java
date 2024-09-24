package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.core.api.command.CustomArguments;
import com.shanebeestudios.core.api.util.Permissions;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;

public class WorldCommand {

    public WorldCommand() {
        registerCommand();
    }

    @SuppressWarnings({"DataFlowIssue", "unchecked"})
    private void registerCommand() {
        CommandTree command = new CommandTree("world")
            .withShortDescription("Teleport to the spawn location of a world.")
            .withPermission(Permissions.COMMANDS_WORLD.get())
            .then(CustomArguments.getWorldArgument("world")
                .then(new EntitySelectorArgument.ManyPlayers("players")
                    .setOptional(true)
                    .withPermission(Permissions.COMMANDS_WORLD_OTHER.get())
                    .executes((sender, args) -> {
                        World world = args.getByClass("world", World.class);
                        Collection<Player> players = (Collection<Player>) args.get("players");
                        if (players == null) {
                            if (sender instanceof Player player) {
                                teleportToWorld(player, world);
                            }
                        } else {
                            players.forEach(player -> teleportToWorld(player, world));
                        }
                    })));

        command.register();
    }

    private void teleportToWorld(Player player, World world) {
        Location clone = world.getSpawnLocation().clone();
        Location location = player.getLocation();
        clone.setYaw(location.getYaw());
        clone.setPitch(location.getPitch());
        player.teleport(clone);
    }
}
