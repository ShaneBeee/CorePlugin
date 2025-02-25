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
        String prefix = msg.replace("[NBTAPI]", "<grey>[<aqua>NBT<dark_aqua>API<grey>]");
        if (msg.contains("google.gson") || msg.contains("bStats")) {
            return;
        }
        Utils.logMini(prefix);
    }

}
