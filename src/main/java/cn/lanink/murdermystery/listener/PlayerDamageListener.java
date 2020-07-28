package cn.lanink.murdermystery.listener;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.entity.EntityPlayerCorpse;
import cn.lanink.murdermystery.room.RoomBase;
import cn.lanink.murdermystery.utils.Language;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.Sound;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.potion.Effect;

public class PlayerDamageListener implements Listener {

    private final MurderMystery murderMystery;
    private final Language language;

    public PlayerDamageListener(MurderMystery murderMystery) {
        this.murderMystery = murderMystery;
        this.language = murderMystery.getLanguage();
    }

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
            RoomBase room = this.murderMystery.getRooms().getOrDefault(damager.getLevel().getName(), null);
            if (room == null) return;
            event.setCancelled(true);
            if (event instanceof  EntityDamageByChildEntityEvent) {
                EntityDamageByChildEntityEvent event1 = (EntityDamageByChildEntityEvent) event;
                damager = (Player) event1.getDamager();
                player = (Player) event1.getEntity();
                Entity child = event1.getChild();
                if (child == null || child.namedTag == null) return;
                if (child.namedTag.getBoolean("isMurderItem")) {
                    if (child.namedTag.getInt("MurderType") == 20) {
                        room.playerDamage(damager, player);
                        //Server.getInstance().getPluginManager().callEvent(new MurderPlayerDamageEvent(room, damager, player));
                    }else if (child.namedTag.getInt("MurderType") == 23) {
                        Tools.addSound(player, Sound.RANDOM_ANVIL_LAND);
                        player.sendMessage(this.language.damageSnowball);
                        Effect effect = Effect.getEffect(2);
                        effect.setAmplifier(2);
                        effect.setDuration(40);
                        player.addEffect(effect);
                    }
                }
            }else {
                if (room.isPlaying(damager) && room.getPlayers(damager) == 3 &&
                        room.isPlaying(player) && room.getPlayers(player) != 0) {
                    if (damager.getInventory().getItemInHand() != null) {
                        CompoundTag tag = damager.getInventory().getItemInHand().getNamedTag();
                        if (tag != null && tag.getBoolean("isMurderItem") && tag.getInt("MurderType") == 2) {
                            room.playerDamage(damager, player);
                        }
                    }
                }
            }
        }
    }

    /**
     * 伤害事件
     * @param event 事件
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            RoomBase room = this.murderMystery.getRooms().getOrDefault(player.getLevel().getName(), null);
            if (room == null) return;
            //虚空 游戏开始前拉回 游戏中判断玩家死亡
            if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                if (room.getStatus() == 2) {
                    room.playerDeath(player);
                }else {
                    player.teleport(room.getWaitSpawn());
                }
            }
            event.setCancelled(true);
        }else if (event.getEntity() instanceof EntityPlayerCorpse) {
            event.setCancelled(true);
        }
    }

}
