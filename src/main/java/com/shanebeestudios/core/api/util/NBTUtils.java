package com.shanebeestudios.core.api.util;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TextComponentTagVisitor;
import net.minecraft.server.MinecraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.inventory.ItemStack;

/**
 * Utility methods for NBT
 */
public class NBTUtils {

    private NBTUtils() {

    }

    private static final RegistryAccess REGISTRY_ACCESS = MinecraftServer.getServer().registryAccess();

    /**
     * Get the full NBT of an ItemStack
     *
     * @param itemStack ItemStack to get NBT from
     * @return Full NBT of ItemStack
     */
    @SuppressWarnings("deprecation")
    public static NBTCompound getFullItem(ItemStack itemStack) {
        return NBTItem.convertItemtoNBT(itemStack);
    }

    /**
     * Get the vanilla NBT of an ItemStack
     * <br>This will include vanilla components
     *
     * @param itemStack ItemStack to get NBT from
     * @return Full NBT of ItemStack
     */
    public static NBTCompound getVanillaNBT(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = ((CraftItemStack) itemStack).handle;
        Tag vanillaTag = DataComponentMap.CODEC.encode(nmsItemStack.getComponents(), REGISTRY_ACCESS.createSerializationContext(NbtOps.INSTANCE), new CompoundTag()).getOrThrow();
        NBTContainer components = new NBTContainer(vanillaTag);
        NBTCompound itemCompound = getFullItem(itemStack);
        itemCompound.getOrCreateCompound("components").mergeCompound(components);
        return itemCompound;
    }

    /**
     * Get the pretty string of NBT
     * <br>This will look the same as the vanilla '/data' command
     *
     * @param compound Compound to get pretty nbt from
     * @param split    How the string is split/spaced out
     * @return Pretty string version of NBT
     */
    public static String getPrettyNBT(NBTCompound compound, String split) {
        // This is because NBTCompound#getCompound returns the parent compound
        NBTCompound copy = new NBTContainer(compound.toString());
        split = split != null ? split : "";
        TextComponentTagVisitor visitor = new TextComponentTagVisitor(split);
        return CraftChatMessage.fromComponent(visitor.visit((Tag) copy.getCompound()));
    }

}
