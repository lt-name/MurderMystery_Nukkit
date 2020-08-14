package cn.lanink.murdermystery.addons;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.addons.manager.AddonsManager;
import cn.lanink.murdermystery.addons.manager.exception.AddonsException;
import cn.lanink.murdermystery.addons.manager.logger.AddonsLogger;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandExecutor;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.Logger;
import cn.nukkit.utils.Utils;
import com.google.common.base.Preconditions;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Objects;

public abstract class AddonsBase implements CommandExecutor {

    private final Server server = Server.getInstance();
    private final MurderMystery murderMystery = MurderMystery.getInstance();
    private String addonsName;
    private Logger logger;
    private boolean isEnabled = false;
    private File dataFolder;
    private File configFile;
    private Config config;

    public AddonsBase() {

    }

    public final void init(String addonsName) throws AddonsException {
        if (this.isEnabled) {
            throw new AddonsException("[Error] 请勿在加载后执行此方法！");
        }
        this.addonsName = addonsName;
        this.logger = new AddonsLogger(this);
        this.dataFolder = new File(this.murderMystery.getDataFolder() + "/Addons/" + this.addonsName);
        this.configFile = new File(this.dataFolder, "config.yml");
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

    /**
     * 启用扩展
     */
    public abstract void onEnable();

    /**
     * 卸载扩展
     */
    public abstract void onDisable();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    public InputStream getResource(String filename) {
        return this.getClass().getClassLoader().getResourceAsStream(filename);
    }

    public boolean saveResource(String filename) {
        return this.saveResource(filename, false);
    }

    public boolean saveResource(String filename, boolean replace) {
        return this.saveResource(filename, filename, replace);
    }

    public boolean saveResource(String filename, String outputName, boolean replace) {
        Preconditions.checkArgument(filename != null && outputName != null, "Filename can not be null!");
        Preconditions.checkArgument(filename.trim().length() != 0 && outputName.trim().length() != 0, "Filename can not be empty!");
        File out = new File(this.dataFolder, outputName);
        if (!out.exists() || replace) {
            try (InputStream resource = getResource(filename)) {
                if (resource != null) {
                    File outFolder = out.getParentFile();
                    if (!outFolder.exists()) {
                        outFolder.mkdirs();
                    }
                    Utils.writeFile(out, resource);
                    return true;
                }
            } catch (IOException e) {
                Server.getInstance().getLogger().logException(e);
            }
        }
        return false;
    }

    public Config getConfig() {
        if (this.config == null) {
            this.config = new Config(configFile, 2);
        }
        return this.config;
    }

    public void saveConfig() {
        if (!this.getConfig().save()) {
            this.getLogger().critical("Could not save config to " + this.configFile.toString());
        }
    }

    public void saveDefaultConfig() {
        if (!this.configFile.exists()) {
            this.saveResource("config.yml", false);
        }
    }

    public void reloadConfig() {
        this.config = new Config(this.configFile);
        InputStream configStream = this.getResource("config.yml");
        if (configStream != null) {
            DumperOptions dumperOptions = new DumperOptions();
            dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml yaml = new Yaml(dumperOptions);
            try {
                this.config.setDefault(yaml.loadAs(Utils.readFile(this.configFile), LinkedHashMap.class));
            } catch (IOException e) {
                Server.getInstance().getLogger().logException(e);
            }
        }
    }

    public final File getDataFolder() {
        return this.dataFolder;
    }

    public final Server getServer() {
        return this.server;
    }

    public final MurderMystery getMurderMystery() {
        return this.murderMystery;
    }

    public final AddonsManager getAddonsManager() {
        return MurderMystery.getAddonsManager();
    }

    public final Logger getLogger() {
        return this.logger;
    }

    @Override
    public final boolean equals(Object obj) {
        if(obj instanceof AddonsBase){
            return ((AddonsBase) obj).getAddonsName().equals(this.getAddonsName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.addonsName);
    }

}
