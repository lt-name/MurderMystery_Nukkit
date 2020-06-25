package cn.lanink.murdermystery.utils.scoreboard;

import cn.lanink.murdermystery.MurderMystery;
import cn.nukkit.Player;
import de.theamychan.scoreboard.api.ScoreboardAPI;
import de.theamychan.scoreboard.network.DisplaySlot;
import de.theamychan.scoreboard.network.Scoreboard;
import de.theamychan.scoreboard.network.ScoreboardDisplay;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ScoreboardDe extends BaseScoreboard {

    private final Map<Player, Scoreboard> scoreboards = new HashMap<>();

    /**
     * 计分板显示信息
     * @param player 玩家
     * @param message 信息
     */
    @Override
    public void showScoreboard(Player player, LinkedList<String> message) {
        Scoreboard scoreboard = ScoreboardAPI.createScoreboard();
        ScoreboardDisplay scoreboardDisplay = scoreboard.addDisplay(DisplaySlot.SIDEBAR,
                "MurderMystery", MurderMystery.getInstance().getLanguage().scoreBoardTitle);
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
