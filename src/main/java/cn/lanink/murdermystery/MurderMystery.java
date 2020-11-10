package cn.lanink.murdermystery;

import cn.lanink.gamecore.scoreboard.ScoreboardUtil;
import cn.lanink.gamecore.scoreboard.base.IScoreboard;
import cn.lanink.gamecore.utils.FileUtil;
import cn.lanink.murdermystery.addons.manager.AddonsManager;
import cn.lanink.murdermystery.command.AdminCommand;
import cn.lanink.murdermystery.command.UserCommand;
import cn.lanink.murdermystery.listener.base.IMurderMysteryListener;
import cn.lanink.murdermystery.listener.classic.ClassicGameListener;
import cn.lanink.murdermystery.listener.defaults.*;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.lanink.murdermystery.room.classic.ClassicModeRoom;
import cn.lanink.murdermystery.room.infected.InfectedModeRoom;
import cn.lanink.murdermystery.ui.GuiListener;
import cn.lanink.murdermystery.utils.Language;
import cn.lanink.murdermystery.utils.MetricsLite;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.*;

/**
 * MurderMystery
 *
 * @author lt_name
 */
public class MurderMystery extends PluginBase {

    public static final String VERSION = "?";
    public static boolean debug = false;
    public static final Random RANDOM = new Random();
    public static final ThreadPoolExecutor checkRoomThreadPool = new ThreadPoolExecutor(
            2,
            4,
            30,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadPoolExecutor.DiscardPolicy());
    private static MurderMystery murderMystery;
    private static AddonsManager addonsManager;
    private Config config;
    private Config temporaryRoomsConfig; //文件保存，防止崩服丢失数据
    private final HashMap<String, Config> roomConfigs = new HashMap<>();
    private static final LinkedHashMap<String, Class<? extends BaseRoom>> ROOM_CLASS = new LinkedHashMap<>();
    private final LinkedHashMap<String, BaseRoom> rooms = new LinkedHashMap<>();
    private final HashMap<String, String> roomName = new HashMap<>(); //自定义房间名称
    private CopyOnWriteArrayList<String> temporaryRooms; //临时房间
    private static final HashMap<String, Class<? extends IMurderMysteryListener>> LISTENER_CLASS = new HashMap<>();
    private final HashMap<String, IMurderMysteryListener> murderMysteryListeners = new HashMap<>();
    private final LinkedHashMap<Integer, Skin> skins = new LinkedHashMap<>();
    private Skin sword;
    private final Skin corpseSkin = new Skin();
    private String cmdUser, cmdAdmin;
    private List<String> cmdUserAliases, cmdAdminAliases;
    private IScoreboard scoreboard;
    private boolean hasTips = false;

    private String serverWorldPath;
    private String worldBackupPath;
    private String roomConfigPath;

    private boolean restoreWorld = false;
    private boolean autoCreateTemporaryRoom = false;

    private String defaultLanguage = "zh_CN";
    private final HashMap<String, String> languageMappingTable = new HashMap<>();
    private final HashMap<String, Language> languageMap = new HashMap<>();
    private final ConcurrentHashMap<Player, String> playerLanguage = new ConcurrentHashMap<>();

    public static MurderMystery getInstance() { return murderMystery; }

