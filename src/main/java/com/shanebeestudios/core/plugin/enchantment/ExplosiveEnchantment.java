package com.shanebeestudios.core.plugin.enchantment;

import com.shanebeestudios.core.api.registry.Enchantments;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class ExplosiveEnchantment implements Listener {

    private final Random random = new Random();

    @EventHandler
    private void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        GameMode gameMode = player.getGameMode();
        boolean damageItem = gameMode == GameMode.SURVIVAL || gameMode == GameMode.ADVENTURE;

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        int ench = itemInHand.getEnchantmentLevel(Enchantments.BEER_EXPLOSIVE);
        if (ench == 0) return;

        Block block = event.getBlock();
        if (!block.isSolid()) return;

        World world = block.getWorld();

        for (int x = -ench; x <= ench; x++) {
            for (int z = -ench; z <= ench; z++) {
                for (int y = -ench; y <= ench; y++) {
                    Block toBreak = block.getRelative(x, y, z);
                    if (toBreak == block || toBreak.getType() == Material.BEDROCK || !toBreak.isSolid()) continue;

                    toBreak.breakNaturally(itemInHand, true, true);
                    world.createExplosion(toBreak.getLocation(), 0, false, false);
                    if (damageItem) itemInHand.damage(1, player);
                    if (this.random.nextInt(100) < 5) {
                        toBreak.setType(Material.FIRE);
                    }
                }
            }
        }
    }

}
