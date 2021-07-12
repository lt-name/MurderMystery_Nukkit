package cn.lanink.murdermystery.addons.uishop;

import cn.lanink.gamecore.form.element.ResponseElementButton;
import cn.lanink.gamecore.form.windows.AdvancedFormWindowModal;
import cn.lanink.gamecore.form.windows.AdvancedFormWindowSimple;
import cn.lanink.murdermystery.addons.AddonsBase;
import cn.lanink.murdermystery.event.MurderMysteryRoomStartEvent;
import cn.lanink.murdermystery.form.FormCreate;
import cn.lanink.murdermystery.item.ItemManager;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.lanink.murdermystery.room.base.RoomStatus;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.Task;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author lt_name
 */
public class UiShop extends AddonsBase implements Listener {

    private ArrayList<String> items = new ArrayList<>();
    private final HashSet<Player> cache = new HashSet<>();

    @Override
    public void onEnable() {
        this.getMurderMystery().saveResource("Addons/UiShop/config.yml", false);
        this.items.addAll(this.getConfig().getStringList("items"));
        this.getAddonsManager().registerEvents(this, this);
        this.getLogger().info("§a加载完成！");
    }

    @Override
    public void onDisable() {
        this.items.clear();
        this.getLogger().info("§c已卸载！");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRoomStart(MurderMysteryRoomStartEvent event) {
        BaseRoom room = event.getRoom();
        Server.getInstance().getScheduler().scheduleDelayedTask(this.getMurderMystery(), new Task() {
            @Override
            public void onRun(int i) {
                String[] s = getConfig().get("UiShopItem", "347:0").split(":");
                Item item = Item.get(Integer.parseInt(s[0]), Integer.parseInt(s[1]), 1);
                item.setNamedTag(new CompoundTag().putBoolean("isMurderUiShop", true));
                item.setCustomName(getConfig().getString("UiShopItemName", "§a道具商店"));
                item.setLore(getConfig().getString("UiShopItemLore", "便携道具商店\n购买各种道具来帮助你获取胜利！").split("\n"));
                for (Player player : room.getPlayers().keySet()) {
                    player.getInventory().addItem(item);
                }
            }
        }, 1);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();
        if (player == null || item == null || item.getNamedTag() == null) {
            return;
        }
        BaseRoom room = getMurderMystery().getRooms().get(player.getLevel().getName());
        if (room != null && room.getStatus() == RoomStatus.GAME &&
                item.getNamedTag().getBoolean("isMurderUiShop") && !this.cache.contains(player)) {
            this.cache.add(player);
            this.showUiShop(player);
            event.setCancelled(true);
            Server.getInstance().getScheduler().scheduleDelayedTask(this.getMurderMystery(),
                    () -> cache.remove(player), 10);
        }
    }

    /**
     * 显示ui商店
     * @param player 玩家
     */
    public void showUiShop(Player player) {
        int x = 0;
        for (Item item : player.getInventory().getContents().values()) {
            if (item.getId() == 266) {
                x += item.getCount();
            }
        }
        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(FormCreate.PLUGIN_NAME,
                this.getConfig().getString("ShopMain", "§a你当前有 §e %gold% §a块金锭")
                        .replace("%gold%", x + ""));
        for (String s : items) {
            String[] item = s.split(":");
            simple.addButton(new ResponseElementButton(item[1] + "\n" +
                    this.getConfig().getString("ShopPrice", "价格: %gold% 块金锭").replace("%gold%", item[2])
            ).onClicked(p -> {
                AdvancedFormWindowModal modal = new AdvancedFormWindowModal(FormCreate.PLUGIN_NAME,
                        this.getConfig().getString("BuyOK", "§a确定要花费 §e %gold% §a块金锭购买 §e %item% §a？")
                                .replace("%gold%", item[2]).replace("%item%", item[1]),
                        this.getConfig().getString("ButtonOK", "§a购买"),
                        this.getConfig().getString("ButtonReturn", "§c返回"));
                        modal.onClickedTrue(cp2 -> {
                            int count = 0;
                            for (Item item1 : cp2.getInventory().getContents().values()) {
                                if (item1.getId() == 266) {
                                    count += item1.getCount();
                                }
                            }
                            if (count >= Integer.parseInt(item[2])) {
                                player.getInventory().removeItem(ItemManager.get(null, 266));
                                Tools.giveItem(player, Integer.parseInt(item[0]));
                                player.sendMessage(getConfig().getString("BuySuccess", "§a成功兑换到: §e %item% §a已发放到背包！")
                                        .replace("%item%", item[1]));
                            }else {
                                player.sendMessage(getConfig().getString("BuyFailure", "§a你的金锭数量不足！"));
                            }
                        });
                        modal.onClickedFalse(this::showUiShop);
                        p.showFormWindow(modal);
            }));
        }
        player.showFormWindow(simple);
    }

}
