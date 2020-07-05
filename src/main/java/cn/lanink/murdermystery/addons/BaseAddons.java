package cn.lanink.murdermystery.addons;

import cn.lanink.murdermystery.MurderMystery;
import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.Logger;

import java.io.File;

/**
 * 扩展基础
 * @author lt_name
 */
public abstract class BaseAddons {

    private final Server server = Server.getInstance();
    private final MurderMystery murderMystery = MurderMystery.getInstance();
    private String addonsName;
    private Logger logger;
    private boolean isEnabled = false;
    private File configFile;
    private Config config;

    public BaseAddons() {

    }

    public final void init(String addonsName) throws Exception {
        if (this.isEnabled) {
            throw new Exception("[Error] 请勿在加载后执行此方法！");
        }
        this.addonsName = addonsName;
        this.logger = new AddonsLogger(this);
        this.configFile = new File(this.getDataFolder() + "/" + this.addonsName, "config.yml");
    }

    public final boolean isEnabled() {
        return this.isEnabled;
    }

    public final void setEnabled() {
        this.setEnabled(true);
    }

    public final void setEnabled(boolean value) {
        if (this.isEnabled != value) {
            this.isEnabled = value;
            if (this.isEnabled) {
                this.onEnable();
            } else {
                this.onDisable();
            }
        }

    }

    private void setAddonsName(String name) {
        this.addonsName = name;
    }

    public final String getAddonsName() {
        return this.addonsName;
    }

    public abstract void onEnable();

    public abstract void onDisable();

    public final Server getServer() {
        return this.server;
    }

    public final MurderMystery getMurderMystery() {
        return this.murderMystery;
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

    public final Logger getLogger() {
        return this.logger;
    }

    @Override
    public final boolean equals(Object obj) {
        if(obj instanceof BaseAddons){
            return ((BaseAddons) obj).getAddonsName().equals(this.getAddonsName());
        }
        return false;
    }

}
