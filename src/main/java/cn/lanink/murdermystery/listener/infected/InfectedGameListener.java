package cn.lanink.murdermystery.listener.infected;

import cn.lanink.murdermystery.listener.BaseMurderMysteryListener;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.lanink.murdermystery.room.base.PlayerIdentity;
import cn.lanink.murdermystery.room.base.RoomStatus;
import cn.lanink.murdermystery.room.infected.InfectedModeRoom;
import cn.lanink.murdermystery.tasks.game.ScanTask;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * @author lt_name
 */
public class InfectedGameListener extends BaseMurderMysteryListener<InfectedModeRoom> {

    /**
     * 玩家点击事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (player == null || block == null) {
            return;
        }
        BaseRoom room = this.getListenerRooms().get(player.getLevel().getFolderName());
        if (room == null || !room.isPlaying(player)) {
            return;
        }
        if (room.getStatus() == RoomStatus.GAME &&
                event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_AIR &&
                room.getPlayers(player) == PlayerIdentity.KILLER) {
            CompoundTag tag = player.getInventory().getItemInHand() == null ? null : player.getInventory().getItemInHand().getNamedTag();
            if (tag != null && tag.getBoolean("isMurderItem")) {
                if (tag.getInt("MurderType") == 3) {
                    if (room.killerScanCD.getOrDefault(player, 0) < 1) {
                        Server.getInstance().getScheduler().scheduleTask(this.murderMystery,
                                new ScanTask(this.murderMystery, room, player));
                        room.killerScanCD.put(player, 30);
                    }else {
                        player.sendMessage(this.murderMystery.getLanguage(player).translateString("useItemScanCD"));
                    }
                }
            }
        }
    }

}
