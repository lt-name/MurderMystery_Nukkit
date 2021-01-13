package cn.lanink.murdermystery.listener.defaults;

import cn.lanink.murdermystery.entity.EntityPlayerCorpse;
import cn.lanink.murdermystery.entity.EntitySword;
import cn.lanink.murdermystery.listener.BaseMurderMysteryListener;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.entity.EntityDamageEvent;

/**
 * @author lt_name
 */
@SuppressWarnings("unused")
public class DefaultDamageListener extends BaseMurderMysteryListener<BaseRoom> {

    /**
     * 伤害事件
     * @param event 事件
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            BaseRoom room = this.getListenerRooms().get(player.getLevel().getFolderName());
            if (room == null) {
                return;
            }
            //虚空 游戏开始前拉回 游戏中判断玩家死亡
            if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                if (room.getStatus() == BaseRoom.ROOM_STATUS_GAME) {
                    room.playerDeath(player);
                }else {
                    player.teleport(room.getWaitSpawn());
                }
            }
            event.setCancelled(true);
        }else if (event.getEntity() instanceof EntityPlayerCorpse ||
                event.getEntity() instanceof EntitySword) {
            event.setCancelled(true);
        }
    }

}
