package cn.lanink.murdermystery.room.classic;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.entity.EntityPlayerCorpse;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.lanink.murdermystery.room.base.IAsyncTipsTask;
import cn.lanink.murdermystery.room.base.IRoomStatus;
import cn.lanink.murdermystery.room.base.ITimeTask;
import cn.lanink.murdermystery.utils.Tools;
import cn.lanink.murdermystery.utils.exception.RoomLoadException;
import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 经典模式房间类
 *
 * @author lt_name
 */
public class ClassicModeRoom extends BaseRoom implements ITimeTask, IAsyncTipsTask {

    protected int goldSpawnTime;
    public Player killKillerPlayer = null; //击杀杀手的玩家
    public EntityItem detectiveBow = null; //掉落的侦探弓

    /**
     * 初始化
     *
     * @param level 世界
     * @param config 配置文件
     */
    public ClassicModeRoom(Level level, Config config) throws RoomLoadException {
        super(level, config);
        //经典模式人数低于三将进入死循环！
        if (minPlayers < 3) {
            this.minPlayers = 3;
        }
    }

    @Override
    public void enableListener() {
        super.enableListener();
        this.murderMystery.getMurderMysteryListeners().get("ClassicGameListener").addListenerRoom(this);
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
        this.setStatus(ROOM_STATUS_GAME);
        this.assignIdentityEvent();
        int x=0;
        for (Player player : this.getPlayers().keySet()) {
            if (x >= this.getRandomSpawn().size()) {
                x = 0;
            }
            player.teleport(this.getRandomSpawn().get(x));
            x++;
        }
    }

    /**
     * 结束本局游戏
     * @param victory 胜利队伍
     */
    @Override
    protected synchronized void endGame(int victory) {
        int oldStatus = this.status;
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
        this.initTime();
        switch (oldStatus) {
            case IRoomStatus.ROOM_STATUS_GAME:
            case IRoomStatus.ROOM_STATUS_VICTORY:
                this.restoreWorld();
                break;
        }
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

    @Override
    public ITimeTask getTimeTask() {
        return this; //本类已经实现了ITimeTask接口
    }

    @Override
    public IAsyncTipsTask getTipsTask() {
        return this; //本类已经实现了ITipsTask接口
    }

    /**
     * 计时Task
     */
    @Override
    public void timeTask() {
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
            CompletableFuture.runAsync(() -> {
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
            });
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
            EntityItem entityItem = new EntityItem(detectiveBow.chunk, detectiveBow.namedTag);
            entityItem.spawnToAll();
            detectiveBow = entityItem;
        }
        this.goldSpawn();
        this.goldExchange();
    }

    /**
     * 金锭生成
     */
    public void goldSpawn() {
        if (this.goldSpawnTime > 0) {
            this.goldSpawnTime--;
        }else {
            this.goldSpawnTime = this.setGoldSpawnTime;
            Tools.cleanEntity(this.getLevel());
            for (Vector3 spawn : this.goldSpawnVector3List) {
                this.getLevel().dropItem(spawn, Item.get(266, 0));
            }
        }
    }

    /**
     * 金锭自动兑换弓箭检测
     */
    public void goldExchange() {
        CompletableFuture.runAsync(() -> {
            for (Map.Entry<Player, Integer> entry : this.players.entrySet()) {
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
        });
    }

    @Override
    public void asyncTipsTask() {
        int playerNumber = this.getSurvivorPlayerNumber();
        boolean detectiveSurvival = this.players.containsValue(2);
        String identity;
        for (Map.Entry<Player, Integer> entry : this.players.entrySet()) {
            entry.getKey().setNameTag("");
            switch (entry.getValue()) {
                case 1:
                    identity = this.murderMystery.getLanguage().commonPeople;
                    break;
                case 2:
                    identity = this.murderMystery.getLanguage().detective;
                    break;
                case 3:
                    identity = this.murderMystery.getLanguage().killer;
                    break;
                default:
                    identity = this.murderMystery.getLanguage().death;
                    break;
            }
            LinkedList<String> ms = new LinkedList<>(Arrays.asList(this.language.gameTimeScoreBoard
                    .replace("%roomMode%", Tools.getStringRoomMode(this))
                    .replace("%identity%", identity)
                    .replace("%playerNumber%", playerNumber + "")
                    .replace("%time%", this.gameTime + "").split("\n")));
            ms.add(" ");
            if (detectiveSurvival) {
                ms.addAll(Arrays.asList(this.language.detectiveSurvival.split("\n")));
            }else {
                ms.addAll(Arrays.asList(this.language.detectiveDeath.split("\n")));
            }
            ms.add("  ");
            if (entry.getValue() == 3) {
                if (this.effectCD > 0) {
                    ms.add(this.language.gameEffectCDScoreBoard
                            .replace("%time%", this.effectCD + ""));
                }
                if (this.swordCD > 0) {
                    ms.add(this.language.gameSwordCDScoreBoard
                            .replace("%time%", this.swordCD + ""));
                }
                if (this.scanCD > 0) {
                    ms.add(this.language.gameScanCDScoreBoard
                            .replace("%time%", this.scanCD + ""));
                }
            }
            this.murderMystery.getScoreboard().showScoreboard(entry.getKey(), this.language.scoreBoardTitle, ms);
        }
    }

    /**
     * 分配玩家身份
     */
    @Override
    protected void assignIdentity() {
        ConcurrentHashMap<Player, Integer> players = this.getPlayers();
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
        player.getAdventureSettings().set(AdventureSettings.Type.NO_CLIP, false);
        player.getAdventureSettings().update();
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
