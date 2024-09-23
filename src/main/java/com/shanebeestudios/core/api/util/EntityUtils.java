package com.shanebeestudios.core.api.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * General utility class for {@link Entity entities}
 */
public class EntityUtils {

    private EntityUtils() {
    }

    /**
     * Get a combined list of entities from all worlds
     *
     * @return List of all entities
     */
    public static List<Entity> getAllEntities() {
        List<Entity> entities = new ArrayList<>();
        Bukkit.getWorlds().forEach(world -> entities.addAll(world.getEntities()));
        return entities;
    }

}