    @Override
    public void onLoad() {
        if (murderMystery == null) murderMystery = this;

        this.serverWorldPath = this.getServer().getFilePath() + "/worlds/";
        this.worldBackupPath = this.getDataFolder() + "/RoomLevelBackup/";
        this.roomConfigPath = this.getDataFolder() + "/Rooms/";

        File file1 = new File(this.getDataFolder() + "/Rooms");
        File file2 = new File(this.getDataFolder() + "/PlayerInventory");
        File file3 = new File(this.getDataFolder() + "/Skins");
        if (!file1.exists() && !file1.mkdirs()) {
            getLogger().error("Rooms 文件夹初始化失败");
        }
        if (!file2.exists() && !file2.mkdirs()) {
            getLogger().error("PlayerInventory 文件夹初始化失败");
        }
        if (!file3.exists() && !file3.mkdirs()) {
            getLogger().warning("Skins 文件夹初始化失败");
        }
        saveDefaultConfig();
        this.config = new Config(this.getDataFolder() + "/config.yml", Config.YAML);
        if (config.getBoolean("debug", false)) {
            debug = true;
            getLogger().warning("警告：您开启了debug模式！");
            getLogger().warning("Warning: You have turned on debug mode!");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {

            }
        }
        this.temporaryRoomsConfig = new Config(this.getDataFolder() + "/temporaryRoomList.yml", Config.YAML);
        this.temporaryRooms = new CopyOnWriteArrayList<>(this.temporaryRoomsConfig.getStringList("temporaryRooms"));
        this.removeAllTemporaryRoom();
        this.restoreWorld = this.config.getBoolean("restoreWorld", false);
        this.autoCreateTemporaryRoom = this.config.getBoolean("autoCreateTemporaryRoom", false);
        this.cmdUser = this.config.getString("cmdUser", "murdermystery");
        this.cmdUserAliases = this.config.getStringList("cmdUserAliases");
        this.cmdAdmin = this.config.getString("cmdAdmin", "murdermysteryadmin");
        this.cmdAdminAliases = this.config.getStringList("cmdAdminAliases");
        //语言文件 (按时间排序/Sort by time)
        this.saveResource("Resources/Language/zh_CN.yml");
        this.saveResource("Resources/Language/en_US.yml");
        this.saveResource("Resources/Language/ko_KR.yml");
        this.saveResource("Resources/Language/vi_VN.yml");
        this.saveResource("Resources/Language/de_DE.yml");
        this.defaultLanguage = this.config.getString("defaultLanguage", "zh_CN");
        this.languageMappingTable.putAll(this.config.get("languageMappingTable", new HashMap<>()));
        File[] files = new File(getDataFolder() + "/Resources/Language").listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                String name = file.getName().split("\\.")[0];
                this.languageMap.put(name, new Language(new Config(file, Config.YAML)));
                getLogger().info("§aLanguage: " + name + " loaded !");
            }
        }
        if (this.languageMap.isEmpty()) {
            this.languageMap.put(this.defaultLanguage, new Language(new Config()));
            getLogger().error("§cThe language file is read abnormally, only the default(zh_CN) language is loaded!");
        }
        //扩展
        if (addonsManager == null) addonsManager = new AddonsManager(this);
        //注册监听器
        registerListener("RoomLevelProtection", RoomLevelProtection.class);
        registerListener("DefaultGameListener", DefaultGameListener.class);
        registerListener("DefaultChatListener", DefaultChatListener.class);
        registerListener("DefaultDamageListener", DefaultDamageListener.class);
        registerListener("ClassicGameListener", ClassicGameListener.class);
        //注册房间类
        registerRoom("classic", ClassicModeRoom.class);
        registerRoom("infected", InfectedModeRoom.class);
    }

    @Override
    public void onEnable() {
        getLogger().info("§e插件开始加载！本插件是免费哒~如果你花钱了，那一定是被骗了~");
        getLogger().info("§l§eVersion: " + VERSION);
        //加载计分板
        this.scoreboard = ScoreboardUtil.getScoreboard();
        //检查Tips
        try {
            Class.forName("tip.Main");
            if (getServer().getPluginManager().getPlugin("Tips").isDisabled()) {
                throw new Exception("Not Loaded");
            }
            this.hasTips = true;
        } catch (Exception ignored) {

        }
        getServer().getCommandMap().register("",
                new UserCommand(this.cmdUser, this.cmdUserAliases.toArray(new String[0])));
        getServer().getCommandMap().register("",
                new AdminCommand(this.cmdAdmin, this.cmdAdminAliases.toArray(new String[0])));
        getServer().getPluginManager().registerEvents(new PlayerJoinAndQuit(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        this.loadAllListener();
        this.loadResources();
        this.loadAllRoom();
        this.loadSkins();
        //启用扩展-使用task保证在所有插件都加载完后加载扩展
        getServer().getScheduler().scheduleTask(this, new Task() {
            @Override
            public void onRun(int i) {
                getLogger().info(getLanguage(null).startLoadingAddons);
                addonsManager.enableAll();
                getLogger().info(getLanguage(null).addonsLoaded);
            }
        });
        try {
            new MetricsLite(this, 7290);
        } catch (Throwable ignore) {

        }
        getLogger().info(this.getLanguage(null).pluginEnable);
    }

    @Override
    public void onDisable() {
        addonsManager.disableAll();
        if (this.rooms.size() > 0) {
            Iterator<Map.Entry<String, BaseRoom>> it = this.rooms.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<String, BaseRoom> entry = it.next();
                if (entry.getValue().getPlayers().size() > 0) {
                    entry.getValue().endGameEvent(0);
                    getLogger().info(this.getLanguage(null).roomUnloadFailure.replace("%name%", entry.getKey()));
                }else {
                    getLogger().info(this.getLanguage(null).roomUnloadSuccess.replace("%name%", entry.getKey()));
                }
                Tools.cleanEntity(entry.getValue().getLevel(), true);
                it.remove();
            }
            this.rooms.clear();
        }
        this.removeAllTemporaryRoom();
        this.temporaryRooms.clear();
        this.roomConfigs.clear();
        this.skins.clear();
        getLogger().info(this.getLanguage(null).pluginDisable);
    }

    /**
     * 注册房间类
     *
     * @param name 名称
     * @param roomClass 房间类
     */
    public static void registerRoom(String name, Class<? extends BaseRoom> roomClass) {
        ROOM_CLASS.put(name, roomClass);
    }

    /**
     * 注册监听器类
     *
     * @param name 名称
     * @param listenerClass 监听器类
     */
    public static void registerListener(String name, Class<? extends IMurderMysteryListener> listenerClass) {
        LISTENER_CLASS.put(name, listenerClass);
    }

    public void loadAllListener() {
        for (Map.Entry<String, Class<? extends IMurderMysteryListener>> entry : LISTENER_CLASS.entrySet()) {
            try {
                Constructor<? extends IMurderMysteryListener> constructor = entry.getValue().getConstructor(MurderMystery.class);
                IMurderMysteryListener murderMysteryListener = constructor.newInstance(this);
                murderMysteryListener.setListenerName(entry.getKey());
                this.loadListener(murderMysteryListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void loadListener(IMurderMysteryListener iMurderMysteryListener) {
        this.murderMysteryListeners.put(iMurderMysteryListener.getListenerName(), iMurderMysteryListener);
        this.getServer().getPluginManager().registerEvents(iMurderMysteryListener, this);
        if (debug) {
            this.getLogger().info("[debug] registerListener: " + iMurderMysteryListener.getListenerName());
        }
    }

    public static boolean hasRoomClass(String name) {
        return ROOM_CLASS.containsKey(name);
    }

    public static LinkedHashMap<String, Class<? extends BaseRoom>> getRoomClass() {
        return ROOM_CLASS;
    }

    public HashMap<String, IMurderMysteryListener> getMurderMysteryListeners() {
        return this.murderMysteryListeners;
    }

    public Language getLanguage(Object obj) {
        if (obj instanceof Player) {
            Player player = (Player) obj;
            String lang = this.playerLanguage.getOrDefault(player, this.defaultLanguage);
            if (!this.languageMap.containsKey(lang) && this.languageMappingTable.containsKey(lang)) {
                lang = this.languageMappingTable.get(lang);
            }
            if (!this.languageMap.containsKey(lang)) {
                lang = this.defaultLanguage;
            }
            return this.languageMap.get(lang);
        }
        return this.languageMap.get(this.defaultLanguage);
    }

    public static AddonsManager getAddonsManager() {
        return addonsManager;
    }

    public IScoreboard getScoreboard() {
        return this.scoreboard;
    }

    @Override
    public Config getConfig() {
        return this.config;
    }

    public String getServerWorldPath() {
        return this.serverWorldPath;
    }

    public String getWorldBackupPath() {
        return this.worldBackupPath;
    }

    public String getRoomConfigPath() {
        return this.roomConfigPath;
    }

    public boolean isHasTips() {
        return this.hasTips;
    }

    public boolean isRestoreWorld() {
        return this.restoreWorld;
    }

    public boolean isAutoCreateTemporaryRoom() {
        return this.autoCreateTemporaryRoom;
    }

    public String getCmdUser() {
        return this.cmdUser;
    }

    public List<String> getCmdUserAliases() {
        return this.cmdUserAliases;
    }

    public String getCmdAdmin() {
        return this.cmdAdmin;
    }

    public List<String> getCmdAdminAliases() {
        return this.cmdAdminAliases;
    }

    public Skin getSword() {
        return this.sword;
    }

    public Skin getCorpseSkin() {
        return this.corpseSkin;
    }

    public LinkedHashMap<String, BaseRoom> getRooms() {
        return this.rooms;
    }

    public HashMap<String, String> getRoomName() {
        return this.roomName;
    }

    public List<String> getTemporaryRooms() {
        return this.temporaryRooms;
    }

    public HashMap<String, Language> getLanguageMap() {
        return this.languageMap;
    }

    public ConcurrentHashMap<Player, String> getPlayerLanguage() {
        return this.playerLanguage;
    }

    public synchronized void addTemporaryRoom(String template) {
        String newRoom;
        do {
            newRoom = "MurderMysteryTemporaryRoom" + RANDOM.nextInt(100000);
        }while (this.temporaryRooms.contains(newRoom) ||
                this.getRooms().containsKey(newRoom) ||
                this.getRoomName().containsValue(newRoom));
        String finalNewRoom = newRoom;
        this.temporaryRooms.add(finalNewRoom);
        this.temporaryRoomsConfig.set("temporaryRooms", this.temporaryRooms);
        this.temporaryRoomsConfig.save();
        FileUtil.copyDir(this.getRoomConfigPath() + template + ".yml", this.getRoomConfigPath() + finalNewRoom + ".yml");
        FileUtil.copyDir(this.getWorldBackupPath() + template, this.getServerWorldPath() + finalNewRoom);
        if (MurderMystery.debug) {
            this.getLogger().info("自动创建临时房间: " + template + " -> " + finalNewRoom);
        }
        //主线程操作
        Server.getInstance().getScheduler().scheduleTask(this, () -> murderMystery.loadRoom(finalNewRoom));
    }

    public void removeAllTemporaryRoom() {
        for (String name : new ArrayList<>(this.temporaryRooms)) {
            this.removeTemporaryRoom(name);
        }
    }

    public synchronized void removeTemporaryRoom(String levelName) {
        if (!this.temporaryRooms.contains(levelName)) {
            return;
        }
        this.unloadRoom(levelName);
        Level level = this.getServer().getLevelByName(levelName);
        if (level != null) {
            this.getServer().unloadLevel(level);
        }
        CompletableFuture.runAsync(() -> {
            FileUtil.deleteFile(this.getRoomConfigPath() + levelName + ".yml");
            FileUtil.deleteFile(this.getServerWorldPath() + levelName);
            FileUtil.deleteFile(this.getWorldBackupPath() + levelName);
            this.temporaryRooms.remove(levelName);
            this.temporaryRoomsConfig.set("temporaryRooms", this.temporaryRooms);
            this.temporaryRoomsConfig.save();
            if (MurderMystery.debug) {
                this.getLogger().info("临时房间: " + levelName + " 已删除");
            }
        });
    }

    public Config getRoomConfig(Level level) {
        return getRoomConfig(level.getFolderName());
    }

    private Config getRoomConfig(String level) {
        if (this.roomConfigs.containsKey(level)) {
            return this.roomConfigs.get(level);
        }
        Config config = new Config(getDataFolder() + "/Rooms/" + level + ".yml", 2);
        this.roomConfigs.put(level, config);
        return config;
    }

    public LinkedHashMap<Integer, Skin> getSkins() {
        return this.skins;
    }

    private void loadResources() {
        //剑
        saveResource("Resources/Sword/skin.png", false);
        saveResource("Resources/Sword/skin.json", false);
        File fileImg = new File(getDataFolder() + "/Resources/Sword/skin.png");
        File fileJson = new File(getDataFolder() + "/Resources/Sword/skin.json");
        Skin skin = new Skin();
        skin.setTrusted(true);
        BufferedImage skinData;
        try {
            skinData = ImageIO.read(fileImg);
            if (skinData != null) {
                skin.setSkinData(skinData);
                skin.setSkinId("sword");
                Map<String, Object> skinJson = new Config(fileJson, 1).getAll();
                String name = null;
                for (Map.Entry<String, Object> entry1 : skinJson.entrySet()) {
                    if (name == null || name.trim().equals("")) {
                        name = entry1.getKey();
                    }else {
                        break;
                    }
                }
                skin.setGeometryName(name);
                skin.setGeometryData(Utils.readFile(fileJson));
                this.sword = skin;
                getLogger().info(this.getLanguage(null).swordSuccess);
            }else {
                getLogger().warning(this.getLanguage(null).swordFailure);
            }
        } catch (IOException ignored) {
            getLogger().warning(this.getLanguage(null).swordFailure);
        }
        //默认尸体皮肤
        skinData = null;
        try {
            skinData = ImageIO.read(this.getResource("skin.png"));
        } catch (IOException ignored) { }
        if (skinData != null) {
            this.corpseSkin.setTrusted(true);
            this.corpseSkin.setSkinData(skinData);
            this.corpseSkin.setSkinId("defaultSkin");
            getLogger().info(this.getLanguage(null).defaultSkinSuccess);
        }else {
            getLogger().error(this.getLanguage(null).defaultSkinFailure);
        }
    }

    /**
     * 加载所有房间
     */
    public void loadAllRoom() {
        getLogger().info(this.getLanguage(null).startLoadingRoom);
        File[] s = new File(getDataFolder() + "/Rooms").listFiles();
        if (s != null && s.length > 0) {
            for (File file1 : s) {
                String[] fileName = file1.getName().split("\\.");
                if (fileName.length > 0) {
                    this.loadRoom(fileName[0]);
                }
            }
        }
        getLogger().info(this.getLanguage(null).roomLoadedAllSuccess.replace(" %number%", this.rooms.size() + ""));
    }

    public void loadRoom(String world) {
        Config config = getRoomConfig(world);
        String name = this.temporaryRooms.contains(world) ? world : config.getString("roomName", world);
        if (config.getInt("waitTime", 0) == 0 ||
                config.getInt("gameTime", 0) == 0 ||
                config.getString("waitSpawn", "").trim().equals("") ||
                config.getStringList("randomSpawn").size() == 0 ||
                config.getStringList("goldSpawn").size() == 0 ||
                config.getInt("goldSpawnTime", 0) == 0 ||
                config.getString("gameMode", "").trim().equals("")) {
            getLogger().warning(this.getLanguage(null).roomLoadedFailureByConfig.replace("%name%", name + "(" + world + ")"));
            return;
        }
        if (Server.getInstance().getLevelByName(world) == null && !Server.getInstance().loadLevel(world)) {
            getLogger().warning(this.getLanguage(null).roomLoadedFailureByLevel.replace("%name%", name + "(" + world + ")"));
            return;
        }
        String gameMode = config.getString("gameMode", "classic");
        if (!ROOM_CLASS.containsKey(gameMode)) {
            getLogger().warning(this.getLanguage(null).roomLoadedFailureByGameMode
                    .replace("%name%", name + "(" + world + ")")
                    .replace("%gameMode%", gameMode));
            return;
        }
        try {
            this.roomName.put(world, name);
            Constructor<? extends BaseRoom> constructor = ROOM_CLASS.get(gameMode).getConstructor(Level.class, Config.class);
            BaseRoom baseRoom = constructor.newInstance(Server.getInstance().getLevelByName(world), config);
            baseRoom.setGameMode(gameMode);
            this.rooms.put(world, baseRoom);
            getLogger().info(this.getLanguage(null).roomLoadedSuccess.replace("%name%", name + "(" + world + ")"));
        } catch (Exception e) {
            this.roomName.remove(world);
            e.printStackTrace();
        }
    }

    /**
     * 卸载所有房间
     */
    public void unloadRooms() {
        if (this.rooms.size() > 0) {
            Iterator<Map.Entry<String, BaseRoom>> it = this.rooms.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<String, BaseRoom> entry = it.next();
                entry.getValue().endGameEvent();
                for (IMurderMysteryListener listener : this.murderMysteryListeners.values()) {
                    listener.removeListenerRoom(entry.getValue());
                }
                getLogger().info(this.getLanguage(null).roomUnloadSuccess
                        .replace("%name%", this.roomName.get(entry.getKey()) + "(" + entry.getKey() + ")"));
                it.remove();
            }
            this.rooms.clear();
        }
        this.roomName.clear();
        this.roomConfigs.clear();
    }

    public void unloadRoom(String world) {
        if (this.rooms.containsKey(world)) {
            this.rooms.get(world).endGameEvent();
            for (IMurderMysteryListener listener : this.murderMysteryListeners.values()) {
                listener.removeListenerRoom(world);
            }
            this.rooms.remove(world);
            getLogger().info(this.getLanguage(null).roomUnloadSuccess
                    .replace("%name%", this.roomName.get(world) + "(" + world + ")"));
            this.roomName.remove(world);
        }
    }

    /**
     * 重载所有房间
     */
    public void reLoadRooms() {
        this.unloadRooms();
        this.loadAllRoom();
    }

    /**
     * 加载所有皮肤
     */
    private void loadSkins() {
        getLogger().info(this.getLanguage(null).startLoadingSkin);
        File[] files = (new File(getDataFolder() + "/Skins")).listFiles();
        if (files != null && files.length > 0) {
            int x = 0;
            for (File file : files) {
                String skinName = file.getName();
                File skinFile = new File(getDataFolder() + "/Skins/" + skinName + "/skin.png");
                if (skinFile.exists()) {
                    Skin skin = new Skin();
                    skin.setTrusted(true);
                    BufferedImage skinData = null;
                    try {
                        skinData = ImageIO.read(skinFile);
                    } catch (IOException ignored) {
                        getLogger().warning(this.getLanguage(null).skinFailureByFormat.replace("%name%", skinName));
                    }
                    if (skinData != null) {
                        skin.setSkinData(skinData);
                        skin.setSkinId(skinName);
                        getLogger().info(this.getLanguage(null).skinLoadedSuccess.replace("%number%", x + "")
                                .replace("%name%", skinName));
                        this.skins.put(x, skin);
                        x++;
                    }else {
                        getLogger().warning(this.getLanguage(null).skinFailureByFormat.replace("%name%", skinName));
                    }
                } else {
                    getLogger().warning(this.getLanguage(null).skinFailureByName.replace("%name%", skinName));
                }
            }
        }
        if (this.skins.size() >= 16) {
            getLogger().info(this.getLanguage(null).skinLoadedAllSuccess.replace("%number%", this.skins.size() + ""));
        }else {
            getLogger().warning(this.getLanguage(null).skinLoadedAllFailureByNumber);
        }
    }

}
