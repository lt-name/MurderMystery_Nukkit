package cn.lanink.murdermystery.addons;

import cn.lanink.murdermystery.MurderMystery;
import cn.nukkit.Server;

public abstract class BaseAddons {

    private final Server server = Server.getInstance();
    protected MurderMystery murderMystery = MurderMystery.getInstance();
    protected String addonsName;

    public BaseAddons(String addonsName) {
        this.addonsName = addonsName;
    }

    public String getAddonsName() {
        return this.addonsName;
    }

    public abstract void onEnable();

    public abstract void onDisable();

    public Server getServer() {
        return this.server;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof BaseAddons){
            return ((BaseAddons) obj).getAddonsName().equals(this.getAddonsName());
        }
        return false;
    }

}
