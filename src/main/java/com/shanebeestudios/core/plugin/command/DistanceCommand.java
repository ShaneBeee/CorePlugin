package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.core.api.util.Permissions;
import com.shanebeestudios.coreapi.util.Utils;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DistanceCommand implements Listener {

    private final Map<UUID, Integer> fakeDistances = new HashMap<>();
    private final CommandTree commandTree;

    public DistanceCommand() {
        this.commandTree = new CommandTree("distance");
        this.commandTree.withShortDescription("Change different distances for a player.");
        registerDistanceCommand("view");
        registerDistanceCommand("simulation");
        registerDistanceCommand("fakeview");
        registerDistanceCommand("send");
        this.commandTree.register();
    }

    @SuppressWarnings("DataFlowIssue")
    private void registerDistanceCommand(String type) {
        this.commandTree
            .then(LiteralArgument.literal(type)
                .withPermission(Permissions.getCommandPermissions("distance." + type).get())
                .executesPlayer((player, args) -> {
                    int distance = getDistance(player, type);
                    Component mini = Utils.getMini("<grey>[<aqua>Distance<grey>] <#FF8033>Your " + type + " distance is <aqua>" + distance);
                    player.sendMessage(mini);
                })
                .then(new IntegerArgument("distance", 2, 32)
                    .executesPlayer((player, args) -> {
                        int distance = args.getUnchecked("distance");
                        setDistance(List.of(player), distance, type);
                    })
                    .then(new EntitySelectorArgument.ManyPlayers("players")
                        .withPermission(Permissions.getCommandPermissions("distance." + type + ".other").get())
                        .executes((sender, args) -> {
                            int distance = args.getUnchecked("distance");
                            Collection<Player> players = args.getUnchecked("players");
                            setDistance(players, distance, type);
                        })
                    )));
    }

    private int getDistance(Player player, String type) {
        return switch (type) {
            case "view" -> player.getViewDistance();
            case "simulation" -> player.getSimulationDistance();
            case "send" -> player.getSendViewDistance();
            case "fakeview" -> this.fakeDistances.getOrDefault(player.getUniqueId(), player.getViewDistance());
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    private void setDistance(Collection<Player> players, int distance, String type) {
        players.forEach(player -> {
            if (type.equalsIgnoreCase("view")) {
                player.setViewDistance(distance);
                this.fakeDistances.remove(player.getUniqueId());
            } else if (type.equalsIgnoreCase("simulation")) {
                player.setSimulationDistance(distance);
            } else if (type.equalsIgnoreCase("send")) {
                player.setSendViewDistance(distance);
            } else {
                ClientboundSetChunkCacheRadiusPacket packet = new ClientboundSetChunkCacheRadiusPacket(distance);
                ((CraftPlayer) player).getHandle().connection.sendPacket(packet);
                this.fakeDistances.put(player.getUniqueId(), distance);
            }
        });
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        this.fakeDistances.remove(event.getPlayer().getUniqueId());
    }

}
