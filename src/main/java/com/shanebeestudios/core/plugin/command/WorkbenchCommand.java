package com.shanebeestudios.core.plugin.command;

import dev.jorel.commandapi.CommandTree;

public class WorkbenchCommand {

    public WorkbenchCommand() {
        registerCommand();
    }

    private void registerCommand() {
        CommandTree command = new CommandTree("workbench")
            .executesPlayer(info -> {
                info.sender().openWorkbench(null, true);
            });
        command.register();
    }

}
