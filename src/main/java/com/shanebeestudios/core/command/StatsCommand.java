package com.shanebeestudios.core.command;

import com.shanebeestudios.core.CorePlugin;
import com.shanebeestudios.core.stats.Stats;
import com.shanebeestudios.core.util.Permissions;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsCommand {

    private final Map<String, Stats> statsMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    public StatsCommand(CorePlugin plugin) {
        statsMap.put("sidebar", plugin.getStatsSidebar());
        statsMap.put("biomebar", plugin.getStatsBiomebar());
        statsMap.put("rambar", plugin.getStatsRambar());
        statsMap.put("all", null);

        CommandTree statsCommand = new CommandTree("stats");
        statsCommand.executesPlayer(info -> {
            List<Player> sender = List.of(info.sender());
            runStat(null, sender, "toggle");
        });
        statsMap.forEach((statKey, stat) -> {
            // Fetch permissions for subcommand
            Permissions perm = Permissions.getCommandPermissions("stats." + statKey);
            Permissions permOther = Permissions.getCommandPermissions("stats." + statKey + ".other");

            // Register subcommand
            statsCommand.then(LiteralArgument.literal(statKey)
                .withPermission(perm.get())
                .then(new EntitySelectorArgument.ManyPlayers("players")
                    .withPermission(permOther.get())
                    .then(new StringArgument("operation")
                        .includeSuggestions(ArgumentSuggestions.strings("toggle", "enable", "disable"))
                        .setOptional(true)
                        .executes((sender, args) -> {
                            Collection<Player> players = args.getByClass("players", Collection.class);
                            String operation = args.getByClassOrDefault("operation", String.class, "toggle");
                            runStat(stat, players, operation);
                        })))
                .executesPlayer(info -> {
                    runStat(stat, List.of(info.sender()), "toggle");
                })
            );
        });

        statsCommand.register("core");
    }

    private void runStat(Stats stat, Collection<Player> players, String operation) {
        if (stat == null) {
            this.statsMap.values().forEach(stats -> {
                if (stats == null) return;
                switch (operation) {
                    case "toggle" -> players.forEach(stats::toggle);
                    case "enable" -> players.forEach(stats::enable);
                    case "disable" -> players.forEach(stats::disable);
                }
            });
        } else {
            switch (operation) {
                case "toggle" -> players.forEach(stat::toggle);
                case "enable" -> players.forEach(stat::enable);
                case "disable" -> players.forEach(stat::disable);
            }
        }
    }

}
