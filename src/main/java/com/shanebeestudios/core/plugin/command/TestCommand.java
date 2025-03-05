package com.shanebeestudios.core.plugin.command;

import dev.jorel.commandapi.CommandTree;
import org.bukkit.entity.Player;

public class TestCommand {

    public TestCommand() {
        registerCommand();
    }

    private void registerCommand() {
        CommandTree command = new CommandTree("testcommand");
        command.executesPlayer(info -> {
            Player player = info.sender();
            // Used for tests, add when need be
        });

        command.register();
    }

}
