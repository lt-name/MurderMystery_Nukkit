package cn.lanink.murdermystery.room;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.entity.EntityPlayerCorpse;
import cn.lanink.murdermystery.tasks.game.GoldTask;
import cn.lanink.murdermystery.tasks.game.TimeTask;
import cn.lanink.murdermystery.tasks.game.TipsTask;
import cn.lanink.murdermystery.utils.SavePlayerInventory;
import cn.lanink.murdermystery.utils.Tips;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;

import java.util.*;

/**
 * 经典模式房间类
 *
 * @author lt_name
 */
public class ClassicModeRoom extends BaseRoom {


    public Player killKillerPlayer = null; //击杀杀手的玩家
    public EntityItem detectiveBow = null; //掉落的侦探弓

    /**
     * 初始化
     *
     * @param level 世界
     * @param config 配置文件
     */
    public ClassicModeRoom(Level level, Config config) {
        super(level, config);
    }

    /**
     * 加入房间
     *
     * @param player 玩家
     */
    @Override
    public synchronized void joinRoom(Player player) {
        if (this.players.size() < 16) {
            if (this.status == 0) {
                this.initTask();
            }
            this.players.put(player, 0);
            Tools.rePlayerState(player, true);
            SavePlayerInventory.save(player);
            if (player.teleport(this.getWaitSpawn())) {
                this.setRandomSkin(player);
                Tools.giveItem(player, 10);
                if (this.murderMystery.isHasTips()) {
                    Tips.closeTipsShow(this.level.getName(), player);
                }
                player.sendMessage(language.joinRoom.replace("%name%", this.level.getName()));
            }else {
                this.quitRoom(player);
            }
        }
    }

    /**
     * 退出房间
     *
     * @param player 玩家
     */
    @Override
    public synchronized void quitRoom(Player player) {
        this.players.remove(player);
        if (this.murderMystery.isHasTips()) {
            Tips.removeTipsConfig(this.level.getName(), player);
        }
        MurderMystery.getInstance().getScoreboard().closeScoreboard(player);
        player.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
        Tools.rePlayerState(player, false);
        SavePlayerInventory.restore(player);
        this.restorePlayerSkin(player);
        this.skinNumber.remove(player);
        this.skinCache.remove(player);
        for (Player p : this.players.keySet()) {
            p.showPlayer(player);
            player.showPlayer(p);
        }
    }

