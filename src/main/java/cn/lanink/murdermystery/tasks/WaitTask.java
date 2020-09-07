package cn.lanink.murdermystery.tasks;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.base.BaseRoom;
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
        this.room = room;
        this.language = owner.getLanguage();
    }

    @Override
    public void onRun(int i) {
        if (this.room.getStatus() != 1) {
            this.cancel();
            return;
        }
        if (this.room.getPlayers().size() >= this.room.getMinPlayers()) {
            if (this.room.getPlayers().size() == this.room.getMaxPlayers() && this.room.waitTime > 10) {
                this.room.waitTime = 10;
            }
            this.room.waitTime--;
            if (this.room.waitTime > 0) {
                if (this.room.waitTime <= 10) {
                    Tools.playSound(this.room, Sound.RANDOM_CLICK);
                    String title = "§e";
                    if (this.room.waitTime <= 3) {
                        title = "§c";
                    }
                    title += this.room.waitTime;
                    for (Player player : this.room.getPlayers().keySet()) {
                        player.sendTitle(title, "", 0, 15, 5);
                    }
                    for (Player player : this.room.getSpectatorPlayers()) {
                        player.sendTitle(title, "", 0, 15, 5);
                    }
                }
                String waitTimeBottom = this.language.waitTimeBottom
                        .replace("%roomMode%", Tools.getStringRoomMode(this.room))
                        .replace("%playerNumber%", this.room.getPlayers().size() + "")
                        .replace("%time%", this.room.waitTime + "");
                LinkedList<String> ms =  new LinkedList<>(Arrays.asList(this.language.waitTimeScoreBoard
                        .replace("%roomMode%", Tools.getStringRoomMode(this.room))
                        .replace("%playerNumber%", this.room.getPlayers().size() + "")
                        .replace("%maxPlayers%", this.room.getMaxPlayers() + "")
                        .replace("%time%", this.room.waitTime + "").split("\n")));
                for (Player player : this.room.getPlayers().keySet()) {
                    if (!"".equals(waitTimeBottom.trim())) {
                        player.sendTip(waitTimeBottom);
                    }
                    owner.getScoreboard().showScoreboard(player,this.language.scoreBoardTitle, ms);
                }
                for (Player player : this.room.getSpectatorPlayers()) {
                    if (!"".equals(waitTimeBottom.trim())) {
                        player.sendTip(waitTimeBottom);
                    }
                    owner.getScoreboard().showScoreboard(player,this.language.scoreBoardTitle, ms);
                }
            }else {
                this.room.gameStartEvent();
                this.cancel();
            }
        }else if (this.room.getPlayers().size() > 0 || this.room.getSpectatorPlayers().size() > 0) {
            if (this.room.waitTime != this.room.setWaitTime) {
                this.room.waitTime = this.room.setWaitTime;
            }
            String waitBottom = this.language.waitBottom
                    .replace("%roomMode%", Tools.getStringRoomMode(this.room))
                    .replace("%playerNumber%", this.room.getPlayers().size() + "");
            LinkedList<String> ms = new LinkedList<>(Arrays.asList(this.language.waitScoreBoard
                    .replace("%roomMode%", Tools.getStringRoomMode(this.room))
                    .replace("%playerNumber%", room.getPlayers().size() + "")
                    .replace("%minPlayers%", this.room.getMinPlayers() + "")
                    .replace("%maxPlayers%", this.room.getMaxPlayers() + "").split("\n")));
            for (Player player : this.room.getPlayers().keySet()) {
                if (!"".equals(waitBottom.trim())) {
                    player.sendTip(waitBottom);
                }
                owner.getScoreboard().showScoreboard(player, this.language.scoreBoardTitle, ms);
            }
            for (Player player : this.room.getSpectatorPlayers()) {
                if (!"".equals(waitBottom.trim())) {
                    player.sendTip(waitBottom);
                }
                owner.getScoreboard().showScoreboard(player, this.language.scoreBoardTitle, ms);
            }
        }else {
            this.room.endGameEvent();
            this.cancel();
        }
    }

}
