package com.shanebeestudios.core.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.commons.lang3.mutable.MutableInt;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility methods for handling conversions between Bukkit and Minecraft(NMS)
 */
public class McUtils {

    private McUtils() {
    }

    /**
     * Get an instance of ServerLevel from a {@link World Bukkit World}
     *
     * @param world World to get ServerLevel from
     * @return ServerLevel from World
     */
    @NotNull
    public static ServerLevel getServerLevel(@NotNull World world) {
        return ((CraftWorld) world).getHandle();
    }

    /**
     * Get an instance of LevelChunk from a {@link Chunk Bukkit Chunk}
     *
     * @param chunk Bukkit chunk to get LevelChunk from
     * @return LevelChunk from Chunk
     */
    public static LevelChunk getLevelChunk(@NotNull Chunk chunk) {
        ServerLevel serverLevel = getServerLevel(chunk.getWorld());
        return serverLevel.getChunk(chunk.getX(), chunk.getZ());
    }

    /**
     * Get a Minecraft BlockPos from a Bukkit Location
     *
     * @param location Location to change to BlockPos
     * @return BlockPos from Location
     */
    @NotNull
    public static BlockPos getPos(@NotNull Location location) {
        return new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * Convert Minecraft ResourceLocation to Bukkit NamespacedKey
     *
     * @param resourceLocation ResourceLocation to change to NamespacedKey
     * @return ResourceLocation from NamespacedKey
     */
    @NotNull
    public static NamespacedKey getNamespacedKey(ResourceLocation resourceLocation) {
        return new NamespacedKey(resourceLocation.getNamespace(), resourceLocation.getPath());
    }

    /**
     * Convert a Bukkit NamespacedKey to Minecraft ResourceLocation
     *
     * @param bukkitKey NamespacedKey to change to ResourceLocation
     * @return ResourceLocation from NamespacedKey
     */
    @NotNull
    public static ResourceLocation getResourceLocation(NamespacedKey bukkitKey) {
        return ResourceLocation.fromNamespaceAndPath(bukkitKey.getNamespace(), bukkitKey.getKey());
    }

    /**
     * Get a holder reference from a registry
     *
     * @param registry Registry to grab holder from
     * @param key      Key of holder
     * @param <T>      Class type of registry
     * @return Holder from registry
     */
    @Nullable
    public static <T> Holder.Reference<T> getHolderReference(Registry<T> registry, NamespacedKey key) {
        ResourceLocation resourceLocation = McUtils.getResourceLocation(key);
        ResourceKey<T> resourceKey = ResourceKey.create(registry.key(), resourceLocation);
        try {
            return registry.getHolderOrThrow(resourceKey);
        } catch (IllegalStateException ignore) {
            return null;
        }
    }

    /**
     * Get a Minecraft Registry
     *
     * @param registry ResourceKey of registry
     * @param <T>      ResourceKey
     * @return Registry from key
     */
    public static <T> Registry<T> getRegistry(ResourceKey<? extends Registry<? extends T>> registry) {
        return MinecraftServer.getServer().registryAccess().registryOrThrow(registry);
    }

    /**
     * Get all keys from a registry
     *
     * @param registry Registry to grab keys from
     * @param <T>      Registry class type
     * @return List of NamespacedKeys for all keys in registry
     */
    @NotNull
    public static <T> List<NamespacedKey> getRegistryKeys(Registry<T> registry) {
        List<NamespacedKey> keys = new ArrayList<>();
        registry.keySet().forEach(resourceLocation -> {
            NamespacedKey namespacedKey = McUtils.getNamespacedKey(resourceLocation);
            keys.add(namespacedKey);
        });
        return keys.stream().sorted(Comparator.comparing(NamespacedKey::toString)).collect(Collectors.toList());
    }

    /**
     * Make a resolver for 3D shifted biomes
     *
     * @param count       counter
     * @param chunkAccess Chunk where biome is
     * @param box         BoundingBox for biome change
     * @param biome       Biome
     * @param filter      Filter
     * @return Biome resolver
     */
    @NotNull
    public static BiomeResolver getBiomeResolver(MutableInt count, ChunkAccess chunkAccess, BoundingBox box, Holder<Biome> biome, Predicate<Holder<Biome>> filter) {
        return (x, y, z, noise) -> {
            Holder<Biome> biomeHolder = chunkAccess.getNoiseBiome(x, y, z);
            if (box.isInside(x << 2, y << 2, z << 2) && filter.test(biomeHolder)) {
                count.increment();
                return biome;
            } else {
                return biomeHolder;
            }
        };
    }

}
