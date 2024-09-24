package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.core.api.command.CustomArguments;
import com.shanebeestudios.core.api.util.EntityUtils;
import com.shanebeestudios.core.api.util.McUtils;
import com.shanebeestudios.core.api.util.Permissions;
import com.shanebeestudios.core.api.util.Util;
import com.shanebeestudios.core.api.util.WorldUtils;
import com.shanebeestudios.core.plugin.CorePlugin;
import dev.jorel.commandapi.BukkitStringTooltip;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.entity.Display;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

@SuppressWarnings("DuplicatedCode")
public class FixCommand {

    @SuppressWarnings("unused")
    public FixCommand(CorePlugin plugin) {
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

        command.register();
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

        command.register();
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
            Util.sendTo(player, "&cCopy of world &r'&b%s&r'&c is not loaded.", worldName);
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

        radius--;
        for (int cX = -radius; cX <= radius; cX++) {
            for (int cZ = -radius; cZ <= radius; cZ++) {
                int chunkX = currentChunkX + cX;
                int chunkZ = currentChunkZ + cZ;
                Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                worldCopy.getChunkAtAsyncUrgently(chunkX, chunkZ).thenApply(chunkCopy -> {
                    LevelChunk levelChunk = McUtils.getLevelChunk(chunk);
                    for (int x = 0; x <= 15; x++) {
                        for (int z = 0; z <= 15; z++) {
                            for (int y = min; y < 195; y++) {
                                BlockData data = chunkCopy.getBlock(x, y, z).getBlockData();
                                BlockPos pos = new BlockPos(x + (chunkX << 4), y, z + (chunkZ << 4));
                                BlockState state = ((CraftBlockData) data).getState();
                                // Set the block in an NMS chunk for faster results
                                levelChunk.setBlockState(pos, state, false, false);
                            }
                        }
                    }
                    // We have to manually update the light after setting blocks
                    levelChunk.initializeLightSources();
                    // Now we have to manually update the chunk to the player
                    world.refreshChunk(chunkX, chunkZ);
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
            Util.sendTo(player, "&cCopy of world &r'&b%s&r'&c is not loaded.", worldName);
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

}
