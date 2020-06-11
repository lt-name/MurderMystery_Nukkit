package cn.lanink.murdermystery.tasks.game;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.GameMode;
import cn.lanink.murdermystery.room.Room;
import cn.lanink.murdermystery.tasks.VictoryTask;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.level.Sound;
import cn.nukkit.potion.Effect;
import cn.nukkit.scheduler.PluginTask;

import java.util.Map;
import java.util.Random;

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
        //开局20秒后给物品
        int time = room.gameTime - (room.getSetGameTime() - 20);
        if (time >= 0) {
            if (time <= 5 && time >= 1) {
                this.sendMessage(owner.getLanguage().killerGetSwordTime.replace("%time%", time + ""));
                Tools.addSound(room, Sound.RANDOM_CLICK);
            }
            switch (this.room.getGameMode()) {
                case CLASSIC:
                    if (time == 0) {
                        this.sendMessage(owner.getLanguage().killerGetSword);
                        for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
                            if (entry.getValue() == 2) {
                                Tools.giveItem(entry.getKey(), 1);
                            }else if (entry.getValue() == 3) {
                                Tools.giveItem(entry.getKey(), 2);
                            }
                        }
                    }
                    break;
                case INFECTED:
                    if (time == 0) {
                        this.sendMessage(owner.getLanguage().killerGetSword);
                        int y = new Random().nextInt(room.getPlayers().size());
                        int x = 0;
                        for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
                            if (x == y) {
                                entry.setValue(3);
                                entry.getKey().sendTitle(owner.getLanguage().titleKillerTitle,
                                        owner.getLanguage().titleKillerSubtitle, 10, 40, 10);
                                entry.getKey().getInventory().clearAll();
                                entry.getKey().getInventory().setItem(1, Tools.getMurderItem(2));
                                Effect effect = Effect.getEffect(2).setAmplifier(1).setDuration(200);
                                effect.setColor(0, 255, 0);
                                entry.getKey().addEffect(effect);
                                break;
                            }
                            x++;
                        }
                    }
                    break;
            }
        }
        //计时与胜利判断
        if (room.gameTime > 0) {
            room.gameTime--;
            int playerNumber = 0;
            boolean killer = false;
            for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
                switch (entry.getValue()) {
                    case 1:
                    case 2:
                        playerNumber++;
                        break;
                    case 3:
                        killer = true;
                        if (this.room.getGameMode() == GameMode.INFECTED && this.room.gameTime%10 == 0) {
                            Effect effect = Effect.getEffect(1).setDuration(300)
                                    .setAmplifier(1).setVisible(true);
                            effect.setColor(0, 255, 0);
                            entry.getKey().addEffect(effect);
                        }
                        break;
                }
            }
            if (this.room.getGameMode() == GameMode.INFECTED && time >= 0) {
                killer = true;
            }
            if (killer) {
                if (playerNumber == 0) {
                    victory(3);
                }
            }else {
                victory(1);
            }
        }else {
            victory(1);
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
