package cn.lanink.murdermystery.listener.assassin;

import cn.lanink.murdermystery.item.ItemManager;
import cn.lanink.murdermystery.listener.BaseMurderMysteryListener;
import cn.lanink.murdermystery.room.assassin.AssassinModeRoom;
import cn.lanink.murdermystery.room.base.RoomStatus;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * @author lt_name
 */
@SuppressWarnings("unused")
public class AssassinDamageListener extends BaseMurderMysteryListener<AssassinModeRoom> {

    /**
     * 实体受到另一实体伤害事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player player = (Player) event.getEntity();
            if (damager == null || player == null) {
                return;
            }
            AssassinModeRoom room = this.getListenerRooms().get(damager.getLevel().getFolderName());
            if (room == null) {
                return;
            }
            event.setCancelled(true);
            if (room.isPlaying(damager) && room.isPlaying(player) && room.getStatus() == RoomStatus.GAME) {
                Item item = damager.getInventory().getItemInHand();
                if (item != null && item.hasCompoundTag()) {
                    CompoundTag tag = item.getNamedTag();
                    if (tag.getBoolean(ItemManager.IS_MURDER_MYSTERY_TAG) && tag.getInt(ItemManager.INTERNAL_ID_TAG) == 2) {
                        room.playerDamage(damager, player);
                    }
                }
            }
        }
    }


}
