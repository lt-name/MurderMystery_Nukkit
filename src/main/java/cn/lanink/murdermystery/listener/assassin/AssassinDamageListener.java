package cn.lanink.murdermystery.listener.assassin;

import cn.lanink.murdermystery.listener.BaseMurderMysteryListener;
import cn.lanink.murdermystery.room.assassin.AssassinModeRoom;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Sound;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.potion.Effect;

/**
 * @author lt_name
 */
public class AssassinDamageListener extends BaseMurderMysteryListener<AssassinModeRoom> {

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
            AssassinModeRoom room = this.getListenerRooms().get(damager.getLevel().getFolderName());
            if (room == null) {
                return;
            }
            event.setCancelled(true);
            if (room.isPlaying(damager) && room.isPlaying(player)) {
                Item item = damager.getInventory().getItemInHand();
                if (item != null && item.hasCompoundTag()) {
                    CompoundTag tag = item.getNamedTag();
                    if (tag.getBoolean("isMurderItem") && tag.getInt("MurderType") == 2) {
                        if (room.targetMap.get(damager) == player) { //杀死目标
                            damager.sendTitle("",
                                    this.murderMystery.getLanguage(damager).translateString("game_assassin_killTarget"));
                            damager.getOffhandInventory().clearAll();
                            room.targetWait.add(damager);
                            room.killCount.put(damager, room.killCount.getOrDefault(damager, 0) + 1);
                            Server.getInstance().getScheduler().scheduleDelayedTask(this.murderMystery,
                                    () -> room.assignTarget(damager), 60);
                            //TODO
                            player.sendTitle(this.murderMystery.getLanguage(player).translateString("deathTitle"),
                                    "你被刺客杀死了");
                            room.playerDeath(player);
                        }else if (room.targetMap.get(player) == damager) { //反杀
                            //TODO
                            damager.sendTitle("", "成功反杀");
                            room.killCount.put(damager, room.killCount.getOrDefault(damager, 0) + 1);
                            //TODO
                            player.sendTitle(this.murderMystery.getLanguage(player).translateString("deathTitle"),
                                    "你的目标捅了你一刀");
                            room.playerDeath(player);
                        }else { //杀错
                            damager.addEffect(Effect.getEffect(2).setAmplifier(10).setDuration(100).setVisible(false));//缓慢
                            Tools.playSound(damager, Sound.RANDOM_ANVIL_LAND);
                            //TODO
                            damager.sendTitle("", "§c你找错目标了！");
                            damager.getInventory().remove(Tools.getMurderMysteryItem(damager, 2));
                            Server.getInstance().getScheduler().scheduleDelayedTask(this.murderMystery,
                                    () -> damager.getInventory().setItem(2, Tools.getMurderMysteryItem(damager, 2)), 100);
                        }
                    }
                }
            }
        }
    }


}
