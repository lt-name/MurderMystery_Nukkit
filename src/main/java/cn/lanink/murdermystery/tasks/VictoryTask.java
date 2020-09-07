package cn.lanink.murdermystery.tasks;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.lanink.murdermystery.utils.Language;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;


public class VictoryTask extends PluginTask<MurderMystery> {

    private final BaseRoom room;
    private final Language language;
    private int victoryTime;
    private final int victory;

    public VictoryTask(MurderMystery owner, BaseRoom room, int victory) {
        super(owner);
        this.room = room;
        this.language = owner.getLanguage();
        this.victoryTime = 10;
        this.victory = victory;
        for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
            if (victory == 3) {
                entry.getKey().sendTitle(owner.getLanguage().titleVictoryKillerTitle,
                        "", 10, 30, 10);
                LinkedList<String> ms = new LinkedList<>(Arrays.asList(this.language.victoryKillerScoreBoard.split("\n")));
                owner.getScoreboard().showScoreboard(entry.getKey(), this.language.scoreBoardTitle, ms);
            }else {
                entry.getKey().sendTitle(this.language.titleVictoryCommonPeopleSubtitle,
                        "", 10, 30, 10);
                LinkedList<String> ms = new LinkedList<>(Arrays.asList(this.language.victoryCommonPeopleScoreBoard.split("\n")));
                owner.getScoreboard().showScoreboard(entry.getKey(), this.language.scoreBoardTitle, ms);
            }
        }
        for (Player player : room.getSpectatorPlayers()) {
            if (victory == 3) {
                player.sendTitle(owner.getLanguage().titleVictoryKillerTitle,
                        "", 10, 30, 10);
                LinkedList<String> ms = new LinkedList<>(Arrays.asList(this.language.victoryKillerScoreBoard.split("\n")));
                owner.getScoreboard().showScoreboard(player, this.language.scoreBoardTitle, ms);
            }else {
                player.sendTitle(this.language.titleVictoryCommonPeopleSubtitle,
                        "", 10, 30, 10);
                LinkedList<String> ms = new LinkedList<>(Arrays.asList(this.language.victoryCommonPeopleScoreBoard.split("\n")));
                owner.getScoreboard().showScoreboard(player, this.language.scoreBoardTitle, ms);
            }
        }
    }

    @Override
    public void onRun(int i) {
        if (this.room.getStatus() != BaseRoom.ROOM_STATUS_VICTORY) {
            this.cancel();
            return;
        }
        if (this.victoryTime < 1) {
            this.cancel();
            this.room.endGameEvent(this.victory);
        }else {
            this.victoryTime--;
            for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
                String bottom;
                if (victory == 3) {
                    bottom = this.language.victoryKillerBottom;
                }else {
                    bottom = this.language.victoryCommonPeopleBottom;
                }
                if (!bottom.trim().equals("")) {
                    entry.getKey().sendTip(bottom);
                }
                if (entry.getValue() != 0) {
                    if (this.victory == 1 && entry.getValue() == 3) {
                        continue;
                    }
                    Tools.spawnFirework(entry.getKey());
                }
            }
        }
    }

}
