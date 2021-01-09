package cn.lanink.murdermystery.listener.defaults;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.form.GuiCreate;
import cn.lanink.murdermystery.tasks.admin.SetRoomTask;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.inventory.InventoryClickEvent;
import cn.nukkit.event.inventory.InventoryPickupItemEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;

import java.util.List;

/**
 * @author lt_name
 */
public class SetRoomListener implements Listener {

    private final MurderMystery murderMystery;

    public SetRoomListener(MurderMystery murderMystery) {
        this.murderMystery = murderMystery;
    }

    @EventHandler
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        if (event.getItem().namedTag.getBoolean("cannotPickup")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = event.getPlayer();
        if (player != null && this.murderMystery.setRoomTask.containsKey(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();
        if (player == null || item == null) {
            return;
        }
        if (this.murderMystery.setRoomTask.containsKey(player) && item.hasCompoundTag()) {
            Block block = event.getBlock();
            if (block.getFloorX() == 0 && block.getFloorY() == 0 && block.getFloorZ() == 0) {
                return;
            }
            event.setCancelled(true);
            Config config = this.murderMystery.getRoomConfig(player.getLevel());
            SetRoomTask task = this.murderMystery.setRoomTask.get(player);
            switch (item.getNamedTag().getInt("MurderMysteryItemType")) {
                case 110: //上一步
                    switch (task.getSetRoomSchedule()) {
                        case 10:
                            return;
                        case 50:
                            if (task.isAutoNext()) {
                                config.remove("waitTime");
                                config.remove("gameTime");
                                config.remove("victoryScore");
                            }
                            break;
                        case 60:
                            if (task.isAutoNext()) {
                                config.remove("minPlayers");
                                config.remove("maxPlayers");
                            }
                            break;
                    }
                    task.setRoomSchedule(task.getBackRoomSchedule());
                    break;
                case 111: //下一步
                    task.setRoomSchedule(task.getNextRoomSchedule());
                    break;
                case 112: //保存设置
                    task.setRoomSchedule(70);
                    break;
                case 113: //设置
                    String pos = block.getFloorX() + ":" + (block.getFloorY() + 1) + ":" + block.getFloorZ();
                    switch (task.getSetRoomSchedule()) {
                        case 10:
                            GuiCreate.sendAdminModeMenu(player);
                            break;
                        case 20:
                            config.set("waitSpawn", pos);
                            task.setRoomSchedule(20);
                            break;
                        case 30:
                            List<String> randomSpawns = config.getStringList("randomSpawn");
                            randomSpawns.add(pos);
                            config.set("randomSpawn", randomSpawns);
                            player.sendMessage(this.murderMystery.getLanguage().translateString("adminAddRandomSpawn"));
                            task.setRoomSchedule(task.getNextRoomSchedule());
                            break;
                        case 40:
                            List<String> goldSpawns = config.getStringList("goldSpawn");
                            goldSpawns.add(pos);
                            config.set("goldSpawn", goldSpawns);
                            player.sendMessage(this.murderMystery.getLanguage().translateString("adminAddGoldSpawn"));
                            task.setRoomSchedule(task.getNextRoomSchedule());
                            if (task.isAutoNext()) {
                                GuiCreate.sendAdminTimeMenu(player);
                            }
                            break;
                        case 50:
                            GuiCreate.sendAdminTimeMenu(player);
                            break;
                        case 60:
                            GuiCreate.sendAdminPlayersMenu(player);
                            break;
                    }
                    break;
            }
        }
    }

}
