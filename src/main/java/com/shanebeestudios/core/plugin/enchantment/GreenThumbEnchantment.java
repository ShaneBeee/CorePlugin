package com.shanebeestudios.core.plugin.enchantment;

import com.shanebeestudios.core.api.registry.Enchantments;
import com.shanebeestudios.core.plugin.CorePlugin;
import com.shanebeestudios.coreapi.util.TagUtils;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.Tag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnstableApiUsage", "NullableProblems"})
public class GreenThumbEnchantment implements Listener {

    private final CorePlugin plugin;
    private final BukkitScheduler scheduler = Bukkit.getScheduler();
    private final Tag<BlockType> CROPS_TAG = TagUtils.getTag(BlockTypeTagKeys.CROPS);
    private final Tag<ItemType> HOES_TAG = TagUtils.getTag(ItemTypeTagKeys.HOES);

    public GreenThumbEnchantment(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (!canGreen(block, itemInHand)) return;

        BlockData blockData = block.getBlockData();
        if (blockData instanceof Ageable ageable) {
            this.scheduler.runTaskLater(this.plugin, () -> {
                ageable.setAge(0);
                block.setBlockData(ageable);
                growPlant(block);
            }, 0);
        }
    }

    private boolean canGreen(Block block, ItemStack itemStack) {
        TypedKey<BlockType> blockKey = TagUtils.getBlockTypedKey(block);
        if (CROPS_TAG.contains(blockKey)) {
            TypedKey<ItemType> itemKey = TagUtils.getItemTypedKey(itemStack);
            if (HOES_TAG.contains(itemKey)) {
                if (itemStack.getEnchantmentLevel(Enchantments.BEER_GREEN_THUMB) > 0) {
                    BlockData blockData = block.getBlockData();
                    return blockData instanceof Ageable ageable && ageable.getAge() == ageable.getMaximumAge();
                }
            }
        }
        return false;
    }

    private void growPlant(Block block) {
        BlockData blockData = block.getBlockData();
        if (blockData instanceof Ageable ageable) {
            Material oldType = block.getType();

            List<BukkitTask> tasks = new ArrayList<>();
            for (int age = 1; age <= ageable.getMaximumAge(); age++) {
                int finalAge = age;
                BukkitTask task = this.scheduler.runTaskLater(this.plugin, () -> {
                    if (block.getType() != oldType) {
                        tasks.forEach(BukkitTask::cancel);
                        tasks.clear();
                        return;
                    }
                    ageable.setAge(finalAge);
                    block.setBlockData(ageable);
                }, 20L * age);
                tasks.add(task);
            }
        }
    }

}
