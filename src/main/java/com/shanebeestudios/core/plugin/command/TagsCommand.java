package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.core.api.command.CustomArguments;
import com.shanebeestudios.core.api.util.Permissions;
import com.shanebeestudios.coreapi.util.TagUtils;
import com.shanebeestudios.coreapi.util.Utils;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.Keyed;
import org.bukkit.Registry;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class TagsCommand {

    public TagsCommand() {
        registerCommand();
    }

    private void registerCommand() {
        CommandTree command = new CommandTree("tags")
            .withShortDescription("See different tags for objects and values of tags.")
            .withPermission(Permissions.COMMANDS_TAGS.get());

        LiteralArgument tags = LiteralArgument.literal("tags");
        LiteralArgument values = LiteralArgument.literal("values");
        registerRegistry(tags, values, RegistryKey.BIOME);
        registerRegistry(tags, values, RegistryKey.BLOCK);
        registerRegistry(tags, values, RegistryKey.DAMAGE_TYPE);
        registerRegistry(tags, values, RegistryKey.ENCHANTMENT);
        registerRegistry(tags, values, RegistryKey.ENTITY_TYPE);
        registerRegistry(tags, values, RegistryKey.ITEM);
        registerRegistry(tags, values, RegistryKey.STRUCTURE);
        command.then(tags);
        command.then(values);

        command.register();
    }

    @SuppressWarnings("unchecked")
    private <T extends Keyed> void registerRegistry(LiteralArgument tags, LiteralArgument values, RegistryKey<T> registryKey) {
        String tagsName = registryKey.key().value();
        tags.then(LiteralArgument.literal(tagsName)
            .then(CustomArguments.getRegistryArgument(registryKey, tagsName)
                .executes(info -> {
                    T keyed = (T) info.args().getByClass(tagsName, Keyed.class);
                    if (keyed == null) return;
                    printTags(keyed, registryKey);
                })));

        String valuesName = registryKey.key().value();
        values.then(LiteralArgument.literal(valuesName)
            .then(CustomArguments.getTagArgument(valuesName + "tag", registryKey)
                .executes(info -> {
                    TagKey<T> tag = info.args().getByClass(valuesName + "tag", TagKey.class);
                    if (tag == null) return;
                    printTagValues(tag, registryKey);
                })));
    }

    @SuppressWarnings("NullableProblems")
    private <T extends Keyed> void printTags(T keyed, RegistryKey<T> registryKey) {
        List<String> keys = new ArrayList<>();

        TypedKey<T> typedKey = TypedKey.create(registryKey, keyed.key());
        Registry<T> registry = RegistryAccess.registryAccess().getRegistry(registryKey);
        TagUtils.getTagKeys(registryKey).forEach(key -> {
            if (registry.hasTag(key) && registry.getTag(key).contains(typedKey)) {
                keys.add(key.key().toString());
            }
        });

        Utils.log("Tags for " + registryKey.key() + " &r'&b" + keyed.getKey() + "&r'&7:");
        keys.forEach(key -> Utils.log("&7- &r#<#FFF270>" + key));
    }

    private <T extends Keyed> void printTagValues(TagKey<T> tagKey, RegistryKey<T> registry) {
        Utils.log("Values of " + registry.key() + " tag &r'&b#" + tagKey.key() + "&r'&7:");
        Tag<?> tag = TagUtils.getTag(tagKey);
        tag.values().forEach(value -> Utils.log("&7- &r<#FFF270>" + value.key()));
    }

}
