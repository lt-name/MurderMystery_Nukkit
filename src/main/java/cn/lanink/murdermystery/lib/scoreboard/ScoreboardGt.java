package cn.lanink.murdermystery.lib.scoreboard;

import cn.nukkit.Player;
import gt.creeperface.nukkit.scoreboardapi.ScoreboardAPI;
import gt.creeperface.nukkit.scoreboardapi.scoreboard.SimpleScoreboard;

import java.util.HashMap;
import java.util.List;

/**
 * @author lt_name
 */
public class ScoreboardGt implements IScoreboard {

    private final HashMap<Player, SimpleScoreboard> scoreboards = new HashMap<>();

    @Override
    public void showScoreboard(Player player, String title, List<String> message) {
        SimpleScoreboard simpleScoreboard;
        if (!this.scoreboards.containsKey(player)) {
            simpleScoreboard = ScoreboardAPI.builder().build();
        }else {
            simpleScoreboard = this.scoreboards.get(player);
            simpleScoreboard.clearCache();
            simpleScoreboard.resetAllScores();
        }
        simpleScoreboard.setDisplayName(title);
        for (int line = 0; line < message.size(); line++) {
            simpleScoreboard.setScore(line, message.get(line), line);
        }
        simpleScoreboard.update();
        simpleScoreboard.addPlayer(player);
        this.scoreboards.put(player, simpleScoreboard);
    }

    @Override
    public void closeScoreboard(Player player) {
        if (this.scoreboards.containsKey(player)) {
            SimpleScoreboard simpleScoreboard = this.scoreboards.get(player);
            simpleScoreboard.removePlayer(player);
            simpleScoreboard.update();
            this.scoreboards.remove(player);
        }
    }

}
