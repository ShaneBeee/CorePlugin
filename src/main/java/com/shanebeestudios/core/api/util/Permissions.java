package com.shanebeestudios.core.api.util;

import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.permissions.DefaultPermissions;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"SameParameterValue", "unused"})
public class Permissions {

    // COMMANDS
    private static final Map<String, Permissions> COMMAND_PERMS = new HashMap<>();
    // Stats
    public static final Permissions COMMANDS_STATS_ALL = registerCommand("stats.all");
    public static final Permissions COMMANDS_STATS_ALL_OTHER = registerCommand("stats.all.other");
    public static final Permissions COMMANDS_STATS_BIOME = registerCommand("stats.biomebar");
    public static final Permissions COMMANDS_STATS_BIOME_OTHER = registerCommand("stats.biomebar.other");
    public static final Permissions COMMANDS_STATS_RAMBAR = registerCommand("stats.rambar");
    public static final Permissions COMMANDS_STATS_RAMBAR_OTHER = registerCommand("stats.rambar.other");
    public static final Permissions COMMANDS_STATS_SIDEBAR = registerCommand("stats.sidebar");
    public static final Permissions COMMANDS_STATS_SIDEBAR_OTHER = registerCommand("stats.sidebar.other");
    // Distance
    public static final Permissions COMMANDS_VIEW_DISTANCE = registerCommand("distance.view");
    public static final Permissions COMMANDS_VIEW_DISTANCE_OTHER = registerCommand("distance.view.other");
    public static final Permissions COMMANDS_SIM_DISTANCE = registerCommand("distance.simulation");
    public static final Permissions COMMANDS_SIM_DISTANCE_OTHER = registerCommand("distance.simulation.other");
    public static final Permissions COMMANDS_FAKEVIEW_DISTANCE = registerCommand("distance.fakeview");
    public static final Permissions COMMANDS_FAKEVIEW_DISTANCE_OTHER = registerCommand("distance.fakeview.other");
    // Other
    public static final Permissions COMMANDS_PRETTY_NBT = registerCommand("prettynbt");
    public static final Permissions COMMANDS_REMOVE_ENTITY = registerCommand("remove.entity");
    public static final Permissions COMMANDS_TAGS = registerCommand("tags");
    public static final Permissions COMMANDS_WORLD = registerCommand("world");
    public static final Permissions COMMANDS_WORLD_OTHER = registerCommand("world.other");

    // STATS
    public final static Permissions STATS_SIDEBAR = registerStats("sidebar");
    public final static Permissions STATS_BIOMEBAR = registerStats("biomebar");
    public final static Permissions STATS_RAMBAR = registerStats("rambar");

    private static Permissions registerCommand(String permission) {
        Permissions p = new Permissions("core.commands." + permission);
        registerBukkit(p.get());
        COMMAND_PERMS.put(permission, p);
        return p;
    }

    private static Permissions registerStats(String permission) {
        Permissions p = new Permissions("core.stats." + permission);
        registerBukkit(p.get());
        return p;
    }

    private static void registerBukkit(String permission) {
        Permission perm = new Permission(permission, PermissionDefault.OP);
        DefaultPermissions.registerPermission(perm);
    }

    public static Permissions getCommandPermissions(String permission) {
        return COMMAND_PERMS.get(permission);
    }

    private final String permission;

    private Permissions(String permission) {
        this.permission = permission;
    }

    public boolean hasPermission(Permissible permissible) {
        return permissible.hasPermission(this.permission);
    }

    public String get() {
        return this.permission;
    }

}
