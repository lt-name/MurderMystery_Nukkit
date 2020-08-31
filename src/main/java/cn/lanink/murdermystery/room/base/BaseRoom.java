package cn.lanink.murdermystery.room.base;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.event.*;
import cn.lanink.murdermystery.tasks.VictoryTask;
import cn.lanink.murdermystery.tasks.WaitTask;
import cn.lanink.murdermystery.tasks.game.TimeTask;
import cn.lanink.murdermystery.tasks.game.TipsTask;
import cn.lanink.murdermystery.utils.Language;
import cn.lanink.murdermystery.utils.SavePlayerInventory;
import cn.lanink.murdermystery.utils.Tips;
import cn.lanink.murdermystery.utils.Tools;
import cn.lanink.murdermystery.utils.exception.RoomLoadException;
import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Config;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 房间抽象类
 * 任何房间类都应继承此类
 *
 * @author lt_name
 */
public abstract class BaseRoom implements IRoomStatus {

    private String gameMode = null;
    protected MurderMystery murderMystery = MurderMystery.getInstance();
    protected Language language = MurderMystery.getInstance().getLanguage();
    protected int status;
    protected int minPlayers, maxPlayers; //房间人数
    public final int setWaitTime, setGameTime, setGoldSpawnTime;
    public int waitTime, gameTime; //秒
    public int effectCD, swordCD, scanCD; //杀手技能CD
    protected final List<Position> randomSpawn = new ArrayList<>();
    protected final List<Vector3> goldSpawnVector3List = new ArrayList<>();
    protected final Position waitSpawn;
    protected Level level;
    private final String levelName;
    public List<List<Vector3>> placeBlocks = new LinkedList<>();
    protected final ConcurrentHashMap<Player, Integer> players = new ConcurrentHashMap<>(); //0未分配 1平民 2侦探 3杀手
    protected final HashSet<Player> spectatorPlayers = new HashSet<>(); //旁观玩家
    protected final HashMap<Player, Integer> skinNumber = new HashMap<>(); //玩家使用皮肤编号，用于防止重复使用
    protected final HashMap<Player, Skin> skinCache = new HashMap<>(); //缓存玩家皮肤，用于退出房间时还原

    /**
     * 初始化
     *
     * @param level 世界
     * @param config 配置文件
     */
    public BaseRoom(Level level, Config config) throws RoomLoadException {
        this.status = ROOM_STATUS_LEVEL_NOT_LOADED;
        this.level = level;
        this.levelName = level.getFolderName();
        if (!this.murderMystery.getTemporaryRooms().contains(this.levelName)) {
            File backup = new File(this.murderMystery.getWorldBackupPath() + this.levelName);
            if (!backup.exists()) {
                this.murderMystery.getLogger().info(this.language.roomLevelBackup.replace("%name%", this.levelName));
                Server.getInstance().unloadLevel(this.level);
                if (Tools.copyDir(Server.getInstance().getFilePath() + "/worlds/" + this.levelName, backup)) {
                    Server.getInstance().loadLevel(this.levelName);
                    this.level = Server.getInstance().getLevelByName(this.levelName);
                }else {
                    throw new RoomLoadException("房间地图备份失败！ / The room world backup failed!");
                }
            }else {
                this.murderMystery.getLogger().info(this.language.roomLevelBackupExist.replace("%name%", this.levelName));
            }
        }
        this.minPlayers = config.getInt("minPlayers", 3);
        this.maxPlayers = config.getInt("maxPlayers", 16);
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
            this.goldSpawnVector3List.add(new Vector3(
                    Integer.parseInt(s[0]),
                    Integer.parseInt(s[1]),
                    Integer.parseInt(s[2])));
        }
        this.initTime();
        this.enableListener();
        this.status = ROOM_STATUS_TASK_NEED_INITIALIZED;
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
     * @return 房间最少人数
     */
    public int getMinPlayers() {
        return this.minPlayers;
    }

