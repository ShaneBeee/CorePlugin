package com.shanebeestudios.core.api.util;

import com.shanebeestudios.core.plugin.CorePlugin;
import net.kyori.adventure.util.TriState;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.IOException;

/**
 * General utility class for {@link World worlds}
 */
@SuppressWarnings("unused")
public class WorldUtils {

    private WorldUtils() {
    }

    /**
     * Copy and load a world
     *
     * @param world World to copy
     */
    public static void copyAndLoadWorld(World world) {
        String name = world.getName();
        String cloneName = name + "_copy";
        World worldCopy = Bukkit.getWorld(name + "_copy");
        if (worldCopy == null) {
            WorldCreator worldCreator = new WorldCreator(cloneName);
            worldCreator.copy(world);
            worldCreator.keepSpawnLoaded(TriState.FALSE);
            worldCreator.createWorld();
        }
    }

    /**
     * Clone and load a world
     *
     * @param world World to clone
     */
    @SuppressWarnings({"CallToPrintStackTrace", "DataFlowIssue"})
    public static void cloneAndLoadWorld(World world) {
        CorePlugin plugin = CorePlugin.getInstance();
        String name = world.getName();
        String cloneName = name + "_copy";
        World worldCopy = Bukkit.getWorld(name + "_copy");
        if (worldCopy == null) {
            File worldContainer = Bukkit.getWorldContainer();
            File worldDirectorToClone = world.getWorldFolder();

            WorldCreator worldCreator = new WorldCreator(name + "_copy");
            // Clone the world off the main thread
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                File cloneDirectory = new File(worldContainer, cloneName);
                if (worldDirectorToClone.exists()) {
                    try {
                        for (File file : worldDirectorToClone.listFiles()) {
                            String fileName = file.getName();
                            if (file.isDirectory()) {
                                FileUtils.copyDirectory(file, new File(cloneDirectory, fileName));
                            } else if (!fileName.contains("session") && !fileName.contains("uid.dat")) {
                                FileUtils.copyFile(file, new File(cloneDirectory, fileName));
                            }
                        }
                        // Let's head back to the main thread
                        Bukkit.getScheduler().runTaskLater(plugin, worldCreator::createWorld, 0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

}
