package cn.lanink.murdermystery.tasks.game;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.Room;
import cn.lanink.murdermystery.tasks.VictoryTask;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.level.Sound;
import cn.nukkit.scheduler.PluginTask;

import java.util.Map;

/**
 * 游戏时间计算
 */
public class TimeTask extends PluginTask<MurderMystery> {

    private final Room room;

    public TimeTask(MurderMystery owner, Room room) {
        super(owner);
        owner.taskList.add(this.getTaskId());
        this.room = room;
    }

    public void onRun(int i) {
        if (this.room.getMode() != 2) {
            this.cancel();
            return;
        }
        //计时与胜利判断
        if (room.gameTime > 0) {
            room.gameTime--;
            int playerNumber = 0;
            boolean killer = false;
            for (Integer integer : room.getPlayers().values()) {
                if (integer != 0) {
                    playerNumber++;
                }
                if (integer == 3) {
                    killer = true;
                }
            }
            if (killer) {
                if (playerNumber < 2) {
                    victory(3);
                }
            }else {
                victory(1);
            }
        }else {
            victory(1);
        }
        //开局10秒后给物品
        if (room.gameTime >= room.getSetGameTime()-10) {
            int time = room.gameTime - (room.getSetGameTime() - 10);
            if (time <= 5 && time >= 1) {
                this.sendMessage(owner.getLanguage().killerGetSwordTime.replace("%time%", time + ""));
                Tools.addSound(room, Sound.RANDOM_CLICK);
            }else if (time < 1) {
                this.sendMessage(owner.getLanguage().killerGetSword);
                for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
                    if (entry.getValue() == 2) {
                        Tools.giveItem(entry.getKey(), 1);
                    }else if (entry.getValue() == 3) {
                        Tools.giveItem(entry.getKey(), 2);
                    }
                }
            }
        }
        //杀手CD计算
        if (room.effectCD > 0) {
            room.effectCD--;
        }
        if (room.swordCD > 0) {
            room.swordCD--;
        }
        if (room.scanCD > 0) {
            room.scanCD--;
        }
    }

    private void sendMessage(String string) {
        for (Player player : this.room.getPlayers().keySet()) {
            player.sendMessage(string);
        }
    }

    private void victory(int victoryMode) {
        this.cancel();
        if (room.getPlayers().values().size() > 0) {
            room.setMode(3);
            owner.getServer().getScheduler().scheduleRepeatingTask(owner, new VictoryTask(owner, room, victoryMode), 20);
        }else {
           room.endGame();
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
