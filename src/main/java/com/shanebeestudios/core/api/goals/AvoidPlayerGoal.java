package com.shanebeestudios.core.api.goals;

import com.destroystokyo.paper.entity.Pathfinder;
import com.destroystokyo.paper.entity.Pathfinder.PathResult;
import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import com.destroystokyo.paper.entity.ai.VanillaGoal;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Random;

@SuppressWarnings({"UnstableApiUsage", "NullableProblems"})
public class AvoidPlayerGoal implements Goal<@NotNull Mob> {

    private static final Registry<ItemType> ITEM_REGISTRY = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM);

    @SuppressWarnings("DataFlowIssue")
    private static final GoalKey<@NotNull Mob> GOAL_KEY = GoalKey.of(Mob.class, NamespacedKey.fromString("beer:avoid_player_goal"));

    private final Random random = new Random();
    private final Mob mob;
    private Entity avoid;
    private Pathfinder pathfinder;
    private PathResult path;
    private @Nullable Tag<ItemType> foodTag;

    @SuppressWarnings("PatternValidation")
    public AvoidPlayerGoal(Mob mob) {
        this.mob = mob;
        this.pathfinder = mob.getPathfinder();
        TagKey<ItemType> tagKey = TagKey.create(RegistryKey.ITEM, Key.key(mob.getType().key() + "_food"));
        if (ITEM_REGISTRY.hasTag(tagKey)) {
            foodTag = ITEM_REGISTRY.getTag(tagKey);
        }
    }

    private void stopPigFollowingParent(Mob mob) {
        if (mob instanceof Animals animal && !animal.isAdult()) {
            Goal<Animals> goal = Bukkit.getMobGoals().getGoal(animal, VanillaGoal.FOLLOW_PARENT);
            if (goal != null)
                goal.stop();
        }
    }

    @Override
    public boolean shouldActivate() {
        Optional<Entity> any = this.mob.getNearbyEntities(7, 7, 7).stream().filter(entity -> entity.getType() == EntityType.PLAYER).findAny();
        if (any.isPresent()) {
            this.avoid = any.get();
            if (shouldAvoid()) {
                this.pathfinder = this.mob.getPathfinder();
                if (this.mob instanceof Animals animal && !animal.isAdult()) {
                    Goal<Animals> goal = Bukkit.getMobGoals().getGoal(animal, VanillaGoal.FOLLOW_PARENT);
                    if (goal != null) {
                        goal.stop();
                        Optional<Entity> parent = this.mob.getNearbyEntities(10, 4, 10).stream().filter(entity -> entity != this.mob
                            && entity.getType() == this.mob.getType()).findFirst();
                        if (parent.isPresent()) {
                            PathResult pathToParent = this.pathfinder.findPath(parent.get().getLocation());
                            if (pathToParent != null) {
                                this.path = pathToParent;
                                return true;
                            }
                        }
                    }
                }
                Location mobLoc = this.mob.getLocation();
                Vector direction = mobLoc.toVector().subtract(this.avoid.getLocation().toVector()).normalize().multiply(10);
                Location newloc = mobLoc.add(direction);
                int x = random.nextInt(2, 5);
                int z = random.nextInt(2, 5);
                x = random.nextBoolean() ? x : -x;
                z = random.nextBoolean() ? z : -z;
                Location add = newloc.add(x, 0, z).getWorld().getHighestBlockAt(newloc).getLocation().add(0, 1, 0);
                this.path = pathfinder.findPath(add);
                return this.path != null;
            }
        }
        return false;
    }

    private boolean shouldAvoid() {
        if (this.avoid instanceof Player player && this.foodTag != null) {
            PlayerInventory inventory = player.getInventory();
            TypedKey<ItemType> hand = TypedKey.create(RegistryKey.ITEM, inventory.getItemInMainHand().getType().key());
            TypedKey<ItemType> off = TypedKey.create(RegistryKey.ITEM, inventory.getItemInOffHand().getType().key());
            return !this.foodTag.contains(hand) && !this.foodTag.contains(off);
        }
        return !this.avoid.isSneaking();
    }

    @Override
    public boolean shouldStayActive() {
        return this.shouldAvoid() && this.path != null && this.pathfinder.hasPath();
    }

    @Override
    public void start() {
        this.pathfinder.moveTo(this.path, 2);
    }

    @Override
    public void stop() {
        this.avoid = null;
        this.path = null;
    }

    @Override
    public @NotNull GoalKey<@NotNull Mob> getKey() {
        return GOAL_KEY;
    }

    @Override
    public @NotNull EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE);
    }

}
