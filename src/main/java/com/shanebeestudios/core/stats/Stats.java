package com.shanebeestudios.core.stats;

import org.bukkit.entity.Player;

public interface Stats {

    void toggle(Player player);

    void enable(Player player);

    void disable(Player player);

}
