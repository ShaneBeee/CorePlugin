package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.coreapi.util.Utils;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.IntegerArgument;

import java.awt.*;

public class ColorCommand {

    public ColorCommand() {
        registerCommand();
    }

    @SuppressWarnings("DataFlowIssue")
    private void registerCommand() {
        CommandTree command = new CommandTree("rgb")
            .then(new IntegerArgument("red", 0, 255)
                .then(new IntegerArgument("green", 0, 255)
                    .then(new IntegerArgument("blue", 0, 255)
                        .executes((sender, args) -> {
                            int red = args.getByClass("red", Integer.class);
                            int green = args.getByClass("green", Integer.class);
                            int blue = args.getByClass("blue", Integer.class);

                            Color color = new Color(red, green, blue);
                            int rgb = color.getRGB();
                            String hexColor = String.format("#%02X%02X%02X", red, green, blue);
                            Utils.sendMiniTo(sender, "Color <color:%s><click:copy_to_clipboard:%s><hover:show_text:'Click to copy color'>%s<reset><grey> from (%s, %s, %s)",
                                hexColor, rgb, rgb, red, green, blue);
                        }))));

        command.register();

    }
}
