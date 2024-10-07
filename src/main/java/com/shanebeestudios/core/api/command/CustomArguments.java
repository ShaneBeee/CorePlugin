package com.shanebeestudios.core.api.command;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.NamespacedKeyArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.TreeType;
import org.bukkit.World;

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
        return new CustomArgument<>(new StringArgument(name), info -> Bukkit.getWorld(info.input()))
            .includeSuggestions(ArgumentSuggestions.stringCollectionAsync(info ->
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
        return new CustomArgument<>(new StringArgument(name), info ->
            TreeType.valueOf(info.input().toUpperCase(Locale.ROOT)))
            .includeSuggestions(ArgumentSuggestions.stringCollectionAsync(info ->
                CompletableFuture.supplyAsync(() -> Arrays.stream(TreeType.values()).map(treeType -> treeType.name().toLowerCase(Locale.ROOT)).toList())));
    }

}
