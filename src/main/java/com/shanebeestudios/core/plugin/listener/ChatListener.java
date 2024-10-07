package com.shanebeestudios.core.plugin.listener;

import com.shanebeestudios.core.api.registry.Ranks;
import com.shanebeestudios.core.api.util.ChatUtil;
import com.shanebeestudios.core.api.util.Permissions;
import com.shanebeestudios.coreapi.util.Utils;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Team;

public class ChatListener implements Listener {

    @EventHandler
    private void onChat(AsyncChatEvent event) {
        Component[] components = new Component[4];

        // Create click event to delete message
        SignedMessage signedMessage = event.signedMessage();
        ChatUtil.logMessage(signedMessage);
        components[0] = Utils.getMini("<grey>[<red>X<grey>]")
            .hoverEvent(Utils.getMini("Click to delete message."))
            .clickEvent(ClickEvent.callback(audience -> {
                if (audience instanceof Player clicker && Permissions.CHAT_DELETE.hasPermission(clicker)) {
                    Bukkit.getOnlinePlayers().forEach(player -> player.deleteMessage(signedMessage));
                }
            }));

        // Format info before message
        Player player = event.getPlayer();
        components[1] = getTeamPrefix(player);
        components[2] = Utils.getMini("<aqua>").append(player.displayName()).append(Utils.getMini(" <grey>» <reset>"));
        components[3] = event.message();
        Component format = Component.join(JoinConfiguration.noSeparators(), components);

        // Item format in message
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (LegacyComponentSerializer.legacySection().serialize(format).contains("[item]") && itemInHand.getItemMeta() != null) {
            format = getFormatWithItem(format, itemInHand);
        }

        // Set format
        Component finalFormat = format;
        event.renderer((source, sourceDisplayName, message, viewer) -> finalFormat);
    }

    private Component getTeamPrefix(Player player) {
        Team team = player.getScoreboard().getPlayerTeam(player);
        if (team == null) return Ranks.PLAYER.prefix();
        return team.prefix();
    }

    private Component getFormatWithItem(Component format, ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        Component itemComp;
        if (itemMeta.hasDisplayName()) {
            itemComp = itemMeta.displayName();
        } else {
            itemComp = Component.translatable(item);
            itemComp = itemComp.color(TextColor.color(0, 253, 196));
        }
        assert itemComp != null;
        itemComp = itemComp.hoverEvent(item.asHoverEvent());
        itemComp = Component.empty().append(Component.text("[")).append(itemComp).append(Component.text("]"));
        return format.replaceText(TextReplacementConfig.builder().match("\\[item]").replacement(itemComp).build());
    }

}
