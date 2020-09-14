package cn.lanink.murdermystery.listener.defaults;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.base.BaseRoom;
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
        if (player == null) {
            return;
        }
        this.murderMystery.getPlayerLanguage().put(player, player.getLoginChainData().getLanguageCode());
        if (this.murderMystery.getRooms().containsKey(player.getLevel().getFolderName())) {
            Server.getInstance().getScheduler().scheduleDelayedTask(this.murderMystery, new Task() {
                @Override
                public void onRun(int i) {
                    if (player.isOnline()) {
                        Tools.rePlayerState(player ,false);
                        SavePlayerInventory.restore(player);
                        player.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
                    }
                }
            }, 20);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        for (BaseRoom room : this.murderMystery.getRooms().values()) {
            if (room.isPlaying(player) || room.isSpectator(player)) {
                room.quitRoom(player);
            }
        }
        GuiCreate.UI_CACHE.remove(player);
        this.murderMystery.getPlayerLanguage().remove(player);
    }

    @EventHandler
    public void onPlayerTp(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        String fromLevel = event.getFrom().getLevel() == null ? null : event.getFrom().getLevel().getFolderName();
        String toLevel = event.getTo().getLevel()== null ? null : event.getTo().getLevel().getFolderName();
        if (player == null || fromLevel == null || toLevel == null) return;
        if (!fromLevel.equals(toLevel)) {
            LinkedHashMap<String, BaseRoom> room = this.murderMystery.getRooms();
            if (room.containsKey(fromLevel) &&
                    (room.get(fromLevel).isPlaying(player) || room.get(fromLevel).isSpectator(player))) {
                event.setCancelled(true);
                player.sendMessage(this.murderMystery.getLanguage(player).tpQuitRoomLevel);
            }else if (!player.isOp() && room.containsKey(toLevel) &&
                    !room.get(toLevel).isPlaying(player) && !room.get(toLevel).isSpectator(player)) {
                event.setCancelled(true);
                player.sendMessage(this.murderMystery.getLanguage(player).tpJoinRoomLevel);
            }
        }
    }

}
