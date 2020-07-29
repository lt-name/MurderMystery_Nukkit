package cn.lanink.murdermystery.room;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.entity.EntityPlayerCorpse;
import cn.lanink.murdermystery.event.MurderPlayerCorpseSpawnEvent;
import cn.lanink.murdermystery.event.MurderPlayerDamageEvent;
import cn.lanink.murdermystery.event.MurderPlayerDeathEvent;
import cn.lanink.murdermystery.event.MurderRoomAssignIdentityEvent;
import cn.lanink.murdermystery.tasks.game.GoldTask;
import cn.lanink.murdermystery.tasks.game.TimeTask;
import cn.lanink.murdermystery.tasks.game.TipsTask;
import cn.lanink.murdermystery.utils.SavePlayerInventory;
import cn.lanink.murdermystery.utils.Tips;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * 经典模式房间类
 *
 * @author lt_name
 */
public class RoomClassicMode extends RoomBase {


    public Player killKillerPlayer = null; //击杀杀手的玩家

    /**
     * 初始化
     *
     * @param config 配置文件
     */
    public RoomClassicMode(Config config) {
        super(config);
    }

    /**
     * 加入房间
     *
     * @param player 玩家
     */
    public void joinRoom(Player player) {
        if (this.players.values().size() < 16) {
            if (this.status == 0) {
                this.initTask();
            }
            this.players.put(player, 0);
            Tools.rePlayerState(player, true);
            SavePlayerInventory.save(player);
            if (player.teleport(this.getWaitSpawn())) {
                this.setRandomSkin(player);
                Tools.giveItem(player, 10);
                if (Server.getInstance().getPluginManager().getPlugins().containsKey("Tips")) {
                    Tips.closeTipsShow(this.level.getName(), player);
                }
                player.sendMessage(MurderMystery.getInstance().getLanguage().joinRoom
                        .replace("%name%", this.level.getName()));
            }else {
                this.quitRoom(player, true);
            }
        }
    }

    /**
     * 退出房间
     *
     * @param player 玩家
     * @param online 是否在线
     */
    public void quitRoom(Player player, boolean online) {
        if (this.isPlaying(player)) {
            this.players.remove(player);
        }
        if (Server.getInstance().getPluginManager().getPlugins().containsKey("Tips")) {
            Tips.removeTipsConfig(this.level.getName(), player);
        }
        if (online) {
            MurderMystery.getInstance().getScoreboard().closeScoreboard(player);
            player.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
            Tools.rePlayerState(player, false);
            SavePlayerInventory.restore(player);
            this.restorePlayerSkin(player);
        }else {
            this.skinNumber.remove(player);
            this.skinCache.remove(player);
        }
    }

    /**
     * 房间开始游戏
     */
    public void gameStart() {
        Tools.cleanEntity(this.getLevel(), true);
        this.setStatus(2);
        this.assignIdentity();
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
     * @param normal 正常关闭
     */
    public void endGame(boolean normal) {
        this.status = 0;
        Server.getInstance().getScheduler().scheduleDelayedTask(MurderMystery.getInstance(), new Task() {
            @Override
            public void onRun(int i) {
                if (normal) {
                    Iterator<Map.Entry<Player, Integer>> it = players.entrySet().iterator();
                    while(it.hasNext()) {
                        Map.Entry<Player, Integer> entry = it.next();
                        it.remove();
                        quitRoom(entry.getKey());
                    }
                }else {
                    getLevel().getPlayers().values().forEach(
                            player -> player.kick(MurderMystery.getInstance().getLanguage().roomSafeKick));
                }
                placeBlocks.forEach(list -> list.forEach(vector3 -> getLevel().setBlock(vector3, Block.get(0))));
                placeBlocks.clear();
                skinNumber.clear();
                skinCache.clear();
                killKillerPlayer = null;
                Tools.cleanEntity(getLevel(), true);
                initTime();
            }
        }, 1);
    }

    /**
     * 计时Task
     */
    @Override
    public void asyncTimeTask() {
        //开局20秒后给物品
        int time = this.gameTime - (this.getSetGameTime() - 20);
        if (time >= 0) {
            if (time <= 5 && time >= 1) {
                Tools.sendMessage(this, this.language.killerGetSwordTime
                        .replace("%time%", time + ""));
                Tools.addSound(this, Sound.RANDOM_CLICK);
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
    public void asyncGoldTask() {
        for (Player player : this.getPlayers().keySet()) {
            int x = 0;
            boolean bow = true;
            for (Item item : player.getInventory().getContents().values()) {
                if (item.getId() == 266) {
                    x += item.getCount();
                    continue;
                }
                if (item.getId() == 261) {
                    bow = false;
                }
            }
            if (x > 9) {
                player.getInventory().removeItem(Item.get(266, 0, 10));
                player.getInventory().addItem(Item.get(262, 0, 1));
                if (bow) {
                    player.getInventory().addItem(Item.get(261, 0, 1));
                }
            }
        }
    }

    /**
     * 分配玩家身份
     */
    public void assignIdentity() {
        MurderRoomAssignIdentityEvent ev = new MurderRoomAssignIdentityEvent(this);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) return;
        LinkedHashMap<Player, Integer> players = this.getPlayers();
        Random random = new Random();
        int random1 = random.nextInt(players.size()) + 1;
        int random2;
        do {
            random2 = random.nextInt(players.size()) + 1;
        }while (random1 == random2);
        int j = 0;
        for (Player player : players.keySet()) {
            player.getInventory().clearAll();
            player.getUIInventory().clearAll();
            j++;
            //侦探
            if (j == random1) {
                this.players.put(player, 2);
                player.sendTitle(this.language.titleDetectiveTitle,
                        this.language.titleDetectiveSubtitle, 10, 40, 10);
                continue;
            }
            //杀手
            if (j == random2) {
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
    public void playerDamage(Player damage, Player player) {
        if (this.getPlayers(player) == 0) return;
        MurderPlayerDamageEvent ev = new MurderPlayerDamageEvent(this, damage, player);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) return;
        //攻击者是杀手
        if (this.getPlayers(damage) == 3) {
            damage.sendMessage(this.language.killPlayer);
            player.sendTitle(this.language.deathTitle,
                    this.language.deathByKillerSubtitle, 20, 60, 20);
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
                this.playerDeath(damage);
            }
        }
        this.playerDeath(player);
    }

    /**
     * 玩家死亡
     *
     * @param player 玩家
     */
    public void playerDeath(Player player) {
        MurderPlayerDeathEvent ev = new MurderPlayerDeathEvent(this, player);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) return;
        player.getInventory().clearAll();
        player.getUIInventory().clearAll();
        player.setAdventureSettings((new AdventureSettings(player)).set(AdventureSettings.Type.ALLOW_FLIGHT, true));
        player.setGamemode(3);
        if (this.getPlayers(player) == 2) {
            this.getLevel().dropItem(player, Tools.getMurderItem(1));
        }
        this.players.put(player, 0);
        Tools.setPlayerInvisible(player, true);
        Tools.addSound(this, Sound.GAME_PLAYER_HURT);
        this.playerCorpseSpawn(player);
    }

    /**
     * 尸体生成
     *
     * @param player 玩家
     */
    public void playerCorpseSpawn(Player player) {
        MurderPlayerCorpseSpawnEvent ev = new MurderPlayerCorpseSpawnEvent(this, player);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) return;
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
