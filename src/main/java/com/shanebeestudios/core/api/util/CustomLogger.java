package com.shanebeestudios.core.api.util;

import com.shanebeestudios.coreapi.util.Utils;

import java.util.logging.Logger;

public class CustomLogger extends Logger {

    protected CustomLogger(String name, String resourceBundleName) {
        super(name, resourceBundleName);
    }

    /**
     * Get an instance of CustomLogger
     *
     * @return new instance of CustomLogger
     */
    public static CustomLogger getLogger() {
        return new CustomLogger("", null);
    }

    @Override
    public void info(String msg) {
        String prefix = msg.replace("[NBTAPI]", "&7[&bNBT&3API&7]");
        if (msg.contains("google.gson") || msg.contains("bStats")) {
            return;
        }
        Utils.log(prefix);
    }

}
