package cn.lanink.murdermystery.addons.manager;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.addons.AddonsBase;
import cn.lanink.murdermystery.addons.manager.autoregister.RegisterCommand;
import cn.lanink.murdermystery.addons.manager.autoregister.RegisterCommands;
import cn.lanink.murdermystery.addons.manager.autoregister.RegisterListener;
import cn.lanink.murdermystery.addons.manager.command.AddonsCommand;
import cn.lanink.murdermystery.addons.manager.logger.AddonsLogger;
import cn.lanink.murdermystery.addons.uishop.UiShop;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.Listener;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * @author lt_name
 */
public final class AddonsManager {

    private final MurderMystery murderMystery;
    private final Server server;
    private final Logger logger;
    private final Config config;
    private static final HashMap<String, AddonsBase> ADDONS_BASE_MAP = new HashMap<>();
    private static final HashMap<String, Class<? extends AddonsBase>> ADDONS_CLASS_MAP = new HashMap<>();
    private static final HashMap<String, HashSet<Listener>> ADDONS_LISTENERS = new HashMap<>();
    private static final HashMap<String, HashSet<Command>> ADDONS_COMMANDS = new HashMap<>();

    public AddonsManager(MurderMystery murderMystery) {
        this.murderMystery = murderMystery;
        this.server = murderMystery.getServer();
        this.logger = new AddonsLogger(this);
        this.murderMystery.saveResource("Addons/config.yml", false);
        this.config = new Config(this.murderMystery.getDataFolder() + "/Addons/config.yml", 2);
        if (this.getConfig().getBoolean("UiShop", false)) {
            registerAddons("UiShop", UiShop.class);
        }
    }

    public Config getConfig() {
        return this.config;
    }

    /**
     * 注册扩展
     * @param name 名称
     * @param addonsBase 扩展
     */
    public static void registerAddons(String name, Class<? extends AddonsBase> addonsBase) {
        ADDONS_CLASS_MAP.put(name, addonsBase);
    }

    /**
     * 注册监听器
     * @param listener 监听器
     * @param addonsBase 所属扩展
     */
    public void registerEvents(Listener listener, AddonsBase addonsBase) {
        if (!ADDONS_LISTENERS.containsKey(addonsBase.getAddonsName())) {
            ADDONS_LISTENERS.put(addonsBase.getAddonsName(), new HashSet<>());
        }
        if (ADDONS_LISTENERS.get(addonsBase.getAddonsName()).add(listener)) {
            getServer().getPluginManager().registerEvents(listener, this.murderMystery);
        }
    }

    public boolean registerCommand(String fallbackPrefix, Command command, AddonsBase addonsBase) {
        if (!ADDONS_COMMANDS.containsKey(addonsBase.getAddonsName())) {
            ADDONS_COMMANDS.put(addonsBase.getAddonsName(), new HashSet<>());
        }
        if (ADDONS_COMMANDS.get(addonsBase.getAddonsName()).add(command)) {
            return getServer().getCommandMap().register(fallbackPrefix, command);
        }
        return false;
    }

    public static AddonsManager getInstance() {
        return MurderMystery.getAddonsManager();
    }

    public Server getServer() {
        return this.server;
    }

    public Logger getLogger() {
        return this.logger;
    }

    /**
     * @return 已加载的Addons HashMap
     */
    public HashMap<String, AddonsBase> getAddonsBaseMap() {
        return ADDONS_BASE_MAP;
    }

    public boolean enable(String addonName) {
        if (!ADDONS_BASE_MAP.containsKey(addonName) && ADDONS_CLASS_MAP.containsKey(addonName)) {
            this.getLogger().info("Loading " + addonName + " ...");
            try {
                Class<? extends AddonsBase> addonsClass = ADDONS_CLASS_MAP.get(addonName);
                AddonsBase addonsBase = addonsClass.newInstance();
                addonsClass.getMethod("init", String.class).invoke(addonsBase, addonName);
                if (addonsClass.isAnnotationPresent(RegisterListener.class) && addonsBase instanceof Listener) {
                    this.registerEvents((Listener) addonsBase, addonsBase);
                }
                if (addonsClass.isAnnotationPresent(RegisterCommands.class)) {
                    RegisterCommands commands = addonsClass.getAnnotation(RegisterCommands.class);
                    for (RegisterCommand command : commands.value()) {
                        this.registerCommand(command.fallbackPrefix(), new AddonsCommand(addonsBase, command), addonsBase);
                    }
                }else if (addonsClass.isAnnotationPresent(RegisterCommand.class)) {
                    RegisterCommand command = addonsClass.getAnnotation(RegisterCommand.class);
                    this.registerCommand(command.fallbackPrefix(), new AddonsCommand(addonsBase, command), addonsBase);
                }
                addonsBase.setEnabled(true);
                ADDONS_BASE_MAP.put(addonName, addonsBase);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void enableAll() {
        for (String addonsName : ADDONS_CLASS_MAP.keySet()) {
            this.enable(addonsName);
        }
    }

    public boolean disable(String addonsName) {
        return this.disable(addonsName, true);
    }

    private boolean disable(String addonsName, Boolean delete) {
        if (ADDONS_BASE_MAP.containsKey(addonsName)) {
            this.getLogger().info("Disabling " + addonsName + " ...");
            ADDONS_BASE_MAP.get(addonsName).setEnabled(false);
            if (ADDONS_COMMANDS.containsKey(addonsName)) {
                Iterator<Command> commandIt = ADDONS_COMMANDS.get(addonsName).iterator();
                while (commandIt.hasNext()) {
                    Command command = commandIt.next();
                    command.unregister(getServer().getCommandMap());
                    commandIt.remove();
                }
                ADDONS_COMMANDS.remove(addonsName);
            }
            if (ADDONS_LISTENERS.containsKey(addonsName)) {
                Iterator<Listener> listenerIt = ADDONS_LISTENERS.get(addonsName).iterator();
                while (listenerIt.hasNext()) {
                    Listener listener = listenerIt.next();
                    HandlerList.unregisterAll(listener);
                    listenerIt.remove();
                }
                ADDONS_LISTENERS.remove(addonsName);
            }
            if (delete) {
                ADDONS_BASE_MAP.remove(addonsName);
            }
            return true;
        }
        return false;
    }

    public void disableAll() {
        Iterator<Map.Entry<String, AddonsBase>> iterator = ADDONS_BASE_MAP.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, AddonsBase> entry = iterator.next();
            this.disable(entry.getKey(), false);
            iterator.remove();
        }
    }

}
