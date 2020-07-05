package cn.lanink.murdermystery.addons;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.addons.uishop.UiShop;
import cn.nukkit.utils.Config;

import java.lang.reflect.Constructor;
import java.util.HashMap;

/**
 * @author lt_name
 */
public class Addons {

    private final MurderMystery murderMystery;
    private final Config config;
    private final HashMap<String, BaseAddons> baseAddons = new HashMap<>();
    private final HashMap<String, Class<? extends BaseAddons>> addonClassMap = new HashMap<>();

    public Addons(MurderMystery murderMystery) {
        this.murderMystery = murderMystery;
        this.murderMystery.saveResource("Addons/config.yml", false);
        this.config = new Config(this.murderMystery.getDataFolder() + "/Addons/config.yml", 2);
        if (this.getConfig().getBoolean("UiShop", false)) {
            this.addAddons("UiShop", UiShop.class);
        }
    }

    public Config getConfig() {
        return this.config;
    }

    public boolean enable(String addonName) {
        if (!this.baseAddons.containsKey(addonName)) {
            if (this.addonClassMap.containsKey(addonName)) {
                try {
                    Constructor<? extends BaseAddons> constructor = this.addonClassMap.get(addonName)
                            .getConstructor(MurderMystery.class, String.class);
                    BaseAddons baseAddons = constructor.newInstance(this.murderMystery, addonName);
                    this.baseAddons.put(addonName, baseAddons);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (this.baseAddons.containsKey(addonName)) {
            this.baseAddons.get(addonName).onEnable();
            return true;
        }
        return false;
    }

    public void enableAll() {
        for (String addonsName : this.addonClassMap.keySet()) {
            this.enable(addonsName);
        }
    }

    public boolean disable(String addonsName) {
        if (this.baseAddons.containsKey(addonsName)) {
            this.baseAddons.get(addonsName).onDisable();
            this.baseAddons.remove(addonsName);
            return true;
        }
        return false;
    }

    public void disableAll() {
        for (BaseAddons baseAddons : this.baseAddons.values()) {
            baseAddons.onDisable();
        }
        this.baseAddons.clear();
    }

    public boolean addAddons(BaseAddons baseAddons) {
        if (!this.baseAddons.containsValue(baseAddons)) {
            this.baseAddons.put(baseAddons.getAddonsName(), baseAddons);
            return true;
        }
        return false;
    }

    public void addAddons(String name, Class<? extends BaseAddons> addon) {
        this.addonClassMap.put(name, addon);
    }

}
