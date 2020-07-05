package cn.lanink.murdermystery.addons;

import cn.lanink.murdermystery.MurderMystery;
import cn.nukkit.Server;
import cn.nukkit.plugin.PluginLogger;
import cn.nukkit.utils.Config;

import java.io.File;

/**
 * 扩展基础
 * @author lt_name
 */
public abstract class BaseAddons {

    protected final Server server;
    protected final MurderMystery murderMystery;
    protected String addonsName;
    private final File configFile;
    private Config config;

    public BaseAddons(MurderMystery murderMystery, String addonsName) {
        this.murderMystery = murderMystery;
        this.server = murderMystery.getServer();
        this.addonsName = addonsName;
        this.configFile = new File(this.getDataFolder() + "/" + this.addonsName, "config.yml");
    }

    private void setAddonsName(String name) {
        this.addonsName = name;
    }

    public String getAddonsName() {
        return this.addonsName;
    }

    public abstract void onEnable();

    public abstract void onDisable();

    public Server getServer() {
        return this.server;
    }

    public Config getConfig() {
        if (this.config == null) {
            this.config = new Config(configFile, 2);
        }
        return this.config;
    }

    public final File getDataFolder() {
        return new File(this.murderMystery.getDataFolder() + "/Addons");
    }

    public PluginLogger getLogger() {
        return this.murderMystery.getLogger();
    }

    @Override
    public final boolean equals(Object obj) {
        if(obj instanceof BaseAddons){
            return ((BaseAddons) obj).getAddonsName().equals(this.getAddonsName());
        }
        return false;
    }

}
