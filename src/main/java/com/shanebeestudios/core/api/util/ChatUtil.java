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

    public static void logMessage(SignedMessage message) {
        MESSAGES.addFirst(message);
    }

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
