package cn.lanink.murdermystery.addons.uishop;

import cn.lanink.murdermystery.addons.BaseAddons;

import java.util.ArrayList;

public class UiShop extends BaseAddons {

    public UiShop() {
        super("UiShop");
    }

    @Override
    public void onEnable() {
        this.murderMystery.saveResource("Addons/UiShop/config.yml", false);
        ArrayList<String> items = (ArrayList<String>) this.getConfig().getStringList("items");
        getServer().getPluginManager().registerEvents(new UiShopListener(items), murderMystery);
        getLogger().info("§aUiShop 扩展已加载！");
    }

    @Override
    public void onDisable() {

    }

}
