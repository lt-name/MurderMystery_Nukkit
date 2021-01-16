package cn.lanink.murdermystery.room.doubleMode;

import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.event.MurderMysteryRoomAssignIdentityEvent;
import cn.lanink.murdermystery.room.base.PlayerIdentity;
import cn.lanink.murdermystery.room.classic.ClassicModeRoom;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.utils.Config;

import java.util.HashSet;

/**
 * @author lt_name
 */
public class DoubleRoomMode extends ClassicModeRoom {

    /**
     * 初始化
     *
     * @param level  世界
     * @param config 配置文件
     */
    public DoubleRoomMode(Level level, Config config) throws RoomLoadException {
        super(level, config);
        if (this.minPlayers < 5) {
            this.minPlayers = 5;
        }
        if (this.maxPlayers < this.minPlayers) {
            this.maxPlayers = this.minPlayers;
        }
    }

    /**
     * 分配玩家身份
     */
    protected void assignIdentity() {
        MurderMysteryRoomAssignIdentityEvent ev = new MurderMysteryRoomAssignIdentityEvent(this);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return;
        }
        HashSet<Integer> set1 = new HashSet<>();
        do {
            set1.add(MurderMystery.RANDOM.nextInt(this.getPlayers().size()) + 1);
        } while (set1.size() < 2);
        HashSet<Integer> set2 = new HashSet<>();
        do {
            set2.add(MurderMystery.RANDOM.nextInt(this.getPlayers().size()) + 1);
        } while (set2.size() < 2);
        int i = 0;
        for (Player player : this.getPlayers().keySet()) {
            player.getInventory().clearAll();
            player.getUIInventory().clearAll();
            i++;
            //侦探
            if (set1.contains(i)) {
                this.players.put(player, PlayerIdentity.DETECTIVE);
                player.sendTitle(this.murderMystery.getLanguage(player).translateString("titleDetectiveTitle"),
                        this.murderMystery.getLanguage(player).translateString("titleDetectiveSubtitle"), 10, 40, 10);
                continue;
            }
            //杀手
            if (set2.contains(i)) {
                this.players.put(player, PlayerIdentity.KILLER);
                player.sendTitle(this.murderMystery.getLanguage(player).translateString("titleKillerTitle"),
                        this.murderMystery.getLanguage(player).translateString("titleKillerSubtitle"), 10, 40, 10);
                continue;
            }
            this.players.put(player, PlayerIdentity.COMMON_PEOPLE);
            player.sendTitle(this.murderMystery.getLanguage(player).translateString("titleCommonPeopleTitle"),
                    this.murderMystery.getLanguage(player).translateString("titleCommonPeopleSubtitle"), 10, 40, 10);
        }
    }

    @Override
    protected void victoryReward(int victory) {

    }

}
