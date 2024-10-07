package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.coreapi.util.Utils;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import org.bukkit.Bukkit;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class NoMonstersCommand implements Listener {

    private boolean noMonsters = true;

    public NoMonstersCommand() {
        registerCommand();
    }

    @SuppressWarnings("DataFlowIssue")
    private void registerCommand() {
        CommandTree command = new CommandTree("nomonsters")
            .withShortDescription("Disable monster spawning.")
            .executes((sender, args) -> {
                String enabled = this.noMonsters ? "&aEnabled" : "&cDisabled";
                Utils.sendTo(sender, "NoMonsters is currently %s", enabled);
            })
            .then(new MultiLiteralArgument("type", "enable", "disable")
                .executes((sender, args) -> {
                    if (args.getByClass("type", String.class).equalsIgnoreCase("enable")) {
                        this.noMonsters = true;
                        Utils.sendTo(sender, "NoMonsters is now &aEnabled &7monsters will no longer spawn");
                        deleteMonsters();
                    } else {
                        this.noMonsters = false;
                        Utils.sendTo(sender, "NoMonsters is now &cDisabled &7monsters will now spawn");
                    }
                }));

        command.register();
    }

    private void deleteMonsters() {
        Bukkit.getWorlds().forEach(world -> world.getEntities().forEach(entity -> {
            if (entity instanceof Bat || entity instanceof Enemy) entity.remove();
        }));
    }

    @EventHandler
    private void onSpawn(CreatureSpawnEvent event) {
        if (!this.noMonsters) return;
        SpawnReason reason = event.getSpawnReason();
        if (reason == SpawnReason.COMMAND || reason == SpawnReason.CUSTOM) return;


        LivingEntity entity = event.getEntity();
        if (entity instanceof Enemy || entity instanceof Bat) {
            event.setCancelled(true);
        }
    }

}
