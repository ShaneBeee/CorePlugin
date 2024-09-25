package com.shanebeestudios.core.api.util;

import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.permissions.DefaultPermissions;

import java.util.HashMap;
import java.util.Map;

/**
 * Permissions of CorePlugin
 */
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
    // Warps
    public static final Permissions COMMANDS_WARPS = registerCommand("warps");
    public static final Permissions COMMANDS_WARPS_SET = registerCommand("warps.set");
    public static final Permissions COMMANDS_WARPS_WARP = registerCommand("warps.warp");
    public static final Permissions COMMANDS_WARPS_WARP_OTHER = registerCommand("warps.warp.other");
    public static final Permissions COMMANDS_WARPS_DELETE = registerCommand("warps.delete");
    // Other
    public static final Permissions COMMANDS_CLEAR_CHAT = registerCommand("chat.clear");
    public static final Permissions COMMANDS_FIX = registerCommand("fix");
    public static final Permissions COMMANDS_HEAL = registerCommand("heal");
    public static final Permissions COMMANDS_HEAL_OTHER = registerCommand("heal.other");
    public static final Permissions COMMANDS_PATH = registerCommand("path");
    public static final Permissions COMMANDS_PRETTY_NBT = registerCommand("prettynbt");
    public static final Permissions COMMANDS_REMOVE_ENTITY = registerCommand("removeentity");
    public static final Permissions COMMANDS_REPAIR = registerCommand("repair");
    public static final Permissions COMMANDS_SET_BIOME = registerCommand("setbiome");
    public static final Permissions COMMANDS_TAGS = registerCommand("tags");
    public static final Permissions COMMANDS_TOP = registerCommand("top");
    public static final Permissions COMMANDS_WORLD = registerCommand("world");
    public static final Permissions COMMANDS_WORLD_OTHER = registerCommand("world.other");

    // STATS
    public final static Permissions STATS_SIDEBAR = registerStats("sidebar");
    public final static Permissions STATS_BIOMEBAR = registerStats("biomebar");
    public final static Permissions STATS_RAMBAR = registerStats("rambar");

    // RANKS
    public static final Permissions RANK_OWNER = registerRank("owner");
    public static final Permissions RANK_ADMIN = registerRank("admin");
    public static final Permissions RANK_MOD = registerRank("mod");
    public static final Permissions RANK_VIP = registerRank("vip");
    public static final Permissions RANK_LOSER = registerRank("loser");
    public static final Permissions RANK_PLAYER = registerRank("player");

    public static final Permissions CHAT_DELETE = registerCore("chat.delete");

    private static Permissions registerCommand(String permission) {
        Permissions p = registerCore("commands." + permission);
        COMMAND_PERMS.put(permission, p);
        return p;
    }

    private static Permissions registerStats(String permission) {
        return registerCore("stats." + permission);
    }

    private static Permissions registerRank(String permission) {
        return registerCore("ranks." + permission);
    }

    private static Permissions registerCore(String permission) {
        Permissions p = new Permissions("core." + permission);
        registerBukkit(p.get());
        return p;
    }

    private static void registerBukkit(String permission) {
        Permission perm = new Permission(permission, PermissionDefault.OP);
        DefaultPermissions.registerPermission(perm);
    }

    /**
     * Get the permissions for a command
     * <br>Will be prefixed with "core.commands."
     *
     * @param permission Command permission to grab
     * @return Permissions from command
     */
    public static Permissions getCommandPermissions(String permission) {
        return COMMAND_PERMS.get(permission);
    }

    private final String permission;

    private Permissions(String permission) {
        this.permission = permission;
    }

    /**
     * Check if a permissible has permission
     *
     * @param permissible Permissible to check perm
     * @return True if has permission
     */
    public boolean hasPermission(Permissible permissible) {
        return permissible.hasPermission(this.permission);
    }

    /**
     * Get the string version of a permission
     *
     * @return String permission
     */
    public String get() {
        return this.permission;
    }

}
