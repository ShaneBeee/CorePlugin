package com.shanebeestudios.core.api.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * General utility class
 */
public class Util {

    private Util() {
    }

    private static final CommandSender CONSOLE = Bukkit.getConsoleSender();
    private static final String CONSOLE_PREFIX = "&7[&bCore&7] ";
    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f\\d]){6}>");
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    /**
     * Get a colored string, accepts HEX color codes
     *
     * @param string String to apply color to
     * @return Colored string
     */
    @SuppressWarnings("deprecation") // Paper deprecation
    public static String getColString(String string) {
        Matcher matcher = HEX_PATTERN.matcher(string);
        while (matcher.find()) {
            final ChatColor hexColor = ChatColor.of(matcher.group().substring(1, matcher.group().length() - 1));
            final String before = string.substring(0, matcher.start());
            final String after = string.substring(matcher.end());
            string = before + hexColor + after;
            matcher = HEX_PATTERN.matcher(string);
        }

        return ChatColor.translateAlternateColorCodes('&', string);
    }

    /**
     * Get a {@link MiniMessage mini message} from string
     *
     * @param message String to convert to mini message
     * @return Mini message from string
     */
    public static Component getMini(String message) {
        return MINI_MESSAGE.deserialize(message);
    }

    /**
     * Log a formatted message to console
     *
     * @param message Formatted message
     * @param objects Objects for format
     */
    public static void log(String message, Object... objects) {
        sendTo(CONSOLE, message, objects);
    }

    /**
     * Send a colored/prefixed message to a command sender
     *
     * @param sender  Sender to receive message
     * @param message Formatted message to send
     * @param objects Objects to include in format
     */
    public static void sendTo(CommandSender sender, String message, Object... objects) {
        String format;
        if (objects.length > 0) {
            format = String.format(message, objects);
        } else {
            format = message;
        }
        sender.sendMessage(getColString(CONSOLE_PREFIX + format));
    }

}
