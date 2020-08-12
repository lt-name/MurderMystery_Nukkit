package cn.lanink.murdermystery;

import cn.lanink.murdermystery.addons.manager.AddonsManager;
import cn.lanink.murdermystery.command.AdminCommand;
import cn.lanink.murdermystery.command.UserCommand;
import cn.lanink.murdermystery.lib.scoreboard.IScoreboard;
import cn.lanink.murdermystery.lib.scoreboard.ScoreboardDe;
import cn.lanink.murdermystery.lib.scoreboard.ScoreboardGt;
import cn.lanink.murdermystery.listener.PlayerDamageListener;
import cn.lanink.murdermystery.listener.PlayerGameListener;
import cn.lanink.murdermystery.listener.PlayerJoinAndQuit;
import cn.lanink.murdermystery.listener.RoomLevelProtection;
import cn.lanink.murdermystery.room.BaseRoom;
import cn.lanink.murdermystery.room.ClassicModeRoom;
import cn.lanink.murdermystery.room.InfectedModeRoom;
import cn.lanink.murdermystery.ui.GuiListener;
import cn.lanink.murdermystery.utils.Language;
import cn.lanink.murdermystery.utils.MetricsLite;
import cn.lanink.murdermystery.utils.Tools;
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

    public static final String VERSION = "1.0.6-SNAPSHOT git-e61c4a0";
    private static MurderMystery murderMystery;
    private static AddonsManager addonsManager;
    private Language language;
    private Config config;
    private final HashMap<String, Config> roomConfigs = new HashMap<>();
    private static final LinkedHashMap<String, Class<? extends BaseRoom>> ROOM_CLASS = new LinkedHashMap<>();
    private final LinkedHashMap<String, BaseRoom> rooms = new LinkedHashMap<>();
    private final LinkedHashMap<Integer, Skin> skins = new LinkedHashMap<>();
    private Skin sword;
    private final Skin corpseSkin = new Skin();
    public final Set<Integer> taskList = new HashSet<>();
    private String cmdUser, cmdAdmin;
    private List<String> cmdUserAliases, cmdAdminAliases;
    private IScoreboard scoreboard;
    public static final Random RANDOM = new Random();
    private boolean hasTips = false;

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
        this.cmdUser = this.config.getString("cmdUser", "murdermystery");
        this.cmdUserAliases = this.config.getStringList("cmdUserAliases");
        this.cmdAdmin = this.config.getString("cmdAdmin", "murdermysteryadmin");
        this.cmdAdminAliases = this.config.getStringList("cmdAdminAliases");
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
        //注册房间类
        registerRoom("classic", ClassicModeRoom.class);
        registerRoom("infected", InfectedModeRoom.class);
    }

    @Override
    public void onEnable() {
        getLogger().info("§e插件开始加载！本插件是免费哒~如果你花钱了，那一定是被骗了~");
        getLogger().info("§l§eVersion: " + VERSION);
        //加载计分板
        try {
            Class.forName("de.theamychan.scoreboard.ScoreboardPlugin");
            if (getServer().getPluginManager().getPlugin("ScoreboardPlugin").isDisabled()) {
                throw new Exception("Not Loaded");
            }
            this.scoreboard = new ScoreboardDe();
        } catch (Exception e) {
            try {
                Class.forName("gt.creeperface.nukkit.scoreboardapi.ScoreboardAPI");
                if (getServer().getPluginManager().getPlugin("ScoreboardAPI").isDisabled()) {
                    throw new Exception("Not Loaded");
                }
                this.scoreboard = new ScoreboardGt();
            } catch (Exception ignored) {
                getLogger().error(this.language.scoreboardAPINotFound);
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }
        //检查Tips
        try {
            Class.forName("tip.Main");
            if (getServer().getPluginManager().getPlugin("Tips").isDisabled()) {
                throw new Exception("Not Loaded");
            }
            this.hasTips = true;
        } catch (Exception ignored) {

        }
        this.loadResources();
        this.loadRooms();
        this.loadSkins();
        getServer().getCommandMap().register("",
                new UserCommand(this.cmdUser, this.cmdUserAliases.toArray(new String[0])));
        getServer().getCommandMap().register("",
                new AdminCommand(this.cmdAdmin, this.cmdAdminAliases.toArray(new String[0])));
        getServer().getPluginManager().registerEvents(new PlayerJoinAndQuit(this), this);
        getServer().getPluginManager().registerEvents(new RoomLevelProtection(this), this);
        getServer().getPluginManager().registerEvents(new PlayerGameListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(this), this);
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
            new MetricsLite(this, 7290);
        } catch (Throwable ignore) {

        }
        getLogger().info(this.language.pluginEnable);
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
                    getLogger().info(this.language.roomUnloadFailure.replace("%name%", entry.getKey()));
                }else {
                    getLogger().info(this.language.roomUnloadSuccess.replace("%name%", entry.getKey()));
                }
                Tools.cleanEntity(entry.getValue().getLevel(), true);
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
    public static void registerRoom(String name, Class<? extends BaseRoom> roomClass) {
        ROOM_CLASS.put(name, roomClass);
    }

    public static boolean hasRoomClass(String name) {
        return ROOM_CLASS.containsKey(name);
    }

    public static LinkedHashMap<String, Class<? extends BaseRoom>> getRoomClass() {
        return ROOM_CLASS;
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

    public boolean isHasTips() {
        return hasTips;
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
        skin.setSkinResourcePatch(Skin.GEOMETRY_CUSTOM);
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
        this.corpseSkin.setSkinResourcePatch(Skin.GEOMETRY_CUSTOM);
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
                            config.getString("gameMode", "").trim().equals("")) {
                        getLogger().warning(this.language.roomLoadedFailureByConfig.replace("%name%", worldName));
                        continue;
                    }
                    if (Server.getInstance().getLevelByName(worldName) == null && !Server.getInstance().loadLevel(worldName)) {
                        getLogger().warning(this.language.roomLoadedFailureByLevel.replace("%name%", worldName));
                        continue;
                    }
                    String gameMode = config.getString("gameMode", "classic");
                    if (!ROOM_CLASS.containsKey(gameMode)) {
                        getLogger().warning(this.language.roomLoadedFailureByGameMode
                                .replace("%name%", worldName)
                                .replace("%gameMode%", gameMode));
                        continue;
                    }
                    try {
                        Constructor<? extends BaseRoom> constructor =  ROOM_CLASS.get(gameMode)
                                .getConstructor(Level.class, Config.class);
                        BaseRoom baseRoom = constructor.newInstance(Server.getInstance().getLevelByName(worldName), config);
                        baseRoom.setGameMode(gameMode);
                        this.rooms.put(worldName, baseRoom);
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
            Iterator<Map.Entry<String, BaseRoom>> it = this.rooms.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<String, BaseRoom> entry = it.next();
                entry.getValue().endGameEvent();
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
                    skin.setSkinResourcePatch(Skin.GEOMETRY_CUSTOM);
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
