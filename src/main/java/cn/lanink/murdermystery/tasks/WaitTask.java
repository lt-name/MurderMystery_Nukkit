package cn.lanink.murdermystery.tasks;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.event.MurderRoomStartEvent;
import cn.lanink.murdermystery.room.Room;
import cn.lanink.murdermystery.utils.Language;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.level.Sound;
import cn.nukkit.scheduler.PluginTask;

import java.util.LinkedList;

public class WaitTask extends PluginTask<MurderMystery> {

    private final Room room;
    private final Language language;

    public WaitTask(MurderMystery owner, Room room) {
        super(owner);
        owner.taskList.add(this.getTaskId());
        this.room = room;
        this.language = owner.getLanguage();
    }

    @Override
    public void onRun(int i) {
        if (this.room.getMode() != 1) {
            this.cancel();
            return;
        }
        if (this.room.getPlayers().size() >= 5) {
            if (this.room.waitTime > 0) {
                this.room.waitTime--;
                if (this.room.waitTime <= 5) {
                    Tools.addSound(this.room, Sound.RANDOM_CLICK);
                }
                for (Player player : this.room.getPlayers().keySet()) {
                    player.sendActionBar(language.waitTimeBottom
                            .replace("%roomMode%", Tools.getStringRoomMode(this.room))
                            .replace("%playerNumber%", room.getPlayers().size() + "")
                            .replace("%time%", room.waitTime + ""));
                    LinkedList<String> ms = new LinkedList<>();
                    for (String string : language.waitTimeScoreBoard.split("\n")) {
                        ms.add(string.replace("%roomMode%", Tools.getStringRoomMode(this.room))
                                .replace("%playerNumber%", room.getPlayers().size() + "")
                                .replace("%time%", room.waitTime + ""));
                    }
                    owner.getScoreboard().showScoreboard(player,this.language.scoreBoardTitle, ms);
                }
            }else {
                owner.getServer().getPluginManager().callEvent(new MurderRoomStartEvent(this.room));
                this.cancel();
            }
        }else if (this.room.getPlayers().size() > 0) {
            if (this.room.waitTime != this.room.getSetWaitTime()) {
                this.room.waitTime = this.room.getSetWaitTime();
            }
            for (Player player : this.room.getPlayers().keySet()) {
                player.sendActionBar(this.language.waitBottom
                        .replace("%roomMode%", Tools.getStringRoomMode(this.room))
                        .replace("%playerNumber%", room.getPlayers().size() + ""));
                LinkedList<String> ms = new LinkedList<>();
                for (String string : this.language.waitScoreBoard.split("\n")) {
                    ms.add(string.replace("%roomMode%", Tools.getStringRoomMode(this.room))
                            .replace("%playerNumber%", room.getPlayers().size() + ""));
                }
                owner.getScoreboard().showScoreboard(player, this.language.scoreBoardTitle,  ms);
            }
        }else {
            this.room.endGame();
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
