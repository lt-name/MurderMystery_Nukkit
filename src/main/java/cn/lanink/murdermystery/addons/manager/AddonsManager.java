package cn.lanink.murdermystery.addons.manager;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.addons.BaseAddons;
import cn.lanink.murdermystery.addons.manager.autoregister.RegisterCommand;
import cn.lanink.murdermystery.addons.manager.autoregister.RegisterCommands;
import cn.lanink.murdermystery.addons.manager.autoregister.RegisterListener;
import cn.lanink.murdermystery.addons.manager.command.AddonsCommand;
import cn.lanink.murdermystery.addons.manager.logger.AddonsLogger;
import cn.lanink.murdermystery.addons.uishop.UiShop;
import cn.nukkit.Server;
import cn.nukkit.event.Listener;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.Logger;

import java.util.HashMap;
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
    private final HashMap<String, BaseAddons> baseAddons = new HashMap<>();
    private final HashMap<String, Class<? extends BaseAddons>> addonsClassMap = new HashMap<>();

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

    public void registerAddons(String name, Class<? extends BaseAddons> addon) {
        this.addonsClassMap.put(name, addon);
    }

    public Server getServer() {
        return this.server;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public boolean enable(String addonName) {
        if (!this.baseAddons.containsKey(addonName) && this.addonsClassMap.containsKey(addonName)) {
            this.getLogger().info("Loading " + addonName + " ...");
            try {
                Class<? extends BaseAddons> addonsClass = this.addonsClassMap.get(addonName);
                BaseAddons baseAddons = addonsClass.newInstance();
                addonsClass.getMethod("init", String.class).invoke(baseAddons, addonName);
                if (addonsClass.isAnnotationPresent(RegisterListener.class) && baseAddons instanceof Listener) {
                    getServer().getPluginManager().registerEvents((Listener) baseAddons, this.murderMystery);
                }
                if (addonsClass.isAnnotationPresent(RegisterCommands.class)) {
                    RegisterCommands commands = addonsClass.getAnnotation(RegisterCommands.class);
                    for (RegisterCommand command : commands.value()) {
                        getServer().getCommandMap().register(command.fallbackPrefix(), new AddonsCommand(baseAddons, command));
                    }
                }else if (addonsClass.isAnnotationPresent(RegisterCommand.class)) {
                    RegisterCommand command = addonsClass.getAnnotation(RegisterCommand.class);
                    getServer().getCommandMap().register(command.fallbackPrefix(), new AddonsCommand(baseAddons,command));
                }
                baseAddons.setEnabled(true);
                this.baseAddons.put(addonName, baseAddons);
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
        if (this.baseAddons.containsKey(addonsName)) {
            this.getLogger().info("Disabling " + addonsName + " ...");
            this.baseAddons.get(addonsName).setEnabled(false);
            this.baseAddons.remove(addonsName);
            return true;
        }
        return false;
    }

    public void disableAll() {
        Iterator<Map.Entry<String, BaseAddons>> iterator = this.baseAddons.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, BaseAddons> entry = iterator.next();
            this.getLogger().info("Disabling " + entry.getKey() + " ...");
            entry.getValue().setEnabled(false);
            iterator.remove();
        }
    }

}
