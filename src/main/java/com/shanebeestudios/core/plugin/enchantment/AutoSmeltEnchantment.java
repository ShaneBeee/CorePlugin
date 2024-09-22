package com.shanebeestudios.core.plugin.enchantment;

import com.shanebeestudios.core.api.registry.Enchantments;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class AutoSmeltEnchantment implements Listener {

    private final Map<Material, Material> smeltingMap = new HashMap<>();

    public AutoSmeltEnchantment() {
        Bukkit.recipeIterator().forEachRemaining(recipe -> {
            if (recipe instanceof CookingRecipe<?> cookingRecipe) {
                Material resultType = cookingRecipe.getResult().getType();
                RecipeChoice inputChoice = cookingRecipe.getInputChoice();
                if (inputChoice instanceof RecipeChoice.ExactChoice exactChoice) {
                    exactChoice.getChoices().forEach(itemStack -> this.smeltingMap.put(itemStack.getType(), resultType));
                } else if (inputChoice instanceof RecipeChoice.MaterialChoice materialChoice) {
                    materialChoice.getChoices().forEach(material -> this.smeltingMap.put(material, resultType));
                }
            }
        });
    }

    @EventHandler
    private void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        GameMode gameMode = player.getGameMode();
        if (gameMode != GameMode.SURVIVAL && gameMode != GameMode.ADVENTURE) return;

        Block block = event.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!canAutoSmelt(block, tool)) return;

        World world = block.getWorld();
        Location location = block.getLocation().clone().add(0.5, 0.5, 0.5);

        block.getDrops(tool, player).forEach(drop -> {
            Material toDrop = this.smeltingMap.get(drop.getType());
            if (toDrop != null) {
                event.setDropItems(false); // Only cancel drops if we found a replacement

                Item item = world.dropItem(location, new ItemStack(toDrop, drop.getAmount()));
                item.setVelocity(new Vector(0, 0.1, 0));
            }
        });

    }

    private boolean canAutoSmelt(Block block, ItemStack itemStack) {
        if (Tag.MINEABLE_PICKAXE.isTagged(block.getType())) {
            if (Tag.ITEMS_PICKAXES.isTagged(itemStack.getType())) {
                return itemStack.containsEnchantment(Enchantments.BEER_AUTO_SMELT);
            }
        }
        return false;
    }

}
