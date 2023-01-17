package cn.lanink.murdermystery;

import cn.lanink.gamecore.GameCore;
import cn.lanink.gamecore.scoreboard.ScoreboardUtil;
import cn.lanink.gamecore.scoreboard.base.IScoreboard;
import cn.lanink.gamecore.utils.FileUtils;
import cn.lanink.gamecore.utils.Language;
import cn.lanink.gamecore.utils.VersionUtils;
import cn.lanink.murdermystery.addons.manager.AddonsManager;
import cn.lanink.murdermystery.command.AdminCommand;
import cn.lanink.murdermystery.command.UserCommand;
import cn.lanink.murdermystery.entity.data.MurderMysterySkin;
import cn.lanink.murdermystery.listener.BaseMurderMysteryListener;
import cn.lanink.murdermystery.listener.assassin.AssassinDamageListener;
import cn.lanink.murdermystery.listener.assassin.AssassinGameListener;
import cn.lanink.murdermystery.listener.classic.ClassicDamageListener;
import cn.lanink.murdermystery.listener.classic.ClassicGameListener;
import cn.lanink.murdermystery.listener.defaults.*;
import cn.lanink.murdermystery.listener.infected.InfectedGameListener;
import cn.lanink.murdermystery.playerdata.PlayerDataManager;
import cn.lanink.murdermystery.room.assassin.AssassinModeRoom;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.lanink.murdermystery.room.classic.ClassicModeRoom;
import cn.lanink.murdermystery.room.doubleMode.DoubleRoomMode;
import cn.lanink.murdermystery.room.infected.InfectedModeRoom;
import cn.lanink.murdermystery.tasks.Watchdog;
import cn.lanink.murdermystery.tasks.admin.SetRoomTask;
import cn.lanink.murdermystery.utils.MetricsLite;
import cn.lanink.murdermystery.utils.RsNpcVariable;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.Utils;
import lombok.Getter;
import me.petterim1.scoreboards.Main;

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
    public static final ThreadPoolExecutor CHECK_ROOM_THREAD_POOL = new ThreadPoolExecutor(
            2,
            4,
            30,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            task -> new Thread(task, "MurderMystery Thread Pool"),
            new ThreadPoolExecutor.DiscardPolicy());

    private static MurderMystery murderMystery;
    private static AddonsManager addonsManager;
    @Getter
    private PlayerDataManager playerDataManager;

    private Config config;

    private final HashMap<String, Config> roomConfigs = new HashMap<>();
    private static final LinkedHashMap<String, Class<? extends BaseRoom>> ROOM_CLASS = new LinkedHashMap<>();
    private final LinkedHashMap<String, BaseRoom> rooms = new LinkedHashMap<>();
    private final HashMap<String, String> roomName = new HashMap<>(); //自定义房间名称

    private Config temporaryRoomsConfig; //文件保存，防止崩服丢失数据
    private CopyOnWriteArrayList<String> temporaryRooms; //临时房间

    @SuppressWarnings("rawtypes")
    private static final HashMap<String, Class<? extends BaseMurderMysteryListener>> LISTENER_CLASS = new HashMap<>();
    @SuppressWarnings("rawtypes")
    private final HashMap<String, BaseMurderMysteryListener> murderMysteryListeners = new HashMap<>();

    private final LinkedHashMap<Integer, MurderMysterySkin> skins = new LinkedHashMap<>();
    private Skin sword;
    private final Skin corpseSkin = new Skin();

    private String cmdUser;
    private String cmdAdmin;
    private List<String> cmdUserAliases;
    private List<String> cmdAdminAliases;
    @Getter
    private List<String> cmdWhitelist;

    private IScoreboard scoreboard;
    private boolean hasTips = false;

    private String serverWorldPath;
    private String worldBackupPath;
    private String roomConfigPath;

    private boolean restoreWorld = false;
    private boolean autoCreateTemporaryRoom = false;
    @Getter
    private boolean automaticNextRound = false; //游戏结束后自动加入新房间

    private String defaultLanguage = "zh_CN";
    private final HashMap<String, String> languageMappingTable = new HashMap<>();
    private final HashMap<String, Language> languageMap = new HashMap<>();
    private final ConcurrentHashMap<Player, String> playerLanguage = new ConcurrentHashMap<>();

    public final HashMap<Player, SetRoomTask> setRoomTask = new HashMap<>();

    public static MurderMystery getInstance() {
        return murderMystery;
    }

    @Override
    public void onLoad() {
        if (murderMystery != null) {
            throw new RuntimeException("Repeat the onLoad() method!");
        }
        murderMystery = this;

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
        this.saveDefaultConfig();

        this.config = new Config(this.getDataFolder() + "/config.yml", Config.YAML);
        if (config.getBoolean("debug", false)) {
            debug = true;
            this.getLogger().warning("§c=========================================");
            this.getLogger().warning("§c 警告：您开启了debug模式！");
            this.getLogger().warning("§c Warning: You have turned on debug mode!");
            this.getLogger().warning("§c=========================================");
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
        this.automaticNextRound = this.config.getBoolean("AutomaticNextRound", false);

        this.cmdUser = this.config.getString("cmdUser", "murdermystery");
        this.cmdUserAliases = this.config.getStringList("cmdUserAliases");
        this.cmdAdmin = this.config.getString("cmdAdmin", "murdermysteryadmin");
        this.cmdAdminAliases = this.config.getStringList("cmdAdminAliases");
        this.cmdWhitelist = this.config.getStringList("cmdWhitelist");

        this.loadLanguage();

        this.playerDataManager = new PlayerDataManager(this);

        //扩展
        if (addonsManager == null) {
            addonsManager = new AddonsManager(this);
        }

        //注册监听器
        this.registerListeners();

        //注册房间类
        this.registerRooms();
    }

    private void loadLanguage() {
        //语言文件 (按时间排序/Sort by time)
        List<String> languages = Arrays.asList("zh_CN", "en_US", "ko_KR", "vi_VN", "de_DE");
        for (String language : languages) {
            this.saveResource("Resources/Language/" + language + ".yml");
        }
        this.defaultLanguage = this.config.getString("defaultLanguage", "zh_CN");
        this.languageMappingTable.putAll(this.config.get("languageMappingTable", new HashMap<>()));
        File[] files = new File(this.getDataFolder() + "/Resources/Language").listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isFile()) {
                    String name = file.getName().split("\\.")[0];
                    Language language = new Language(new Config(file, Config.YAML));
                    if (this.config.getBoolean("autoUpdateLanguage")) {
                        //更新插件自带的语言文件
                        if (languages.contains(name)) {
                            Config config = new Config(Config.YAML);
                            if (config.load(this.getResource("Resources/Language/" + language + ".yml"))) {
                                language.update(config);
                            }
                        }
                        //以zh_CN为基础 更新所有语言文件
                        Config config = new Config(Config.YAML);
                        if (config.load(this.getResource("Resources/Language/zh_CN.yml"))) {
                            language.update(config);
                        }
                    }
                    this.languageMap.put(name, language);
                    getLogger().info("§aLanguage: " + name + " loaded !");
                }
            }
        }else {
            this.getLogger().error("§cFailed to load language file! The plugin does not work");
            this.getServer().getPluginManager().disablePlugin(this);
        }
        if (!this.languageMap.containsKey(this.defaultLanguage)) {
            this.getLogger().error("§cNo default language found: " + this.defaultLanguage + " Has been set to 'zh_CN'");
            this.defaultLanguage = "zh_CN";
        }
    }

    private void registerListeners() {
        registerListeners("RoomLevelProtection", RoomLevelProtection.class);
        registerListeners("DefaultGameListener", DefaultGameListener.class);
        registerListeners("DefaultChatListener", DefaultChatListener.class);
        registerListeners("DefaultDamageListener", DefaultDamageListener.class);
        registerListeners("ClassicGameListener", ClassicGameListener.class);
        registerListeners("ClassicDamageListener", ClassicDamageListener.class);
        registerListeners("InfectedGameListener", InfectedGameListener.class);
        registerListeners("AssassinDamageListener", AssassinDamageListener.class);
        registerListeners("AssassinGameListener", AssassinGameListener.class);
    }

    private void registerRooms() {
        registerRoom("classic", ClassicModeRoom.class);
        registerRoom("infected", InfectedModeRoom.class);
        registerRoom("assassin", AssassinModeRoom.class);
        //TODO need dev
        if (MurderMystery.debug) {
            registerRoom("double", DoubleRoomMode.class);
        }
    }

    @Override
    public void onEnable() {
        this.getLogger().info("§e插件开始加载！本插件是免费哒~如果你花钱了，那一定是被骗了~");
        this.getLogger().info("§l§e https://github.com/lt-name/MurderMystery_Nukkit");
        this.getLogger().info("§l§eVersion: " + VERSION);

        //检查依赖版本
        try {
            String needGameCoreVersion = "1.5.6";
            if (!VersionUtils.checkMinimumVersion(GameCore.getInstance(), needGameCoreVersion)) {
                throw new RuntimeException("[MemoriesOfTime-GameCore] plugin version is too low! At least version " + needGameCoreVersion + " is needed!");
            }
        }catch (Exception e) {
            this.getLogger().error("Please check the dependency plugin version!", e);
            this.getLogger().error("Please update the plugin to the required version!");
            //延迟3秒方便查看报错
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {

            }
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        //加载计分板
        this.scoreboard = ScoreboardUtil.getScoreboard();
        //检查Tips
        try {
            Class.forName("tip.Main");
            if (getServer().getPluginManager().getPlugin("Tips").isDisabled()) {
                throw new RuntimeException("Not Loaded");
            }
            this.hasTips = true;
        } catch (Exception ignored) {

        }
        //注册RsNPC变量
        try {
            Class.forName("com.smallaswater.npc.variable.BaseVariableV2");
            com.smallaswater.npc.variable.VariableManage.addVariableV2("MurderMysteryVariable", RsNpcVariable.class);
        } catch (Exception ignored) {

        }

        this.loadResources();
        this.loadSkins();

        //注册命令
        this.getServer().getCommandMap().register("",
                new UserCommand(this.cmdUser, this.cmdUserAliases.toArray(new String[0])));
        this.getServer().getCommandMap().register("",
                new AdminCommand(this.cmdAdmin, this.cmdAdminAliases.toArray(new String[0])));

        //注册监听器
        this.getServer().getPluginManager().registerEvents(new PlayerJoinAndQuit(this), this);
        this.getServer().getPluginManager().registerEvents(new SetRoomListener(this), this);
        this.loadAllListener();

        this.loadAllRoom();

        this.getServer().getScheduler().scheduleRepeatingTask(this, new Watchdog(), 20, true);
        //启用扩展-使用task保证在所有插件都加载完后加载扩展
        this.getServer().getScheduler().scheduleTask(this, () -> {
            this.getLogger().info(this.getLanguage().translateString("startLoadingAddons"));
            addonsManager.enableAll();
            this.getLogger().info(this.getLanguage().translateString("addonsLoaded"));
        });
        try {
            new MetricsLite(this, 11922);
        } catch (Throwable ignore) { }

        this.getLogger().info(this.getLanguage().translateString("pluginEnable"));
    }

    @Override
    public void onDisable() {
        if (addonsManager != null) {
            addonsManager.disableAll();
        }
        if (this.playerDataManager != null) {
            this.playerDataManager.saveAll();
            this.playerDataManager.clearAll();
        }
        this.removeAllTemporaryRoom();
        this.unloadAllRoom();
        this.getMurderMysteryListeners().values().forEach(BaseMurderMysteryListener::clearListenerRooms);
        this.skins.clear();
        this.getLogger().info(this.getLanguage().translateString("pluginDisable"));
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
    @SuppressWarnings("rawtypes")
    public static void registerListeners(String name, Class<? extends BaseMurderMysteryListener> listenerClass) {
        LISTENER_CLASS.put(name, listenerClass);
    }

    @SuppressWarnings("rawtypes")
    public void loadAllListener() {
        for (Map.Entry<String, Class<? extends BaseMurderMysteryListener>> entry : LISTENER_CLASS.entrySet()) {
            try {
                BaseMurderMysteryListener murderMysteryListener = entry.getValue().newInstance();
                murderMysteryListener.init(entry.getKey());
                this.loadListener(murderMysteryListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public void loadListener(BaseMurderMysteryListener baseMurderMysteryListener) {
        this.murderMysteryListeners.put(baseMurderMysteryListener.getListenerName(), baseMurderMysteryListener);
        this.getServer().getPluginManager().registerEvents(baseMurderMysteryListener, this);
        if (debug) {
            this.getLogger().info("[debug] registerListener: " + baseMurderMysteryListener.getListenerName());
        }
    }

    public static boolean hasRoomClass(String name) {
        return ROOM_CLASS.containsKey(name);
    }

    public static LinkedHashMap<String, Class<? extends BaseRoom>> getRoomClass() {
        return ROOM_CLASS;
    }

    @SuppressWarnings("rawtypes")
    public HashMap<String, BaseMurderMysteryListener> getMurderMysteryListeners() {
        return this.murderMysteryListeners;
    }

    public Language getLanguage() {
        return this.getLanguage(null);
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
        FileUtils.copyDir(this.getRoomConfigPath() + template + ".yml", this.getRoomConfigPath() + finalNewRoom + ".yml");
        FileUtils.copyDir(this.getWorldBackupPath() + template, this.getServerWorldPath() + finalNewRoom);
        if (MurderMystery.debug) {
            this.getLogger().info("自动创建临时房间: " + template + " -> " + finalNewRoom);
        }
        //主线程操作
        Server.getInstance().getScheduler().scheduleTask(this, () -> this.loadRoom(finalNewRoom));
    }

    public void removeAllTemporaryRoom() {
        for (String name : new ArrayList<>(this.temporaryRooms)) {
            this.removeTemporaryRoom(name);
        }
        this.temporaryRooms.clear();
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
            FileUtils.deleteFile(this.getRoomConfigPath() + levelName + ".yml");
            FileUtils.deleteFile(this.getServerWorldPath() + levelName);
            FileUtils.deleteFile(this.getWorldBackupPath() + levelName);
            this.temporaryRooms.remove(levelName);
            this.temporaryRoomsConfig.set("temporaryRooms", this.temporaryRooms);
            this.temporaryRoomsConfig.save();
            if (MurderMystery.debug) {
                this.getLogger().info("临时房间: " + levelName + " 已删除");
            }
        });
    }

    public HashMap<String, Config> getRoomConfigs() {
        return this.roomConfigs;
    }

    public Config getRoomConfig(Level level) {
        return getRoomConfig(level.getFolderName());
    }

    public Config getRoomConfig(String level) {
        if (this.roomConfigs.containsKey(level)) {
            return this.roomConfigs.get(level);
        }
        Config config = new Config(this.getDataFolder() + "/Rooms/" + level + ".yml", Config.YAML);
        this.roomConfigs.put(level, config);
        return config;
    }

    public LinkedHashMap<Integer, MurderMysterySkin> getSkins() {
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
                    if (name == null || "".equals(name.trim())) {
                        name = entry1.getKey();
                    }else {
                        break;
                    }
                }
                skin.setGeometryName(name);
                skin.setGeometryData(Utils.readFile(fileJson));
                this.sword = skin;
                getLogger().info(this.getLanguage(null).translateString("swordSuccess"));
            }else {
                getLogger().warning(this.getLanguage(null).translateString("swordFailure"));
            }
        } catch (IOException ignored) {
            getLogger().warning(this.getLanguage(null).translateString("swordFailure"));
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
            getLogger().info(this.getLanguage(null).translateString("defaultSkinSuccess"));
        }else {
            getLogger().error(this.getLanguage(null).translateString("defaultSkinFailure"));
        }
    }

    /**
     * 加载所有房间
     */
    public void loadAllRoom() {
        this.getLogger().info(this.getLanguage().translateString("startLoadingRoom"));
        File[] files = new File(this.getDataFolder() + "/Rooms").listFiles();
        if (files != null && files.length > 0) {
            Arrays.stream(files)
                    .filter(File::isFile)
                    .filter(file -> file.getName().endsWith(".yml"))
                    .forEach(file -> {
                        String[] fileName = file.getName().split("\\.");
                        if (fileName.length > 0) {
                            this.loadRoom(fileName[0]);
                        }
                    });
        }
        this.getLogger().info(this.getLanguage().translateString("roomLoadedAllSuccess")
                .replace(" %number%", this.rooms.size() + ""));
    }

    public void loadRoom(String world) {
        Config config = getRoomConfig(world);
        String name = this.temporaryRooms.contains(world) ? world : config.getString("roomName", world);
        if (config.getInt("waitTime", 0) == 0 ||
                config.getInt("gameTime", 0) == 0 ||
                "".equals(config.getString("waitSpawn", "").trim()) ||
                config.getStringList("randomSpawn").size() == 0 ||
                config.getStringList("goldSpawn").size() == 0 ||
                config.getInt("goldSpawnTime", 0) == 0 ||
                "".equals(config.getString("gameMode", "").trim())) {
            this.getLogger().warning(this.getLanguage().translateString("roomLoadedFailureByConfig")
                    .replace("%name%", name + "(" + world + ")"));
            return;
        }
        if (Server.getInstance().getLevelByName(world) == null && !Server.getInstance().loadLevel(world)) {
            this.getLogger().warning(this.getLanguage().translateString("roomLoadedFailureByLevel")
                    .replace("%name%", name + "(" + world + ")"));
            return;
        }
        String gameMode = config.getString("gameMode", "classic");
        if (!ROOM_CLASS.containsKey(gameMode)) {
            this.getLogger().warning(this.getLanguage().translateString("roomLoadedFailureByGameMode")
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
            this.getLogger().info(this.getLanguage().translateString("roomLoadedSuccess")
                    .replace("%name%", name + "(" + world + ")"));
        } catch (Exception e) {
            this.roomName.remove(world);
            this.getLogger().error("Room loading error", e);
            return;
        }
        //兼容SimpleScoreboards
        try {
            Class.forName("me.petterim1.scoreboards.Main");
            if (!Main.noScoreboardWorlds.contains(world)) {
                Main.noScoreboardWorlds.add(world);
            }
        }catch (Exception e) {
            if (debug) {
                this.getLogger().error("调用SimpleScoreboards插件接口错误: ", e);
            }
        }
    }

    /**
     * 卸载所有房间
     */
    public void unloadAllRoom() {
        if (!this.rooms.isEmpty()) {
            for (String world : new HashSet<>(this.rooms.keySet())) {
                this.unloadRoom(world);
            }
            this.rooms.clear();
        }
        this.roomName.clear();
        this.roomConfigs.clear();
    }

    @SuppressWarnings("rawtypes")
    public void unloadRoom(String world) {
        if (this.rooms.containsKey(world)) {
            BaseRoom room = this.rooms.remove(world);
            try {
                room.endGame();
            } catch (Exception ignored) {

            }
            for (BaseMurderMysteryListener listener : this.murderMysteryListeners.values()) {
                listener.removeListenerRoom(world);
            }
            Watchdog.remove(room);
            this.getLogger().info(this.getLanguage().translateString("roomUnloadSuccess")
                    .replace("%name%", room.getFullRoomName()));
            this.roomName.remove(world);

            //兼容SimpleScoreboards
            try {
                Class.forName("me.petterim1.scoreboards.Main");
                Main.noScoreboardWorlds.remove(world);
            }catch (Exception e) {
                if (debug) {
                    this.getLogger().error("调用SimpleScoreboards插件接口错误: ", e);
                }
            }
        }
    }

    /**
     * 重载所有房间
     */
    public void reLoadRooms() {
        this.unloadAllRoom();
        this.loadAllRoom();
    }

    /**
     * 加载所有皮肤
     */
    private void loadSkins() {
        this.getLogger().info(this.getLanguage().translateString("startLoadingSkin"));
        File[] files = (new File(this.getDataFolder() + "/Skins")).listFiles();
        if (files != null && files.length > 0) {
            int x = 0;
            for (File file : files) {
                if (!file.isDirectory()) {
                    continue;
                }
                String skinName = file.getName();
                File skinFile = new File(getDataFolder() + "/Skins/" + skinName + "/skin.png");
                if (skinFile.exists()) {
                    MurderMysterySkin skin = new MurderMysterySkin();
                    skin.setTrusted(true);
                    BufferedImage skinData = null;
                    try {
                        skinData = ImageIO.read(skinFile);
                    } catch (Exception ignored) {
                        this.getLogger().warning(this.getLanguage()
                                .translateString("skinFailureByFormat").replace("%name%", skinName));
                    }
                    if (skinData != null) {
                        skin.setSkinData(skinData);
                        skin.setSkinId(skinName);
                        String tip = this.getLanguage().translateString("skinLoadedSuccess")
                                .replace("%number%", x + "")
                                .replace("%name%", skinName) + "  ";

                        try {
                            File wantedFile = new File(this.getDataFolder() + "/Skins/" + skinName + "/wanted.png");
                            if (wantedFile.exists()) {
                                skin.setWantedImage(ImageIO.read(wantedFile));
                                tip += this.getLanguage().translateString("skinWantedLoadedSuccess");
                            }else {
                                throw new IOException();
                            }
                        } catch (IOException ignored) {
                            tip += this.getLanguage().translateString("skinWantedLoadedFailure");
                        }

                        this.getLogger().info(tip);
                        this.skins.put(x, skin);
                        x++;
                    }else {
                        this.getLogger().warning(this.getLanguage().translateString("skinFailureByFormat").replace("%name%", skinName));
                    }
                } else {
                    this.getLogger().warning(this.getLanguage().translateString("skinFailureByName").replace("%name%", skinName));
                }
            }
        }
        if (this.skins.size() >= 16) {
            this.getLogger().info(this.getLanguage().translateString("skinLoadedAllSuccess")
                    .replace("%number%", this.skins.size() + ""));
        }else {
            this.getLogger().warning(this.getLanguage().translateString("skinLoadedAllFailureByNumber"));
        }
    }

}
