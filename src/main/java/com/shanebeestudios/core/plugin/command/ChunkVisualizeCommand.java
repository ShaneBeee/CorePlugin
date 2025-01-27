package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.coreapi.util.TaskUtils;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import net.kyori.adventure.util.TriState;
import org.bukkit.Chunk;
import org.bukkit.Chunk.LoadLevel;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChunkVisualizeCommand {

    private static final Map<LoadLevel, BlockData> LOAD_LEVEL_TO_BLOCK_DATA = new HashMap<>() {
        {
            put(LoadLevel.UNLOADED, Material.BLACK_WOOL.createBlockData());
            put(LoadLevel.INACCESSIBLE, Material.GRAY_WOOL.createBlockData());
            put(LoadLevel.BORDER, Material.RED_WOOL.createBlockData());
            put(LoadLevel.TICKING, Material.YELLOW_WOOL.createBlockData());
            put(LoadLevel.ENTITY_TICKING, Material.LIME_WOOL.createBlockData());
        }
    };
    private static final List<Player> PLAYERS = new ArrayList<>();

    public ChunkVisualizeCommand() {
        registerCommand();
        startTimer();
    }

    private void registerCommand() {
        CommandTree command = new CommandTree("chunkvisualize")
            .then(new MultiLiteralArgument("enable", "on", "off", "toggle")
                .executesPlayer(info -> {
                    String enable = info.args().getByClass("enable", String.class);
                    assert enable != null;
                    TriState enabled = switch (enable) {
                        case "on" -> TriState.TRUE;
                        case "off" -> TriState.FALSE;
                        default -> TriState.NOT_SET;
                    };
                    enableChunkVisualization(info.sender(), enabled);
                }));

        command.register();
    }

    @SuppressWarnings("UnstableApiUsage")
    private void enableChunkVisualization(Player player, TriState enabled) {
        if (enabled == TriState.TRUE) {
            if (!PLAYERS.contains(player)) PLAYERS.add(player);
            visualizeChunks(player);
        } else if (enabled == TriState.FALSE) {
            PLAYERS.remove(player);
            World world = player.getWorld();
            player.getSentChunks().forEach(chunk -> world.refreshChunk(chunk.getX(), chunk.getZ()));
        } else {
            TriState toggle = PLAYERS.contains(player) ? TriState.FALSE : TriState.TRUE;
            enableChunkVisualization(player, toggle);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private void visualizeChunks(Player player) {
        Set<Chunk> sentChunks = player.getSentChunks();
        TaskUtils.runTaskEndOfTickAsynchronously(() ->
            sentChunks.forEach(chunk -> visualizeChunk(chunk, player)));
    }

    @SuppressWarnings("UnstableApiUsage")
    private void visualizeChunk(Chunk chunk, Player player) {
        World world = chunk.getWorld();
        ChunkSnapshot snapshot = chunk.getChunkSnapshot(true, false, false);
        List<BlockState> states = new ArrayList<>();
        BlockState blockState = LOAD_LEVEL_TO_BLOCK_DATA.get(chunk.getLoadLevel()).createBlockState();

        int chunkX = chunk.getX() << 4;
        int chunkZ = chunk.getZ() << 4;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int y = snapshot.getHighestBlockYAt(x, z);
                Location location = new Location(world, x + chunkX, y, z + chunkZ);
                states.add(blockState.copy(location));
            }
        }
        player.sendBlockChanges(states);
    }

    private void startTimer() {
        TaskUtils.runTaskTimer(() -> PLAYERS.forEach(this::visualizeChunks), 10, 10);
    }

}
