package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.core.api.util.ChatUtil;
import com.shanebeestudios.core.api.util.Permissions;
import dev.jorel.commandapi.CommandTree;

public class ClearChatCommand {

    public ClearChatCommand() {
        registerCommand();
    }

    private void registerCommand() {
        CommandTree command = new CommandTree("clearchat")
            .withShortDescription("Clears the chat.")
            .withPermission(Permissions.COMMANDS_CLEAR_CHAT.get())
                .executes((sender, args) -> {
                    ChatUtil.clearChat();
                });

        command.register();
    }

}
