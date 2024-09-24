package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.core.api.registry.Warps;
import com.shanebeestudios.core.api.registry.Warps.Warp;
import com.shanebeestudios.core.api.util.Permissions;
import com.shanebeestudios.core.api.util.Util;
import com.shanebeestudios.core.plugin.CorePlugin;
import dev.jorel.commandapi.BukkitStringTooltip;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.IStringTooltip;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
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
                            send(player, "&6Warp already set &r'&c%s&r'", key);
                        } else {
                            this.warps.addWarp(key, player.getLocation());
                            send(player, "&6Created new warp &r'&b%s&r'", key);
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
                            send(player, "&6Unknown warp &r'&c%s&r'", key);
                        } else {
                            this.warps.removeWarp(key);
                            send(player, "&6Deleted warp &r'&b%s&r'", key);
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
                            send(player, "&6Unknown warp &r'&c%s&r'", key);
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
                                send(sender, "&6Unknown warp &r'&c%s&r'", key);
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
                    tooltips.add(BukkitStringTooltip.ofString(key, prettyLocation(warp))));
                return tooltips.toArray(new IStringTooltip[0]);
            })
        );
    }

    private String prettyLocation(Warp warp) {
        Location location = warp.getLocation();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        String world = location.getWorld().getName();
        String format = Util.getColString("&7x: &b%s &7y: &b%s &7z: &b%s &7world: &a%s");
        return String.format(format, x, y, z, world);
    }

    private void send(CommandSender sender, String message, Object... objects) {
        String format = String.format("&7[&bWarps&7] " + message, objects);
        sender.sendMessage(Util.getColString(format));
    }

}
