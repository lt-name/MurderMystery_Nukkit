package cn.lanink.murdermystery.listener;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.event.MurderMysteryRoomEndEvent;
import cn.lanink.murdermystery.room.RoomClassicMode;
import cn.lanink.murdermystery.utils.Language;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.scheduler.Task;

import java.util.LinkedList;
import java.util.Map;

/**
 * 游戏监听器（插件事件）
 * @author lt_name
 */
public class MurderListener implements Listener {

    private final MurderMystery murderMystery;
    private final Language language;

    public MurderListener(MurderMystery murderMystery) {
        this.murderMystery = murderMystery;
        this.language = murderMystery.getLanguage();
    }

    /**
     * 房间结束事件
     * @param event 事件
     */
    @EventHandler
    public void onRoomEnd(MurderMysteryRoomEndEvent event) {
        if (event.getRoom() instanceof RoomClassicMode) {
            RoomClassicMode room = (RoomClassicMode) event.getRoom();
            final Player killKillerPlayer = room.killKillerPlayer;
            int victoryMode = event.getVictoryMode();
            Player killerVictory = null;
            LinkedList<Player> commonPeopleVictory = new LinkedList<>();
            LinkedList<Player> defeatPlayers = new LinkedList<>();
            for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
                if (victoryMode == 3) {
                    if (entry.getValue() == 3) {
                        killerVictory = entry.getKey();
                    }else {
                        defeatPlayers.add(entry.getKey());
                    }
                }else {
                    switch (entry.getValue()) {
                        case 1:
                        case 2:
                            commonPeopleVictory.add(entry.getKey());
                            break;
                        default:
                            defeatPlayers.add(entry.getKey());
                            break;
                    }
                }
            }
            //延迟执行，防止给物品被清
            Player finalKillerVictory = killerVictory;
            this.murderMystery.getServer().getScheduler().scheduleDelayedTask(this.murderMystery, new Task() {
                @Override
                public void onRun(int i) {
                    if (killKillerPlayer != null) {
                        Tools.cmd(killKillerPlayer, murderMystery.getConfig().getStringList("killKillerCmd"));
                    }
                    if (finalKillerVictory != null) {
                        Tools.cmd(finalKillerVictory, murderMystery.getConfig().getStringList("killerVictoryCmd"));
                    }
                    for (Player player : commonPeopleVictory) {
                        Tools.cmd(player, murderMystery.getConfig().getStringList("commonPeopleVictoryCmd"));
                    }
                    for (Player player : defeatPlayers) {
                        Tools.cmd(player, murderMystery.getConfig().getStringList("defeatCmd"));
                    }
                }
            }, 40);
        }
    }

}
