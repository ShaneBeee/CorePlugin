package com.shanebeestudios.core.api.command;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentException;
import dev.jorel.commandapi.arguments.NamespacedKeyArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.inventory.EquipmentSlotGroup;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Collection of custom {@link Argument arguments}
 */
public class CustomArguments {

    private CustomArguments() {
    }

    /**
     * Custom argument using world names rather than keys
     *
     * @param name Name of argument
     * @return Custom argument for worlds
     */
    public static Argument<World> getWorldArgument(String name) {
        return new CustomArgument<>(new StringArgument(name), info -> {
            World world = Bukkit.getWorld(info.input());
            if (world == null) {
                throw CustomArgumentException.fromString("Invalid world: " + info.input());
            }
            return world;
        }).includeSuggestions(ArgumentSuggestions.stringCollectionAsync(info ->
            CompletableFuture.supplyAsync(() -> Bukkit.getWorlds().stream().map(World::getName).toList())
        ));
    }

    /**
     * Custom argument using {@link Tag Minecraft Tags}
     *
     * @param name     Name of argument
     * @param registry Registry for type of tag
     * @return Custom argument for tags
     */
    public static Argument<Tag<Material>> getTagArgument(String name, String registry) {
        return new CustomArgument<>(new NamespacedKeyArgument(name), info ->
            Bukkit.getTag(registry, info.currentInput(), Material.class))
            .includeSuggestions(ArgumentSuggestions.stringCollectionAsync(info ->
                CompletableFuture.supplyAsync(() ->
                    ((Collection<Tag<Material>>) Bukkit.getTags(registry, Material.class))
                        .stream().map(tag -> tag.getKey().toString()).toList())
            ));
    }

    /**
     * Custom argument using {@link TreeType Bukkit TreeType}
     *
     * @param name Name of argument
     * @return Custom argument for TreeType
     */
    public static Argument<TreeType> getTreeTypeArgument(String name) {
        return new CustomArgument<>(new StringArgument(name), info -> {
            try {
                return TreeType.valueOf(info.input().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw CustomArgumentException.fromString("Invalid tree type: " + info.input());
            }
        }).includeSuggestions(ArgumentSuggestions.stringCollectionAsync(info ->
            CompletableFuture.supplyAsync(() -> Arrays.stream(TreeType.values()).map(treeType -> treeType.name().toLowerCase(Locale.ROOT)).toList())));
    }

    /**
     * Get a {@link Registry} based argument
     * <p>This will used NamespacedKeys as suggestions provided from the registry</p>
     *
     * @param registry Registry to use
     * @param name     Name of argument
     * @param <R>      Class type of registry
     * @return Custom argument for a Registry value
     */
    public static <R extends Keyed> Argument<R> getRegistryArgument(Registry<R> registry, String name) {
        return new CustomArgument<>(new NamespacedKeyArgument(name), info -> {
            R value = registry.get(info.currentInput());
            if (value == null) {
                throw CustomArgumentException.fromString("Invalid value: " + info.input());
            }
            return value;
        }).includeSuggestions(ArgumentSuggestions.strings(registry.stream().map(r -> r.getKey().toString()).toList()));
    }

    /**
     * Get an enum based argument
     * <p>This will use the lowercase names of the enums for suggestions</p>
     *
     * @param enumClass Enum class
     * @param name      Name of argument
     * @param <E>       Class type of enum
     * @return Custom argument for an enum
     */
    public static <E extends Enum<E>> Argument<E> getEnumArgument(Class<E> enumClass, String name) {
        return new CustomArgument<>(new StringArgument(name), info -> {
            try {
                return Enum.valueOf(enumClass, info.input().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw CustomArgumentException.fromString("Invalid value: " + info.input());
            }
        }).includeSuggestions(ArgumentSuggestions.strings(
            Arrays.stream(enumClass.getEnumConstants()).map(e -> e.name().toLowerCase(Locale.ROOT)).toList()
        ));
    }

    @SuppressWarnings("UnstableApiUsage")
    public static Argument<EquipmentSlotGroup> getSlotGroupArgument(String name) {
        return new CustomArgument<>(new StringArgument(name), info -> {
            EquipmentSlotGroup slot = EquipmentSlotGroup.getByName(info.input().toUpperCase(Locale.ROOT));
            if (slot == null) {
                throw CustomArgumentException.fromString("Invalid slot: " + info.input());
            }
            return slot;
        }).includeSuggestions(ArgumentSuggestions.strings("any", "armor", "chest", "feed", "hand", "head", "legs", "mainhand", "offhand"));
    }

}
