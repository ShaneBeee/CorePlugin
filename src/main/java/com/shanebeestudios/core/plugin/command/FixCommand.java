package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.core.api.command.CustomArguments;
import com.shanebeestudios.core.api.util.EntityUtils;
import com.shanebeestudios.core.api.util.Permissions;
import com.shanebeestudios.core.api.util.Util;
import com.shanebeestudios.core.api.util.WorldUtils;
import com.shanebeestudios.core.plugin.CorePlugin;
import dev.jorel.commandapi.BukkitStringTooltip;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Display;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("DuplicatedCode")
public class FixCommand {

    private final CorePlugin plugin;
    private final BukkitScheduler scheduler = Bukkit.getScheduler();

    public FixCommand(CorePlugin plugin) {
        this.plugin = plugin;
        registerFixCommand();
        registerLoadCommand();
    }

    private void registerFixCommand() {
        ArgumentSuggestions<CommandSender> suggestions = ArgumentSuggestions.stringsWithTooltips(
            BukkitStringTooltip.ofString("falling", "Removes all falling blocks"),
            BukkitStringTooltip.ofString("decor", "Removes all dropped items and falling blocks"),
            BukkitStringTooltip.ofString("nonticking", "Removes all entities in non-ticking chunks"),
            BukkitStringTooltip.ofString("displays", "Removes all display entities"),
            BukkitStringTooltip.ofString("chunk", "Regenerate chunks within an optional radius"),
            BukkitStringTooltip.ofString("biome", "Regenerate biomes within an optional radius")
        );

        CommandTree command = new CommandTree("fix")
            .withPermission(Permissions.COMMANDS_FIX.get())
            .then(new StringArgument("type")
                .includeSuggestions(suggestions)
                .then(new IntegerArgument("radius", 1, 10)
                    .setOptional(true)
                    .executesPlayer(info -> {
                        Player player = info.sender();
                        String type = info.args().getByClass("type", String.class);
                        Integer radius = info.args().getByClassOrDefault("radius", Integer.class, 1);
                        switch (type) {
                            case "falling" -> fixFalling();
                            case "decor" -> fixDecor();
                            case "nonticking" -> fixNonTicking();
                            case "displays" -> fixDisplays();
                            case "chunk" -> fixChunks(player, radius);
                            case "biome" -> fixBiomes(player, radius);
                            case null, default -> {
                            }
                        }

                    })));

        command.register("core");
    }

    private void registerLoadCommand() {
        CommandTree command = new CommandTree("loadworld")
            .then(CustomArguments.getWorldArgument("world")
                .setOptional(true)
                .executesPlayer(info -> {
                    Player player = info.sender();
                    World world = info.args().getByClassOrDefault("world", World.class, player.getWorld());
                    WorldUtils.copyAndLoadWorld(world);
                }));

        command.register("core");
    }

    private void fixFalling() {
        EntityUtils.getAllEntities().forEach(entity -> {
            if (entity instanceof FallingBlock) entity.remove();
        });
    }

    private void fixDecor() {
        EntityUtils.getAllEntities().forEach(entity -> {
            if (entity instanceof FallingBlock || entity instanceof Item) entity.remove();
        });
    }

    private void fixNonTicking() {
        EntityUtils.getAllEntities().forEach(entity -> {
            if (!entity.isTicking()) entity.remove();
        });
    }

    private void fixDisplays() {
        EntityUtils.getAllEntities().forEach(entity -> {
            if (entity instanceof Display) entity.remove();
        });
    }

    private void fixChunks(Player player, int radius) {
        World world = player.getWorld();
        String worldName = world.getName();
        World worldCopy = Bukkit.getWorld(worldName + "_copy");
        if (worldCopy == null) {
            Util.sendTo(player, "Copy of world %s is not loaded.", worldName);
            return;
        }
        int min = world.getMinHeight();
        Chunk currentChunk;
        Block targetBlock = player.getTargetBlockExact(64);
        if (targetBlock != null) {
            currentChunk = targetBlock.getChunk();
        } else {
            currentChunk = player.getChunk();
        }
        int currentChunkX = currentChunk.getX();
        int currentChunkZ = currentChunk.getZ();

        AtomicInteger delay = new AtomicInteger();

        radius--;
        for (int cX = -radius; cX <= radius; cX++) {
            for (int cZ = -radius; cZ <= radius; cZ++) {
                Chunk chunk = world.getChunkAt(currentChunkX + cX, currentChunkZ + cZ);
                worldCopy.getChunkAtAsync(currentChunkX + cX, currentChunkZ + cZ)
                    .thenApply(chunkCopy -> {
                        for (int x = 0; x <= 15; x++) {
                            int finalX = x;
                            schedule(() -> {
                                for (int z = 0; z <= 15; z++) {
                                    for (int y = min; y < 195; y++) {
                                        BlockData data = chunkCopy.getBlock(finalX, y, z).getBlockData();
                                        chunk.getBlock(finalX, y, z).setBlockData(data);
                                    }
                                }
                            }, delay.getAndIncrement());
                        }
                        return null;
                    });
            }
        }
    }

    private void fixBiomes(Player player, int radius) {
        World world = player.getWorld();
        String worldName = world.getName();
        World worldCopy = Bukkit.getWorld(worldName + "_copy");
        if (worldCopy == null) {
            Util.sendTo(player, "Copy of world %s is not loaded.", worldName);
            return;
        }
        int min = world.getMinHeight() >> 2;
        Chunk currentChunk;
        Block targetBlock = player.getTargetBlockExact(64);
        if (targetBlock != null) {
            currentChunk = targetBlock.getChunk();
        } else {
            currentChunk = player.getChunk();
        }
        int currentChunkX = currentChunk.getX();
        int currentChunkZ = currentChunk.getZ();

        radius--;
        for (int cX = -radius; cX <= radius; cX++) {
            for (int cZ = -radius; cZ <= radius; cZ++) {
                Chunk chunk = world.getChunkAt(currentChunkX + cX, currentChunkZ + cZ);
                worldCopy.getChunkAtAsync(currentChunkX + cX, currentChunkZ + cZ)
                    .thenApply(chunkCopy -> {
                        for (int x = 0; x < 4; x++) {
                            for (int z = 0; z < 4; z++) {
                                for (int y = min; y <= 48; y++) {
                                    Biome biome = chunkCopy.getBlock(x << 2, y << 2, z << 2).getBiome();
                                    chunk.getBlock(x << 2, y << 2, z << 2).setBiome(biome);
                                }
                            }
                            if (x == 3) {
                                world.refreshChunk(chunk.getX(), chunk.getZ());
                            }
                        }
                        return null;
                    });
            }
        }
    }

    private void schedule(Runnable task, int delay) {
        this.scheduler.runTaskLater(this.plugin, task, delay);
    }

}
