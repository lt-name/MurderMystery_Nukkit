package cn.lanink.murdermystery.listener.assassin;

import cn.lanink.murdermystery.listener.BaseMurderMysteryListener;
import cn.lanink.murdermystery.room.assassin.AssassinModeRoom;
import cn.lanink.murdermystery.room.base.PlayerIdentity;
import cn.lanink.murdermystery.room.base.RoomStatus;
import cn.lanink.murdermystery.tasks.game.SwordMoveTask;
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
@SuppressWarnings("unused")
public class AssassinGameListener extends BaseMurderMysteryListener<AssassinModeRoom> {

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
        AssassinModeRoom room = this.getListenerRooms().get(player.getLevel().getFolderName());
        if (room == null || !room.isPlaying(player)) {
            return;
        }
        if (room.getStatus() == RoomStatus.GAME &&
                event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_AIR &&
                room.getPlayers(player) == PlayerIdentity.ASSASSIN) {
            CompoundTag tag = player.getInventory().getItemInHand().getNamedTag();
            if (tag != null && tag.getBoolean("isMurderItem") && tag.getInt("MurderType") == 2) {
                if (room.killerSwordCD.getOrDefault(player, 0) < 1) {
                    room.killerSwordCD.put(player, 5);
                    Server.getInstance().getScheduler().scheduleAsyncTask(this.murderMystery,
                            new SwordMoveTask(room, player));
                }else {
                    player.sendMessage(this.murderMystery.getLanguage(player).translateString("useItemSwordCD"));
                }
            }
        }
    }


}
