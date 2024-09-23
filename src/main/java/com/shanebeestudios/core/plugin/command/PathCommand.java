package com.shanebeestudios.core.plugin.command;

import com.destroystokyo.paper.entity.Pathfinder;
import com.destroystokyo.paper.entity.Pathfinder.PathResult;
import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import com.shanebeestudios.core.api.util.Util;
import com.shanebeestudios.core.plugin.CorePlugin;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.EntityTypeArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class PathCommand implements Listener {

    private final CorePlugin plugin;
    private final BukkitScheduler scheduler = Bukkit.getScheduler();
    private final List<EntityType> types = new ArrayList<>();

    public PathCommand(CorePlugin plugin) {
        this.plugin = plugin;
        registerCommand();
    }

    private void registerCommand() {
        CommandTree command = new CommandTree("path")
            .then(LiteralArgument.literal("list")
                .executes((sender, args) -> {
                    if (this.types.isEmpty()) {
                        Util.sendTo(sender, "&cCurrently not pathfinding");
                        return;
                    }
                    String entityTypes = StringUtils.join(this.types.stream().map(entityType -> entityType.getKey().getKey()).sorted().toArray(), "&r, &b");
                    Util.sendTo(sender, "&eCurrently pathfinding for &b%s", entityTypes);
                }))
            .then(LiteralArgument.literal("disable")
                .executes((sender, args) -> {
                    this.types.clear();
                    Util.sendTo(sender, "&cDisabled pathfinding");
                }))
            .then(LiteralArgument.literal("enable")
                .then(new EntityTypeArgument("entityType")
                    .executes((sender, args) -> {
                        EntityType type = args.getByClass("entityType", EntityType.class);
                        if (type == null) return;

                        String key = type.getKey().getKey();
                        if (this.types.contains(type)) {
                            Util.sendTo(sender, "&6Already pathfinding for &b%s", key);
                            return;
                        }
                        Class<? extends Entity> entityClass = type.getEntityClass();
                        if (entityClass == null || !Mob.class.isAssignableFrom(entityClass)) {
                            // Only mobs can pathfind
                            Util.sendTo(sender, "&cInvalid mob type &b%s", key);
                            return;
                        }
                        this.types.add(type);
                        Util.sendTo(sender, "&aEnabled pathfinding for &b%s", key);
                    })));

        command.register();
    }

    private void drawPathPoint(Location loc1, Location loc2, Color color) {
        World world = loc1.getWorld();
        if (world == null) return;

        Vector vector = new Vector(loc2.getX() - loc1.getX(), loc2.getY() - loc1.getY(), loc2.getZ() - loc1.getZ());
        double distance = loc1.distance(loc2);
        DustOptions dust = new DustOptions(color, 0.8F);

        for (int i = 1; i <= (int) (distance / 0.2); i++) {
            if (!vector.isNormalized()) vector.normalize();
            vector.multiply(i * 0.2);
            world.spawnParticle(Particle.DUST, loc1.clone().add(vector), 1, dust);
        }
    }

    private void drawPath(Mob mob) {
        var ref = new Object() {
            int id = -1;
        };
        BukkitTask bukkitTask = this.scheduler.runTaskTimer(this.plugin, () -> {
            Location from = null;
            Pathfinder pathfinder = mob.getPathfinder();
            PathResult currentPath = pathfinder.getCurrentPath();
            if (!mob.isValid() || !this.types.contains(mob.getType()) || currentPath == null || currentPath.getPoints().isEmpty()) {
                if (ref.id > 0) {
                    this.scheduler.cancelTask(ref.id);
                }
                return;
            }
            int size = (int) Math.floor(150d / currentPath.getPoints().size());
            int green = 100;
            int blue = 200;
            for (Location point : currentPath.getPoints()) {
                point = point.clone().add(0.5, 0.5, 0.5);
                green += size;
                blue -= size;
                if (from != null) {
                    drawPathPoint(from, point, Color.fromRGB(0, green, blue));
                }
                from = point;
            }
        }, 5, 5);
        ref.id = bukkitTask.getTaskId();
    }

    @EventHandler
    private void onStartsPathfinding(EntityPathfindEvent event) {
        Entity entity = event.getEntity();
        EntityType type = entity.getType();
        if (!(entity instanceof Mob mob)) return;
        if (!this.types.contains(type)) return;

        this.scheduler.runTaskLater(this.plugin, () -> drawPath(mob), 1);
    }

}
