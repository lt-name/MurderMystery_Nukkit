package cn.lanink.murdermystery.tasks;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.lanink.murdermystery.utils.Language;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.level.Sound;
import cn.nukkit.scheduler.PluginTask;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

public class WaitTask extends PluginTask<MurderMystery> {

    private final BaseRoom room;

    public WaitTask(MurderMystery owner, BaseRoom room) {
        super(owner);
        this.room = room;
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
                HashSet<Player> players = new HashSet<>(this.room.getPlayers().keySet());
                players.addAll(this.room.getSpectatorPlayers());
                for (Player player : players) {
                    Language language = this.owner.getLanguage(player);
                    String waitTimeBottom = language.waitTimeBottom
                            .replace("%roomMode%", Tools.getStringRoomMode(this.room))
                            .replace("%playerNumber%", this.room.getPlayers().size() + "")
                            .replace("%time%", this.room.waitTime + "");
                    LinkedList<String> ms =  new LinkedList<>(Arrays.asList(language.waitTimeScoreBoard
                            .replace("%roomMode%", Tools.getStringRoomMode(this.room))
                            .replace("%playerNumber%", this.room.getPlayers().size() + "")
                            .replace("%maxPlayers%", this.room.getMaxPlayers() + "")
                            .replace("%time%", this.room.waitTime + "").split("\n")));
                    if (!"".equals(waitTimeBottom.trim())) {
                        player.sendTip(waitTimeBottom);
                    }
                    owner.getScoreboard().showScoreboard(player, language.scoreBoardTitle, ms);
                }
            }else {
                this.room.gameStartEvent();
                this.cancel();
            }
        }else if (this.room.getPlayers().size() > 0 || this.room.getSpectatorPlayers().size() > 0) {
            if (this.room.waitTime != this.room.setWaitTime) {
                this.room.waitTime = this.room.setWaitTime;
            }
            HashSet<Player> players = new HashSet<>(this.room.getPlayers().keySet());
            players.addAll(this.room.getSpectatorPlayers());
            for (Player player : players) {
                Language language = this.owner.getLanguage(player);
                String waitBottom = language.waitBottom
                        .replace("%roomMode%", Tools.getStringRoomMode(this.room))
                        .replace("%playerNumber%", this.room.getPlayers().size() + "");
                LinkedList<String> ms = new LinkedList<>(Arrays.asList(language.waitScoreBoard
                        .replace("%roomMode%", Tools.getStringRoomMode(this.room))
                        .replace("%playerNumber%", room.getPlayers().size() + "")
                        .replace("%minPlayers%", this.room.getMinPlayers() + "")
                        .replace("%maxPlayers%", this.room.getMaxPlayers() + "").split("\n")));
                if (!"".equals(waitBottom.trim())) {
                    player.sendTip(waitBottom);
                }
                owner.getScoreboard().showScoreboard(player, language.scoreBoardTitle, ms);
            }
        }else {
            this.room.endGameEvent();
            this.cancel();
        }
    }

}
