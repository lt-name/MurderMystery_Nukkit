package cn.lanink.murdermystery.room.base;

import cn.lanink.gamecore.utils.FileUtil;
import cn.lanink.gamecore.utils.Language;
import cn.lanink.gamecore.utils.SavePlayerInventory;
import cn.lanink.gamecore.utils.Tips;
import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.entity.EntityPlayerCorpse;
import cn.lanink.murdermystery.entity.data.MurderMysterySkin;
import cn.lanink.murdermystery.event.*;
import cn.lanink.murdermystery.item.ItemManager;
import cn.lanink.murdermystery.listener.BaseMurderMysteryListener;
import cn.lanink.murdermystery.tasks.VictoryTask;
import cn.lanink.murdermystery.tasks.WaitTask;
import cn.lanink.murdermystery.tasks.Watchdog;
import cn.lanink.murdermystery.tasks.game.TimeTask;
import cn.lanink.murdermystery.tasks.game.TipsTask;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.AdventureSettings;
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
import cn.nukkit.utils.Config;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 房间抽象类
 * 任何房间类都应继承此类
 *
 * @author LT_Name
 */
public abstract class BaseRoom implements ITimeTask, IAsyncTipsTask {

    protected MurderMystery murderMystery = MurderMystery.getInstance();

    private String gameMode = null;

    protected RoomStatus status;

    protected int minPlayers; //房间人数
    protected int maxPlayers;

    public final int setWaitTime;
    public final int setGameTime;
    public final int setGoldSpawnTime;

    public int waitTime; //秒
    public int gameTime;
    public int goldSpawnTime;

    public Map<Player, Integer> killerSwordCD = new HashMap<>();
    public Map<Player, Integer> killerEffectCD = new HashMap<>();
    public Map<Player, Integer> killerScanCD = new HashMap<>();

    protected final Position waitSpawn;
    protected final ArrayList<Position> randomSpawn = new ArrayList<>();
    protected final ArrayList<Vector3> goldSpawnVector3List = new ArrayList<>();

    protected Level level;
    private final String levelName;

    public List<List<Vector3>> placeBlocks = new LinkedList<>();

    protected final ConcurrentHashMap<Player, PlayerIdentity> players = new ConcurrentHashMap<>();
    protected final Set<Player> spectatorPlayers = Collections.synchronizedSet(new HashSet<>()); //旁观玩家

    protected final HashMap<Player, Integer> skinNumber = new HashMap<>(); //玩家使用皮肤编号，用于防止重复使用
    protected final HashMap<Player, Skin> skinCache = new HashMap<>(); //缓存玩家皮肤，用于退出房间时还原

    public Player killKillerPlayer = null; //击杀杀手的玩家
    public EntityItem detectiveBow = null; //掉落的侦探弓

    /**
     * 初始化
     *
     * @param level 世界
     * @param config 配置文件
     */
    public BaseRoom(@NotNull Level level, @NotNull Config config) throws RoomLoadException {
        this.setStatus(RoomStatus.LEVEL_NOT_LOADED);
        this.level = level;
        this.levelName = level.getFolderName();
        if (!this.murderMystery.getTemporaryRooms().contains(this.levelName)) {
            File backup = new File(this.murderMystery.getWorldBackupPath() + this.levelName);
            if (!backup.exists()) {
                this.murderMystery.getLogger().info(this.murderMystery.getLanguage(null)
                        .translateString("roomLevelBackup").replace("%name%", this.getFullRoomName()));
                Server.getInstance().unloadLevel(this.level);
                if (FileUtil.copyDir(Server.getInstance().getFilePath() + "/worlds/" + this.levelName, backup)) {
                    Server.getInstance().loadLevel(this.levelName);
                    this.level = Server.getInstance().getLevelByName(this.levelName);
                }else {
                    throw new RoomLoadException("房间地图备份失败！ / The room world backup failed!");
                }
            }else {
                this.murderMystery.getLogger().info(this.murderMystery.getLanguage(null)
                        .translateString("roomLevelBackupExist").replace("%name%", this.getFullRoomName()));
            }
        }
        try {
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
            this.waitSpawn.y += 0.2; //防止卡地里
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
            this.initData();
            this.enableListener();
            this.setStatus(RoomStatus.TASK_NEED_INITIALIZED);
            Watchdog.add(this);
        } catch (Exception e) {
            throw new RoomLoadException(MurderMystery.getInstance().getLanguage()
                    .translateString("roomLoadedFailureByConfig").replace("%name%", this.getFullRoomName()));
        }
    }

