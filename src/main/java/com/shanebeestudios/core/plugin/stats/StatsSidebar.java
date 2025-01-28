package com.shanebeestudios.core.plugin.stats;

import com.shanebeestudios.core.api.util.EntityUtils;
import com.shanebeestudios.core.api.util.Pair;
import com.shanebeestudios.core.api.util.Permissions;
import com.shanebeestudios.coreapi.util.TaskUtils;
import com.shanebeestudios.coreapi.util.Utils;
import fr.mrmicky.fastboard.adventure.FastBoard;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class StatsSidebar implements Listener, Stats {

    private static final Scoreboard DUMMY_BOARD = Bukkit.getScoreboardManager().getNewScoreboard();
    private Pair<String,String> secondWorld = null;

    private final Map<World, String> worldMap = new HashMap<>();
    private final Map<String, Integer> loadedChunks = new HashMap<>();
    private final Map<UUID, FastBoard> fastBoards = new HashMap<>();
    private double gradient = -1;

    private Component title;
    private final Component[] lines = new Component[15];

    private int entitiesAll = 0;
    private int entitiesTicking = 0;
    private int mobsAll = 0;
    private int mobsTicking = 0;
    private int enemiesAll = 0;
    private int enemiesTicking = 0;
    private int displaysAll = 0;
    private int displaysTicking = 0;
    private int villagersAll = 0;
    private int villagersTicking = 0;
    private int fallingAll = 0;
    private int fallingTicking = 0;
    private int dropsAll = 0;
    private int dropsTicking = 0;

    public StatsSidebar() {
        Bukkit.getWorlds().forEach(world -> {
            String name = world.getName();
            this.worldMap.put(world, name);
            this.loadedChunks.put(name, 0);
        });
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (Permissions.STATS_SIDEBAR.hasPermission(player)) {
                enable(player);
            }
        });
        startLineTimer();
        startEntityTimer();
        startPlayerTimer();
    }

    private void startLineTimer() {
        TaskUtils.runTaskTimerAsynchronously(() -> {
            this.gradient += 0.025;
            if (this.gradient > 1) this.gradient = -1;

            // Title
            this.title = Utils.getMini("<grey>---[<gradient:#F008F4:#08F4B4:" + this.gradient + ">SERVER STATS<grey>]---");

            // MSPT
            double averageTickTime = Bukkit.getAverageTickTime();
            double grad = Math.min(averageTickTime, 40) / 40;
            String avg = String.format("%.2f", averageTickTime);
            this.lines[4] = Utils.getMini("<grey>- <transition:#17FA04:#F3FA04:#FA7804:#FA1704:" + grad + ">" + avg + "<grey>ms");

            // TPS
            double[] tps = Bukkit.getTPS();
            String[] tpsStrings = new String[tps.length];
            for (int i = 0; i < tps.length; i++) {
                if (tps[i] > 18)
                    tpsStrings[i] = String.format("<green>%.2f", tps[i]);
                else if (tps[i] > 15)
                    tpsStrings[i] = String.format("<yellow>%.2f", tps[i]);
                else tpsStrings[i] = String.format("<red>%.2f", tps[i]);
            }

            this.lines[0] = Utils.getMini("<#11C3D8>Loaded Chunks:");
            this.lines[1] = Utils.getMini("<grey>- <aqua>World: <#78D811>" + this.loadedChunks.get("world"));
            Pair<String, String> secondWorld = getSecondWorld();
            if (secondWorld != null) {
                this.lines[2] = Utils.getMini("<grey>- <aqua>" + secondWorld.getSecond() + ": <#D85C11>" + this.loadedChunks.get(secondWorld.getFirst()));
            } else {
                this.lines[2] = Utils.getMini("<grey>- <light_red> NoWorld");
            }
            this.lines[3] = Utils.getMini("<#11C3D8>Average Tick:");
            this.lines[5] = Utils.getMini("<#11C3D8>TPS:");
            this.lines[6] = Utils.getMini("<grey>- " + StringUtils.join(tpsStrings, ", "));
            this.lines[7] = Utils.getMini("<#11C3D8>Entities: <grey>(<#11D875>ticking<grey>/<#11D875>total<grey>)");
            this.lines[8] = Utils.getMini("<grey>- <aqua>All: <#11D875>" + this.entitiesTicking + "<grey>/<#11D875>" + this.entitiesAll);
            this.lines[9] = Utils.getMini("<grey>- <aqua>Mobs: <#11D875>" + this.mobsTicking + "<grey>/<#11D875>" + this.mobsAll);
            this.lines[10] = Utils.getMini("<grey>- <aqua>Enemies: <#11D875>" + this.enemiesTicking + "<grey>/<#11D875>" + this.enemiesAll);
            this.lines[11] = Utils.getMini("<grey>- <aqua>Displays: <#11D875>" + this.displaysTicking + "<grey>/<#11D875>" + this.displaysAll);
            this.lines[12] = Utils.getMini("<grey>- <aqua>Villagers: <#11D875>" + this.villagersTicking + "<grey>/<#11D875>" + this.villagersAll);
            this.lines[13] = Utils.getMini("<grey>- <aqua>Falling: <#11D875>" + this.fallingTicking + "<grey>/<#11D875>" + this.fallingAll);
            this.lines[14] = Utils.getMini("<grey>- <aqua>Drops: <#11D875>" + this.dropsTicking + "<grey>/<#11D875>" + this.dropsAll);

        }, 1, 1);
    }

    private void startEntityTimer() {
        TaskUtils.runTaskTimer(() -> {
            List<Entity> entities = EntityUtils.getAllEntities();
            updateChunks();
            TaskUtils.runTaskLaterAsynchronously(() -> {
                AtomicInteger entitiesTicking = new AtomicInteger();
                AtomicInteger mobsAll = new AtomicInteger();
                AtomicInteger mobsTicking = new AtomicInteger();
                AtomicInteger enemiesAll = new AtomicInteger();
                AtomicInteger enemiesTicking = new AtomicInteger();
                AtomicInteger displaysAll = new AtomicInteger();
                AtomicInteger displaysTicking = new AtomicInteger();
                AtomicInteger villagersAll = new AtomicInteger();
                AtomicInteger villagersTicking = new AtomicInteger();
                AtomicInteger fallingAll = new AtomicInteger();
                AtomicInteger fallingTicking = new AtomicInteger();
                AtomicInteger dropsAll = new AtomicInteger();
                AtomicInteger dropsTicking = new AtomicInteger();

                entities.forEach(entity -> {
                    if (entity.isTicking()) {
                        entitiesTicking.getAndIncrement();
                    }
                    if (entity instanceof Mob) {
                        mobsAll.getAndIncrement();
                        if (entity.isTicking()) mobsTicking.getAndIncrement();
                    }
                    if (entity instanceof Enemy) {
                        enemiesAll.getAndIncrement();
                        if (entity.isTicking())
                            enemiesTicking.getAndIncrement();
                    }
                    if (entity instanceof Display) {
                        displaysAll.getAndIncrement();
                        if (entity.isTicking())
                            displaysTicking.getAndIncrement();
                    }
                    if (entity instanceof Villager) {
                        villagersAll.getAndIncrement();
                        if (entity.isTicking())
                            villagersTicking.getAndIncrement();
                    }
                    if (entity instanceof FallingBlock) {
                        fallingAll.getAndIncrement();
                        if (entity.isTicking())
                            fallingTicking.getAndIncrement();
                    }
                    if (entity instanceof Item) {
                        dropsAll.getAndIncrement();
                        if (entity.isTicking()) dropsTicking.getAndIncrement();
                    }
                });

                this.entitiesAll = entities.size();
                this.entitiesTicking = entitiesTicking.get();
                this.mobsAll = mobsAll.get();
                this.mobsTicking = mobsTicking.get();
                this.enemiesAll = enemiesAll.get();
                this.enemiesTicking = enemiesTicking.get();
                this.displaysAll = displaysAll.get();
                this.displaysTicking = displaysTicking.get();
                this.villagersAll = villagersAll.get();
                this.villagersTicking = villagersTicking.get();
                this.fallingAll = fallingAll.get();
                this.fallingTicking = fallingTicking.get();
                this.dropsAll = dropsAll.get();
                this.dropsTicking = dropsTicking.get();

            }, 0);

        }, 5, 5);
    }

    private void startPlayerTimer() {
        TaskUtils.runTaskTimerAsynchronously(() -> {
            if (this.title == null) return;
            for (FastBoard fastBoard : this.fastBoards.values()) {
                fastBoard.updateTitle(this.title);
                fastBoard.updateLines(this.lines);
            }
        }, 1, 1);
    }

    private void updateChunks() {
        this.worldMap.forEach((world, name) -> this.loadedChunks.put(name, world.getChunkCount()));
    }

    public void enable(Player player) {
        UUID uuid = player.getUniqueId();
        if (this.fastBoards.containsKey(uuid)) return;
        this.fastBoards.put(uuid, new FastBoard(player));
    }

    public void disable(Player player) {
        UUID uuid = player.getUniqueId();
        if (!this.fastBoards.containsKey(uuid)) return;
        this.fastBoards.remove(uuid).delete();
        // This force resends the vanilla scoreboard to the client
        Scoreboard previous = player.getScoreboard();
        player.setScoreboard(DUMMY_BOARD);
        player.setScoreboard(previous);
    }

    public void toggle(Player player) {
        if (this.fastBoards.containsKey(player.getUniqueId())) {
            disable(player);
        } else {
            enable(player);
        }
    }

    public void unload() {
        this.fastBoards.values().forEach(FastBoard::delete);
        this.fastBoards.clear();
    }

    // Listeners
    @EventHandler
    private void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();
        this.worldMap.put(world, world.getName());
    }

    @EventHandler
    private void onWorldUnload(WorldUnloadEvent event) {
        this.worldMap.remove(event.getWorld());
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (Permissions.STATS_SIDEBAR.hasPermission(player)) enable(player);
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        disable(event.getPlayer());
    }

    private Pair<String,String> getSecondWorld() {
        if (Bukkit.getWorlds().size() == 1) return null;

        if (this.secondWorld == null) {
            String key = Bukkit.getWorlds().get(1).getName();
            String name = WordUtils.capitalizeFully(key.replace("world_", "").replace("_", " "));
            this.secondWorld = new Pair<>(key, name);
        }
        return this.secondWorld;
    }

}
