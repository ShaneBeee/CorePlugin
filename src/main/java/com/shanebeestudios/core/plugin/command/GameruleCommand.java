package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.coreapi.util.Utils;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GameruleCommand {

    public GameruleCommand() {
        registerCommand();
    }

    private void registerCommand() {
        CommandAPI.unregister("gamerule");
        CommandTree command = new CommandTree("gamerule");
        StringArgument worldArg = createWorldArg();
        for (@NotNull GameRule<?> value : GameRule.values()) {
            if (value.getType() == Boolean.class) {
                worldArg.then(createBooleanArg(value.getName()));
            } else if (value.getType() == Integer.class) {
                worldArg.then(createIntegerArg(value.getName()));
            }
        }

        command.then(worldArg);
        command.register();
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

    @SuppressWarnings({"unchecked", "DataFlowIssue"})
    private LiteralArgument createBooleanArg(String gameruleName) {
        LiteralArgument literal = LiteralArgument.literal(gameruleName);
        literal.then(new BooleanArgument("value")
            .executes(info -> {
                String worldName = info.args().getByClass("world", String.class);
                Boolean value = info.args().getByClass("value", Boolean.class);
                GameRule<Boolean> gameRule = (GameRule<Boolean>) GameRule.getByName(gameruleName);
                setGamerule(info.sender(), worldName, gameRule, value);
            }));
        literal.executes(info -> {
            String worldName = info.args().getByClass("world", String.class);
            GameRule<Integer> gameRule = (GameRule<Integer>) GameRule.getByName(gameruleName);
            printGamerule(info.sender(), worldName, gameRule);
        });
        return literal;
    }

    @SuppressWarnings({"unchecked", "DataFlowIssue"})
    private LiteralArgument createIntegerArg(String gameruleName) {
        LiteralArgument literal = LiteralArgument.literal(gameruleName);
        literal.then(new IntegerArgument("value")
            .executes(info -> {
                String worldName = info.args().getByClass("world", String.class);
                Integer value = info.args().getByClass("value", Integer.class);
                GameRule<Integer> gameRule = (GameRule<Integer>) GameRule.getByName(gameruleName);
                setGamerule(info.sender(), worldName, gameRule, value);
            }));
        literal.executes(info -> {
            String worldName = info.args().getByClass("world", String.class);
            GameRule<Integer> gameRule = (GameRule<Integer>) GameRule.getByName(gameruleName);
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
            Utils.sendTo(sender, "Set gamerule &r'&e%s&r'&7 for all worlds to &a%s",
                rule.getName(), getColoredValue(value));
        } else {
            Bukkit.getWorld(worldName).setGameRule(rule, value);
            Utils.sendTo(sender, "Set gamerule &r'&e%s&r'&7 for world &r'&e%s&r'&7 to &a%s",
                rule.getName(), worldName, getColoredValue(value));
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private void printGamerule(CommandSender sender, String worldName, GameRule<?> rule) {
        if (worldName.equalsIgnoreCase("server")) {
            Utils.sendTo(sender, "Gamerule values of &r'&e%s&r'&7:", rule.getName());
            for (World world : Bukkit.getWorlds()) {
                Utils.sendTo(sender, "- &r'&e%s&r' &7= %s", world.getName(), getColoredValue(world.getGameRuleValue(rule)));
            }
        } else {
            Object gameRuleValue = Bukkit.getWorld(worldName).getGameRuleValue(rule);
            Utils.sendTo(sender, "Gamerule values of &r'&e%s&r'&7 for world &r'&e%s&r' &7= %s",
                rule.getName(), worldName, getColoredValue(gameRuleValue));
        }
    }

    private String getColoredValue(Object value) {
        if (value instanceof Boolean bool) {
            return bool ? "&atrue" : "&cfalse";
        } else if (value instanceof Integer integer) {
            return "&b" + integer;
        }
        throw new IllegalArgumentException("Invalid gamerule value: " + value);
    }

}
