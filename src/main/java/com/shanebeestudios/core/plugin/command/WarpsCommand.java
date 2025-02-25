package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.core.api.registry.Warps;
import com.shanebeestudios.core.api.registry.Warps.Warp;
import com.shanebeestudios.core.api.util.Permissions;
import com.shanebeestudios.core.plugin.CorePlugin;
import com.shanebeestudios.coreapi.util.Utils;
import dev.jorel.commandapi.BukkitStringTooltip;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.IStringTooltip;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WarpsCommand {

    private final Warps warps;

    public WarpsCommand(CorePlugin plugin) {
        this.warps = plugin.getRegistries().getWarps();
        registerCommand();
    }

    @SuppressWarnings({"unchecked", "DataFlowIssue"})
    private void registerCommand() {
        CommandTree command = new CommandTree("warps")
            .withShortDescription("Warp to different points in the world.")
            .withPermission(Permissions.COMMANDS_WARPS.get())
            .then(LiteralArgument.literal("set")
                .withPermission(Permissions.COMMANDS_WARPS_SET.get())
                .then(new StringArgument("warp")
                    .executesPlayer(info -> {
                        Player player = info.sender();
                        String key = info.args().getByClass("warp", String.class);
                        if (this.warps.warpExists(key)) {
                            send(player, "<gold>Warp already set <white>'<red>%s<white>'", key);
                        } else {
                            this.warps.addWarp(key, player.getLocation());
                            send(player, "<gold>Created new warp <white>'<aqua>%s<white>'", key);
                        }

                    })))
            .then(LiteralArgument.literal("delete")
                .withPermission(Permissions.COMMANDS_WARPS_DELETE.get())
                .then(new StringArgument("warp")
                    .includeSuggestions(getWarpsSuggestions())
                    .executesPlayer(info -> {
                        Player player = info.sender();
                        String key = info.args().getByClass("warp", String.class);
                        if (!this.warps.warpExists(key)) {
                            send(player, "<gold>Unknown warp <white>'<red>%s<white>'", key);
                        } else {
                            this.warps.removeWarp(key);
                            send(player, "<gold>Deleted warp <white>'<aqua>%s<white>'", key);
                        }
                    })))
            .then(LiteralArgument.literal("warp")
                .withPermission(Permissions.COMMANDS_WARPS_WARP.get())
                .then(new StringArgument("warp")
                    .includeSuggestions(getWarpsSuggestions())
                    .executesPlayer(info -> {
                        Player player = info.sender();
                        String key = info.args().getByClass("warp", String.class);
                        if (!this.warps.warpExists(key)) {
                            send(player, "<gold>Unknown warp <white>'<red>%s<white>'", key);
                        } else {
                            Warp warp = this.warps.getWarp(key);
                            assert warp != null;
                            warp.teleport(player);
                        }
                    })
                    .then(new EntitySelectorArgument.ManyPlayers("players")
                        .withPermission(Permissions.COMMANDS_WARPS_WARP_OTHER.get())
                        .executes((sender, args) -> {
                            String key = args.getByClass("warp", String.class);
                            if (!this.warps.warpExists(key)) {
                                send(sender, "<gold>Unknown warp <white>'<red>%s<white>'", key);
                            } else {
                                Warp warp = this.warps.getWarp(key);
                                assert warp != null;
                                Collection<Player> players = (Collection<Player>) args.get("players");
                                players.forEach(warp::teleport);
                            }
                        }))));

        command.register();
    }

    private ArgumentSuggestions<CommandSender> getWarpsSuggestions() {
        return ArgumentSuggestions.stringsWithTooltipsAsync(info -> CompletableFuture.supplyAsync(() -> {
                List<IStringTooltip> tooltips = new ArrayList<>();
                this.warps.getAllWarps().forEach((key, warp) ->
                    tooltips.add(BukkitStringTooltip.ofAdventureComponent(key, prettyLocation(warp))));
                return tooltips.toArray(new IStringTooltip[0]);
            })
        );
    }

    private Component prettyLocation(Warp warp) {
        Location location = warp.getLocation();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        String world = location.getWorld().getName();
        String format = String.format("<grey>x: <aqua>%s <grey>y: <aqua>%s <grey>z: <aqua>%s <grey>world: <green>%s",
            x, y, z, world);
        return Utils.getMini(format);
    }

    private void send(CommandSender sender, String message, Object... objects) {
        String format = String.format("<grey>[<aqua>Warps<grey>] " + message, objects);
        sender.sendMessage(Utils.getMini(format));
    }

}
