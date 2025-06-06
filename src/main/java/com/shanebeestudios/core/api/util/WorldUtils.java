package com.shanebeestudios.core.api.util;

import com.shanebeestudios.core.plugin.CorePlugin;
import com.shanebeestudios.coreapi.util.McUtils;
import net.kyori.adventure.util.TriState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * General utility class for {@link World worlds}
 */
@SuppressWarnings("unused")
public class WorldUtils {

    private static final Registry<Biome> BIOME_REGISTRY = McUtils.getRegistry(Registries.BIOME, false);

    private WorldUtils() {
    }

    /**
     * Copy and load a world
     *
     * @param world World to copy
     */
    public static TriState copyAndLoadWorld(World world, boolean includeDecoration) {
        String name = world.getName();
        String cloneName = name + "_copy";
        if (Bukkit.getWorld(cloneName) != null) return TriState.NOT_SET;

        Location spawnLocation = world.getSpawnLocation().clone();
        WorldCreator worldCreator = new WorldCreator(cloneName);
        worldCreator.copy(world);
        if (worldCreator.generator() == null) {
            worldCreator.generator(new ChunkGenerator() {
                @Override
                public boolean shouldGenerateNoise() {
                    return true;
                }

                @Override
                public boolean shouldGenerateSurface() {
                    return true;
                }

                @Override
                public boolean shouldGenerateCaves() {
                    return true;
                }

                @Override
                public boolean shouldGenerateDecorations() {
                    return includeDecoration;
                }

                @Override
                public boolean shouldGenerateStructures() {
                    return includeDecoration;
                }

                @Override
                public Location getFixedSpawnLocation(@NotNull World world, @NotNull Random random) {
                    spawnLocation.setWorld(world);
                    return spawnLocation;
                }
            });
        }
        worldCreator.keepSpawnLoaded(TriState.FALSE);
        if (worldCreator.createWorld() != null) return TriState.TRUE;
        return TriState.FALSE;
    }

    /**
     * Clone and load a world
     *
     * @param world World to clone
     */
    @SuppressWarnings({"CallToPrintStackTrace", "DataFlowIssue"})
    public static void cloneAndLoadWorld(World world) {
        CorePlugin plugin = CorePlugin.getInstance();
        String name = world.getName();
        String cloneName = name + "_copy";
        World worldCopy = Bukkit.getWorld(name + "_copy");
        if (worldCopy == null) {
            File worldContainer = Bukkit.getWorldContainer();
            File worldDirectorToClone = world.getWorldFolder();

            WorldCreator worldCreator = new WorldCreator(name + "_copy");
            // Clone the world off the main thread
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                File cloneDirectory = new File(worldContainer, cloneName);
                if (worldDirectorToClone.exists()) {
                    try {
                        for (File file : worldDirectorToClone.listFiles()) {
                            String fileName = file.getName();
                            if (file.isDirectory()) {
                                FileUtils.copyDirectory(file, new File(cloneDirectory, fileName));
                            } else if (!fileName.contains("session") && !fileName.contains("uid.dat")) {
                                FileUtils.copyFile(file, new File(cloneDirectory, fileName));
                            }
                        }
                        // Let's head back to the main thread
                        Bukkit.getScheduler().runTaskLater(plugin, worldCreator::createWorld, 0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * Get a list of all available biomes as {@link NamespacedKey NamespacedKeys}
     * Includes custom biomes as well
     *
     * @return List of biomes
     */
    @NotNull
    public static List<NamespacedKey> getBiomeKeys() {
        return McUtils.getRegistryKeys(BIOME_REGISTRY);
    }

    /**
     * Fill a Biome between 2 locations with an option to only replace a specific Biome.
     * <p>Will also send biome updates to players.</p>
     *
     * @param location   First corner
     * @param location2  Second corner
     * @param biomeKey   Key of biome
     * @param replaceKey Key of biome to replace
     * @return True if biome was set, otherwise false
     */
    public static boolean fillBiome(@NotNull Location location, @NotNull Location location2, @NotNull NamespacedKey biomeKey, @Nullable NamespacedKey replaceKey) {
        World world = location.getWorld();
        if (world != location2.getWorld()) {
            throw new IllegalArgumentException("Worlds for both locations do not match!");
        }

        BlockPos blockPos = McUtils.getPos(location);
        BlockPos blockPos2 = McUtils.getPos(location2);
        BoundingBox box = BoundingBox.fromCorners(blockPos, blockPos2);
        ServerLevel level = McUtils.getServerLevel(world);

        Holder.Reference<Biome> biome = McUtils.getHolderReference(BIOME_REGISTRY, biomeKey);
        ResourceLocation replaceBiome = replaceKey != null ? McUtils.getResourceLocation(replaceKey) : null;
        if (biome == null) return false;

        List<ChunkAccess> chunkAccessList = new ArrayList<>();
        for (int z = SectionPos.blockToSectionCoord(box.minZ()); z <= SectionPos.blockToSectionCoord(box.maxZ()); ++z) {
            for (int x = SectionPos.blockToSectionCoord(box.minX()); x <= SectionPos.blockToSectionCoord(box.maxX()); ++x) {
                ChunkAccess chunkAccess = level.getChunk(x, z, ChunkStatus.FULL, false);
                if (chunkAccess != null) chunkAccessList.add(chunkAccess);
            }
        }

        for (ChunkAccess chunkAccess : chunkAccessList) {
            chunkAccess.fillBiomesFromNoise(McUtils.getBiomeResolver(new MutableInt(0), chunkAccess, box, biome,
                biomeHolder -> replaceBiome == null || biomeHolder.is(replaceBiome)), level.getChunkSource().randomState().sampler());
            chunkAccess.markUnsaved();
        }
        level.getChunkSource().chunkMap.resendBiomesForChunks(chunkAccessList);
        return true;
    }

}
