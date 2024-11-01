package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.core.api.util.Permissions;
import dev.jorel.commandapi.CommandTree;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChunkRefreshCommand {

    public ChunkRefreshCommand() {
        registerCommand();
    }

    private void registerCommand() {
        CommandTree command = new CommandTree("chunkrefresh")
            .withAliases("refreshchunks")
            .withShortDescription("Resends chunks to all players.")
            .withPermission(Permissions.COMMANDS_CHUNK_REFRESH.get())
            .executes((sender, args) -> {
                if (sender instanceof Player player) {
                    refreshChunks(player.getWorld());
                } else {
                    Bukkit.getWorlds().forEach(this::refreshChunks);
                }
            });

        command.register();
    }

    private void refreshChunks(World world) {
        for (@NotNull Chunk chunk : world.getLoadedChunks()) {
            world.refreshChunk(chunk.getX(), chunk.getZ());
        }
    }

}
