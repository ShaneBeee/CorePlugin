package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.core.api.command.CustomArguments;
import com.shanebeestudios.core.api.util.Permissions;
import com.shanebeestudios.coreapi.util.Utils;
import dev.jorel.commandapi.AbstractArgumentTree;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.EnchantmentArgument;
import dev.jorel.commandapi.arguments.FloatArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.ListArgumentBuilder;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.NamespacedKeyArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.ShieldMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

@SuppressWarnings({"deprecation", "UnstableApiUsage"})
public class ModifyItemCommand {

    private static final String[] OPERATIONS = Arrays.stream(Operation.values()).map(operation -> operation.name().toLowerCase(Locale.ROOT)).toArray(String[]::new);

    public ModifyItemCommand() {
        registerCommand();
    }

    private void registerCommand() {
        CommandTree command = new CommandTree("modifyitem")
            .withPermission(Permissions.COMMANDS_MODIFY_ITEM.get())
            .then(attribute())
            .then(enchantment())
            .then(food())
            .then(color())
            .then(lore())
            .then(name())
            .then(potion())
            .then(tool());
        command.register();
    }

    private AbstractArgumentTree<?, Argument<?>, CommandSender> attribute() {
        return LiteralArgument.literal("attributemodifier")
            .then(CustomArguments.getRegistryArgument(Registry.ATTRIBUTE, "attribute")
                .then(new NamespacedKeyArgument("key")
                    .then(new DoubleArgument("amount")
                        .then(new MultiLiteralArgument("operation", OPERATIONS)
                            .then(CustomArguments.getSlotGroupArgument("slot")
                                .executesPlayer(info -> {
                                    CommandArguments args = info.args();
                                    Attribute attribute = args.getByClass("attribute", Attribute.class);
                                    NamespacedKey key = args.getByClass("key", NamespacedKey.class);
                                    Double amount = args.getByClass("amount", Double.class);
                                    String operationString = args.getByClass("operation", String.class);
                                    EquipmentSlotGroup slot = args.getByClass("slot", EquipmentSlotGroup.class);

                                    assert attribute != null;
                                    assert key != null;
                                    assert operationString != null;
                                    assert amount != null;
                                    assert slot != null;
                                    Operation operation = Operation.valueOf(operationString.toUpperCase(Locale.ROOT));

                                    AttributeModifier modifier = new AttributeModifier(key, amount, operation, slot);
                                    modifyItemStack(info.sender(), (itemMeta, fail) -> itemMeta.addAttributeModifier(attribute, modifier));
                                }))))));
    }

    private AbstractArgumentTree<?, Argument<?>, CommandSender> color() {
        return LiteralArgument.literal("color")
            .then(new IntegerArgument("red", 0, 255)
                .then(new IntegerArgument("green", 0, 255)
                    .then(new IntegerArgument("blue", 0, 255)
                        .executesPlayer(info -> {
                            Integer red = info.args().getByClass("red", Integer.class);
                            Integer green = info.args().getByClass("green", Integer.class);
                            Integer blue = info.args().getByClass("blue", Integer.class);
                            assert red != null;
                            assert green != null;
                            assert blue != null;
                            Color color = Color.fromRGB(red, green, blue);
                            modifyItemStack(info.sender(), (itemMeta, fail) -> {
                                if (itemMeta instanceof PotionMeta potionMeta) {
                                    potionMeta.setColor(color);
                                } else if (itemMeta instanceof LeatherArmorMeta leatherArmorMeta) {
                                    leatherArmorMeta.setColor(color);
                                } else if (itemMeta instanceof ShieldMeta shieldMeta) {
                                    shieldMeta.setBaseColor(DyeColor.getByColor(color));
                                }
                            });
                        }))));
    }

    private AbstractArgumentTree<?, Argument<?>, CommandSender> enchantment() {
        return LiteralArgument.literal("enchantment")
            .then(new EnchantmentArgument("value")
                .then(new IntegerArgument("level")
                    .executesPlayer(info -> {
                        Enchantment enchantment = info.args().getByClass("value", Enchantment.class);
                        Integer level = info.args().getByClass("level", int.class);
                        modifyItemStack(info.sender(), (itemMeta, fail) -> {
                            assert enchantment != null;
                            assert level != null;
                            itemMeta.addEnchant(enchantment, level, true);
                        });
                    })));
    }

