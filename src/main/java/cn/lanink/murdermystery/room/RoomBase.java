package cn.lanink.murdermystery.room;

import cn.lanink.murdermystery.MurderMystery;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 房间抽象类
 * 任何房间类都应继承此类
 *
 * @author lt_name
 */

public abstract class RoomBase {

    protected String gameMode = null;
    protected MurderMystery murderMystery = MurderMystery.getInstance();
    protected Language language = MurderMystery.getInstance().getLanguage();

    protected int mode; //0等待重置 1玩家等待中 2玩家游戏中 3胜利结算中
    protected final int setWaitTime, setGameTime, setGoldSpawnTime;
    public int waitTime, gameTime; //秒
    public int effectCD, swordCD, scanCD; //杀手技能CD
    protected final ArrayList<Position> randomSpawn = new ArrayList<>();
    protected final ArrayList<Position> goldSpawn = new ArrayList<>();
    protected final Position waitSpawn;
    protected final Level level;
    public ArrayList<ArrayList<Vector3>> placeBlocks = new ArrayList<>();
    protected final LinkedHashMap<Player, Integer> players = new LinkedHashMap<>(); //0未分配 1平民 2侦探 3杀手
    protected final LinkedHashMap<Player, Integer> skinNumber = new LinkedHashMap<>(); //玩家使用皮肤编号，用于防止重复使用
    protected final LinkedHashMap<Player, Skin> skinCache = new LinkedHashMap<>(); //缓存玩家皮肤，用于退出房间时还原

    /**
     * 初始化
     *
     * @param config 配置文件
     */
    public RoomBase(Config config) {
        this.level = Server.getInstance().getLevelByName(config.getString("world"));
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
        this.mode = 0;
        this.initTime();
    }

    public final void setGameName(String gameMode) {
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
    private void initTask() {
        if (this.mode != 1) {
            this.setMode(1);
            Server.getInstance().getScheduler().scheduleRepeatingTask(
                    MurderMystery.getInstance(), new WaitTask(MurderMystery.getInstance(), this), 20);
        }
    }



    /**
     * @param mode 房间状态
     */
    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * @return 房间状态
     */
    public int getMode() {
        return this.mode;
    }

    /**
     * 设置玩家随机皮肤
     *
     * @param player 玩家
     */
    public void setRandomSkin(Player player) {
        for (Map.Entry<Integer, Skin> entry : MurderMystery.getInstance().getSkins().entrySet()) {
            if (!this.skinNumber.containsValue(entry.getKey())) {
                this.skinCache.put(player, player.getSkin());
                this.skinNumber.put(player, entry.getKey());
                Tools.setHumanSkin(player, entry.getValue());
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
            Tools.setHumanSkin(player, this.skinCache.get(player));
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
    public void quitRoom(Player player) {
        this.quitRoom(player, true);
    }

    /**
     * 退出房间
     *
     * @param player 玩家
     * @param online 是否在线
     */
    public abstract void quitRoom(Player player, boolean online);

    /**
     * 记录在游戏内的玩家
     *
     * @param player 玩家
     */
    public void addPlaying(Player player) {
        if (!this.players.containsKey(player)) {
            this.addPlaying(player, 0);
        }
    }

    /**
     * 记录在游戏内的玩家
     *
     * @param player 玩家
     * @param mode 身份
     */
    public void addPlaying(Player player, Integer mode) {
        this.players.put(player, mode);
    }

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
    public LinkedHashMap<Player, Integer> getPlayers() {
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
     * @deprecated
     */
    public Integer getPlayerMode(Player player) {
        if (isPlaying(player)) {
            return this.players.get(player);
        }else {
            return null;
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
     * @return 金锭刷新时间
     */
    public int getSetGoldSpawnTime() {
        return this.setGoldSpawnTime;
    }

    /**
     * @return 等待时间
     */
    public int getSetWaitTime() {
        return this.setWaitTime;
    }

    /**
     * @return 游戏时间
     */
    public int getSetGameTime() {
        return this.setGameTime;
    }

    /**
     * @return 金锭产出地点
     */
    public ArrayList<Position> getGoldSpawn() {
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

    /**
     * 房间开始游戏
     */
    public abstract void gameStart();

    /**
     * 结束本局游戏
     */
    public void endGame() {
        this.endGame(true);
    }

    /**
     * 结束本局游戏
     *
     * @param normal 正常关闭
     */
    public abstract void endGame(boolean normal);

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

    /**
     * 分配玩家身份
     */
    public abstract void assignIdentity();

    /**
     * 获取存活玩家数
     *
     * @return 存活玩家数
     */
    public abstract int getSurvivorPlayerNumber();

    /**
     * 符合游戏条件的攻击
     *
     * @param damage 攻击者
     * @param player 被攻击者
     */
    public abstract void playerDamage(Player damage, Player player);

    /**
     * 玩家死亡
     *
     * @param player 玩家
     */
    public abstract void playerDeath(Player player);

    /**
     * 尸体生成
     *
     * @param player 玩家
     */
    public abstract void playerCorpseSpawn(Player player);

    /**
     * 胜利
     *
     * @param victoryMode 胜利队伍
     */
    protected void victory(int victoryMode) {
        if (this.getPlayers().values().size() > 0) {
            this.setMode(3);
            Server.getInstance().getScheduler().scheduleRepeatingTask(this.murderMystery,
                    new VictoryTask(this.murderMystery, this, victoryMode), 20);
        }else {
            this.endGame();
        }
    }

}
