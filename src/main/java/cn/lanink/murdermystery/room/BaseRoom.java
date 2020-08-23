package cn.lanink.murdermystery.room;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.event.*;
import cn.lanink.murdermystery.tasks.VictoryTask;
import cn.lanink.murdermystery.tasks.WaitTask;
import cn.lanink.murdermystery.utils.Language;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Config;

import java.util.*;

/**
 * 房间抽象类
 * 任何房间类都应继承此类
 *
 * @author lt_name
 */

public abstract class BaseRoom {

    private String gameMode = null;
    protected MurderMystery murderMystery = MurderMystery.getInstance();
    protected Language language = MurderMystery.getInstance().getLanguage();
    public static final int ROOM_STATUS_WAIT = 1;
    public static final int ROOM_STATUS_GAME = 2;
    public static final int ROOM_STATUS_VICTORY = 3;
    protected int status; //0等待重置 1玩家等待中 2玩家游戏中 3胜利结算中
    public final int setWaitTime, setGameTime, setGoldSpawnTime;
    public int waitTime, gameTime; //秒
    public int effectCD, swordCD, scanCD; //杀手技能CD
    protected final List<Position> randomSpawn = new ArrayList<>();
    protected final List<Position> goldSpawn = new ArrayList<>();
    protected final Position waitSpawn;
    protected Level level;
    public List<List<Vector3>> placeBlocks = new LinkedList<>();
    protected final HashMap<Player, Integer> players = new HashMap<>(); //0未分配 1平民 2侦探 3杀手
    protected final HashMap<Player, Integer> skinNumber = new HashMap<>(); //玩家使用皮肤编号，用于防止重复使用
    protected final HashMap<Player, Skin> skinCache = new HashMap<>(); //缓存玩家皮肤，用于退出房间时还原

    /**
     * 初始化
     *
     * @param level 世界
     * @param config 配置文件
     */
    public BaseRoom(Level level, Config config) {
        this.level = level;
        this.setWaitTime = config.getInt("waitTime");
        this.setGameTime = config.getInt("gameTime");
        this.setGoldSpawnTime = config.getInt("goldSpawnTime");
        String[] s1 = config.getString("waitSpawn").split(":");
        this.waitSpawn = new Position(Integer.parseInt(s1[0]),
                Integer.parseInt(s1[1]),
                Integer.parseInt(s1[2]),
                this.getLevel());
        for (String string : config.getStringList("randomSpawn")) {
            String[] s = string.split(":");
            this.randomSpawn.add(new Position(
                    Integer.parseInt(s[0]),
                    Integer.parseInt(s[1]),
                    Integer.parseInt(s[2]),
                    this.level));
        }
        for (String string : config.getStringList("goldSpawn")) {
            String[] s = string.split(":");
            this.goldSpawn.add(new Position(
                    Integer.parseInt(s[0]),
                    Integer.parseInt(s[1]),
                    Integer.parseInt(s[2]),
                    this.level));
        }
        this.status = 0;
        this.initTime();
    }

    public final void setGameMode(String gameMode) {
        if (this.gameMode == null) {
            this.gameMode = gameMode;
        }
    }

    public final String getGameMode() {
        return this.gameMode;
    }

    /**
     * 初始化时间参数
     */
    public void initTime() {
        this.waitTime = this.setWaitTime;
        this.gameTime = this.setGameTime;
        this.effectCD = 0;
        this.swordCD = 0;
        this.scanCD = 0;
    }

    /**
     * 初始化Task
     */
    protected void initTask() {
        if (this.status != 1) {
            this.setStatus(1);
            Server.getInstance().getScheduler().scheduleRepeatingTask(
                    MurderMystery.getInstance(), new WaitTask(MurderMystery.getInstance(), this), 20);
        }
    }

    /**
     * @param status 房间状态
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * @return 房间状态
     */
    public int getStatus() {
        return this.status;
    }

    /**
     * 设置玩家随机皮肤
     *
     * @param player 玩家
     */
    public void setRandomSkin(Player player) {
        for (Map.Entry<Integer, Skin> entry : this.murderMystery.getSkins().entrySet()) {
            if (!this.skinNumber.containsValue(entry.getKey())) {
                this.skinCache.put(player, player.getSkin());
                this.skinNumber.put(player, entry.getKey());
                Tools.setHumanSkin(player, entry.getValue(), true);
                return;
            }
        }
    }

    /**
     * 还原玩家皮肤
     *
     * @param player 玩家
     */
    public void restorePlayerSkin(Player player) {
        if (this.skinCache.containsKey(player)) {
            Tools.setHumanSkin(player, this.skinCache.get(player), true);
            this.skinCache.remove(player);
        }
        this.skinNumber.remove(player);
    }

