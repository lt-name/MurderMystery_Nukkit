package cn.lanink.murdermystery.tasks;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.Room;
import cn.lanink.murdermystery.utils.Language;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;
import tip.messages.ScoreBoardMessage;
import tip.utils.Api;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;


public class VictoryTask extends PluginTask<MurderMystery> {

    private final Room room;
    private final Language language;
    private int victoryTime;
    private final int victory;

    public VictoryTask(MurderMystery owner, Room room, int victory) {
        super(owner);
        owner.taskList.add(this.getTaskId());
        this.room = room;
        this.language = owner.getLanguage();
        this.victoryTime = 10;
        this.victory = victory;
    }

    @Override
    public void onRun(int i) {
        if (this.room.getMode() != 3) {
            this.cancel();
            return;
        }
        if (this.victoryTime < 1) {
            this.cancel();
            this.room.endGame();
        }else {
            this.victoryTime--;
            for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
                if (this.victory == 3) {
                    entry.getKey().sendActionBar(language.victoryKillerBottom);
                    LinkedList<String> ms = new LinkedList<>(Arrays.asList(language.victoryKillerScoreBoard.split("\n")));
                    ScoreBoardMessage score = new ScoreBoardMessage(
                            room.getLevel().getName(), true, this.language.scoreBoardTitle, ms);
                    Api.setPlayerShowMessage(entry.getKey().getName(), score);
                } else {
                    entry.getKey().sendActionBar(language.victoryCommonPeopleBottom);
                    LinkedList<String> ms = new LinkedList<>(Arrays.asList(language.victoryCommonPeopleScoreBoard.split("\n")));
                    ScoreBoardMessage score = new ScoreBoardMessage(
                            room.getLevel().getName(), true, this.language.scoreBoardTitle, ms);
                    Api.setPlayerShowMessage(entry.getKey().getName(), score);
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

    @Override
    public void cancel() {
        while (owner.taskList.contains(this.getTaskId())) {
            owner.taskList.remove(this.getTaskId());
        }
        super.cancel();
    }

}
