package com.shanebeestudios.core.api.util;

import com.shanebeestudios.core.plugin.CorePlugin;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * General utility class for {@link Player players}
 */
public class PlayerUtils {

    private static final CorePlugin PLUGIN = CorePlugin.getInstance();
    private static final BukkitScheduler SCHEDULER = Bukkit.getScheduler();
    private static final List<String> TELEPORTING_PLAYERS = new ArrayList<>();

    static {
        ((Logger) LogManager.getRootLogger()).addFilter(new PlayerUtils.MessageFilter());
    }

    private PlayerUtils() {
    }

    /**
     * Teleport a player without the "moved too quickly" warning
     *
     * @param player   Player to teleport
     * @param location Location to teleport to
     */
    public static void teleportWithoutWarning(Player player, Location location) {
        String name = player.getName();
        TELEPORTING_PLAYERS.add(name);
        player.teleportAsync(location).thenAccept(a -> SCHEDULER.runTaskLaterAsynchronously(PLUGIN,
            () -> TELEPORTING_PLAYERS.remove(name),
            1));
    }

    private static class MessageFilter extends AbstractFilter {
        @Override
        public Result filter(LogEvent event) {
            return event == null ? Result.NEUTRAL : checkMessage(event.getMessage().getFormattedMessage());
        }

        @Override
        public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
            return checkMessage(msg.getFormattedMessage());
        }

        @Override
        public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
            return checkMessage(msg.toString());
        }

        @Override
        public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
            return checkMessage(msg);
        }

        private Result checkMessage(String message) {
            if (message.contains("moved too quickly")) {
                for (String name : TELEPORTING_PLAYERS) {
                    if (message.contains(name)) {
                        return Result.DENY;
                    }
                }
            }
            return Result.NEUTRAL;
        }
    }
}
