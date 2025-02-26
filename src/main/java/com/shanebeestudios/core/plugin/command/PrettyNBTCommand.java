package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.core.api.util.CustomLogger;
import com.shanebeestudios.core.api.util.NBTUtils;
import com.shanebeestudios.core.api.util.Permissions;
import com.shanebeestudios.coreapi.util.Utils;
import de.tr7zw.changeme.nbtapi.NBTBlock;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTEntity;
import de.tr7zw.changeme.nbtapi.NBTTileEntity;
import de.tr7zw.changeme.nbtapi.NbtApiException;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import dev.jorel.commandapi.BukkitStringTooltip;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
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

        CommandTree command = new CommandTree("pretty");
        if (!checkNBT()) {
            command.executes((CommandExecutor) (sender, args) -> {
                throw CommandAPI.failWithString("NBT not enabled!");
            });
        } else {
            command.withShortDescription("See nbt of different objects.")
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
                            Utils.sendMiniTo(player, "NBT for %s is unavailable.", type);
                            return;
                        }
                        Utils.sendMiniTo(player, "NBT for %s sent to console.", type);

                        TextComponent legacyPretty = LegacyComponentSerializer.legacySection().deserialize(pretty);
                        Component mini = Utils.getMini("NBT for " + type + ":\n");
                        Bukkit.getConsoleSender().sendMessage(mini.append(legacyPretty));
                    }));
        }
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

    private boolean checkNBT() {
        try {
            MinecraftVersion.replaceLogger(CustomLogger.getLogger());
            NBTContainer nbt = new NBTContainer("{some:string}");
            nbt.setInteger("someint", 10);
        } catch (NbtApiException ignore) {
            Utils.logMini("<red>NBT not enabled!");
            return false;
        }
        return true;
    }

}
