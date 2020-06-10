package cn.lanink.murdermystery.addons.uishop;

import cn.lanink.murdermystery.addons.BaseAddons;
import cn.nukkit.utils.Config;

import java.util.ArrayList;

public class UiShop extends BaseAddons {

    public UiShop() {
        super("UiShop");
    }

    @Override
    public void onEnable() {
        murderMystery.saveResource("Addons/UiShop/config.yml", "/Addons/UiShop/config.yml", false);
        ArrayList<String> items = (ArrayList<String>) new Config(
                murderMystery.getDataFolder() + "/Addons/UiShop/config.yml", 2).getStringList("items");
        getServer().getPluginManager().registerEvents(new UiShopListener(items), murderMystery);
        murderMystery.getLogger().info("§aUiShop 扩展已加载！");
    }

    @Override
    public void onDisable() {

    }
}
