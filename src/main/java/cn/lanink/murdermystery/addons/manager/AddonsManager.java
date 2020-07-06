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
    private final HashMap<String, AddonsBase> addonsBaseMap = new HashMap<>();
    private final HashMap<String, Class<? extends AddonsBase>> addonsClassMap = new HashMap<>();
    private final HashMap<String, HashSet<Listener>> addonsListeners = new HashMap<>();

    public AddonsManager(MurderMystery murderMystery) {
        this.murderMystery = murderMystery;
        this.server = murderMystery.getServer();
        this.logger = new AddonsLogger(this);
        this.murderMystery.saveResource("Addons/config.yml", false);
        this.config = new Config(this.murderMystery.getDataFolder() + "/Addons/config.yml", 2);
        if (this.getConfig().getBoolean("UiShop", false)) {
            this.registerAddons("UiShop", UiShop.class);
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
    public void registerAddons(String name, Class<? extends AddonsBase> addonsBase) {
        this.addonsClassMap.put(name, addonsBase);
    }

    /**
     * 注册监听器
     * @param listener 监听器
     * @param addonsBase 所属扩展
     */
    public void registerEvents(Listener listener, AddonsBase addonsBase) {
        if (!this.addonsListeners.containsKey(addonsBase.getAddonsName())) {
            this.addonsListeners.put(addonsBase.getAddonsName(), new HashSet<>());
        }
        this.addonsListeners.get(addonsBase.getAddonsName()).add(listener);
        getServer().getPluginManager().registerEvents(listener, this.murderMystery);
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
        return this.addonsBaseMap;
    }

    public boolean enable(String addonName) {
        if (!this.addonsBaseMap.containsKey(addonName) && this.addonsClassMap.containsKey(addonName)) {
            this.getLogger().info("Loading " + addonName + " ...");
            try {
                Class<? extends AddonsBase> addonsClass = this.addonsClassMap.get(addonName);
                AddonsBase addonsBase = addonsClass.newInstance();
                addonsClass.getMethod("init", String.class).invoke(addonsBase, addonName);
                if (addonsClass.isAnnotationPresent(RegisterListener.class) && addonsBase instanceof Listener) {
                    this.registerEvents((Listener) addonsBase, addonsBase);
                }
                if (addonsClass.isAnnotationPresent(RegisterCommands.class)) {
                    RegisterCommands commands = addonsClass.getAnnotation(RegisterCommands.class);
                    for (RegisterCommand command : commands.value()) {
                        getServer().getCommandMap().register(command.fallbackPrefix(), new AddonsCommand(addonsBase, command));
                    }
                }else if (addonsClass.isAnnotationPresent(RegisterCommand.class)) {
                    RegisterCommand command = addonsClass.getAnnotation(RegisterCommand.class);
                    getServer().getCommandMap().register(command.fallbackPrefix(), new AddonsCommand(addonsBase,command));
                }
                addonsBase.setEnabled(true);
                this.addonsBaseMap.put(addonName, addonsBase);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void enableAll() {
        for (String addonsName : this.addonsClassMap.keySet()) {
            this.enable(addonsName);
        }
    }

    public boolean disable(String addonsName) {
        return this.disable(addonsName, true);
    }

    private boolean disable(String addonsName, Boolean delete) {
        if (this.addonsBaseMap.containsKey(addonsName)) {
            this.getLogger().info("Disabling " + addonsName + " ...");
            this.addonsBaseMap.get(addonsName).setEnabled(false);
            if (this.addonsListeners.containsKey(addonsName)) {
                for (Listener listener : this.addonsListeners.get(addonsName)) {
                    HandlerList.unregisterAll(listener);
                }
            }
            if (delete) {
                this.addonsBaseMap.remove(addonsName);
            }
            return true;
        }
        return false;
    }

    public void disableAll() {
        Iterator<Map.Entry<String, AddonsBase>> iterator = this.addonsBaseMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, AddonsBase> entry = iterator.next();
            this.disable(entry.getKey(), false);
            iterator.remove();
        }
    }

}
