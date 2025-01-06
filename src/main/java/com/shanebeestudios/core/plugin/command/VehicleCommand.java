package com.shanebeestudios.core.plugin.command;

import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.EntityTypeArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class VehicleCommand {

    public VehicleCommand() {
        registerCommand();
    }

    private void registerCommand() {
        CommandTree command = new CommandTree("vehicle")
            .then(LiteralArgument.literal("vehicle")
                .then(LiteralArgument.literal("dismount")
                    .executesPlayer(info -> {
                        info.sender().leaveVehicle();
                    }))
                .then(LiteralArgument.literal("add")
                    .then(new EntityTypeArgument("entity type")
                        .executesPlayer(info -> {
                            EntityType entityType = info.args().getByClass("entity type", EntityType.class);
                            Player player = info.sender();
                            Location location = player.getLocation();
                            assert entityType != null;
                            Entity entity = location.getWorld().spawnEntity(location, entityType);
                            entity.addPassenger(player);

                        }))))
            .then(LiteralArgument.literal("passenger")
                .then(LiteralArgument.literal("eject")
                    .executesPlayer(info -> {
                        Player player = info.sender();
                        player.getPassengers().forEach(player::removePassenger);
                    }))
                .then(LiteralArgument.literal("add")
                    .then(new EntityTypeArgument("entity type")
                        .executesPlayer(info -> {
                            EntityType entityType = info.args().getByClass("entity type", EntityType.class);
                            Player player = info.sender();
                            Location location = player.getLocation();
                            assert entityType != null;
                            Entity entity = location.getWorld().spawnEntity(location, entityType);
                            player.addPassenger(entity);
                        }))));

        command.register();
    }
}
