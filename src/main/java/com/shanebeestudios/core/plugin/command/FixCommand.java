package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.core.api.command.CustomArguments;
import com.shanebeestudios.core.api.util.EntityUtils;
import com.shanebeestudios.core.api.util.Permissions;
import com.shanebeestudios.core.api.util.WorldUtils;
import com.shanebeestudios.core.plugin.CorePlugin;
import com.shanebeestudios.coreapi.util.McUtils;
import com.shanebeestudios.coreapi.util.Utils;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import net.kyori.adventure.util.TriState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.entity.Display;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

@SuppressWarnings("DuplicatedCode")
public class FixCommand {

    @SuppressWarnings("unused")
    public FixCommand(CorePlugin plugin) {
        registerFixCommand();
        registerLoadCommand();
    }

    private void registerFixCommand() {
        CommandTree command = new CommandTree("fix")
            .withShortDescription("Fix different objects on the server.")
            .withPermission(Permissions.COMMANDS_FIX.get())
            .then(new MultiLiteralArgument("type", "falling", "decor", "nonticking", "display", "enemy")
                .executesPlayer(info -> {
                    String type = info.args().getByClass("type", String.class);
                    switch (type) {
                        case "falling" -> fixFalling();
                        case "decor" -> fixDecor();
                        case "nonticking" -> fixNonTicking();
                        case "display" -> fixDisplays();
                        case "enemy" -> fixEnemy();
                        case null, default -> {
                        }
                    }
                }))
            .then(LiteralArgument.literal("biome")
                .then(new IntegerArgument("radius", 1, 10)
                    .setOptional(true)
                    .executesPlayer(info -> {
                        Player player = info.sender();
                        Integer radius = info.args().getByClassOrDefault("radius", Integer.class, 1);
                        fixBiomes(player, radius);
                    })))
            .then(LiteralArgument.literal("chunk")
                .then(new IntegerArgument("radius", 1, 10)
                    .setOptional(true)
                    .then(new BooleanArgument("includeTile")
                        .setOptional(true)
                        .executesPlayer(info -> {
                            Player player = info.sender();
                            Integer radius = info.args().getByClassOrDefault("radius", Integer.class, 1);
                            boolean includeTile = info.args().getByClassOrDefault("includeTile", Boolean.class, false);
                            fixChunks(player, radius, includeTile);
                        }))));

        command.register();
    }

    private void registerLoadCommand() {
        CommandTree command = new CommandTree("loadworld")
            .then(CustomArguments.getWorldArgument("world")
                .then(new BooleanArgument("decorations")
                    .setOptional(true)
                    .executesPlayer(info -> {
                        Player player = info.sender();
                        World world = info.args().getByClassOrDefault("world", World.class, player.getWorld());
                        boolean decorations = info.args().getByClassOrDefault("decorations", Boolean.class, true);
                        TriState loaded = WorldUtils.copyAndLoadWorld(world, decorations);
                        if (loaded == TriState.TRUE) {
                            Utils.sendTo(info.sender(),  "World successfully loaded!");
                        } else if (loaded == TriState.FALSE){
                            Utils.sendTo(info.sender(), "World failed to load!");
                        } else {
                            Utils.sendTo(info.sender(), "World could already loaded!");
                        }
                    })));

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
            if (!entity.isTicking() && !(entity instanceof Villager)) entity.remove();
        });
    }

    private void fixDisplays() {
        EntityUtils.getAllEntities().forEach(entity -> {
            if (entity instanceof Display) entity.remove();
        });
    }

    private void fixEnemy() {
        EntityUtils.getAllEntities().forEach(entity -> {
            if (entity instanceof Enemy) entity.remove();
        });
    }

    @SuppressWarnings("UnstableApiUsage")
    private void fixChunks(Player player, int radius, boolean includeTile) {
        World world = player.getWorld();
        String worldName = world.getName();
        World worldCopy = Bukkit.getWorld(worldName + "_copy");
        if (worldCopy == null) {
            Utils.sendTo(player, "&cCopy of world &r'&b%s&r'&c is not loaded.", worldName);
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
                // Don't load chunks
                if (!world.isChunkLoaded(chunkX, chunkZ)) continue;

                Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                worldCopy.getChunkAtAsyncUrgently(chunkX, chunkZ).thenApply(chunkCopy -> {
                    LevelChunk levelChunk = McUtils.getLevelChunk(chunk);
                    for (int x = 0; x <= 15; x++) {
                        for (int z = 0; z <= 15; z++) {
                            for (int y = min; y < 195; y++) {
                                Block blockCopy = chunkCopy.getBlock(x, y, z);
                                BlockData data = blockCopy.getBlockData();
                                BlockPos pos = new BlockPos(x + (chunkX << 4), y, z + (chunkZ << 4));
                                BlockState state = ((CraftBlockData) data).getState();
                                // Set the block in an NMS chunk for faster results
                                levelChunk.setBlockState(pos, state, false, false);

                                // Copy the TileEntity if one is availabe
                                if (includeTile && blockCopy.getState() instanceof TileState tileState) {
                                    Location location = tileState.getLocation();
                                    location.setWorld(world);
                                    BlockEntity copy = ((CraftBlockEntityState<?>) tileState.copy(location)).getTileEntity();
                                    levelChunk.setBlockEntity(copy);
                                }
                            }
                        }
                    }
                    // We have to manually update the light after setting blocks
                    // (not sure if this actually works, the method appears empty)
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
            Utils.sendTo(player, "&cCopy of world &r'&b%s&r'&c is not loaded.", worldName);
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
                int chunkX = currentChunkX + cX;
                int chunkZ = currentChunkZ + cZ;
                // Don't load chunks
                if (!world.isChunkLoaded(chunkX, chunkZ)) continue;

                Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                worldCopy.getChunkAtAsync(chunkX, chunkZ)
                    .thenApply(chunkCopy -> {
                        for (int x = 0; x < 4; x++) {
                            for (int z = 0; z < 4; z++) {
                                for (int y = min; y <= 48; y++) {
                                    Biome biome = chunkCopy.getBlock(x << 2, y << 2, z << 2).getBiome();
                                    chunk.getBlock(x << 2, y << 2, z << 2).setBiome(biome);
                                }
                            }
                        }
                        world.refreshChunk(chunkX, chunkZ);
                        return null;
                    });
            }
        }
    }

}