    public final void setGameMode(@NotNull String gameMode) {
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
    @Override
    public void setStatus(@NotNull RoomStatus status) {
        this.status = status;
    }

    /**
     * @return 房间状态
     */
    @Override
    public RoomStatus getStatus() {
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
    public void initData() {
        this.waitTime = this.setWaitTime;
        this.gameTime = this.setGameTime;
        this.killerEffectCD.clear();
        this.killerSwordCD.clear();
        this.killerScanCD.clear();
        this.placeBlocks.clear();
        this.skinNumber.clear();
        this.skinCache.clear();
        this.killKillerPlayer = null;
        this.detectiveBow = null;
    }

    /**
     * 启用监听器
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void enableListener() {
        HashMap<String, BaseMurderMysteryListener> map = this.murderMystery.getMurderMysteryListeners();
        map.get("RoomLevelProtection").addListenerRoom(this);
        map.get("DefaultGameListener").addListenerRoom(this);
        map.get("DefaultChatListener").addListenerRoom(this);
        map.get("DefaultDamageListener").addListenerRoom(this);
        map.get("ClassicDamageListener").addListenerRoom(this);
    }

    /**
     * 初始化Task
     */
    protected void initTask() {
        if (this.status != RoomStatus.WAIT) {
            this.setStatus(RoomStatus.WAIT);
            Server.getInstance().getScheduler().scheduleRepeatingTask(
                    this.murderMystery, new WaitTask(this.murderMystery, this), 20);
        }
    }

    public MurderMysterySkin getGameSkin(@NotNull Player player) {
        if (this.skinNumber.containsKey(player)) {
            return this.murderMystery.getSkins().get(this.skinNumber.get(player));
        }
        return new MurderMysterySkin(player.getSkin());
    }

    /**
     * 设置玩家随机皮肤
     *
     * @param player 玩家
     */
    public void setRandomSkin(@NotNull Player player) {
        for (Map.Entry<Integer, MurderMysterySkin> entry : this.murderMystery.getSkins().entrySet()) {
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
    public void restorePlayerSkin(@NotNull Player player) {
        if (this.skinCache.containsKey(player)) {
            Tools.setHumanSkin(player, this.skinCache.get(player));
            this.skinCache.remove(player);
        }
        this.skinNumber.remove(player);
    }

    /**
     * @return 是否可以加入房间
     */
    public boolean canJoin() {
        return (this.status == RoomStatus.TASK_NEED_INITIALIZED || this.status == RoomStatus.WAIT) &&
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
    public synchronized void joinRoom(@NotNull Player player, boolean spectator) {
        if (this.getStatus() == RoomStatus.LEVEL_NOT_LOADED ||
                this.getStatus() == RoomStatus.VICTORY) {
            return;
        }

        Server.getInstance().getPluginManager().callEvent(new MurderMysteryRoomPlayerJoinEvent(this, player));

        Server.getInstance().getScheduler().scheduleDelayedTask(this.murderMystery, () -> {
            if (this.isPlaying(player) && player.getLevel() != this.level) {
                this.quitRoom(player);
            }
        }, 1);
        if (this.status == RoomStatus.TASK_NEED_INITIALIZED) {
            this.initTask();
        }
        SavePlayerInventory.save(this.murderMystery, player);
        Tools.rePlayerState(player, true);
        Tools.giveItem(player, 10);
        if (this.murderMystery.isHasTips()) {
            Tips.closeTipsShow(this.level.getName(), player);
        }
        player.sendMessage(this.murderMystery.getLanguage(player).translateString("joinRoom")
                .replace("%name%", this.getRoomName()));
        if (spectator || this.status == RoomStatus.GAME || this.players.size() >= this.getMaxPlayers()) {
            this.spectatorPlayers.add(player);
            player.teleport(this.randomSpawn.get(MurderMystery.RANDOM.nextInt(this.randomSpawn.size())));
            player.setGamemode(3);
            player.getAdventureSettings().set(AdventureSettings.Type.NO_CLIP, false).update();
            Tools.hidePlayer(this, player);
        }else {
            this.players.put(player, PlayerIdentity.NULL);
            this.setRandomSkin(player);
            player.teleport(this.getWaitSpawn());
            this.autoCreateTemporaryRoom();
        }
    }

    /**
     * 退出房间
     *
     * @param player 玩家
     */
    public synchronized void quitRoom(Player player) {
        Server.getInstance().getPluginManager().callEvent(new MurderMysteryRoomPlayerQuitEvent(this, player));

        this.players.remove(player);
        if (this.murderMystery.isHasTips()) {
            Tips.removeTipsConfig(this.level.getName(), player);
        }
        if (this.spectatorPlayers.contains(player)) {
            this.spectatorPlayers.remove(player);
        }else {
            this.restorePlayerSkin(player);
        }
        this.murderMystery.getScoreboard().closeScoreboard(player);
        player.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
        Tools.rePlayerState(player, false);
        SavePlayerInventory.restore(this.murderMystery, player);
        for (Player p : this.players.keySet()) {
            p.showPlayer(player);
            player.showPlayer(p);
        }
        player.sendMessage(this.murderMystery.getLanguage(player).translateString("quitRoom"));
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
    public ConcurrentHashMap<Player, PlayerIdentity> getPlayers() {
        return this.players;
    }

    /**
     * @return 观战玩家
     */
    public Set<Player> getSpectatorPlayers() {
        return this.spectatorPlayers;
    }

    /**
     * 获取玩家身份
     *
     * @param player 玩家
     * @return 身份
     */
    public PlayerIdentity getPlayers(Player player) {
        if (this.isPlaying(player)) {
            return this.players.get(player);
        }else {
            return PlayerIdentity.NULL;
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

    public String getShowRoomName() {
        return this.murderMystery.getRoomName().get(this.getLevelName());
    }

    public String getFullRoomName() {
        return this.getShowRoomName() + "(" + this.getLevelName() + ")";
    }

    /**
     * @return 房间显示名称
     */
    public final String getRoomName() {
        return this.murderMystery.getRoomName().getOrDefault(this.levelName, this.levelName);
    }

    /**
     * 获取玩家在游戏中使用的皮肤
     * @param player 玩家
     * @return 皮肤
     */
    public Skin getPlayerSkin(@NotNull Player player) {
        if (this.skinNumber.containsKey(player)) {
            return MurderMystery.getInstance().getSkins().get(this.skinNumber.get(player));
        }
        return player.getSkin();
    }

    /**
     * 房间开始游戏
     */
    public synchronized void startGame() {
        Watchdog.resetTime(this);
        if (this.status == RoomStatus.GAME || this.status == RoomStatus.VICTORY) {
            return;
        }
        Server.getInstance().getPluginManager().callEvent(new MurderMysteryRoomStartEvent(this));
        Tools.cleanEntity(this.getLevel(), true);
        this.setStatus(RoomStatus.GAME);
        Collections.shuffle(this.randomSpawn, MurderMystery.RANDOM);
        int x=0;
        for (Player player : this.getPlayers().keySet()) {
            player.getInventory().clearAll();
            player.getUIInventory().clearAll();
            if (x >= this.getRandomSpawn().size()) {
                x = 0;
            }
            player.teleport(this.getRandomSpawn().get(x));
            x++;
        }
        LinkedList<Player> gamePlayers = new LinkedList<>(this.players.keySet());
        for (Player player : this.getSpectatorPlayers()) {
            player.teleport(gamePlayers.get(MurderMystery.RANDOM.nextInt(gamePlayers.size())));
        }
        this.scheduleTask();
        this.autoCreateTemporaryRoom();
        Watchdog.resetTime(this);
    }

    public void scheduleTask() {
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                this.murderMystery, new TimeTask(this.murderMystery, this.getTimeTask()), 20);
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                this.murderMystery, new TipsTask(this.murderMystery, this.getTipsTask()), 18, true);
    }

    public void endGame() {
        this.endGame(0);
    }

    /**
     * 结束本局游戏
     * @param victory 胜利队伍
     */
    public synchronized void endGame(int victory) {
        Watchdog.resetTime(this);
        if (this.getStatus() == RoomStatus.LEVEL_NOT_LOADED) {
            return;
        }
        Server.getInstance().getPluginManager().callEvent(new MurderMysteryRoomEndEvent(this, victory));
        RoomStatus oldStatus = this.getStatus();
        this.setStatus(RoomStatus.TASK_NEED_INITIALIZED);
        for (Player p1 : this.players.keySet()) {
            for (Player p2 : this.players.keySet()) {
                p1.showPlayer(p2);
                p2.showPlayer(p1);
            }
        }
        if (victory != 0) {
            this.victoryReward(victory);
        }
        for (Player player : new HashSet<>(this.players.keySet())) {
            this.quitRoom(player);
        }
        for (Player player : new HashSet<>(this.spectatorPlayers)) {
            this.quitRoom(player);
        }
        for (Player player : this.getLevel().getPlayers().values()) {
            //不要触发传送事件，防止某些弱智操作阻止我们！
            player.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn(), null);
        }
        //因为某些原因无法正常传送走玩家，就全部踹出服务器！
        for (Player player : this.getLevel().getPlayers().values()) {
            player.kick("Teleport error!");
        }
        this.placeBlocks.forEach(list -> list.forEach(vector3 -> getLevel().setBlock(vector3, Block.get(0))));
        this.initData();
        switch (oldStatus) {
            case GAME:
            case VICTORY:
                this.restoreWorld();
                break;
        }
        this.autoClearTemporaryRoom();
        Watchdog.resetTime(this);
    }

    protected abstract void victoryReward(int victory);

    public ITimeTask getTimeTask() {
        return this;
    }

    public IAsyncTipsTask getTipsTask() {
        return this;
    }

    /**
     * 计时Task
     */
    @Override
    public void timeTask() {
        //开局20秒后给物品
        int time = this.gameTime - (this.setGameTime - 20);
        if (time >= 0) {
            if ((time%5 == 0 && time != 0) || (time <= 5 && time != 0)) {
                for (Player player : this.getPlayers().keySet()) {
                    player.sendMessage(this.murderMystery.getLanguage(player)
                            .translateString("killerGetSwordTime").replace("%time%", time + ""));
                }
                for (Player player : this.getSpectatorPlayers()) {
                    player.sendMessage(this.murderMystery.getLanguage(player)
                            .translateString("killerGetSwordTime").replace("%time%", time + ""));
                }
                Tools.playSound(this, Sound.RANDOM_CLICK);
            }
            if (time == 0) {
                this.assignIdentity();
                for (Player player : this.getPlayers().keySet()) {
                    player.sendMessage(this.murderMystery.getLanguage(player).translateString("killerGetSword"));
                }
                for (Player player : this.getSpectatorPlayers()) {
                    player.sendMessage(this.murderMystery.getLanguage(player).translateString("killerGetSword"));
                }
                for (Map.Entry<Player, PlayerIdentity> entry : this.getPlayers().entrySet()) {
                    if (entry.getValue() == PlayerIdentity.DETECTIVE) {
                        Tools.giveItem(entry.getKey(), 1);
                    }else if (entry.getValue() == PlayerIdentity.KILLER) {
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
            for (Map.Entry<Player, PlayerIdentity> entry : this.getPlayers().entrySet()) {
                switch (entry.getValue()) {
                    case COMMON_PEOPLE:
                    case DETECTIVE:
                        playerNumber++;
                        break;
                    case KILLER:
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

        //杀手技能CD计算
        for (Map.Entry<Player, Integer> entry : this.killerEffectCD.entrySet()) {
            if (entry.getValue() > 0) {
                entry.setValue(entry.getValue() - 1);
            }
        }
        for (Map.Entry<Player, Integer> entry : this.killerSwordCD.entrySet()) {
            if (entry.getValue() > 0) {
                entry.setValue(entry.getValue() - 1);
            }
        }
        for (Map.Entry<Player, Integer> entry : this.killerScanCD.entrySet()) {
            if (entry.getValue() > 0) {
                entry.setValue(entry.getValue() - 1);
            }
        }

        if (this.detectiveBow != null && this.detectiveBow.isClosed()) {
            EntityItem entityItem = new EntityItem(this.detectiveBow.chunk, this.detectiveBow.namedTag);
            entityItem.spawnToAll();
            detectiveBow = entityItem;
        }
        this.goldSpawn();
        this.goldExchange();

        Watchdog.resetTime(this);
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
                this.getLevel().dropItem(spawn, ItemManager.get(null, 266));
            }
        }
    }

    /**
     * 金锭自动兑换弓箭检测
     */
    public void goldExchange() {
        for (Map.Entry<Player, PlayerIdentity> entry : this.players.entrySet()) {
            if (entry.getValue() == PlayerIdentity.NULL || entry.getValue() == PlayerIdentity.DEATH) {
                continue;
            }
            int x = 0;
            boolean needBow = true;
            for (Item item : entry.getKey().getInventory().getContents().values()) {
                if (item.getId() == 266) {
                    x += item.getCount();
                    continue;
                }
                if (item.getId() == 261) {
                    needBow = false;
                }
            }
            if (x >= 10) {
                Item gold = ItemManager.get(null, 266);
                gold.setCount(10);
                entry.getKey().getInventory().removeItem(gold);
                entry.getKey().getInventory().addItem(Item.get(262, 0, 1));
                if (needBow) {
                    entry.getKey().getInventory().addItem(Item.get(261, 0, 1));
                }
                Tools.playSound(entry.getKey(), Sound.RANDOM_LEVELUP);
            }
        }
    }

    @Override
    public void asyncTipsTask() {
        int time = this.setGameTime - this.gameTime;
        int playerNumber = this.getSurvivorPlayerNumber();
        boolean detectiveSurvival = this.players.containsValue(PlayerIdentity.DETECTIVE);
        String identity;
        for (Map.Entry<Player, PlayerIdentity> entry : this.players.entrySet()) {
            entry.getKey().setNameTag("");
            Language language = this.murderMystery.getLanguage(entry.getKey());
            switch (entry.getValue()) {
                case COMMON_PEOPLE:
                    identity = language.translateString("commonPeople");
                    break;
                case DETECTIVE:
                    identity = language.translateString("detective");
                    break;
                case KILLER:
                    identity = language.translateString("killer");
                    break;
                case DEATH:
                    identity = language.translateString("death");
                    break;
                default:
                    identity = "???";
                    break;
            }
            LinkedList<String> ms = new LinkedList<>(Arrays.asList(language.translateString("gameTimeScoreBoard")
                    .replace("%roomMode%", Tools.getStringRoomMode(entry.getKey(), this))
                    .replace("%identity%", identity)
                    .replace("%playerNumber%", playerNumber + "")
                    .replace("%time%", this.gameTime + "").split("\n")));
            if (time > 20) {
                ms.add("§1§2§3§r");
                if (detectiveSurvival) {
                    ms.addAll(Arrays.asList(language.translateString("detectiveSurvival").split("\n")));
                } else {
                    ms.addAll(Arrays.asList(language.translateString("detectiveDeath").split("\n")));
                }
            }
            if (entry.getValue() == PlayerIdentity.KILLER) {
                ms.add("§1§2§3§4§r");
                int effectCD = this.killerEffectCD.getOrDefault(entry.getKey(), 0);
                if (effectCD > 0) {
                    ms.add(language.translateString("gameEffectCDScoreBoard")
                            .replace("%time%", effectCD + ""));
                }
                int swordCD = this.killerSwordCD.getOrDefault(entry.getKey(), 0);
                if (swordCD > 0) {
                    ms.add(language.translateString("gameSwordCDScoreBoard")
                            .replace("%time%", swordCD + ""));
                }
                int scanCD = this.killerScanCD.getOrDefault(entry.getKey(), 0);
                if (scanCD > 0) {
                    ms.add(language.translateString("gameScanCDScoreBoard")
                            .replace("%time%", scanCD + ""));
                }
            }
            this.murderMystery.getScoreboard().showScoreboard(entry.getKey(), language.translateString("scoreBoardTitle"), ms);
        }
        //旁观玩家只显示部分信息
        for (Player player : this.spectatorPlayers) {
            Language language = this.murderMystery.getLanguage(player);
            LinkedList<String> ms = new LinkedList<>(Arrays.asList(language.translateString("gameTimeScoreBoard")
                    .replace("%roomMode%", Tools.getStringRoomMode(player, this))
                    .replace("%identity%", language.translateString("spectator"))
                    .replace("%playerNumber%", playerNumber + "")
                    .replace("%time%", this.gameTime + "").split("\n")));
            ms.add(" ");
            if (time > 20) {
                if (detectiveSurvival) {
                    ms.addAll(Arrays.asList(language.translateString("detectiveSurvival").split("\n")));
                } else {
                    ms.addAll(Arrays.asList(language.translateString("detectiveDeath").split("\n")));
                }
            }
            this.murderMystery.getScoreboard().showScoreboard(player, language.translateString("scoreBoardTitle"), ms);
        }
    }

    /**
     * 分配玩家身份
     */
    protected void assignIdentity() {
        MurderMysteryRoomAssignIdentityEvent ev = new MurderMysteryRoomAssignIdentityEvent(this);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return;
        }

        ArrayList<Player> needAssignPlayers = new ArrayList<>();
        for (Map.Entry<Player, PlayerIdentity> entry : this.getPlayers().entrySet()) {
            if (entry.getValue() == PlayerIdentity.NULL) {
                needAssignPlayers.add(entry.getKey());
            }
        }

        int random1 = MurderMystery.RANDOM.nextInt(needAssignPlayers.size()) + 1;
        int random2;
        do {
            random2 = MurderMystery.RANDOM.nextInt(needAssignPlayers.size()) + 1;
        }while (random1 == random2);

        int i = 0;
        for (Player player : needAssignPlayers) {
            i++;
            //侦探
            if (i == random1) {
                this.players.put(player, PlayerIdentity.DETECTIVE);
                player.sendTitle(this.murderMystery.getLanguage(player).translateString("titleDetectiveTitle"),
                        this.murderMystery.getLanguage(player).translateString("titleDetectiveSubtitle"), 10, 40, 10);
                continue;
            }
            //杀手
            if (i == random2) {
                this.players.put(player, PlayerIdentity.KILLER);
                player.sendTitle(this.murderMystery.getLanguage(player).translateString("titleKillerTitle"),
                        this.murderMystery.getLanguage(player).translateString("titleKillerSubtitle"), 10, 40, 10);
                continue;
            }
            this.players.put(player, PlayerIdentity.COMMON_PEOPLE);
            player.sendTitle(this.murderMystery.getLanguage(player).translateString("titleCommonPeopleTitle"),
                    this.murderMystery.getLanguage(player).translateString("titleCommonPeopleSubtitle"), 10, 40, 10);
        }
    }

    /**
     * 获取存活玩家数
     *
     * @return 存活玩家数
     */
    public int getSurvivorPlayerNumber() {
        int x = 0;
        for (PlayerIdentity identity : this.getPlayers().values()) {
            if (identity != PlayerIdentity.DEATH) {
                x++;
            }
        }
        return x;
    }

    /**
     * 符合游戏条件的攻击
     *
     * @param damager 攻击者
     * @param player 被攻击者
     */
    public void playerDamage(@NotNull Player damager, @NotNull Player player) {
        MurderMysteryPlayerDamageEvent ev = new MurderMysteryPlayerDamageEvent(this, damager, player);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return;
        }
        if (this.getPlayers(player) == PlayerIdentity.NULL ||
                this.getPlayers(player) == PlayerIdentity.DEATH) {
            return;
        }
        //攻击者是杀手
        if (this.getPlayers(damager) == PlayerIdentity.KILLER) {
            damager.sendMessage(this.murderMystery.getLanguage(damager).translateString("killPlayer"));
            player.sendTitle(this.murderMystery.getLanguage(player).translateString("deathTitle"),
                    this.murderMystery.getLanguage(player).translateString("deathByKillerSubtitle"), 20, 60, 20);
            for (Player p : this.getPlayers().keySet()) {
                Language language = murderMystery.getLanguage(p);
                p.sendMessage(language.translateString("playerKilledByKiller")
                        .replace("%identity%", this.getPlayers(player) == PlayerIdentity.DETECTIVE ? language.translateString("detective") : language.translateString("commonPeople")));
            }
        }else { //攻击者是平民或侦探
            if (this.getPlayers(player) == PlayerIdentity.KILLER) {
                damager.sendMessage(this.murderMystery.getLanguage(damager).translateString("killKiller"));
                this.killKillerPlayer = damager;
                player.sendTitle(this.murderMystery.getLanguage(player).translateString("deathTitle"),
                        this.murderMystery.getLanguage(player).translateString("killerDeathSubtitle"), 10, 20, 20);
            } else {
                damager.sendTitle(this.murderMystery.getLanguage(damager).translateString("deathTitle"),
                        this.murderMystery.getLanguage(damager).translateString("deathByDamageTeammateSubtitle"), 20, 60, 20);
                player.sendTitle(this.murderMystery.getLanguage(player).translateString("deathTitle"),
                        this.murderMystery.getLanguage(player).translateString("deathByTeammateSubtitle"), 20, 60, 20);
                this.playerDeath(damager);
            }
        }
        this.playerDeath(player);
    }

    /**
     * 玩家死亡
     *
     * @param player 玩家
     */
    public void playerDeath(@NotNull Player player) {
        MurderMysteryPlayerDeathEvent ev = new MurderMysteryPlayerDeathEvent(this, player);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return;
        }
        player.getInventory().clearAll();
        player.getUIInventory().clearAll();
        Tools.giveItem(player, 10);
        player.setGamemode(Player.SPECTATOR);
        player.getAdventureSettings().set(AdventureSettings.Type.NO_CLIP, false).update();
        Tools.hidePlayer(this, player);
        if (this.getPlayers(player) == PlayerIdentity.DETECTIVE) {
            this.getLevel().dropItem(player, ItemManager.get(null, 1));
        }
        this.players.put(player, PlayerIdentity.DEATH);
        Tools.playSound(this, Sound.GAME_PLAYER_HURT);
        this.playerCorpseSpawn(player);
    }

    /**
     * 尸体生成
     *
     * @param player 玩家
     */
    public void playerCorpseSpawn(@NotNull Player player) {
        MurderMysteryPlayerCorpseSpawnEvent ev = new MurderMysteryPlayerCorpseSpawnEvent(this, player);
        Server.getInstance().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return;
        }
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
        EntityPlayerCorpse corpse = new EntityPlayerCorpse(player.getChunk(), nbt);
        corpse.setSkin(skin);
        corpse.setPosition(new Vector3(player.getFloorX(), Tools.getFloorY(player), player.getFloorZ()));
        corpse.setGliding(true);
        corpse.setRotation(player.getYaw(), 0);
        corpse.spawnToAll();
        corpse.updateMovement();
    }

    /**
     * 胜利
     *
     * @param victoryMode 胜利队伍
     */
    protected void victory(int victoryMode) {
        if (this.players.size() >= this.getMinPlayers() && this.setGameTime - this.gameTime <= 20) {
            return;
        }
        if (this.getStatus() != RoomStatus.VICTORY && this.getPlayers().size() > 0) {
            this.setStatus(RoomStatus.VICTORY);
            Server.getInstance().getScheduler().scheduleRepeatingTask(this.murderMystery,
                    new VictoryTask(this.murderMystery, this, victoryMode), 20);
        }else {
            this.endGame();
        }
    }

    /**
     * 检查是否需要生成临时房间
     */
    protected void autoCreateTemporaryRoom() {
        if (this.murderMystery.isAutoCreateTemporaryRoom()) {
            CompletableFuture.runAsync(() -> {
                LinkedList<String> cache = new LinkedList<>();
                int x = 0;
                for (Map.Entry<String, BaseRoom> entry : this.murderMystery.getRooms().entrySet()) {
                    if (this.getGameMode().equals(entry.getValue().getGameMode())) {
                        if (!this.murderMystery.getTemporaryRooms().contains(entry.getKey())) {
                            cache.add(entry.getKey());
                        }
                        if (entry.getValue().canJoin()) {
                            x++;
                        }
                    }
                }
                if (x == 0 && cache.size() > 0) {
                    this.murderMystery.addTemporaryRoom(cache.get(MurderMystery.RANDOM.nextInt(cache.size())));
                }
            }, MurderMystery.CHECK_ROOM_THREAD_POOL);
        }
    }

    /**
     * 清理临时房间
     */
    protected void autoClearTemporaryRoom() {
        if (!this.murderMystery.getTemporaryRooms().isEmpty()) {
            for (String world : this.murderMystery.getTemporaryRooms()) {
                if (world.equals(this.levelName)) {
                    continue;
                }
                BaseRoom room = this.murderMystery.getRooms().get(world);
                if (this.gameMode.equals(room.getGameMode()) &&
                        room.getStatus() == RoomStatus.TASK_NEED_INITIALIZED) {
                    room.endGame();
                    room.setStatus(RoomStatus.LEVEL_NOT_LOADED);
                    this.murderMystery.removeTemporaryRoom(world);
                }
            }
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
        this.setStatus(RoomStatus.LEVEL_NOT_LOADED);
        if (MurderMystery.debug) {
            murderMystery.getLogger().info("§a房间：" + this.getFullRoomName() + " 正在还原地图...");
        }
        Server.getInstance().unloadLevel(this.level, true);
        File levelFile = new File(Server.getInstance().getFilePath() + "/worlds/" + this.levelName);
        File backup = new File(this.murderMystery.getWorldBackupPath() + this.levelName);
        if (!backup.exists()) {
            this.murderMystery.getLogger().error(this.murderMystery.getLanguage(null)
                    .translateString("roomLevelBackupNotExist").replace("%name%", this.getFullRoomName()));
            this.murderMystery.unloadRoom(this.levelName);
        }
        CompletableFuture.runAsync(() -> {
            if (FileUtil.deleteFile(levelFile) && FileUtil.copyDir(backup, levelFile)) {
                Server.getInstance().loadLevel(this.levelName);
                this.level = Server.getInstance().getLevelByName(this.levelName);
                this.waitSpawn.setLevel(this.level);
                for (Position position : this.randomSpawn) {
                    position.setLevel(this.level);
                }
                this.setStatus(RoomStatus.TASK_NEED_INITIALIZED);
                if (MurderMystery.debug) {
                    this.murderMystery.getLogger().info("§a房间：" + this.getFullRoomName() + " 地图还原完成！");
                }
            }else {
                this.murderMystery.getLogger().error(this.murderMystery.getLanguage(null)
                        .translateString("roomLevelRestoreLevelFailure").replace("%name%", this.getFullRoomName()));
                this.murderMystery.unloadRoom(this.levelName);
            }
        });
    }

}