    private AbstractArgumentTree<?, Argument<?>, CommandSender> food() {
        return LiteralArgument.literal("food")
            .then(new IntegerArgument("nutrition", 0)
                .then(new FloatArgument("saturation")
                    .then(new BooleanArgument("can_always_eat")
                        .setOptional(true)
                        .then(new FloatArgument("eat_seconds")
                            .setOptional(true)
                            .executesPlayer(info -> {
                                Integer nutrition = info.args().getByClass("nutrition", Integer.class);
                                Float saturation = info.args().getByClass("saturation", Float.class);
                                Optional<Boolean> canAlwaysEat = info.args().getOptionalByClass("can_always_eat", Boolean.class);
                                Optional<Float> eatSeconds = info.args().getOptionalByClass("eat_seconds", Float.class);
                                assert nutrition != null;
                                assert saturation != null;
                                modifyItemStack(info.sender(), (itemMeta, fail) -> {
                                    FoodComponent food = itemMeta.getFood();
                                    food.setNutrition(nutrition);
                                    food.setSaturation(saturation);
                                    food.setCanAlwaysEat(canAlwaysEat.orElse(false));
                                    food.setEatSeconds(eatSeconds.orElse(1.6F));
                                    itemMeta.setFood(food);
                                });
                            })))));
    }

    private AbstractArgumentTree<?, Argument<?>, CommandSender> lore() {
        return LiteralArgument.literal("lore")
            .then(LiteralArgument.literal("set")
                .then(new GreedyStringArgument("value")
                    .executesPlayer(info -> {
                        String value = info.args().getByClass("value", String.class);
                        List<String> lore = new ArrayList<>();
                        assert value != null;
                        for (String s : value.split("\\|\\|")) {
                            lore.add(Utils.getColString(s));
                        }
                        modifyItemStack(info.sender(), (itemMeta, fail) -> itemMeta.setLore(lore));
                    })))
            .then(LiteralArgument.literal("add")
                .then(new GreedyStringArgument("value")
                    .executesPlayer(info -> {
                        String value = info.args().getByClass("value", String.class);
                        modifyItemStack(info.sender(), (itemMeta, fail) -> {
                            List<String> list = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
                            assert list != null;
                            assert value != null;
                            list.add(Utils.getColString(value));
                            itemMeta.setLore(list);
                        });
                    })));
    }

    private AbstractArgumentTree<?, Argument<?>, CommandSender> name() {
        return LiteralArgument.literal("name")
            .then(new GreedyStringArgument("value")
                .executesPlayer(info -> {
                    String name = info.args().getByClass("value", String.class);
                    modifyItemStack(info.sender(), (itemMeta, fail) -> itemMeta.setDisplayName(name));
                }));
    }

    private AbstractArgumentTree<?, Argument<?>, CommandSender> potion() {
        return LiteralArgument.literal("potion")
            .then(CustomArguments.getRegistryArgument(Registry.POTION_EFFECT_TYPE, "type")
                .then(LiteralArgument.literal("infinite")
                    .then(getSubPotion()))
                .then(new IntegerArgument("seconds")
                    .then(getSubPotion())));
    }

    private AbstractArgumentTree<?, Argument<?>, CommandSender> getSubPotion() {
        return new IntegerArgument("amplifier")
            .setOptional(true)
            .then(new BooleanArgument("ambient")
                .setOptional(true)
                .then(new BooleanArgument("particle")
                    .setOptional(true)
                    .then(new BooleanArgument("icon")
                        .setOptional(true)
                        .then(new FloatArgument("probability", 0, 1)
                            .setOptional(true)
                            .executesPlayer(info -> {
                                CommandArguments args = info.args();
                                PotionEffectType type = args.getByClass("type", PotionEffectType.class);
                                int ticks;
                                if (args.argsMap().containsKey("seconds")) {
                                    Integer seconds = args.getByClass("seconds", Integer.class);
                                    assert seconds != null;
                                    if (seconds > 0) ticks = seconds * 20;
                                    else ticks = -1;
                                } else {
                                    ticks = -1;
                                }
                                Optional<Integer> amplifier = args.getOptionalByClass("amplifier", Integer.class);
                                Optional<Boolean> ambient = args.getOptionalByClass("ambient", Boolean.class);
                                Optional<Boolean> particle = args.getOptionalByClass("particle", Boolean.class);
                                Optional<Boolean> icon = args.getOptionalByClass("icon", Boolean.class);
                                Optional<Float> probability = args.getOptionalByClass("probability", Float.class);

                                assert type != null;
                                PotionEffect potionEffect = new PotionEffect(type,
                                    ticks,
                                    amplifier.orElse(0),
                                    ambient.orElse(true),
                                    particle.orElse(true),
                                    icon.orElse(true));

                                modifyItemStack(info.sender(), (itemMeta, fail) -> {
                                    if (itemMeta instanceof PotionMeta potionMeta) {
                                        potionMeta.addCustomEffect(potionEffect, true);
                                    } else if (itemMeta.hasFood()) {
                                        FoodComponent food = itemMeta.getFood();
                                        food.addEffect(potionEffect, probability.orElse(1f));
                                        itemMeta.setFood(food);
                                    } else {
                                        fail.set("Item is neither a potion nor a food.");
                                    }
                                });
                            })))));
    }