    /**
     * 加入房间
     *
     * @param player 玩家
     */
    public abstract void joinRoom(Player player);

    /**
     * 退出房间
     *
     * @param player 玩家
     */
    public abstract void quitRoom(Player player);

    /**
     * @return boolean 玩家是否在游戏里
     * @param player 玩家
     */
    public boolean isPlaying(Player player) {
        return this.players.containsKey(player);
    }

    /**
     * @return 玩家列表
     */
    public HashMap<Player, Integer> getPlayers() {
        return this.players;
    }

    /**
     * 获取玩家身份
     *
     * @param player 玩家
     * @return 身份
     */
    public int getPlayers(Player player) {
        if (isPlaying(player)) {
            return this.players.get(player);
        }else {
            return 0;
        }
    }

    /**
     * @return 出生点
     */
    public Position getWaitSpawn() {
        return this.waitSpawn;
    }

    /**
     * @return 随机出生点列表
     */
    public List<Position> getRandomSpawn() {
        return this.randomSpawn;
    }

    /**
     * @return 金锭产出地点
     */
    public List<Position> getGoldSpawn() {
        return this.goldSpawn;
    }

    /**
     * @return 游戏世界
     */
    public Level getLevel() {
        return this.level;
    }

    /**
     * 获取玩家在游戏中使用的皮肤
     * @param player 玩家
     * @return 皮肤
     */
    public Skin getPlayerSkin(Player player) {
        if (this.skinNumber.containsKey(player)) {
            return MurderMystery.getInstance().getSkins().get(this.skinNumber.get(player));
        }
        return player.getSkin();
    }

    public final void gameStartEvent() {
        Server.getInstance().getPluginManager().callEvent(new MurderMysteryRoomStartEvent(this));
        this.gameStart();
    }

    /**
     * 房间开始游戏
     */
    protected abstract void gameStart();

    /**
     * 结束本局游戏
     */
    public final synchronized void endGameEvent() {
        this.endGameEvent(0);
    }

    public final synchronized void endGameEvent(int victory) {
        Server.getInstance().getPluginManager().callEvent(new MurderMysteryRoomEndEvent(this, victory));
        this.endGame(victory);
    }

    /**
     * 结束本局游戏
     *
     * @param victory 胜利队伍
     */
    protected abstract void endGame(int victory);

    /**
     * 计时Task
     */
    public abstract void asyncTimeTask();

    /**
     * 金锭生成
     */
    public abstract void goldSpawn();

    /**
     * 异步金锭Task
     */
    public abstract void asyncGoldTask();

    public final void assignIdentityEvent() {
        MurderMysteryRoomAssignIdentityEvent ev = new MurderMysteryRoomAssignIdentityEvent(this);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (!ev.isCancelled()) {
            this.assignIdentity();
        }
    }

    /**
     * 分配玩家身份
     */
    protected abstract void assignIdentity();

    /**
     * 获取存活玩家数
     *
     * @return 存活玩家数
     */
    public abstract int getSurvivorPlayerNumber();

    public final void playerDamageEvent(Player damage, Player player) {
        MurderMysteryPlayerDamageEvent ev = new MurderMysteryPlayerDamageEvent(this, damage, player);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (!ev.isCancelled()) {
            this.playerDamage(damage, player);
        }
    }

    /**
     * 符合游戏条件的攻击
     *
     * @param damage 攻击者
     * @param player 被攻击者
     */
    protected abstract void playerDamage(Player damage, Player player);

    public final void playerDeathEvent(Player player) {
        MurderMysteryPlayerDeathEvent ev = new MurderMysteryPlayerDeathEvent(this, player);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (!ev.isCancelled()) {
            this.playerDeath(player);
        }
    }

    /**
     * 玩家死亡
     *
     * @param player 玩家
     */
    protected abstract void playerDeath(Player player);

    public final void playerCorpseSpawnEvent(Player player) {
        MurderMysteryPlayerCorpseSpawnEvent ev = new MurderMysteryPlayerCorpseSpawnEvent(this, player);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (!ev.isCancelled()) {
            this.playerCorpseSpawn(player);
        }
    }

    /**
     * 尸体生成
     *
     * @param player 玩家
     */
    protected abstract void playerCorpseSpawn(Player player);

    /**
     * 胜利
     *
     * @param victoryMode 胜利队伍
     */
    protected void victory(int victoryMode) {
        if (this.status != 3 && this.getPlayers().size() > 0) {
            this.setStatus(3);
            Server.getInstance().getScheduler().scheduleRepeatingTask(this.murderMystery,
                    new VictoryTask(this.murderMystery, this, victoryMode), 20);
        }else {
            this.endGameEvent();
        }
    }

}
