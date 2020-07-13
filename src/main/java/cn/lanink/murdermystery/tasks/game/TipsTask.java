package cn.lanink.murdermystery.tasks.game;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.RoomClassicMode;
import cn.lanink.murdermystery.utils.Language;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;

import java.util.LinkedList;
import java.util.Map;


/**
 * 信息显示
 */
public class TipsTask extends PluginTask<MurderMystery> {

    private final RoomClassicMode room;
    private final Language language;

    public TipsTask(MurderMystery owner, RoomClassicMode room) {
        super(owner);
        owner.taskList.add(this.getTaskId());
        this.language = owner.getLanguage();
        this.room = room;
    }

    @Override
    public void onRun(int i) {
        if (this.room.getMode() != 2) {
            this.cancel();
            return;
        }
        if (room.getPlayers().values().size() > 0) {
            int playerNumber = this.room.getSurvivorPlayerNumber();
            String mode;
            for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
                entry.getKey().setNameTag("");
                switch (entry.getValue()) {
                    case 1:
                        mode = owner.getLanguage().commonPeople;
                        break;
                    case 2:
                        mode = owner.getLanguage().detective;
                        break;
                    case 3:
                        mode = owner.getLanguage().killer;
                        break;
                    default:
                        mode = owner.getLanguage().death;
                        break;
                }
                entry.getKey().sendActionBar(language.gameTimeBottom
                        .replace("%roomMode%", Tools.getStringRoomMode(this.room))
                        .replace("%mode%", mode)
                        .replace("%playerNumber%", playerNumber + "")
                        .replace("%time%", room.gameTime + ""));
                LinkedList<String> ms = new LinkedList<>();
                for (String string : language.gameTimeScoreBoard.split("\n")) {
                    ms.add(string.replace("%roomMode%", Tools.getStringRoomMode(this.room))
                            .replace("%mode%", mode)
                            .replace("%playerNumber%", playerNumber + "")
                            .replace("%time%", room.gameTime + ""));
                }
                if (entry.getValue() == 3) {
                    if (room.effectCD > 0) {
                        ms.add(language.gameEffectCDScoreBoard
                                .replace("%time%", room.effectCD + ""));
                    }
                    if (room.swordCD > 0) {
                        ms.add(language.gameSwordCDScoreBoard
                                .replace("%time%", room.swordCD + ""));
                    }
                    if (room.scanCD > 0) {
                        ms.add(language.gameScanCDScoreBoard
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
