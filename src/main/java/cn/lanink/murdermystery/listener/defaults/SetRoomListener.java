package cn.lanink.murdermystery.listener.defaults;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.tasks.admin.SetRoomTask;
import cn.lanink.murdermystery.utils.FormHelper;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.inventory.InventoryClickEvent;
import cn.nukkit.event.inventory.InventoryPickupItemEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;

import java.util.HashSet;
import java.util.List;

/**
 * @author lt_name
 */
@SuppressWarnings("unused")
public class SetRoomListener implements Listener {

    private final MurderMystery murderMystery;
    private final HashSet<Player> cache = new HashSet<>();

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
        if (this.murderMystery.setRoomTask.containsKey(player) && item.hasCompoundTag() && !this.cache.contains(player)) {
            Block block = event.getBlock();
            if (block.getFloorX() == 0 && block.getFloorY() == 0 && block.getFloorZ() == 0) {
                return;
            }
            this.cache.add(player);
            Server.getInstance().getScheduler().scheduleDelayedTask(this.murderMystery,
                    () -> this.cache.remove(player), 10);
            event.setCancelled(true);
            Config config = this.murderMystery.getRoomConfig(player.getLevel());
            SetRoomTask task = this.murderMystery.setRoomTask.get(player);
            String pos = block.getFloorX() + ":" + (block.getFloorY() + 1) + ":" + block.getFloorZ();
            switch (item.getNamedTag().getInt("MurderMysteryItemType")) {
                case 110: //上一步
                    switch (task.getSetRoomSchedule()) {
                        case 9:
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
                        default:
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
                    switch (task.getSetRoomSchedule()) {
                        case 9:
                            FormHelper.sendAdminRoomNameMenu(player);
                            break;
                        case 10:
                            FormHelper.sendAdminModeMenu(player);
                            break;
                        case 20:
                            config.set("waitSpawn", pos);
                            task.setRoomSchedule(task.getNextRoomSchedule());
                            break;
                        case 30:
                            List<String> randomSpawns = config.getStringList("randomSpawn");
                            randomSpawns.add(pos);
                            config.set("randomSpawn", randomSpawns);
                            player.sendMessage(this.murderMystery.getLanguage().translateString("adminAddRandomSpawn"));
                            break;
                        case 40:
                            List<String> goldSpawns = config.getStringList("goldSpawn");
                            goldSpawns.add(pos);
                            config.set("goldSpawn", goldSpawns);
                            player.sendMessage(this.murderMystery.getLanguage().translateString("adminAddGoldSpawn"));
                            break;
                        case 50:
                            FormHelper.sendAdminMoreMenu(player);
                            break;
                    }
                    break;
                case 114: //删除
                    switch (task.getSetRoomSchedule()) {
                        case 30:
                            List<String> randomSpawns = config.getStringList("randomSpawn");
                            randomSpawns.remove(pos);
                            config.set("randomSpawn", randomSpawns);
                            break;
                        case 40:
                            List<String> goldSpawns = config.getStringList("goldSpawn");
                            goldSpawns.remove(pos);
                            config.set("goldSpawn", goldSpawns);
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    }

}
