package cn.lanink.murdermystery.listener;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.RoomBase;
import cn.lanink.murdermystery.ui.GuiCreate;
import cn.lanink.murdermystery.utils.SavePlayerInventory;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.scheduler.Task;

import java.util.LinkedHashMap;

/**
 * 玩家进入/退出服务器 或传送到其他世界时，退出房间
 */
public class PlayerJoinAndQuit implements Listener {

    private final MurderMystery murderMystery;

    public PlayerJoinAndQuit(MurderMystery murderMystery) {
        this.murderMystery = murderMystery;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player != null && this.murderMystery.getRooms().containsKey(player.getLevel().getName())) {
            Server.getInstance().getScheduler().scheduleDelayedTask(this.murderMystery, new Task() {
                @Override
                public void onRun(int i) {
                    if (player.isOnline()) {
                        Tools.rePlayerState(player ,false);
                        SavePlayerInventory.restore(player);
                        player.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
                    }
                }
            }, 120);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        for (RoomBase room : this.murderMystery.getRooms().values()) {
            if (room.isPlaying(player)) {
                room.quitRoom(player);
            }
        }
        this.murderMystery.getScoreboard().delCache(player);
        GuiCreate.UI_CACHE.remove(player);
    }

    @EventHandler
    public void onPlayerTp(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        String fromLevel = event.getFrom().getLevel() == null ? null : event.getFrom().getLevel().getName();
        String toLevel = event.getTo().getLevel()== null ? null : event.getTo().getLevel().getName();
        if (player == null || fromLevel == null || toLevel == null) return;
        if (!fromLevel.equals(toLevel)) {
            LinkedHashMap<String, RoomBase> room = this.murderMystery.getRooms();
            if (room.containsKey(fromLevel) && room.get(fromLevel).isPlaying(player)) {
                event.setCancelled(true);
                player.sendMessage(this.murderMystery.getLanguage().tpQuitRoomLevel);
            }else if (!player.isOp() && room.containsKey(toLevel) &&
                    !room.get(toLevel).isPlaying(player)) {
                event.setCancelled(true);
                player.sendMessage(this.murderMystery.getLanguage().tpJoinRoomLevel);
            }
        }
    }

}
