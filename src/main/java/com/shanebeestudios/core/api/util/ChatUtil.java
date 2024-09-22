package com.shanebeestudios.core.api.util;

import net.kyori.adventure.chat.SignedMessage;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods relating to chat
 */
public class ChatUtil {

    private static final List<SignedMessage> MESSAGES = new ArrayList<>();

    /**
     * Log a message for clearing later
     *
     * @param message SignedMessage to log
     */
    public static void logMessage(SignedMessage message) {
        MESSAGES.addFirst(message);
        // Chat only holds 100 messages, lets not let this list get too big
        if (MESSAGES.size() > 100) {
            MESSAGES.subList(100, MESSAGES.size()).clear();
        }
    }

    /**
     * Clear signed messages from chat
     */
    public static void clearChat() {
        int length = Math.min(MESSAGES.size(), 100);
        Bukkit.getOnlinePlayers().forEach(player -> {
            for (int i = 0; i < length; i++) {
                player.deleteMessage(MESSAGES.get(i));
            }
        });
        MESSAGES.clear();
    }

}
