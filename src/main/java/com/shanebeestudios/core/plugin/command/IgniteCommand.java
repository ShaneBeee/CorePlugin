package com.shanebeestudios.core.plugin.command;

import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import org.bukkit.entity.Entity;

import java.util.Collection;

public class IgniteCommand {

    public IgniteCommand() {
        registerCommand();
    }

    @SuppressWarnings("unchecked")
    private void registerCommand() {
        CommandTree command = new CommandTree("ignite")
            .withAliases("burn")
            .then(new EntitySelectorArgument.ManyEntities("entity")
                .then(new IntegerArgument("seconds")
                    .setOptional(true)
                    .executes((sender, args) -> {
                        int seconds = args.getByClassOrDefault("seconds", int.class, 5);
                        Collection<Entity> entities = (Collection<Entity>) args.get("entity");
                        assert entities != null;
                        entities.forEach(entity -> entity.setFireTicks(seconds * 20));
                    })));

        command.register();
    }
}
