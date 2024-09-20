package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.core.api.util.Permissions;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.Collection;

public class HealCommand {

    public HealCommand() {
        registerCommand();
    }

    @SuppressWarnings("unchecked")
    private void registerCommand() {
        CommandTree command = new CommandTree("heal")
            .withPermission(Permissions.COMMANDS_HEAL.get())
            .executesPlayer(info -> {
                healPlayer(info.sender());
            })
            .then(new EntitySelectorArgument.ManyPlayers("players")
                .withPermission(Permissions.COMMANDS_HEAL_OTHER.get())
                .executes((sender, args) -> {
                    Collection<Player> players = (Collection<Player>) args.get("players");
                    assert players != null;
                    players.forEach(this::healPlayer);
                }));

        command.register("core");
    }

    @SuppressWarnings("DataFlowIssue")
    private void healPlayer(Player player) {
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(maxHealth);
        player.setFoodLevel(20);
        player.setFireTicks(0);
    }

}