    @SuppressWarnings("unchecked")
    private AbstractArgumentTree<?, Argument<?>, CommandSender> tool() {
        return LiteralArgument.literal("tool")
            .then(LiteralArgument.literal("set")
                .then(new FloatArgument("default_mining_speed")
                    .then(new IntegerArgument("damage_per_block", 0)
                        .then(new IntegerArgument("max_damage", 0)
                            .setOptional(true)
                            .executesPlayer(info -> {
                                Float defaultMiningSpeed = info.args().getByClass("default_mining_speed", Float.class);
                                Integer damagePerBlock = info.args().getByClass("damage_per_block", Integer.class);
                                Optional<Integer> maxDamage = info.args().getOptionalByClass("max_damage", Integer.class);

                                assert defaultMiningSpeed != null;
                                assert damagePerBlock != null;
                                modifyItemStack(info.sender(), (itemMeta, fail) -> {
                                    ToolComponent tool = itemMeta.getTool();
                                    tool.setDefaultMiningSpeed(defaultMiningSpeed);
                                    tool.setDamagePerBlock(damagePerBlock);
                                    itemMeta.setTool(tool);
                                    maxDamage.ifPresent(integer -> {
                                        Damageable damageable = (Damageable) itemMeta;
                                        damageable.setMaxDamage(integer);
                                        damageable.setDamage(0);
                                    });
                                });
                            })))))
            .then(LiteralArgument.literal("rule")
                .then(new FloatArgument("speed")
                    .then(new BooleanArgument("correct_for_drops")
                        .then(LiteralArgument.literal("blocks")
                            .then(new ListArgumentBuilder<Material>("values")
                                .withList(Arrays.stream(Material.values()).filter(material -> !material.isLegacy() && material.isBlock()).toList())
                                .withMapper(material -> material.getKey().toString())
                                .buildGreedy()
                                .executesPlayer(info -> {
                                    Float speed = info.args().getByClass("speed", Float.class);
                                    Boolean correctForDrops = info.args().getByClass("correct_for_drops", Boolean.class);
                                    List<Material> blocks = (List<Material>) info.args().get("values");
                                    assert blocks != null;

                                    modifyItemStack(info.sender(), (itemMeta, fail) -> {
                                        if (!itemMeta.hasTool()) {
                                            fail.set("Item is not a tool");
                                            return;
                                        }
                                        ToolComponent tool = itemMeta.getTool();
                                        tool.addRule(blocks, speed, correctForDrops);
                                        itemMeta.setTool(tool);
                                    });
                                })))
                        .then(LiteralArgument.literal("tag")
                            .then(CustomArguments.getTagArgument("value", Tag.REGISTRY_BLOCKS)
                                .executesPlayer(info -> {
                                    Float speed = info.args().getByClass("speed", Float.class);
                                    Boolean correctForDrops = info.args().getByClass("correct_for_drops", Boolean.class);
                                    Tag<Material> value = info.args().getByClass("value", Tag.class);
                                    assert value != null;

                                    modifyItemStack(info.sender(), (itemMeta, fail) -> {
                                        if (!itemMeta.hasTool()) {
                                            fail.set("Item is not a tool");
                                            return;
                                        }
                                        ToolComponent tool = itemMeta.getTool();
                                        tool.addRule(value, speed, correctForDrops);
                                        itemMeta.setTool(tool);
                                    });
                                }))))));
    }

    private void modifyItemStack(Player player, BiConsumer<ItemMeta, AtomicReference<String>> meta) throws WrapperCommandSyntaxException {
        AtomicReference<String> failure = new AtomicReference<>();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.isEmpty()) throw CommandAPI.failWithString("Invalid item in hand!");

        ItemMeta itemMeta = item.getItemMeta();
        meta.accept(itemMeta, failure);
        item.setItemMeta(itemMeta);
        if (failure.get() != null) {
            throw CommandAPI.failWithString(failure.get());
        }
    }

}
