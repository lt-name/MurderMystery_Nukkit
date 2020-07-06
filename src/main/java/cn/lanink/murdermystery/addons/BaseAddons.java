package cn.lanink.murdermystery.addons;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.addons.manager.exception.AddonsException;
import cn.lanink.murdermystery.addons.manager.logger.AddonsLogger;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandExecutor;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.Logger;

import java.io.File;

/**
 * 扩展基础
 * @author lt_name
 */
public abstract class BaseAddons implements CommandExecutor {

    private final Server server = Server.getInstance();
    private final MurderMystery murderMystery = MurderMystery.getInstance();
    private String addonsName;
    private Logger logger;
    private boolean isEnabled = false;
    private File configFile;
    private Config config;

    public BaseAddons() {

    }

    public final void init(String addonsName) throws AddonsException {
        if (this.isEnabled) {
            throw new AddonsException("[Error] 请勿在加载后执行此方法！");
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

    public final void setAddonsName(String name) throws AddonsException {
        if (this.isEnabled) {
            throw new AddonsException("[Error] 请勿在加载后执行此方法！");
        }
        this.addonsName = name;
    }

    public final String getAddonsName() {
        return this.addonsName;
    }

    public abstract void onEnable();

    public abstract void onDisable();

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

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
