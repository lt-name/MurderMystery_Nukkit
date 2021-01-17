package cn.lanink.murdermystery.listener.classic;

import cn.lanink.murdermystery.listener.BaseMurderMysteryListener;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.lanink.murdermystery.room.base.PlayerIdentity;
import cn.lanink.murdermystery.room.base.RoomStatus;
import cn.lanink.murdermystery.room.classic.ClassicModeRoom;
import cn.lanink.murdermystery.tasks.game.ScanTask;
import cn.lanink.murdermystery.tasks.game.SwordMoveTask;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerItemHeldEvent;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.potion.Effect;

/**
 * 游戏监听器
 *
 * @author lt_name
 */
@SuppressWarnings("unused")
public class ClassicGameListener extends BaseMurderMysteryListener<ClassicModeRoom> {

    /**
     * 玩家手持物品事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();
        if (player == null || item == null) {
            return;
        }
        BaseRoom room = this.getListenerRooms().get(player.getLevel().getFolderName());
        if (room == null) {
            return;
        }
        CompoundTag tag = item.hasCompoundTag() ? item.getNamedTag() : null;
        if (room.getStatus() == RoomStatus.GAME && room.isPlaying(player) && room.getPlayers(player) == PlayerIdentity.KILLER) {
            if (tag != null && tag.getBoolean("isMurderItem") && tag.getInt("MurderType") == 2) {
                if (room.killerEffectCD < 1) {
                    Effect effect = Effect.getEffect(1);
                    effect.setAmplifier(2);
                    effect.setVisible(false);
                    effect.setDuration(40);
                    player.addEffect(effect);
                    room.killerEffectCD = 10;
                }
            }else {
                player.removeEffect(1);
            }
        }
    }

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
                switch (tag.getInt("MurderType")) {
                    case 2:
                        if (room.killerSwordCD.getOrDefault(player, 0) < 1) {
                            room.killerSwordCD.put(player, 5);
                            Server.getInstance().getScheduler().scheduleAsyncTask(this.murderMystery,
                                    new SwordMoveTask(room, player));
                        }else {
                            player.sendMessage(this.murderMystery.getLanguage(player).translateString("useItemSwordCD"));
                        }
                        break;
                    case 3:
                        if (room.killerScanCD < 1) {
                            Server.getInstance().getScheduler().scheduleTask(this.murderMystery,
                                    new ScanTask(this.murderMystery, room, player));
                            room.killerScanCD = 60;
                        }else {
                            player.sendMessage(this.murderMystery.getLanguage(player).translateString("useItemScanCD"));
                        }
                        break;
                }
            }
        }
    }

}
