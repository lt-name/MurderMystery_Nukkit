package cn.lanink.murdermystery.tasks.game.assassin;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.assassin.AssassinModeRoom;
import cn.lanink.murdermystery.room.base.PlayerIdentity;
import cn.lanink.murdermystery.room.base.RoomStatus;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.level.Sound;
import cn.nukkit.scheduler.PluginTask;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lt_name
 */
public class AssassinDistanceTip extends PluginTask<MurderMystery> {

    private final AssassinModeRoom room;
    private final ConcurrentHashMap<Player, Integer> tipTime = new ConcurrentHashMap<>();
    Set<Player> set = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public AssassinDistanceTip(AssassinModeRoom room) {
        super(MurderMystery.getInstance());
        this.room = room;

    }

    @Override
    public void onRun(int i) {
        if (this.room.getStatus() != RoomStatus.GAME) {
            this.cancel();
            return;
        }
        for (Map.Entry<Player, Player> entry : this.room.targetMap.entrySet()) {
            if (this.room.getPlayers(entry.getValue()) != PlayerIdentity.ASSASSIN) {
                continue;
            }
            int t = this.tipTime.getOrDefault(entry.getValue(), 0) + 1;
            this.tipTime.put(entry.getValue(), t);
            double distance = entry.getKey().distance(entry.getValue());
            int flashingT = -1;
            if (distance < 3) {
                flashingT = 1;
            }else if (distance < 5) {
                flashingT = 2;
            }else if (distance < 10) {
                flashingT = 3;
            }
            if (flashingT == -1) {
                this.set.remove(entry.getValue());
                continue;
            }
            if (t >= flashingT) {
                this.tipTime.put(entry.getValue(), 0);
                if (this.set.contains(entry.getValue())) {
                    entry.getValue().sendTip("♡");
                    this.set.remove(entry.getValue());
                }else {
                    entry.getValue().sendTip("❤");
                    Tools.playSound(entry.getValue(), Sound.NOTE_HAT);
                    this.set.add(entry.getValue());
                }
            }
        }
    }


}
