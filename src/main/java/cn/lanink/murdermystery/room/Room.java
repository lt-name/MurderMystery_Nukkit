package cn.lanink.murdermystery.room;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.tasks.WaitTask;
import cn.lanink.murdermystery.utils.SavePlayerInventory;
import cn.lanink.murdermystery.utils.Tips;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;

import java.util.*;

/**
 * 房间类
 */
public class Room {

    private int mode; //0等待重置 1玩家等待中 2玩家游戏中 3胜利结算中
    public int waitTime, gameTime; //秒
    public int effectCD, swordCD, scanCD; //杀手技能CD
    private final int setWaitTime, setGameTime, setGoldSpawnTime;
    private final LinkedHashMap<Player, Integer> players = new LinkedHashMap<>(); //0未分配 1平民 2侦探 3杀手
    private final LinkedHashMap<Player, Integer> skinNumber = new LinkedHashMap<>(); //玩家使用皮肤编号，用于防止重复使用
    private final LinkedHashMap<Player, Skin> skinCache = new LinkedHashMap<>(); //缓存玩家皮肤，用于退出房间时还原
    private final ArrayList<Position> randomSpawn = new ArrayList<>();
    private final ArrayList<Position> goldSpawn = new ArrayList<>();
    private final Position waitSpawn;
    private final Level level;
    public ArrayList<ArrayList<Vector3>> placeBlocks = new ArrayList<>();
    public Player killKillerPlayer = null; //击杀杀手的玩家
    private final GameMode gameMode;

    /**
     * 初始化
     * @param config 配置文件
     */
    public Room(Config config) {
        this.level = Server.getInstance().getLevelByName(config.getString("world"));
        this.setWaitTime = config.getInt("waitTime");
        this.setGameTime = config.getInt("gameTime");
        if (config.getInt("gameMode") == 1) {
            this.gameMode = GameMode.INFECTED;
        }else {
            this.gameMode = GameMode.CLASSIC;
        }
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

    /**
     * 初始化Task
     */
    private void initTask() {
        this.setMode(1);
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                MurderMystery.getInstance(), new WaitTask(MurderMystery.getInstance(), this), 20);
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
     * 结束本局游戏
     */
    public void endGame() {
        this.endGame(true);
    }

    /**
     * 结束本局游戏
     * @param normal 正常关闭
     */
    public void endGame(boolean normal) {
        Server.getInstance().getScheduler().scheduleDelayedTask(MurderMystery.getInstance(), new Task() {
            @Override
            public void onRun(int i) {
                mode = 0;
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
     * 加入房间
     * @param player 玩家
     */
    public void joinRoom(Player player) {
        if (this.players.values().size() < 16) {
            if (this.mode == 0) {
                this.initTask();
            }
            this.addPlaying(player);
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
     * @param player 玩家
     */
    public void quitRoom(Player player) {
        this.quitRoom(player, true);
    }

    /**
     * 退出房间
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
     * 设置玩家随机皮肤
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
     * 记录在游戏内的玩家
     * @param player 玩家
     */
    public void addPlaying(Player player) {
        if (!this.players.containsKey(player)) {
            this.addPlaying(player, 0);
        }
    }

    /**
     * 记录在游戏内的玩家
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
     * @return 玩家身份
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

    public GameMode getGameMode() {
        return gameMode;
    }

}
