package cn.lanink.murdermystery.addons.uishop;

import cn.lanink.murdermystery.addons.AddonsBase;
import cn.lanink.murdermystery.event.MurderMysteryRoomStartEvent;
import cn.lanink.murdermystery.room.RoomBase;
import cn.lanink.murdermystery.ui.GuiCreate;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.scheduler.Task;

import java.util.ArrayList;
import java.util.LinkedList;

public class UiShop extends AddonsBase implements Listener {

    private static final int DLC_UI_SHOP = 1111856485;
    private static final int DLC_UI_SHOP_OK = 1111856486;
    private ArrayList<String> items;
    private final LinkedList<Player> cache = new LinkedList<>();

    @Override
    public void onEnable() {
        getMurderMystery().saveResource("Addons/UiShop/config.yml", false);
        this.items = (ArrayList<String>) this.getConfig().getStringList("items");
        getAddonsManager().registerEvents(this, this);
        getLogger().info("§a加载完成！");
    }

    @Override
    public void onDisable() {

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRoomStart(MurderMysteryRoomStartEvent event) {
        String[] s = getConfig().get("UiShopItem", "347:0").split(":");
        Item item = Item.get(Integer.parseInt(s[0]), Integer.parseInt(s[1]), 1);
        item.setNamedTag(new CompoundTag().putBoolean("isMurderUiShop", true));
        item.setCustomName(getConfig().getString("UiShopItemName", "§a道具商店"));
        item.setLore(getConfig().getString("UiShopItemLore", "便携道具商店\n购买各种道具来帮助你获取胜利！").split("\n"));
        for (Player player : event.getRoom().getPlayers().keySet()) {
            player.getInventory().addItem(item);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();
        if (player == null || item == null || item.getNamedTag() == null) return;
        RoomBase room = getMurderMystery().getRooms().getOrDefault(player.getLevel().getName(), null);
        if (room != null && room.getStatus() == 2 &&
                item.getNamedTag().getBoolean("isMurderUiShop") && !this.cache.contains(player)) {
            this.cache.add(player);
            this.showUiShop(player);
            event.setCancelled(true);
            Server.getInstance().getScheduler().scheduleDelayedTask(getMurderMystery(), new Task() {
                @Override
                public void onRun(int i) {
                    cache.remove(player);
                }
            }, 10);
        }
    }

    /**
     * 显示ui商店
     * @param player 玩家
     */
    public void showUiShop(Player player) {
        Server.getInstance().getScheduler().scheduleAsyncTask(getMurderMystery(), new AsyncTask() {
            @Override
            public void onRun() {
                int x = 0;
                for (Item item : player.getInventory().getContents().values()) {
                    if (item.getId() == 266) {
                        x += item.getCount();
                    }
                }
                FormWindowSimple simple = new FormWindowSimple(GuiCreate.PLUGIN_NAME,
                        getConfig().getString("ShopMain", "§a你当前有 §e %gold% §a块金锭")
                                .replace("%gold%", x + ""));
                for (String s : items) {
                    String[] item = s.split(":");
                    simple.addButton(new ElementButton(item[1]));
                }
                player.showFormWindow(simple, DLC_UI_SHOP);
            }
        });
    }

    @EventHandler
    public void onFormResponded(PlayerFormRespondedEvent event) {
        Player player = event.getPlayer();
        if (player == null || event.getWindow() == null || event.getResponse() == null) {
            return;
        }
        if (event.getWindow() instanceof FormWindowSimple) {
            if (event.getFormID() == DLC_UI_SHOP) {
                FormWindowSimple simple = (FormWindowSimple) event.getWindow();
                int id = simple.getResponse().getClickedButtonId();
                if (items.get(id) != null) {
                    String[] item = items.get(id).split(":");
                    FormWindowModal modal = new FormWindowModal(GuiCreate.PLUGIN_NAME,
                            "§7§k\"" + simple.getResponse().getClickedButtonId() + "\" §r" +
                            getConfig().getString("BuyOK", "§a确定要花费 §e %gold% §a块金锭购买 §e %item% §a？")
                                            .replace("%gold%", item[2]).replace("%item%", item[1]),
                            getConfig().getString("ButtonOK", "§a购买"),
                            getConfig().getString("ButtonReturn", "§c返回"));
                    player.showFormWindow(modal, DLC_UI_SHOP_OK);
                }
            }
        }else if (event.getWindow() instanceof FormWindowModal) {
            if (event.getFormID() == DLC_UI_SHOP_OK) {
                FormWindowModal modal = (FormWindowModal) event.getWindow();
                if (modal.getResponse().getClickedButtonId() == 0) {
                    Server.getInstance().getScheduler().scheduleAsyncTask(getMurderMystery(), new AsyncTask() {
                        @Override
                        public void onRun() {
                            String[] s = modal.getContent().split("\"");
                            int id = Integer.parseInt(s[1]);
                            if (items.get(id) != null) {
                                int x = 0;
                                for (Item item : player.getInventory().getContents().values()) {
                                    if (item.getId() == 266) {
                                        x += item.getCount();
                                    }
                                }
                                String[] item = items.get(id).split(":");
                                if (x >= Integer.parseInt(item[2])) {
                                    player.getInventory().removeItem(Item.get(266, 0, Integer.parseInt(item[2])));
                                    Tools.giveItem(player, Integer.parseInt(item[0]));
                                    player.sendMessage(getConfig().getString("BuySuccess", "§a成功兑换到: §e %item% §a已发放到背包！")
                                            .replace("%item%", item[1]));
                                }else {
                                    player.sendMessage(getConfig().getString("BuyFailure", "§a你的金锭数量不足！"));
                                }
                            }
                        }
                    });
                }else {
                    this.showUiShop(player);
                }
            }
        }
    }

}
