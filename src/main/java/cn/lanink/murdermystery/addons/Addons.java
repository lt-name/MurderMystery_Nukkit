package cn.lanink.murdermystery.addons;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.addons.uishop.UiShop;
import cn.nukkit.utils.Config;

import java.util.HashMap;

public class Addons {

    private final Config config;
    private final HashMap<String, BaseAddons> baseAddons = new HashMap<>();

    public Addons(MurderMystery murderMystery) {
        murderMystery.saveResource("Addons/config.yml", false);
        this.config = new Config(murderMystery.getDataFolder() + "/Addons/config.yml", 2);
        if (this.config.getBoolean("UiShop", false)) {
            this.addAddons(new UiShop());
        }
    }

    public Config getConfig() {
        return this.config;
    }

    public boolean enable(String addonsName) {
        if (this.baseAddons.containsKey(addonsName)) {
            this.baseAddons.get(addonsName).onEnable();
            return true;
        }
        return false;
    }

    public void enable(BaseAddons baseAddons) {
        if (!this.baseAddons.containsValue(baseAddons)) {
            this.baseAddons.put(baseAddons.getAddonsName(), baseAddons);
        }
        this.baseAddons.get(baseAddons.getAddonsName()).onEnable();
    }

    public void enableAll() {
        for (BaseAddons baseAddons : this.baseAddons.values()) {
            baseAddons.onEnable();
        }
    }

    public boolean disable(String addonsName) {
        if (this.baseAddons.containsKey(addonsName)) {
            this.baseAddons.get(addonsName).onDisable();
            return true;
        }
        return false;
    }

    public void disable(BaseAddons baseAddons) {
        if (!this.baseAddons.containsValue(baseAddons)) {
            this.baseAddons.put(baseAddons.getAddonsName(), baseAddons);
        }
        this.baseAddons.get(baseAddons.getAddonsName()).onDisable();
    }

    public void disableAll() {
        for (BaseAddons baseAddons : this.baseAddons.values()) {
            baseAddons.onDisable();
        }
    }

    public boolean addAddons(BaseAddons baseAddons) {
        if (!this.baseAddons.containsValue(baseAddons)) {
            this.baseAddons.put(baseAddons.getAddonsName(), baseAddons);
        }
        return false;
    }

}
