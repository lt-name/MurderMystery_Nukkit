package cn.lanink.murdermystery.addons.manager.logger;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.addons.AddonsBase;
import cn.lanink.murdermystery.addons.manager.AddonsManager;
import cn.nukkit.Server;
import cn.nukkit.utils.LogLevel;
import cn.nukkit.utils.Logger;

/**
 * @author lt_name
 */
public class AddonsLogger implements Logger {

    private final String addonsName;

    public AddonsLogger(AddonsManager addonsManager) {
        String prefix = MurderMystery.getInstance().getDescription().getPrefix();
        String murderMystery = prefix != null ? "[" + prefix + "] " : "[" + MurderMystery.getInstance().getDescription().getName() + "] ";
        this.addonsName = murderMystery + "[AddonsManager] ";
    }

    public AddonsLogger(AddonsBase addonsBase) {
        String prefix = MurderMystery.getInstance().getDescription().getPrefix();
        String murderMystery = prefix != null ? "[" + prefix + "] " : "[" + MurderMystery.getInstance().getDescription().getName() + "] ";
        this.addonsName = murderMystery + "[AddonsManager] [" + addonsBase.getAddonsName() + "] ";
    }

    public void emergency(String message) {
        this.log(LogLevel.EMERGENCY, message);
    }

    public void alert(String message) {
        this.log(LogLevel.ALERT, message);
    }

    public void critical(String message) {
        this.log(LogLevel.CRITICAL, message);
    }

    public void error(String message) {
        this.log(LogLevel.ERROR, message);
    }

    public void warning(String message) {
        this.log(LogLevel.WARNING, message);
    }

    public void notice(String message) {
        this.log(LogLevel.NOTICE, message);
    }

    public void info(String message) {
        this.log(LogLevel.INFO, message);
    }

    public void debug(String message) {
        this.log(LogLevel.DEBUG, message);
    }

    public void log(LogLevel level, String message) {
        Server.getInstance().getLogger().log(level, this.addonsName + message);
    }

    public void emergency(String message, Throwable t) {
        this.log(LogLevel.EMERGENCY, message, t);
    }

    public void alert(String message, Throwable t) {
        this.log(LogLevel.ALERT, message, t);
    }

    public void critical(String message, Throwable t) {
        this.log(LogLevel.CRITICAL, message, t);
    }

    public void error(String message, Throwable t) {
        this.log(LogLevel.ERROR, message, t);
    }

    public void warning(String message, Throwable t) {
        this.log(LogLevel.WARNING, message, t);
    }

    public void notice(String message, Throwable t) {
        this.log(LogLevel.NOTICE, message, t);
    }

    public void info(String message, Throwable t) {
        this.log(LogLevel.INFO, message, t);
    }

    public void debug(String message, Throwable t) {
        this.log(LogLevel.DEBUG, message, t);
    }

    public void log(LogLevel level, String message, Throwable t) {
        Server.getInstance().getLogger().log(level, this.addonsName + message, t);
    }

}
