package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.coreapi.util.Utils;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GameruleCommand {

    public GameruleCommand() {
        registerCommand();
    }

    private void registerCommand() {
        CommandTree command = new CommandTree("gamerule");
        StringArgument worldArg = createWorldArg();
        for (GameRule<?> value : Registry.GAME_RULE) {
            if (value.getType() == Boolean.class) {
                worldArg.then(createBooleanArg(value.key().value()));
            } else if (value.getType() == Integer.class) {
                worldArg.then(createIntegerArg(value.key().value()));
            }
        }

        command.then(worldArg);
        command.override();
    }

    private StringArgument createWorldArg() {
        StringArgument worldArg = new StringArgument("world");

        worldArg.includeSuggestions(ArgumentSuggestions.stringsAsync(info -> {
            List<String> suggestions = new ArrayList<>();
            for (World world : Bukkit.getWorlds()) {
                suggestions.add(world.getName());
            }
            suggestions.add("server");
            return CompletableFuture.supplyAsync(() -> suggestions.toArray(new String[0]));
        }));

        return worldArg;
    }

    @SuppressWarnings({"unchecked", "DataFlowIssue", "PatternValidation"})
    private LiteralArgument createBooleanArg(String gameruleName) {
        LiteralArgument literal = LiteralArgument.literal(gameruleName);
        literal.then(new BooleanArgument("value")
            .executes(info -> {
                String worldName = info.args().getByClass("world", String.class);
                Boolean value = info.args().getByClass("value", Boolean.class);
                GameRule<Boolean> gameRule = (GameRule<Boolean>) Registry.GAME_RULE.get(Key.key(gameruleName));
                setGamerule(info.sender(), worldName, gameRule, value);
            }));
        literal.executes(info -> {
            String worldName = info.args().getByClass("world", String.class);
            GameRule<Boolean> gameRule = (GameRule<Boolean>) Registry.GAME_RULE.get(Key.key(gameruleName));
            printGamerule(info.sender(), worldName, gameRule);
        });
        return literal;
    }

    @SuppressWarnings({"unchecked", "DataFlowIssue", "PatternValidation"})
    private LiteralArgument createIntegerArg(String gameruleName) {
        LiteralArgument literal = LiteralArgument.literal(gameruleName);
        literal.then(new IntegerArgument("value")
            .executes(info -> {
                String worldName = info.args().getByClass("world", String.class);
                Integer value = info.args().getByClass("value", Integer.class);
                GameRule<Integer> gameRule = (GameRule<Integer>) Registry.GAME_RULE.get(Key.key(gameruleName));
                setGamerule(info.sender(), worldName, gameRule, value);
            }));
        literal.executes(info -> {
            String worldName = info.args().getByClass("world", String.class);
            GameRule<Integer> gameRule = (GameRule<Integer>) Registry.GAME_RULE.get(Key.key(gameruleName));
            printGamerule(info.sender(), worldName, gameRule);
        });
        return literal;
    }

    @SuppressWarnings("DataFlowIssue")
    private <T> void setGamerule(CommandSender sender, String worldName, GameRule<T> rule, T value) {
        if (worldName.equalsIgnoreCase("server")) {
            for (World world : Bukkit.getWorlds()) {
                world.setGameRule(rule, value);
            }
            Utils.sendMiniTo(sender, "Set gamerule <white>'<yellow>%s<white>'<grey> for all worlds to <green>%s",
                rule.key().value(), getColoredValue(value));
        } else {
            Bukkit.getWorld(worldName).setGameRule(rule, value);
            Utils.sendMiniTo(sender, "Set gamerule <white>'<yellow>%s<white>'<grey> for world <white>'<yellow>%s<white>'<grey> to <green>%s",
                rule.key().value(), worldName, getColoredValue(value));
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private void printGamerule(CommandSender sender, String worldName, GameRule<?> rule) {
        if (worldName.equalsIgnoreCase("server")) {
            Utils.sendMiniTo(sender, "Gamerule values of <white>'<yellow>%s<white>'<grey>:", rule.key().value());
            for (World world : Bukkit.getWorlds()) {
                Utils.sendMiniTo(sender, "- <white>'<yellow>%s<white>' <grey>= %s", world.getName(), getColoredValue(world.getGameRuleValue(rule)));
            }
        } else {
            Object gameRuleValue = Bukkit.getWorld(worldName).getGameRuleValue(rule);
            Utils.sendMiniTo(sender, "Gamerule values of <white>'<yellow>%s<white>'<grey> for world <white>'<yellow>%s<white>' <grey>= %s",
                rule.key().value(), worldName, getColoredValue(gameRuleValue));
        }
    }

    private String getColoredValue(Object value) {
        if (value instanceof Boolean bool) {
            return bool ? "<green>true" : "<red>false";
        } else if (value instanceof Integer integer) {
            return "<aqua>" + integer;
        }
        throw new IllegalArgumentException("Invalid gamerule value: " + value);
    }

}
