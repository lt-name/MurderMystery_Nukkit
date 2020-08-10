package cn.lanink.murdermystery.tasks;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.BaseRoom;
import cn.lanink.murdermystery.utils.Language;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.level.Sound;
import cn.nukkit.scheduler.PluginTask;

import java.util.Arrays;
import java.util.LinkedList;

public class WaitTask extends PluginTask<MurderMystery> {

    private final BaseRoom room;
    private final Language language;

    public WaitTask(MurderMystery owner, BaseRoom room) {
        super(owner);
        owner.taskList.add(this.getTaskId());
        this.room = room;
        this.language = owner.getLanguage();
    }

    @Override
    public void onRun(int i) {
        if (this.room.getStatus() != 1) {
            this.cancel();
            return;
        }
        if (this.room.getPlayers().size() >= 5) {
            if (this.room.getPlayers().size() == 16 && this.room.waitTime > 10) {
                this.room.waitTime = 10;
            }
            if (this.room.waitTime > 0) {
                this.room.waitTime--;
                if (this.room.waitTime <= 5) {
                    Tools.playSound(this.room, Sound.RANDOM_CLICK);
                }
                for (Player player : this.room.getPlayers().keySet()) {
                    String waitTimeBottom = this.language.waitTimeBottom
                            .replace("%roomMode%", Tools.getStringRoomMode(this.room))
                            .replace("%playerNumber%", room.getPlayers().size() + "")
                            .replace("%time%", this.room.waitTime + "");
                    if (!"".equals(waitTimeBottom.trim())) {
                        player.sendTip(waitTimeBottom);
                    }
                    owner.getScoreboard().showScoreboard(player,this.language.scoreBoardTitle,
                            new LinkedList<>(Arrays.asList(this.language.waitTimeScoreBoard
                                    .replace("%roomMode%", Tools.getStringRoomMode(this.room))
                                    .replace("%playerNumber%", room.getPlayers().size() + "")
                                    .replace("%time%", room.waitTime + "").split("\n"))));
                }
            }else {
                this.room.gameStartEvent();
                this.cancel();
            }
        }else if (this.room.getPlayers().size() > 0) {
            if (this.room.waitTime != this.room.setWaitTime) {
                this.room.waitTime = this.room.setWaitTime;
            }
            for (Player player : this.room.getPlayers().keySet()) {
                String waitBottom = this.language.waitBottom
                        .replace("%roomMode%", Tools.getStringRoomMode(this.room))
                        .replace("%playerNumber%", room.getPlayers().size() + "");
                if (!"".equals(waitBottom.trim())) {
                    player.sendActionBar(waitBottom);
                }
                LinkedList<String> ms = new LinkedList<>();
                for (String string : this.language.waitScoreBoard.split("\n")) {
                    ms.add(string.replace("%roomMode%", Tools.getStringRoomMode(this.room))
                            .replace("%playerNumber%", room.getPlayers().size() + ""));
                }
                owner.getScoreboard().showScoreboard(player, this.language.scoreBoardTitle, ms);
            }
        }else {
            this.room.endGameEvent();
            this.cancel();
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
