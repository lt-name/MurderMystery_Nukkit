package cn.lanink.murdermystery.addons;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.addons.uishop.UiShop;
import cn.nukkit.utils.Config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author lt_name
 */
public class AddonsManager {

    private final MurderMystery murderMystery;
    private final Config config;
    private final HashMap<String, BaseAddons> baseAddons = new HashMap<>();
    private final HashMap<String, Class<? extends BaseAddons>> addonsClassMap = new HashMap<>();

    public AddonsManager(MurderMystery murderMystery) {
        this.murderMystery = murderMystery;
        this.murderMystery.saveResource("Addons/config.yml", false);
        this.config = new Config(this.murderMystery.getDataFolder() + "/Addons/config.yml", 2);
        if (this.getConfig().getBoolean("UiShop", false)) {
            this.registerAddons("UiShop", UiShop.class);
        }
    }

    public Config getConfig() {
        return this.config;
    }

    public boolean enable(String addonName) {
        if (!this.baseAddons.containsKey(addonName) && this.addonsClassMap.containsKey(addonName)) {
            try {
                Class<? extends BaseAddons> addonsClass = this.addonsClassMap.get(addonName);
                BaseAddons baseAddons = addonsClass.newInstance();
                addonsClass.getMethod("init", String.class).invoke(baseAddons, addonName);
                this.baseAddons.put(addonName, baseAddons);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (this.baseAddons.containsKey(addonName)) {
            this.murderMystery.getLogger().info("[AddonsManager] Loading " + addonName + " ...");
            this.baseAddons.get(addonName).setEnabled(true);
            return true;
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
            this.murderMystery.getLogger().info("[AddonsManager] Disabling " + addonsName + " ...");
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
            this.murderMystery.getLogger().info("[AddonsManager] Disabling " + entry.getKey() + " ...");
            entry.getValue().setEnabled(false);
            iterator.remove();
        }
    }

    public void registerAddons(String name, Class<? extends BaseAddons> addon) {
        this.addonsClassMap.put(name, addon);
    }

}
