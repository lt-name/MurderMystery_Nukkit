package cn.lanink.murdermystery.tasks;

import cn.lanink.gamecore.utils.Language;
import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.lanink.murdermystery.room.base.RoomStatus;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
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
        if (this.room.getStatus() != RoomStatus.WAIT) {
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
                    String title = "§e";
                    if (this.room.waitTime <= 3) {
                        title = "§c";
                        Tools.playSound(this.room, Sound.NOTE_HARP);
                    }else {
                        Tools.playSound(this.room, Sound.NOTE_BASSATTACK);
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
                    String waitTimeBottom = language.translateString("waitTimeBottom")
                            .replace("%roomMode%", Tools.getStringRoomMode(player, this.room))
                            .replace("%playerNumber%", this.room.getPlayers().size() + "")
                            .replace("%time%", this.room.waitTime + "");
                    LinkedList<String> ms =  new LinkedList<>(Arrays.asList(language.translateString("waitTimeScoreBoard")
                            .replace("%roomMode%", Tools.getStringRoomMode(player, this.room))
                            .replace("%playerNumber%", this.room.getPlayers().size() + "")
                            .replace("%maxPlayers%", this.room.getMaxPlayers() + "")
                            .replace("%time%", this.room.waitTime + "").split("\n")));
                    if (!"".equals(waitTimeBottom.trim())) {
                        player.sendTip(waitTimeBottom);
                    }
                    this.owner.getScoreboard().showScoreboard(player, language.translateString("scoreBoardTitle"), ms);
                }
            }else {
                this.room.startGame();
                Server.getInstance().getScheduler().scheduleDelayedTask(this.owner,
                        () -> Tools.playSound(this.room, Sound.NOTE_FLUTE), 10);
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
                String waitBottom = language.translateString("waitBottom")
                        .replace("%roomMode%", Tools.getStringRoomMode(player, this.room))
                        .replace("%playerNumber%", this.room.getPlayers().size() + "");
                LinkedList<String> ms = new LinkedList<>(Arrays.asList(language.translateString("waitScoreBoard")
                        .replace("%roomMode%", Tools.getStringRoomMode(player, this.room))
                        .replace("%playerNumber%", room.getPlayers().size() + "")
                        .replace("%minPlayers%", this.room.getMinPlayers() + "")
                        .replace("%maxPlayers%", this.room.getMaxPlayers() + "").split("\n")));
                if (!"".equals(waitBottom.trim())) {
                    player.sendTip(waitBottom);
                }
                this.owner.getScoreboard().showScoreboard(player, language.translateString("scoreBoardTitle"), ms);
            }
        }else {
            this.room.endGame();
            this.cancel();
        }
        Watchdog.resetTime(this.room);
    }

}
