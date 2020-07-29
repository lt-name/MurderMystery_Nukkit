package cn.lanink.murdermystery;

import cn.lanink.lib.scoreboard.IScoreboard;
import cn.lanink.lib.scoreboard.ScoreboardDe;
import cn.lanink.lib.scoreboard.ScoreboardGt;
import cn.lanink.murdermystery.addons.manager.AddonsManager;
import cn.lanink.murdermystery.command.AdminCommand;
import cn.lanink.murdermystery.command.UserCommand;
import cn.lanink.murdermystery.listener.*;
import cn.lanink.murdermystery.room.RoomBase;
import cn.lanink.murdermystery.room.RoomClassicMode;
import cn.lanink.murdermystery.room.RoomInfectedMode;
import cn.lanink.murdermystery.ui.GuiListener;
import cn.lanink.murdermystery.ui.GuiType;
import cn.lanink.murdermystery.utils.Language;
import cn.lanink.murdermystery.utils.MetricsLite;
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

/**
 * MurderMystery
 *
 * @author lt_name
 */
public class MurderMystery extends PluginBase {

    public static final String VERSION = "?";
    private static MurderMystery murderMystery;
    private static AddonsManager addonsManager;
    private Language language;
    private Config config;
    private final HashMap<String, Config> roomConfigs = new HashMap<>();
    private static final LinkedHashMap<String, Class<? extends RoomBase>> ROOMCLASS = new LinkedHashMap<>();
    private final LinkedHashMap<String, RoomBase> rooms = new LinkedHashMap<>();
    private final LinkedHashMap<Integer, Skin> skins = new LinkedHashMap<>();
    private Skin sword;
    private final Skin corpseSkin = new Skin();
    public final List<Integer> taskList = new LinkedList<>();
    private String cmdUser, cmdAdmin;
    private final HashMap<Integer, GuiType> guiCache = new HashMap<>();
    private IScoreboard scoreboard;
    private MetricsLite metricsLite;

    public static MurderMystery getInstance() { return murderMystery; }

    @Override
    public void onLoad() {
        if (murderMystery == null) murderMystery = this;
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
        this.config = new Config(getDataFolder() + "/config.yml", 2);
        //语言文件
        saveResource("Resources/Language/zh_CN.yml", false);
        saveResource("Resources/Language/en_US.yml", false);
        saveResource("Resources/Language/ko_KR.yml", false);
        String s = this.config.getString("language", "zh_CN");
        File languageFile = new File(getDataFolder() + "/Resources/Language/" + s + ".yml");
        if (languageFile.exists()) {
            this.language = new Language(new Config(languageFile, 2));
            getLogger().info("§aLanguage: " + s + " loaded !");
        }else {
            this.language = new Language(new Config());
            getLogger().warning("§cLanguage: " + s + " Not found, Load the default language !");
        }
        //扩展
        if (addonsManager == null) addonsManager = new AddonsManager(this);
        //加载房间类
        registerRoom("classic", RoomClassicMode.class);
        registerRoom("infected", RoomInfectedMode.class);

        //稍微暂停下，方便服主查看提示信息
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {

        }
    }

    @Override
    public void onEnable() {
        getLogger().info("§e插件开始加载！本插件是免费哒~如果你花钱了，那一定是被骗了~");
        getLogger().info("§l§eVersion: " + VERSION);
        //加载计分板
        try {
            Class.forName("de.theamychan.scoreboard.ScoreboardPlugin");
            this.scoreboard = new ScoreboardDe();
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("gt.creeperface.nukkit.scoreboardapi.ScoreboardAPI");
                this.scoreboard = new ScoreboardGt();
            } catch (ClassNotFoundException ignored) {
                getLogger().error(this.language.scoreboardAPINotFound);
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }
        this.loadResources();
        this.loadRooms();
        this.loadSkins();
        this.cmdUser = this.config.getString("cmdUser", "murdermystery");
        this.cmdAdmin = this.config.getString("cmdAdmin", "murdermysteryadmin");
        getServer().getCommandMap().register("", new UserCommand(this.cmdUser));
        getServer().getCommandMap().register("", new AdminCommand(this.cmdAdmin));
        getServer().getPluginManager().registerEvents(new PlayerJoinAndQuit(), this);
        getServer().getPluginManager().registerEvents(new RoomLevelProtection(this), this);
        getServer().getPluginManager().registerEvents(new PlayerGameListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new MurderListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        //启用扩展-使用task保证在所有插件都加载完后加载扩展
        getServer().getScheduler().scheduleTask(this, new Task() {
            @Override
            public void onRun(int i) {
                getLogger().info(language.startLoadingAddons);
                addonsManager.enableAll();
                getLogger().info(language.addonsLoaded);
            }
        });
        try {
            if (this.metricsLite == null) this.metricsLite = new MetricsLite(this, 7290);
        } catch (Throwable ignore) {

        }
        getLogger().info(this.language.pluginEnable);
    }

    @Override
    public void onDisable() {
        addonsManager.disableAll();
        if (this.rooms.values().size() > 0) {
            Iterator<Map.Entry<String, RoomBase>> it = this.rooms.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<String, RoomBase> entry = it.next();
                if (entry.getValue().getPlayers().size() > 0) {
                    entry.getValue().endGame(false);
                    getLogger().info(this.language.roomUnloadFailure.replace("%name%", entry.getKey()));
                }else {
                    getLogger().info(this.language.roomUnloadSuccess.replace("%name%", entry.getKey()));
                }
                it.remove();
            }
        }
        this.rooms.clear();
        this.roomConfigs.clear();
        this.skins.clear();
        for (int id : this.taskList) {
            getServer().getScheduler().cancelTask(id);
        }
        this.taskList.clear();
        getLogger().info(this.language.pluginDisable);
    }

