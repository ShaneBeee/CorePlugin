package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.core.api.util.Permissions;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

public class RepairCommand {

    public RepairCommand() {
        registerCommand();
    }

    private void registerCommand() {
        CommandTree command = new CommandTree("repair")
            .withPermission(Permissions.COMMANDS_REPAIR.get())
            .then(new MultiLiteralArgument("type", "hand", "all")
                .setOptional(true)
                .executesPlayer(info -> {
                    String type = info.args().getByClassOrDefault("type", String.class, "hand");
                    PlayerInventory inventory = info.sender().getInventory();
                    if (type.equalsIgnoreCase("hand")) {
                        ItemStack item = inventory.getItemInMainHand();
                        repairItem(item);
                    } else {
                        for (@Nullable ItemStack item : inventory.getContents()) {
                            if (item != null) repairItem(item);
                        }
                    }
                }));

        command.register();
    }

    private void repairItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) return;

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta instanceof Damageable damageable) {
            damageable.setDamage(0);
            itemStack.setItemMeta(itemMeta);
        }
    }

}
