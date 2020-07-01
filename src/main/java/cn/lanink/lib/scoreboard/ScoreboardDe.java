package cn.lanink.lib.scoreboard;

import cn.nukkit.Player;
import de.theamychan.scoreboard.api.ScoreboardAPI;
import de.theamychan.scoreboard.network.DisplaySlot;
import de.theamychan.scoreboard.network.Scoreboard;
import de.theamychan.scoreboard.network.ScoreboardDisplay;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author lt_name
 */
public class ScoreboardDe implements IScoreboard {

    private final HashMap<Player, Scoreboard> scoreboards = new HashMap<>();

    @Override
    public void showScoreboard(Player player, String title, LinkedList<String> message) {
        Scoreboard scoreboard = ScoreboardAPI.createScoreboard();
        ScoreboardDisplay scoreboardDisplay = scoreboard.addDisplay(DisplaySlot.SIDEBAR, title, title);
        if (this.scoreboards.containsKey(player)) {
            this.scoreboards.get(player).hideFor(player);
        }
        for (int line = 0; line < message.size(); line++) {
            scoreboardDisplay.addLine(message.get(line), line);
        }
        scoreboard.showFor(player);
        this.scoreboards.put(player, scoreboard);
    }

    @Override
    public void closeScoreboard(Player player) {
        if (this.scoreboards.containsKey(player)) {
            Scoreboard scoreboard = this.scoreboards.get(player);
            scoreboard.hideFor(player);
        }
    }

    @Override
    public void delCache(Player player) {
        this.closeScoreboard(player);
        this.scoreboards.remove(player);
    }

}
