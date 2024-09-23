package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.core.api.util.NBTUtils;
import com.shanebeestudios.core.api.util.Permissions;
import com.shanebeestudios.core.api.util.Util;
import de.tr7zw.changeme.nbtapi.NBTBlock;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTEntity;
import de.tr7zw.changeme.nbtapi.NBTTileEntity;
import dev.jorel.commandapi.BukkitStringTooltip;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("deprecation")
public class PrettyNBTCommand {

    public PrettyNBTCommand() {
        registerCommand();
    }

    private void registerCommand() {
        ArgumentSuggestions<CommandSender> suggestions = ArgumentSuggestions.stringsWithTooltips(
            BukkitStringTooltip.ofString("hand", "NBT of item in player's hand"),
            BukkitStringTooltip.ofString("vanillahand", "NBT of item in player's hand including vanilla components"),
            BukkitStringTooltip.ofString("block", "NBT of target block of player"),
            BukkitStringTooltip.ofString("entity", "NBT of target entity of player"),
            BukkitStringTooltip.ofString("player", "NBT of player"));

        CommandTree command = new CommandTree("pretty")
            .withPermission(Permissions.COMMANDS_PRETTY_NBT.get())
            .then(new StringArgument("type")
                .includeSuggestions(suggestions)
                .executesPlayer(info -> {
                    Player player = info.sender();
                    String type = info.args().getByClass("type", String.class);
                    assert type != null;
                    String pretty = switch (type) {
                        case "hand" -> getHand(player);
                        case "vanillahand" -> getVanillaHand(player);
                        case "block" -> getBlock(player);
                        case "player" -> getPlayer(player);
                        case "entity" -> getEntity(player);
                        default -> null;
                    };
                    if (pretty == null) {
                        Util.sendTo(player, "NBT for %s is unavailable.", type);
                        return;
                    }
                    Util.sendTo(player, "NBT for %s sent to console.", type);
                    Util.log("NBT for %s:\n%s", type, pretty);
                }));

        command.register();
    }

    private String getHand(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.isEmpty()) return null;
        NBTCompound fullHand = NBTUtils.getFullItem(item);
        return NBTUtils.getPrettyNBT(fullHand, "  ");
    }

    private String getVanillaHand(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.isEmpty()) return null;
        NBTCompound vanillaNBT = NBTUtils.getVanillaNBT(item);
        return NBTUtils.getPrettyNBT(vanillaNBT, "  ");
    }

    private String getBlock(Player player) {
        Block target = player.getTargetBlockExact(100);
        if (target != null) {
            NBTCompound compound;
            if (target.getState() instanceof TileState tileState) {
                compound = new NBTTileEntity(tileState);
            } else {
                compound = new NBTBlock(target).getData();
            }
            if (compound != null) {
                return NBTUtils.getPrettyNBT(compound, "  ");
            }
        }
        return null;
    }

    private String getPlayer(Player player) {
        NBTEntity nbtEntity = new NBTEntity(player);
        return NBTUtils.getPrettyNBT(nbtEntity, "  ");
    }

    private String getEntity(Player player) {
        Entity targetEntity = player.getTargetEntity(100);
        if (targetEntity != null) {
            NBTEntity nbtEntity = new NBTEntity(targetEntity);
            return NBTUtils.getPrettyNBT(nbtEntity, "  ");
        }
        return null;
    }

}