    /**
     * 房间开始游戏
     */
    @Override
    protected synchronized void gameStart() {
        if (this.status == 2) {
            return;
        }
        Tools.cleanEntity(this.getLevel(), true);
        this.setStatus(2);
        this.assignIdentityEvent();
        int x=0;
        for (Player player : this.getPlayers().keySet()) {
            if (x >= this.getRandomSpawn().size()) {
                x = 0;
            }
            player.teleport(this.getRandomSpawn().get(x));
            x++;
        }
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                MurderMystery.getInstance(), new TimeTask(this.murderMystery, this), 20,true);
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                MurderMystery.getInstance(), new GoldTask(this.murderMystery, this), 20, true);
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                MurderMystery.getInstance(), new TipsTask(this.murderMystery, this), 18, true);
    }

    /**
     * 结束本局游戏
     * @param victory 胜利队伍
     */
    @Override
    protected synchronized void endGame(int victory) {
        this.status = 0;
        for (Player p1 : this.players.keySet()) {
            for (Player p2 : this.players.keySet()) {
                p1.showPlayer(p2);
                p2.showPlayer(p1);
            }
        }
        this.victoryReward(victory);
        Iterator<Map.Entry<Player, Integer>> it = this.players.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Player, Integer> entry = it.next();
            it.remove();
            quitRoom(entry.getKey());
        }
        this.placeBlocks.forEach(list -> list.forEach(vector3 -> getLevel().setBlock(vector3, Block.get(0))));
        this.placeBlocks.clear();
        this.skinNumber.clear();
        this.skinCache.clear();
        this.killKillerPlayer = null;
        this.detectiveBow = null;
        Tools.cleanEntity(this.level, true);
        initTime();
    }

    protected void victoryReward(int victory) {
        if (victory == 0) {
            return;
        }
        Player killerVictory = null;
        Set<Player> commonPeopleVictory = new HashSet<>();
        Set<Player> defeatPlayers = new HashSet<>();
        for (Map.Entry<Player, Integer> entry : this.getPlayers().entrySet()) {
            if (victory == 3) {
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
        final Player finalKillKillerPlayer = this.killKillerPlayer;
        final Player finalKillerVictory = killerVictory;
        Server.getInstance().getScheduler().scheduleDelayedTask(this.murderMystery, new Task() {
            @Override
            public void onRun(int i) {
                if (finalKillKillerPlayer != null) {
                    Tools.cmd(finalKillKillerPlayer, murderMystery.getConfig().getStringList("killKillerCmd"));
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
        }, 20);
    }

    /**
     * 计时Task
     */
    @Override
    public void asyncTimeTask() {
        //开局20秒后给物品
        int time = this.gameTime - (this.setGameTime - 20);
        if (time >= 0) {
            if (time <= 5 && time >= 1) {
                Tools.sendMessage(this, this.language.killerGetSwordTime
                        .replace("%time%", time + ""));
                Tools.playSound(this, Sound.RANDOM_CLICK);
            }
            if (time == 0) {
                Tools.sendMessage(this, this.language.killerGetSword);
                for (Map.Entry<Player, Integer> entry : this.getPlayers().entrySet()) {
                    if (entry.getValue() == 2) {
                        Tools.giveItem(entry.getKey(), 1);
                    }else if (entry.getValue() == 3) {
                        Tools.giveItem(entry.getKey(), 2);
                    }
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
                        break;
                    default:
                        break;
                }
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
        //TODO 需要验证
        if (this.detectiveBow != null && this.detectiveBow.isClosed()) {
            Server.getInstance().getScheduler().scheduleTask(this.murderMystery, new Task() {
                @Override
                public void onRun(int i) {
                    EntityItem entityItem = new EntityItem(detectiveBow.chunk, detectiveBow.namedTag);
                    entityItem.spawnToAll();
                    detectiveBow = entityItem;
                }
            });
        }
    }

    /**
     * 金锭生成
     */
    @Override
    public void goldSpawn() {
        Tools.cleanEntity(this.getLevel());
        for (Position spawn : this.getGoldSpawn()) {
            this.getLevel().dropItem(spawn, Item.get(266, 0));
        }
    }

    /**
     * 异步金锭Task 金锭自动兑换弓箭检测
     */
    @Override
    public void asyncGoldTask() {
        for (Map.Entry<Player, Integer> entry : this.getPlayers().entrySet()) {
            if (entry.getValue() == 0) {
                continue;
            }
            int x = 0;
            boolean bow = true;
            for (Item item : entry.getKey().getInventory().getContents().values()) {
                if (item.getId() == 266) {
                    x += item.getCount();
                    continue;
                }
                if (item.getId() == 261) {
                    bow = false;
                }
            }
            if (x > 9) {
                entry.getKey().getInventory().removeItem(Item.get(266, 0, 10));
                entry.getKey().getInventory().addItem(Item.get(262, 0, 1));
                if (bow) {
                    entry.getKey().getInventory().addItem(Item.get(261, 0, 1));
                }
            }
        }
    }

    /**
     * 分配玩家身份
     */
    @Override
    protected void assignIdentity() {
        LinkedHashMap<Player, Integer> players = this.getPlayers();
        int random1 = MurderMystery.RANDOM.nextInt(players.size()) + 1;
        int random2;
        do {
            random2 = MurderMystery.RANDOM.nextInt(players.size()) + 1;
        }while (random1 == random2);
        int i = 0;
        for (Player player : players.keySet()) {
            player.getInventory().clearAll();
            player.getUIInventory().clearAll();
            i++;
            //侦探
            if (i == random1) {
                this.players.put(player, 2);
                player.sendTitle(this.language.titleDetectiveTitle,
                        this.language.titleDetectiveSubtitle, 10, 40, 10);
                continue;
            }
            //杀手
            if (i == random2) {
                this.players.put(player, 3);
                player.sendTitle(this.language.titleKillerTitle,
                        this.language.titleKillerSubtitle, 10, 40, 10);
                continue;
            }
            this.players.put(player, 1);
            player.sendTitle(this.language.titleCommonPeopleTitle,
                    this.language.titleCommonPeopleSubtitle, 10, 40, 10);
        }
    }

    @Override
    public int getSurvivorPlayerNumber() {
        int x = 0;
        for (Integer integer : this.getPlayers().values()) {
            if (integer != 0) {
                x++;
            }
        }
        return x;
    }

    /**
     * 符合游戏条件的攻击
     *
     * @param damage 攻击者
     * @param player 被攻击者
     */
    @Override
    protected void playerDamage(Player damage, Player player) {
        if (this.getPlayers(player) == 0) {
            return;
        }
        //攻击者是杀手
        if (this.getPlayers(damage) == 3) {
            damage.sendMessage(this.language.killPlayer);
            player.sendTitle(this.language.deathTitle,
                    this.language.deathByKillerSubtitle, 20, 60, 20);
            String tip = this.language.playerKilledByKiller
                    .replace("%identity%", this.getPlayers(player) == 2 ? this.language.detective : this.language.commonPeople);
            this.players.keySet().forEach(p -> p.sendMessage(tip));
        }else { //攻击者是平民或侦探
            if (this.getPlayers(player) == 3) {
                damage.sendMessage(this.language.killKiller);
                this.killKillerPlayer = damage;
                player.sendTitle(this.language.deathTitle,
                        this.language.killerDeathSubtitle, 10, 20, 20);
            } else {
                damage.sendTitle(this.language.deathTitle,
                        this.language.deathByDamageTeammateSubtitle, 20, 60, 20);
                player.sendTitle(this.language.deathTitle,
                        this.language.deathByTeammateSubtitle, 20, 60, 20);
                this.playerDeathEvent(damage);
            }
        }
        this.playerDeathEvent(player);
    }

    /**
     * 玩家死亡
     *
     * @param player 玩家
     */
    @Override
    protected void playerDeath(Player player) {
        player.getInventory().clearAll();
        player.getUIInventory().clearAll();
        player.setGamemode(3);
        Tools.hidePlayer(this, player);
        if (this.getPlayers(player) == 2) {
            this.getLevel().dropItem(player, Tools.getMurderItem(1));
        }
        this.players.put(player, 0);
        Tools.playSound(this, Sound.GAME_PLAYER_HURT);
        this.playerCorpseSpawnEvent(player);
    }

    /**
     * 尸体生成
     *
     * @param player 玩家
     */
    @Override
    protected void playerCorpseSpawn(Player player) {
        Skin skin = this.getPlayerSkin(player);
        switch(skin.getSkinData().data.length) {
            case 8192:
            case 16384:
            case 32768:
            case 65536:
                break;
            default:
                skin = this.murderMystery.getCorpseSkin();
        }
        CompoundTag nbt = EntityPlayerCorpse.getDefaultNBT(player);
        nbt.putCompound("Skin", new CompoundTag()
                .putByteArray("Data", skin.getSkinData().data)
                .putString("ModelId", skin.getSkinId()));
        nbt.putFloat("Scale", -1.0F);
        EntityPlayerCorpse ent = new EntityPlayerCorpse(player.getChunk(), nbt);
        ent.setSkin(skin);
        ent.setPosition(new Vector3(player.getFloorX(), Tools.getFloorY(player), player.getFloorZ()));
        ent.setGliding(true);
        ent.setRotation(player.getYaw(), 0);
        ent.spawnToAll();
        ent.updateMovement();
    }

}
