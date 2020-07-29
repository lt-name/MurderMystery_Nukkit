package cn.lanink.murdermystery.room;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.tasks.game.GoldTask;
import cn.lanink.murdermystery.tasks.game.TimeTask;
import cn.lanink.murdermystery.tasks.game.TipsTask;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Sound;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.Config;

import java.util.Map;
import java.util.Random;

/**
 * 感染模式房间类
 *
 * @author lt_name
 */
public class RoomInfectedMode extends RoomClassicMode {

    /**
     * 初始化
     *
     * @param config 配置文件
     */
    public RoomInfectedMode(Config config) {
        super(config);
    }

    @Override
    public void gameStart() {
        Tools.cleanEntity(this.getLevel(), true);
        this.setStatus(2);
        int x=0;
        for (Player player : this.getPlayers().keySet()) {
            if (x >= this.getRandomSpawn().size()) {
                x = 0;
            }
            player.teleport(this.getRandomSpawn().get(x));
            x++;
            player.getInventory().clearAll();
            this.players.put(player, 2);
            Tools.giveItem(player, 1);
        }
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                MurderMystery.getInstance(), new TimeTask(this.murderMystery, this), 20,true);
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                MurderMystery.getInstance(), new GoldTask(this.murderMystery, this), 20, true);
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                MurderMystery.getInstance(), new TipsTask(this.murderMystery, this), 18, true);
    }

    @Override
    public void asyncTimeTask() {
        //开局20秒后给物品
        int time = this.gameTime - (this.getSetGameTime() - 20);
        if (time >= 0) {
            if (time <= 5 && time >= 1) {
                Tools.sendMessage(this, this.language.killerGetSwordTime.replace("%time%", time + ""));
                Tools.addSound(this, Sound.RANDOM_CLICK);
            }
            if (time == 0) {
                Tools.sendMessage(this, this.language.killerGetSword);
                int y = new Random().nextInt(this.getPlayers().size());
                int x = 0;
                for (Map.Entry<Player, Integer> entry : this.getPlayers().entrySet()) {
                    if (x == y) {
                        entry.setValue(3);
                        entry.getKey().sendTitle(this.language.titleKillerTitle,
                                this.language.titleKillerSubtitle, 10, 40, 10);
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
        }
        //计时与胜利判断
        if (this.gameTime > 0) {
            this.gameTime--;
            int playerNumber = 0;
            boolean killer = false;
            for (Map.Entry<Player, Integer> entry : this.getPlayers().entrySet()) {
                switch (entry.getValue()) {
                    case 1:
                    case 2:
                        playerNumber++;
                        break;
                    case 3:
                        killer = true;
                        if (this.gameTime % 10 == 0) {
                            Effect effect = Effect.getEffect(1).setDuration(300)
                                    .setAmplifier(1).setVisible(true);
                            effect.setColor(0, 255, 0);
                            entry.getKey().addEffect(effect);
                        }
                        break;
                }
            }
            if (time >= 0) {
                killer = true;
            }
            if (killer) {
                if (playerNumber == 0) {
                    this.victory(3);
                }
            }else {
                this.victory(1);
            }
        }else {
            this.victory(1);
        }
        //杀手CD计算
        if (this.effectCD > 0) {
            this.effectCD--;
        }
        if (this.swordCD > 0) {
            this.swordCD--;
        }
        if (this.scanCD > 0) {
            this.scanCD--;
        }
    }

    @Override
    public void asyncGoldTask() {

    }

    @Override
    public int getSurvivorPlayerNumber() {
        int x = 0;
        for (Integer integer : this.getPlayers().values()) {
            if (integer == 2) {
                x++;
            }
        }
        return x;
    }

    @Override
    public void playerDamage(Player damage, Player player) {
        if (this.getPlayers(damage) == 3) {
            if (this.getPlayers(player) == 3) {
                return;
            }
            this.players.put(player, 3);
            player.sendTitle(this.language.titleKillerTitle,
                    this.language.titleKillerSubtitle, 10, 40, 10);
        }else {
            if (this.getPlayers(player) != 3) {
                return;
            }
        }
        player.getLevel().addSound(player, Sound.GAME_PLAYER_HURT);
        player.teleport(this.getRandomSpawn().get(new Random().nextInt(this.getRandomSpawn().size())));
        player.getInventory().clearAll();
        player.getInventory().setItem(1, Tools.getMurderItem(2));
        player.addEffect(Effect.getEffect(2).setAmplifier(2).setDuration(60));
    }

}
