package com.shanebeestudios.core.api.registry;

import com.shanebeestudios.core.api.util.Permissions;
import com.shanebeestudios.core.api.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Ranks for players
 */
@SuppressWarnings("unused")
public class Ranks {

    private static final Scoreboard SCOREBOARD = Bukkit.getScoreboardManager().getMainScoreboard();
    private static final Map<Permissions, Team> TEAM_BY_PERM = new LinkedHashMap<>();

    private Ranks() {
    }

    static void init() {
    }

    public static Team OWNER = getTeam("a-team", "<grey>[<gradient:#6DEC65:#65DEEC>♛OWNER♚<grey>]", Permissions.RANK_OWNER);
    public static Team ADMIN = getTeam("b-team", "<grey>[<gradient:#0FE7F5:#0F85F5>❄ADMIN❄<grey>]", Permissions.RANK_ADMIN);
    public static Team MOD = getTeam("c-team", "<grey>[<gradient:#F2F50F:#F51D0F>♧MOD♧<grey>]", Permissions.RANK_MOD);
    public static Team VIP = getTeam("d-team", "<grey>[<gradient:#C80FF5:#F50FB6>★VIP★<grey>]", Permissions.RANK_VIP);
    public static Team LOSER = getTeam("e-team", "<grey>[<gradient:#EAE30D:#BE0DEA>☁LOSER☁<grey>]", Permissions.RANK_LOSER);
    public static Team PLAYER = getTeam("z-team", "<grey>[<gradient:#D91208:#F5740F>♙PLAYER♟<grey>]", Permissions.RANK_PLAYER);

    private static Team getTeam(String name, String prefix, Permissions permission) {
        Team team = SCOREBOARD.getTeam(name);
        if (team == null) team = SCOREBOARD.registerNewTeam(name);
        team.prefix(Util.getMini(prefix + " "));
        TEAM_BY_PERM.put(permission, team);
        return team;
    }

    public static void joinRank(Player player) {
        for (Permissions perm : TEAM_BY_PERM.keySet()) {
            if (perm.hasPermission(player)) {
                Team team = TEAM_BY_PERM.get(perm);
                team.addPlayer(player);
                return;
            }
        }
    }

}