    /**
     * 注册房间类
     *
     * @param name 名称
     * @param roomClass 房间类
     */
    public static void registerRoom(String name, Class<? extends RoomBase> roomClass) {
        ROOMCLASS.put(name, roomClass);
    }

    public static LinkedHashMap<String, Class<? extends RoomBase>> getRoomClass() {
        return ROOMCLASS;
    }

    public Language getLanguage() {
        return this.language;
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

    public String getCmdUser() {
        return this.cmdUser;
    }

    public String getCmdAdmin() {
        return this.cmdAdmin;
    }

    public HashMap<Integer, GuiType> getGuiCache() {
        return this.guiCache;
    }

    public Skin getSword() {
        return this.sword;
    }

    public Skin getCorpseSkin() {
        return this.corpseSkin;
    }

    public LinkedHashMap<String, RoomBase> getRooms() {
        return this.rooms;
    }

    public Config getRoomConfig(Level level) {
        return getRoomConfig(level.getName());
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
                getLogger().info(this.language.swordSuccess);
            }else {
                getLogger().warning(this.language.swordFailure);
            }
        } catch (IOException ignored) {
            getLogger().warning(this.language.swordFailure);
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
            getLogger().info(this.language.defaultSkinSuccess);
        }else {
            getLogger().error(this.language.defaultSkinFailure);
        }
    }

    /**
     * 加载所有房间
     */
    private void loadRooms() {
        getLogger().info(this.language.startLoadingRoom);
        File[] s = new File(getDataFolder() + "/Rooms").listFiles();
        if (s != null && s.length > 0) {
            for (File file1 : s) {
                String[] fileName = file1.getName().split("\\.");
                if (fileName.length > 0) {
                    String worldName = fileName[0];
                    Config config = getRoomConfig(worldName);
                    if (config.getInt("waitTime", 0) == 0 ||
                            config.getInt("gameTime", 0) == 0 ||
                            config.getString("waitSpawn", "").trim().equals("") ||
                            config.getStringList("randomSpawn").size() == 0 ||
                            config.getStringList("goldSpawn").size() == 0 ||
                            config.getInt("goldSpawnTime", 0) == 0 ||
                            config.getString("world", "").trim().equals("")) {
                        getLogger().warning(this.language.roomLoadedFailureByConfig.replace("%name%", worldName));
                        continue;
                    }
                    if (Server.getInstance().getLevelByName(worldName) == null && !Server.getInstance().loadLevel(worldName)) {
                        getLogger().warning(this.language.roomLoadedFailureByLevel.replace("%name%", worldName));
                        continue;
                    }
                    String gameMode = config.getString("gameMode", "classic");
                    if (!ROOMCLASS.containsKey(gameMode)) {
                        //TODO 加载失败提示

                        continue;
                    }
                    try {
                        Constructor<? extends RoomBase> constructor =  ROOMCLASS.get(gameMode)
                                .getConstructor(Level.class, Config.class);
                        RoomBase roomBase = constructor.newInstance(Server.getInstance().getLevelByName(worldName), config);
                        roomBase.setGameMode(gameMode);
                        this.rooms.put(worldName, roomBase);
                        getLogger().info(this.language.roomLoadedSuccess.replace("%name%", worldName));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        getLogger().info(this.language.roomLoadedAllSuccess.replace(" %number%", this.rooms.size() + ""));
    }

    /**
     * 卸载所有房间
     */
    public void unloadRooms() {
        if (this.rooms.values().size() > 0) {
            Iterator<Map.Entry<String, RoomBase>> it = this.rooms.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<String, RoomBase> entry = it.next();
                entry.getValue().endGame();
                getLogger().info(this.language.roomUnloadSuccess.replace("%name%", entry.getKey()));
                it.remove();
            }
            this.rooms.clear();
        }
        this.roomConfigs.clear();
        for (int id : this.taskList) {
            getServer().getScheduler().cancelTask(id);
        }
        this.taskList.clear();
    }

    /**
     * 重载所有房间
     */
    public void reLoadRooms() {
        this.unloadRooms();
        this.loadRooms();
    }

    /**
     * 加载所有皮肤
     */
    private void loadSkins() {
        getLogger().info(this.language.startLoadingSkin);
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
                        getLogger().warning(this.language.skinFailureByFormat.replace("%name%", skinName));
                    }
                    if (skinData != null) {
                        skin.setSkinData(skinData);
                        skin.setSkinId(skinName);
                        getLogger().info(this.language.skinLoadedSuccess.replace("%number%", x + "")
                                .replace("%name%", skinName));
                        this.skins.put(x, skin);
                        x++;
                    }else {
                        getLogger().warning(this.language.skinFailureByFormat.replace("%name%", skinName));
                    }
                } else {
                    getLogger().warning(this.language.skinFailureByName.replace("%name%", skinName));
                }
            }
        }
        if (this.skins.size() >= 16) {
            getLogger().info(this.language.skinLoadedAllSuccess.replace("%number%", this.skins.size() + ""));
        }else {
            getLogger().warning(this.language.skinLoadedAllFailureByNumber);
        }
    }

}
