package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.core.api.command.CustomArguments;
import com.shanebeestudios.core.api.util.Permissions;
import com.shanebeestudios.core.api.util.Util;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.BlockStateArgument;
import dev.jorel.commandapi.arguments.ItemStackArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TagsCommand {

    public TagsCommand() {
        registerCommand();
    }

    @SuppressWarnings("unchecked")
    private void registerCommand() {
        CommandTree command = new CommandTree("tags")
            .withPermission(Permissions.COMMANDS_TAGS.get())
            .then(LiteralArgument.literal("blocktags")
                .then(new BlockStateArgument("block")
                    .executes(info -> {
                        BlockData blockdata = info.args().getByClass("block", BlockData.class);
                        if (blockdata == null) return;
                        printTags(blockdata.getMaterial(), Tag.REGISTRY_BLOCKS);
                    })))
            .then(LiteralArgument.literal("itemtags")
                .then(new ItemStackArgument("item")
                    .executes(info -> {
                        ItemStack item = info.args().getByClass("item", ItemStack.class);
                        if (item == null) return;
                        printTags(item.getType(), Tag.REGISTRY_ITEMS);
                    })))
            .then(LiteralArgument.literal("blocks")
                .then(CustomArguments.getTagArgument("blocktag", Tag.REGISTRY_BLOCKS)
                    .executes(info -> {
                        Tag<Material> tag = info.args().getByClass("blocktag", Tag.class);
                        if (tag == null) return;
                        printTagValues(tag, Tag.REGISTRY_BLOCKS);
                    })))
            .then(LiteralArgument.literal("items")
                .then(CustomArguments.getTagArgument("itemtag", Tag.REGISTRY_ITEMS)
                    .executes(info -> {
                        Tag<Material> tag = info.args().getByClass("itemtag", Tag.class);
                        if (tag == null) return;
                        printTagValues(tag, Tag.REGISTRY_ITEMS);
                    })));


        command.register("core");
    }

    private void printTags(Material material, String registry) {
        List<String> keys = new ArrayList<>();
        Bukkit.getTags(registry, Material.class).forEach(tag -> {
            if (tag.isTagged(material)) keys.add(tag.getKey().toString());
        });

        Util.log("Tags for " + registry.replace("s", "") + " &r'&b" + material.getKey() + "&r'&7:");
        keys.forEach(key -> Util.log("&7- &r#<#FFF270>" + key));
    }

    private void printTagValues(Tag<Material> tag, String registry) {
        Util.log(StringUtils.capitalize(registry) + " for tag &r'&b#" + tag.getKey() + "&r'&7:");
        tag.getValues().forEach(value -> Util.log("&7- &r<#FFF270>" + value.getKey()));
    }

}
