package com.shanebeestudios.core.command;

import com.shanebeestudios.core.api.util.Permissions;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.WorldArgument;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Optional;

public class RemoveEntityCommand {

    public RemoveEntityCommand() {
        registerCommand();
    }

    @SuppressWarnings("DataFlowIssue")
    private void registerCommand() {
        CommandTree command = new CommandTree("removeentity")
            .withShortDescription("Remove entities with an option to only remove from a specific world")
            .withPermission(Permissions.COMMANDS_REMOVE_ENTITY.get())
            .then(new EntitySelectorArgument.ManyEntities("entities")
                .then(new WorldArgument("world")
                    .setOptional(true)
                    .executes((sender, args) -> {
                        Collection<Entity> entities = args.getUnchecked("entities");
                        Optional<World> world = args.getOptionalByClass("world", World.class);

                        entities.stream().filter(entity -> {
                            // We don't want to remove players
                            if (entity instanceof Player) return false;
                            return world.isEmpty() || world.get().equals(entity.getWorld());
                        }).forEach(Entity::remove);
                    })));
        command.register("core");
    }

}