    /**
     * @return 房间最多人数
     */
    public int getMaxPlayers() {
        return this.maxPlayers;
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
     * 启用监听器
     */
    public void enableListener() {
        this.murderMystery.getMurderMysteryListeners().get("RoomLevelProtection").addListenerRoom(this);
        this.murderMystery.getMurderMysteryListeners().get("DefaultGameListener").addListenerRoom(this);
        this.murderMystery.getMurderMysteryListeners().get("DefaultDamageListener").addListenerRoom(this);
    }

    /**
     * 初始化Task
     */
    protected void initTask() {
        if (this.status != 1) {
            this.setStatus(1);
            Server.getInstance().getScheduler().scheduleRepeatingTask(
                    MurderMystery.getInstance(), new WaitTask(this.murderMystery, this), 20);
        }
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
     * @return 是否可以加入房间
     */
    public boolean canJoin() {
        return (this.status == ROOM_STATUS_TASK_NEED_INITIALIZED || this.status == ROOM_STATUS_WAIT) &&
                this.players.size() < this.getMaxPlayers();
    }

    public synchronized void joinRoom(Player player) {
        this.joinRoom(player, false);
    }

    /**
     * 加入房间
     *
     * @param player 玩家
     * @param spectator 观战
     */
    public void joinRoom(Player player, boolean spectator) {
        if (this.status < 0 || this.status > 2) {
            return;
        }
        if (this.status == 0) {
            this.initTask();
        }
        SavePlayerInventory.save(player);
        Tools.rePlayerState(player, true);
        Tools.giveItem(player, 10);
        if (this.murderMystery.isHasTips()) {
            Tips.closeTipsShow(this.level.getName(), player);
        }
        player.sendMessage(language.joinRoom.replace("%name%", this.level.getName()));
        if (spectator || this.status == ROOM_STATUS_GAME || this.players.size() >= this.getMaxPlayers()) {
            this.spectatorPlayers.add(player);
            player.teleport(this.randomSpawn.get(MurderMystery.RANDOM.nextInt(this.randomSpawn.size())));
            player.setGamemode(3);
            player.getAdventureSettings().set(AdventureSettings.Type.NO_CLIP, false);
            player.getAdventureSettings().update();
            Tools.hidePlayer(this, player);
        }else {
            this.players.put(player, 0);
            this.setRandomSkin(player);
            player.teleport(this.getWaitSpawn());
            this.autoExpansionRoom();
        }
        Server.getInstance().getScheduler().scheduleDelayedTask(this.murderMystery, () -> {
            if (player.level != this.level) {
                this.quitRoom(player);
            }
        }, 20);
    }

    /**
     * 退出房间
     *
     * @param player 玩家
     */
    public synchronized void quitRoom(Player player) {
        this.players.remove(player);
        if (this.murderMystery.isHasTips()) {
            Tips.removeTipsConfig(this.level.getName(), player);
        }
        if (this.spectatorPlayers.contains(player)) {
            this.spectatorPlayers.remove(player);
        }else {
            this.restorePlayerSkin(player);
            this.skinNumber.remove(player);
            this.skinCache.remove(player);
        }
        MurderMystery.getInstance().getScoreboard().closeScoreboard(player);
        player.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
        Tools.rePlayerState(player, false);
        SavePlayerInventory.restore(player);
        for (Player p : this.players.keySet()) {
            p.showPlayer(player);
            player.showPlayer(p);
        }
    }

    /**
     * @param player 玩家
     * @return boolean 玩家是否在游戏里
     */
    public boolean isPlaying(Player player) {
        return this.players.containsKey(player);
    }

    /**
     * @param player 玩家
     * @return 是否是旁观者（观战）
     */
    public boolean isSpectator(Player player) {
        return this.spectatorPlayers.contains(player);
    }

    /**
     * @return 玩家列表
     */
    public ConcurrentHashMap<Player, Integer> getPlayers() {
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
    public List<Vector3> getGoldSpawnVector3List() {
        return this.goldSpawnVector3List;
    }

    /**
     * @return 游戏世界
     */
    public Level getLevel() {
        return this.level;
    }

    /**
     * @return 游戏世界名称
     */
    public final String getLevelName() {
        return this.levelName;
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

    public synchronized final void gameStartEvent() {
        if (this.status == ROOM_STATUS_GAME || this.status == ROOM_STATUS_VICTORY) {
            return;
        }
        Server.getInstance().getPluginManager().callEvent(new MurderMysteryRoomStartEvent(this));
        this.gameStart();
        this.scheduleTask();
        this.autoExpansionRoom();
    }

    /**
     * 房间开始游戏
     */
    protected abstract void gameStart();

    public void scheduleTask() {
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                MurderMystery.getInstance(), new TimeTask(this.murderMystery, this.getTimeTask()), 20);
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                MurderMystery.getInstance(), new TipsTask(this.murderMystery, this.getTipsTask()), 18, true);
    }

    /**
     * 结束本局游戏
     */
    public final synchronized void endGameEvent() {
        this.endGameEvent(0);
    }

    public final synchronized void endGameEvent(int victory) {
        if (this.status == ROOM_STATUS_LEVEL_NOT_LOADED) {
            return;
        }
        Server.getInstance().getPluginManager().callEvent(new MurderMysteryRoomEndEvent(this, victory));
        this.endGame(victory);
        if (this.murderMystery.getTemporaryRooms().contains(this.levelName)) {
            this.status = ROOM_STATUS_LEVEL_NOT_LOADED;
            this.murderMystery.removeTemporaryRoom(this.levelName);
        }
    }

    /**
     * 结束本局游戏
     *
     * @param victory 胜利队伍
     */
    protected abstract void endGame(int victory);

    /**
     * @return 计时Task
     */
    public abstract ITimeTask getTimeTask();

    /**
     * @return 显示Task
     */
    public abstract IAsyncTipsTask getTipsTask();

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
        if (this.status != ROOM_STATUS_VICTORY && this.getPlayers().size() > 0) {
            this.setStatus(ROOM_STATUS_VICTORY);
            Server.getInstance().getScheduler().scheduleRepeatingTask(this.murderMystery,
                    new VictoryTask(this.murderMystery, this, victoryMode), 20);
        }else {
            this.endGameEvent();
        }
    }

    /**
     * 检查是否需要生成临时房间
     */
    protected void autoExpansionRoom() {
        if (this.murderMystery.isAutomaticExpansionRoom()) {
            CompletableFuture.runAsync(() -> {
                LinkedList<String> cache = new LinkedList<>();
                int x = 0;
                for (Map.Entry<String, BaseRoom> entry : this.murderMystery.getRooms().entrySet()) {
                    if (this.getGameMode().equals(entry.getValue().getGameMode())) {
                        cache.add(entry.getKey());
                        if ((entry.getValue().getStatus() == ROOM_STATUS_TASK_NEED_INITIALIZED ||
                                entry.getValue().getStatus() == ROOM_STATUS_WAIT) &&
                                entry.getValue().players.size() < entry.getValue().getMaxPlayers()) {
                            x++;
                        }
                    }
                }
                if (x == 0 && cache.size() > 0) {
                    this.murderMystery.addTemporaryRoom(cache.get(MurderMystery.RANDOM.nextInt(cache.size())));
                }
            }, MurderMystery.checkRoomThreadPool);
        }
    }

    /**
     * 还原房间地图
     */
    protected void restoreWorld() {
        if (!this.murderMystery.isRestoreWorld() ||
                this.murderMystery.getTemporaryRooms().contains(this.levelName)) {
            return;
        }
        this.status = ROOM_STATUS_LEVEL_NOT_LOADED;
        if (MurderMystery.debug) {
            murderMystery.getLogger().info("§a房间：" + this.levelName + " 正在还原地图...");
        }
        Server.getInstance().unloadLevel(this.level);
        File levelFile = new File(Server.getInstance().getFilePath() + "/worlds/" + this.levelName);
        File backup = new File(this.murderMystery.getWorldBackupPath() + this.levelName);
        if (!backup.exists()) {
            this.murderMystery.getLogger().error(this.language.roomLevelBackupNotExist.replace("%name%", this.levelName));
            this.murderMystery.unloadRoom(this.levelName);
        }
        CompletableFuture.runAsync(() -> {
            if (Tools.deleteFile(levelFile) && Tools.copyDir(backup, levelFile)) {
                Server.getInstance().loadLevel(this.levelName);
                this.level = Server.getInstance().getLevelByName(this.levelName);
                this.waitSpawn.setLevel(this.level);
                for (Position position : this.randomSpawn) {
                    position.setLevel(this.level);
                }
                this.status = ROOM_STATUS_TASK_NEED_INITIALIZED;
                if (MurderMystery.debug) {
                    this.murderMystery.getLogger().info("§a房间：" + this.levelName + " 地图还原完成！");
                }
            }else {
                this.murderMystery.getLogger().error(this.language.roomLevelRestoreLevelFailure.replace("%name%", this.levelName));
                this.murderMystery.unloadRoom(this.levelName);
            }
        });
    }

}
