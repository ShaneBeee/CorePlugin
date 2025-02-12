package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.core.api.registry.DumpRegistry;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;

public class DumpRegistryCommand {

    public DumpRegistryCommand() {
        registerCommand();
    }

    private void registerCommand() {
        CommandTree command = new CommandTree("dumpregistry")
            .then(new StringArgument("registry")
                .includeSuggestions(ArgumentSuggestions.strings(DumpRegistry.getRegistryNames()))
                .executes(context -> {
                    String reg = context.args().getByClass("registry", String.class);
                    assert reg != null;
                    DumpRegistry<?> dumpRegistry = DumpRegistry.getByName(reg);
                    if (dumpRegistry != null) {
                        dumpRegistry.dumpObjects();
                    }
                }));

        command.register();
    }

}
