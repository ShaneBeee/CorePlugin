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
import org.bukkit.block.DecoratedPot;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.ShieldMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

@SuppressWarnings({"deprecation", "UnstableApiUsage", "unchecked"})
public class ModifyItemCommand {

    private String[] sherds;

    public ModifyItemCommand() {
        createSherds();
        registerCommand();
    }

    private void createSherds() {
        List<String> sherds = new ArrayList<>();
        for (Material material : Material.values()) {
            if (material.isLegacy()) continue;
            if (material != Material.BRICK && !Tag.ITEMS_DECORATED_POT_SHERDS.isTagged(material))
                continue;

            sherds.add(material.getKey().getKey());
        }
        this.sherds = sherds.toArray(new String[0]);
    }

    private void registerCommand() {
        CommandTree command = new CommandTree("modifyitem")
            .withPermission(Permissions.COMMANDS_MODIFY_ITEM.get())
            .then(attribute())
            .then(color())
            .then(damage())
            .then(enchantment())
            .then(food())
            .then(glint())
            .then(itemFlag())
            .then(lore())
            .then(name())
            .then(potion())
            .then(sherd())
            .then(stackSize())
            .then(tool())
            .then(trim());
        command.register();
    }

    private AbstractArgumentTree<?, Argument<?>, CommandSender> attribute() {
        return LiteralArgument.literal("attributemodifier")
            .then(CustomArguments.getRegistryArgument(Registry.ATTRIBUTE, "attribute")
                .then(new NamespacedKeyArgument("key")
                    .then(new DoubleArgument("amount")
                        .then(CustomArguments.getEnumArgument(Operation.class, "operation")
                            .then(CustomArguments.getSlotGroupArgument("slot")
                                .executesPlayer(info -> {
                                    CommandArguments args = info.args();
                                    Attribute attribute = args.getByClass("attribute", Attribute.class);
                                    NamespacedKey key = args.getByClass("key", NamespacedKey.class);
                                    Double amount = args.getByClass("amount", Double.class);
                                    Operation operation = args.getByClass("operation", Operation.class);
                                    EquipmentSlotGroup slot = args.getByClass("slot", EquipmentSlotGroup.class);

                                    assert attribute != null;
                                    assert key != null;
                                    assert operation != null;
                                    assert amount != null;
                                    assert slot != null;

                                    AttributeModifier modifier = new AttributeModifier(key, amount, operation, slot);
                                    modifyItemStack(info.sender(), (itemMeta, fail) -> {
                                        if (itemMeta.hasAttributeModifiers()) {
                                            //noinspection DataFlowIssue
                                            for (AttributeModifier mod : itemMeta.getAttributeModifiers(attribute)) {
                                                if (mod.getKey().equals(key)) {
                                                    fail.set("AttributeModifier with key '" + key + "' already exists");
                                                    return;
                                                }
                                            }
                                        }
                                        itemMeta.addAttributeModifier(attribute, modifier);
                                    });
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
                                } else if (itemMeta instanceof MapMeta mapMeta) {
                                    mapMeta.setColor(color);
                                }
                            });
                        }))));
    }

    private AbstractArgumentTree<?, Argument<?>, CommandSender> damage() {
        return LiteralArgument.literal("damage")
            .then(LiteralArgument.literal("set")
                .then(new IntegerArgument("value", 0)
                    .executesPlayer(info -> {
                        Integer damage = info.args().getByClass("value", Integer.class);
                        assert damage != null;
                        modifyItemStack(info.sender(), (itemMeta, fail) -> {
                            Damageable damageable = (Damageable) itemMeta;
                            if (damage <= 0) damageable.resetDamage();
                            else if (damageable.hasMaxDamage()) {
                                damageable.setDamage(Math.min(damage, damageable.getMaxDamage()));
                            }
                        });
                    })))
            .then(LiteralArgument.literal("max")
                .then(new IntegerArgument("value", 0)
                    .executesPlayer(info -> {
                        Integer max = info.args().getByClass("value", Integer.class);
                        assert max != null;
                        modifyItemStack(info.sender(), (itemMeta, fail) -> ((Damageable) itemMeta).setMaxDamage(max > 0 ? max : null));
                    })));
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
                            if (itemMeta instanceof EnchantmentStorageMeta storageMeta) {
                                storageMeta.addStoredEnchant(enchantment, level, true);
                            } else {
                                itemMeta.addEnchant(enchantment, level, true);
                            }
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

    private AbstractArgumentTree<?, Argument<?>, CommandSender> glint() {
        return LiteralArgument.literal("glint")
            .then(new MultiLiteralArgument("value", "true", "false", "reset")
                .executesPlayer(info -> {
                    String value = info.args().getByClass("value", String.class);
                    assert value != null;
                    modifyItemStack(info.sender(), (itemMeta, fail) ->
                        itemMeta.setEnchantmentGlintOverride(value.equalsIgnoreCase("true") ? Boolean.TRUE : value.equalsIgnoreCase("false") ? Boolean.FALSE : null));
                }));
    }

    private AbstractArgumentTree<?, Argument<?>, CommandSender> itemFlag() {
        return LiteralArgument.literal("itemFlag")
            .then(CustomArguments.getEnumArgument(ItemFlag.class, "flag")
                .executesPlayer(info -> {
                    ItemFlag flag = info.args().getByClass("flag", ItemFlag.class);
                    assert flag != null;
                    modifyItemStack(info.sender(), (itemMeta, fail) -> itemMeta.addItemFlags(flag));
                }));
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
            .then(new MultiLiteralArgument("type", "item_name", "custom_name")
                .then(new GreedyStringArgument("value")
                    .executesPlayer(info -> {
                        String type = info.args().getByClass("type", String.class);
                        String name = info.args().getByClass("value", String.class);
                        assert type != null;
                        assert name != null;
                        modifyItemStack(info.sender(), (itemMeta, fail) -> {
                            if (type.equalsIgnoreCase("item_name")) {
                                itemMeta.setItemName(Utils.getColString(name));
                            } else {
                                itemMeta.setDisplayName(Utils.getColString(name));
                            }
                        });
                    })));
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
                                    } else if (itemMeta instanceof SuspiciousStewMeta stewMeta) {
                                        stewMeta.addCustomEffect(potionEffect, true);
                                    } else if (itemMeta.hasFood()) {
                                        FoodComponent food = itemMeta.getFood();
                                        food.addEffect(potionEffect, probability.orElse(1f));
                                        itemMeta.setFood(food);
                                    } else {
                                        fail.set("Item is not a potion, suspicious stew or food");
                                    }
                                });
                            })))));
    }

    private AbstractArgumentTree<?, Argument<?>, CommandSender> sherd() {
        return LiteralArgument.literal("sherd")
            .then(new MultiLiteralArgument("side", "back", "front", "left", "right")
                .then(new MultiLiteralArgument("type", this.sherds)
                    .executesPlayer(info -> {
                        String sideString = info.args().getByClass("side", String.class);
                        String typeString = info.args().getByClass("type", String.class);

                        assert sideString != null;
                        assert typeString != null;
                        DecoratedPot.Side side = DecoratedPot.Side.valueOf(sideString.toUpperCase(Locale.ROOT));
                        Material type = Material.getMaterial(typeString.toUpperCase(Locale.ROOT));
                        modifyItemStack(info.sender(), (itemMeta, fail) -> {
                            if (itemMeta instanceof BlockStateMeta blockMeta && blockMeta.getBlockState() instanceof DecoratedPot pot) {
                                pot.setSherd(side, type);
                                blockMeta.setBlockState(pot);
                            } else {
                                fail.set("Item is not a decorated pot.");
                            }
                        });
                    })));
    }

    private AbstractArgumentTree<?, Argument<?>, CommandSender> stackSize() {
        return LiteralArgument.literal("stackSize")
            .then(new MultiLiteralArgument("type", "set", "max")
                .then(new IntegerArgument("value", 1, 99)
                    .executesPlayer(info -> {
                        String type = info.args().getByClass("type", String.class);
                        Integer value = info.args().getByClass("value", Integer.class);
                        assert type != null;
                        assert value != null;

                        ItemStack item = info.sender().getInventory().getItemInMainHand();
                        if (type.equalsIgnoreCase("set")) {
                            item.setAmount(value);
                        } else {
                            modifyItemStack(info.sender(), (itemMeta, fail) -> itemMeta.setMaxStackSize(value));
                        }
                    })));
    }

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

    private AbstractArgumentTree<?, Argument<?>, CommandSender> trim() {
        return LiteralArgument.literal("trim")
            .then(CustomArguments.getRegistryArgument(Registry.TRIM_PATTERN, "pattern")
                .then(CustomArguments.getRegistryArgument(Registry.TRIM_MATERIAL, "material")
                    .executesPlayer(info -> {
                        TrimPattern pattern = info.args().getByClass("pattern", TrimPattern.class);
                        TrimMaterial material = info.args().getByClass("material", TrimMaterial.class);

                        assert pattern != null;
                        assert material != null;
                        modifyItemStack(info.sender(), (itemMeta, fail) -> {
                            if (itemMeta instanceof ArmorMeta armorMeta) {
                                ArmorTrim armorTrim = new ArmorTrim(material, pattern);
                                armorMeta.setTrim(armorTrim);
                            } else {
                                fail.set("Item cannot have trim applied");
                            }
                        });
                    })));
    }

    private void modifyItemStack(Player player, BiConsumer<ItemMeta, AtomicReference<String>> meta) throws WrapperCommandSyntaxException {
        AtomicReference<String> failure = new AtomicReference<>();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.isEmpty())
            throw CommandAPI.failWithString("Invalid item in hand!");

        ItemMeta itemMeta = item.getItemMeta();
        meta.accept(itemMeta, failure);
        item.setItemMeta(itemMeta);
        if (failure.get() != null) {
            throw CommandAPI.failWithString(failure.get());
        }
    }

}
