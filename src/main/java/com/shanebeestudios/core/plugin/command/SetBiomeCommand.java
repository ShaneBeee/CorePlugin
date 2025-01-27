package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.core.api.util.Permissions;
import com.shanebeestudios.core.api.util.WorldUtils;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.NamespacedKeyArgument;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class SetBiomeCommand {

    public SetBiomeCommand() {
        registerCommand();
    }

    @SuppressWarnings("DataFlowIssue")
    private void registerCommand() {
        ArgumentSuggestions<CommandSender> suggestions = ArgumentSuggestions.stringsAsync(info ->
            CompletableFuture.supplyAsync(() ->
                WorldUtils.getBiomeKeys().stream().map(NamespacedKey::toString).toArray(String[]::new)));

        CommandTree command = new CommandTree("setbiome")
            .withShortDescription("Sets the biome in a radius around the target block of player.")
            .withPermission(Permissions.COMMANDS_SET_BIOME.get())
            .then(new NamespacedKeyArgument("biome")
                .includeSuggestions(suggestions)
                .then(new IntegerArgument("radius", 1, 100)
                    .setOptional(true)
                    .then(new NamespacedKeyArgument("replace")
                        .includeSuggestions(suggestions)
                        .setOptional(true)
                        .executesPlayer(info -> {
                            NamespacedKey biome = info.args().getByClass("biome", NamespacedKey.class);
                            int radius = info.args().getByClassOrDefault("radius", Integer.class, 10);
                            NamespacedKey replace = info.args().getByClass("replace", NamespacedKey.class);
                            Player player = info.sender();
                            Block targetBlock = player.getTargetBlockExact(100);
                            if (targetBlock == null) {
                                throw CommandAPI.failWithString("Target too far away");
                            }
                            Location center = targetBlock.getLocation();
                            Location low = center.clone().subtract(radius, radius, radius);
                            Location high = center.clone().add(radius, radius, radius);
                            if (!WorldUtils.fillBiome(low, high, biome, replace)) {
                                throw CommandAPI.failWithString("Invalid biome: " + biome);
                            }
                        }))));

        command.register();
    }

}
