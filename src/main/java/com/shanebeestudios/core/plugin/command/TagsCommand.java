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
import java.util.Collection;
import java.util.Comparator;
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
        LiteralArgument alltags = LiteralArgument.literal("alltags");
        registerRegistry(tags, values, alltags, RegistryKey.ATTRIBUTE);
        registerRegistry(tags, values, alltags, RegistryKey.BIOME);
        registerRegistry(tags, values, alltags, RegistryKey.BLOCK);
        registerRegistry(tags, values, alltags, RegistryKey.DAMAGE_TYPE);
        registerRegistry(tags, values, alltags, RegistryKey.DATA_COMPONENT_TYPE);
        registerRegistry(tags, values, alltags, RegistryKey.DIALOG);
        registerRegistry(tags, values, alltags, RegistryKey.ENCHANTMENT);
        registerRegistry(tags, values, alltags, RegistryKey.ENTITY_TYPE);
        registerRegistry(tags, values, alltags, RegistryKey.GAME_EVENT);
        registerRegistry(tags, values, alltags, RegistryKey.GAME_RULE);
        registerRegistry(tags, values, alltags, RegistryKey.ITEM);
        registerRegistry(tags, values, alltags, RegistryKey.MOB_EFFECT);
        registerRegistry(tags, values, alltags, RegistryKey.SOUND_EVENT);
        registerRegistry(tags, values, alltags, RegistryKey.STRUCTURE);
        command.then(tags);
        command.then(values);
        command.then(alltags);

        command.register();
    }

    @SuppressWarnings("unchecked")
    private <T extends Keyed> void registerRegistry(LiteralArgument tags, LiteralArgument values, LiteralArgument alltags, RegistryKey<T> registryKey) {
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

        alltags.then(LiteralArgument.literal(tagsName)
            .executes(info -> {
                printAllTags(registryKey);
            }));
    }

    private <T extends Keyed> void printAllTags(RegistryKey<T> registryKey) {
        Registry<T> registry = RegistryAccess.registryAccess().getRegistry(registryKey);
        Collection<Tag<T>> tags = registry.getTags();
        if (!tags.isEmpty()) {
            Utils.logMini("All tags for registry <white>'<aqua>%s<white>'<grey>:", registryKey.key());
            tags.stream().sorted(Comparator.comparing(tag -> tag.tagKey().key().toString())).forEach(tag -> {
                Utils.logMini("<grey>- <white>#<#FFF270>%s", tag.tagKey().key());
            });
        } else {
            Utils.logMini("<red>No tags found for registry <white>'<aqua>%s<white>'<grey>", registryKey.key());

        }
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

        Utils.logMini("Tags for " + registryKey.key() + " <white>'<aqua>" + keyed.getKey() + "<white>'<grey>:");
        keys.forEach(key -> Utils.logMini("<grey>- <white>#<#FFF270>" + key));
    }

    private <T extends Keyed> void printTagValues(TagKey<T> tagKey, RegistryKey<T> registry) {
        Utils.logMini("Values of " + registry.key() + " tag <white>'<aqua>#" + tagKey.key() + "<white>'<grey>:");
        Tag<?> tag = TagUtils.getTag(tagKey);
        tag.values().forEach(value -> Utils.logMini("<grey>- <white><#FFF270>" + value.key()));
    }

}
