package com.shanebeestudios.core.plugin.enchantment;

import com.shanebeestudios.core.api.registry.Enchantments;
import com.shanebeestudios.core.plugin.CorePlugin;
import com.shanebeestudios.coreapi.util.TagUtils;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys;
import io.papermc.paper.registry.tag.Tag;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.scheduler.BukkitScheduler;

@SuppressWarnings({"UnstableApiUsage", "NullableProblems"})
public class FellerEnchantment implements Listener {

    private final CorePlugin plugin;
    private final BukkitScheduler scheduler = Bukkit.getScheduler();
    private final Tag<BlockType> LOGS_TAG = TagUtils.getTag(BlockTypeTagKeys.LOGS);

    public FellerEnchantment(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        TypedKey<BlockType> blockKey = TagUtils.getBlockTypedKey(block);
        if (!LOGS_TAG.contains(blockKey)) return;

        Player player = event.getPlayer();
        ItemStack handItem = player.getInventory().getItemInMainHand();
        int fellerLevel = handItem.getEnchantmentLevel(Enchantments.BEER_FELLER);
        if (fellerLevel == 0) return;

        int delayTicks = 6 - fellerLevel;
        fell(block, player, fellerLevel, delayTicks);
    }

    private void fell(Block block, Player player, int fellerLevel, int delayTicks) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    Block relative = block.getRelative(x, y, z);
                    ItemStack handItem = player.getInventory().getItemInMainHand();
                    if (!canFell(relative, handItem, fellerLevel)) continue;


                    if (delayTicks > 0) {
                        this.scheduler.runTaskLater(this.plugin, () ->
                            processBreak(relative, player, fellerLevel, delayTicks), delayTicks);
                    } else {
                        processBreak(relative, player, fellerLevel, delayTicks);
                    }
                }
            }
        }
    }

    private boolean canFell(Block block, ItemStack handItem, int fellerLevel) {
        TypedKey<BlockType> blockKey = TagUtils.getBlockTypedKey(block);
        if (!LOGS_TAG.contains(blockKey)) return false;
        if (!(handItem.getItemMeta() instanceof Damageable damageable)) return false;
        if (damageable.hasMaxDamage() && damageable.getDamage() >= damageable.getMaxDamage()) return false;
        return handItem.getEnchantmentLevel(Enchantments.BEER_FELLER) == fellerLevel;
    }

    private void breakTool(Player player) {
        GameMode gameMode = player.getGameMode();
        if (gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR) return;
        player.getInventory().getItemInMainHand().damage(1, player);
    }

    private void processBreak(Block block, Player player, int fellerLevel, int delayTicks) {
        block.breakNaturally(player.getInventory().getItemInMainHand(), true, true);
        breakTool(player);
        fell(block, player, fellerLevel, delayTicks);
    }

}
