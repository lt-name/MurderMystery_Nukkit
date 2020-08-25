package cn.lanink.murdermystery.room;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.potion.Effect;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 感染模式房间类
 *
 * @author lt_name
 */
public class InfectedModeRoom extends ClassicModeRoom {

    private final HashMap<Player, Integer> playerRespawnTime = new HashMap<>();

    /**
     * 初始化
     *
     * @param level 世界
     * @param config 配置文件
     */
    public InfectedModeRoom(Level level, Config config) {
        super(level, config);
        if (MurderMystery.debug) {
            this.minPlayers = 2;
        }
    }

    @Override
    protected synchronized void endGame(int victory) {
        this.playerRespawnTime.clear();
        super.endGame(victory);
    }

    @Override
    protected void victoryReward(int victory) {

    }

    @Override
    public void asyncTimeTask() {
        //开局20秒选出杀手
        int time = this.gameTime - (this.setGameTime - 20);
        if (time >= 0) {
            if (time <= 5 && time >= 1) {
                Tools.sendMessage(this, this.language.killerGetSwordTime.replace("%time%", time + ""));
                Tools.playSound(this, Sound.RANDOM_CLICK);
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
                        this.playerRespawn(entry.getKey());
                        break;
                    }
                    x++;
                }
            }
        }
        //复活计时
        for (Map.Entry<Player, Integer> entry : this.playerRespawnTime.entrySet()) {
            if (entry.getValue() > 0) {
                entry.setValue(entry.getValue() - 1);
                if (entry.getValue() == 0) {
                    this.playerRespawn(entry.getKey());
                }else {
                    entry.getKey().sendTip(this.language.playerRespawnTime
                            .replace("%time%", entry.getValue() + ""));
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
                        if (this.gameTime % 20 == 0) {
                            Effect effect = Effect.getEffect(1).setDuration(1000)
                                    .setAmplifier(1).setVisible(true);
                            effect.setColor(0, 255, 0);
                            entry.getKey().addEffect(effect);
                        }
                        break;
                }
            }
            if (time >= 0) {
                if (this.players.size() < 2) {
                    this.endGameEvent();
                    return;
                }
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
    }

    @Override
    public void asyncGoldTask() {

    }

    @Override
    protected void assignIdentity() {
        for (Player player : this.players.keySet()) {
            player.getInventory().clearAll();
            this.players.put(player, 2);
            Tools.giveItem(player, 1);
        }
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
    protected void playerDamage(Player damage, Player player) {
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
        this.playerDeathEvent(player);
    }

    @Override
    protected void playerDeath(Player player) {
        player.getInventory().clearAll();
        player.getUIInventory().clearAll();
        player.setGamemode(3);
        player.getAdventureSettings().set(AdventureSettings.Type.NO_CLIP, false);
        player.getAdventureSettings().update();
        Tools.hidePlayer(this, player);
        Tools.playSound(this, Sound.GAME_PLAYER_HURT);
        this.playerRespawnTime.put(player, 10);
    }

    public void playerRespawn(Player player) {
        Tools.showPlayer(this, player);
        Tools.rePlayerState(player, true);
        player.getInventory().setItem(1, Tools.getMurderItem(2));
        Effect effect = Effect.getEffect(2).setAmplifier(2).setDuration(60); //缓慢
        effect.setColor(0, 255, 0);
        player.addEffect(effect);
        effect = Effect.getEffect(15).setAmplifier(2).setDuration(60); //失明
        effect.setColor(0, 255, 0);
        player.addEffect(effect);
        player.teleport(this.getRandomSpawn().get(new Random().nextInt(this.getRandomSpawn().size())));
        Server.getInstance().getScheduler().scheduleDelayedTask(this.murderMystery, new Task() {
            @Override
            public void onRun(int i) {
                Effect effect = Effect.getEffect(1).setDuration(1000).setAmplifier(1).setVisible(true); // 速度
                effect.setColor(0, 255, 0);
                player.addEffect(effect);
            }
        }, 60);
    }

}
