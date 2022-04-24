package cn.lanink.murdermystery.tasks;

import cn.lanink.gamecore.utils.Language;
import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.lanink.murdermystery.room.base.PlayerIdentity;
import cn.lanink.murdermystery.room.base.RoomStatus;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;


public class VictoryTask extends PluginTask<MurderMystery> {

    private final BaseRoom room;
    private int victoryTime;
    private final int victory;

    public VictoryTask(MurderMystery owner, BaseRoom room, int victory) {
        super(owner);
        this.room = room;
        this.victoryTime = 10;
        this.victory = victory;
        HashSet<Player> players = new HashSet<>(this.room.getPlayers().keySet());
        players.addAll(this.room.getSpectatorPlayers());
        for (Player player : players) {
            Language language = owner.getLanguage(player);
            if (victory == 3) {
                player.sendTitle(language.translateString("titleVictoryKillerTitle"), "", 10, 30, 10);
                LinkedList<String> ms = new LinkedList<>(Arrays.asList(language.translateString("victoryKillerScoreBoard").split("\n")));
                owner.getScoreboard().showScoreboard(player, language.translateString("scoreBoardTitle"), ms);
            }else {
                player.sendTitle(language.translateString("titleVictoryCommonPeopleSubtitle"), "", 10, 30, 10);
                LinkedList<String> ms = new LinkedList<>(Arrays.asList(language.translateString("victoryCommonPeopleScoreBoard").split("\n")));
                owner.getScoreboard().showScoreboard(player, language.translateString("scoreBoardTitle"), ms);
            }
        }
        Watchdog.resetTime(room);
    }

    @Override
    public void onRun(int i) {
        if (this.room.getStatus() != RoomStatus.VICTORY) {
            this.cancel();
            return;
        }
        if (this.victoryTime < 1) {
            this.cancel();
            this.room.endGame(this.victory);
        }else {
            this.victoryTime--;
            for (Map.Entry<Player, PlayerIdentity> entry : room.getPlayers().entrySet()) {
                String bottom;
                if (this.owner.isAutomaticNextRound()) {
                    bottom = this.owner.getLanguage(entry.getKey()).translateString("victory_automaticallyJoinTheNextGameCountdown_Bottom", this.victoryTime);
                }else {
                    if (victory == 3) {
                        bottom = this.owner.getLanguage(entry.getKey()).translateString("victoryKillerBottom");
                    } else {
                        bottom = this.owner.getLanguage(entry.getKey()).translateString("victoryCommonPeopleBottom");
                    }
                }
                if (!bottom.trim().equals("")) {
                    entry.getKey().sendTip(bottom);
                }
                if (entry.getValue() != PlayerIdentity.DEATH) {
                    if (this.victory == 1 && entry.getValue() == PlayerIdentity.KILLER) {
                        continue;
                    }
                    Tools.spawnFirework(entry.getKey());
                }
            }
        }
        Watchdog.resetTime(this.room);
    }

}
