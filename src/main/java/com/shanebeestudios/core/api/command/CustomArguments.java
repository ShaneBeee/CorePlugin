package com.shanebeestudios.core.api.command;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.concurrent.CompletableFuture;

/**
 * Collection of custom {@link Argument arguments}
 */
public class CustomArguments {

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

}
