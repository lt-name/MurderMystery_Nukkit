package cn.lanink.murdermystery.listener.classic;

import cn.lanink.murdermystery.item.ItemManager;
import cn.lanink.murdermystery.listener.BaseMurderMysteryListener;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.lanink.murdermystery.room.base.PlayerIdentity;
import cn.lanink.murdermystery.room.base.RoomStatus;
import cn.lanink.murdermystery.room.classic.ClassicModeRoom;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.level.Sound;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.potion.Effect;

/**
 * @author lt_name
 */
@SuppressWarnings("unused")
public class ClassicDamageListener extends BaseMurderMysteryListener<ClassicModeRoom> {

    /**
     * 实体受到另一实体伤害事件
     * @param event 事件
     */
    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player player = (Player) event.getEntity();
            if (damager == null || player == null) {
                return;
            }
            BaseRoom room = this.getListenerRooms().get(damager.getLevel().getFolderName());
            if (room == null) {
                return;
            }
            event.setCancelled(true);
            if (room.getStatus() != RoomStatus.GAME) {
                return;
            }
            if (event instanceof EntityDamageByChildEntityEvent) {
                EntityDamageByChildEntityEvent event1 = (EntityDamageByChildEntityEvent) event;
                damager = (Player) event1.getDamager();
                player = (Player) event1.getEntity();
                Entity child = event1.getChild();
                if (child == null || child.namedTag == null) {
                    return;
                }
                if (child.namedTag.getBoolean(ItemManager.IS_MURDER_MYSTERY_TAG)) {
                    if (child.namedTag.getInt(ItemManager.INTERNAL_ID_TAG) == 20) {
                        room.playerDamage(damager, player);
                    }else if (child.namedTag.getInt(ItemManager.INTERNAL_ID_TAG) == 23) {
                        Tools.playSound(player, Sound.RANDOM_ANVIL_LAND);
                        player.sendMessage(this.murderMystery.getLanguage(player).translateString("damageSnowball"));
                        Effect effect = Effect.getEffect(2);
                        effect.setAmplifier(2);
                        effect.setDuration(40);
                        player.addEffect(effect);
                    }
                }
            }else {
                if (room.isPlaying(damager) && room.getPlayers(damager) == PlayerIdentity.KILLER &&
                        room.isPlaying(player) && room.getPlayers(player) != PlayerIdentity.DEATH) {
                    if (damager.getInventory().getItemInHand() != null) {
                        CompoundTag tag = damager.getInventory().getItemInHand().getNamedTag();
                        if (tag != null && tag.getBoolean(ItemManager.IS_MURDER_MYSTERY_TAG) && tag.getInt(ItemManager.INTERNAL_ID_TAG) == 2) {
                            room.playerDamage(damager, player);
                        }
                    }
                }
            }
        }
    }

}
