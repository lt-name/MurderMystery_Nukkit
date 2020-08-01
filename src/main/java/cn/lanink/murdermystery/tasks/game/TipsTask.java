package cn.lanink.murdermystery.tasks.game;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.RoomBase;
import cn.lanink.murdermystery.utils.Language;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;


/**
 * 信息显示
 */
public class TipsTask extends PluginTask<MurderMystery> {

    private final RoomBase room;
    private final Language language;

    public TipsTask(MurderMystery owner, RoomBase room) {
        super(owner);
        owner.taskList.add(this.getTaskId());
        this.language = owner.getLanguage();
        this.room = room;
    }

    @Override
    public void onRun(int i) {
        if (this.room.getStatus() != 2) {
            this.cancel();
            return;
        }
        if (room.getPlayers().size() > 0) {
            int playerNumber = this.room.getSurvivorPlayerNumber();
            boolean detectiveSurvival = room.getPlayers().containsValue(2);
            String identity;
            for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
                entry.getKey().setNameTag("");
                switch (entry.getValue()) {
                    case 1:
                        identity = owner.getLanguage().commonPeople;
                        break;
                    case 2:
                        identity = owner.getLanguage().detective;
                        break;
                    case 3:
                        identity = owner.getLanguage().killer;
                        break;
                    default:
                        identity = owner.getLanguage().death;
                        break;
                }
                LinkedList<String> ms = new LinkedList<>(Arrays.asList(this.language.gameTimeScoreBoard
                        .replace("%roomMode%", Tools.getStringRoomMode(this.room))
                        .replace("%identity%", identity)
                        .replace("%playerNumber%", playerNumber + "")
                        .replace("%time%", this.room.gameTime + "").split("\n")));
                ms.add(" ");
                if (detectiveSurvival) {
                    ms.addAll(Arrays.asList(this.language.detectiveSurvival.split("\n")));
                }else {
                    ms.addAll(Arrays.asList(this.language.detectiveDeath.split("\n")));
                }
                ms.add("  ");
                if (entry.getValue() == 3) {
                    if (this.room.effectCD > 0) {
                        ms.add(this.language.gameEffectCDScoreBoard
                                .replace("%time%", room.effectCD + ""));
                    }
                    if (this.room.swordCD > 0) {
                        ms.add(this.language.gameSwordCDScoreBoard
                                .replace("%time%", room.swordCD + ""));
                    }
                    if (this.room.scanCD > 0) {
                        ms.add(this.language.gameScanCDScoreBoard
                                .replace("%time%", room.scanCD + ""));
                    }
                }
                owner.getScoreboard().showScoreboard(entry.getKey(), this.language.scoreBoardTitle, ms);
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
