package cn.lanink.murdermystery.listener;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.event.MurderPlayerDamageEvent;
import cn.lanink.murdermystery.event.MurderPlayerDeathEvent;
import cn.lanink.murdermystery.room.Room;
import cn.lanink.murdermystery.utils.Language;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.potion.Effect;
import me.onebone.economyapi.EconomyAPI;

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
            Player player1 = (Player) event.getDamager();
            Player player2 = (Player) event.getEntity();
            if (player1 == null || player2 == null) {
                return;
            }
            Room room = this.murderMystery.getRooms().getOrDefault(player1.getLevel().getName(), null);
            if (room != null) {
                if (room.isPlaying(player1) && room.getPlayerMode(player1) == 3 &&
                        room.isPlaying(player2) && room.getPlayerMode(player2) != 0) {
                    if (player1.getInventory().getItemInHand() != null) {
                        CompoundTag tag = player1.getInventory().getItemInHand().getNamedTag();
                        if (tag != null && tag.getBoolean("isMurderItem") && tag.getInt("MurderType") == 2) {
                            Server.getInstance().getPluginManager().callEvent(new MurderPlayerDamageEvent(room, player1, player2));
                        }
                    }
                }
                event.setCancelled(true);
            }
        }
    }

    /**
     * 实体受到另一个子实体伤害事件
     * @param event 事件
     */
    @EventHandler
    public void onDamageByChild(EntityDamageByChildEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player player1 = ((Player) event.getDamager()).getPlayer();
            Player player2 = ((Player) event.getEntity()).getPlayer();
            if (player1 == player2 || event.getChild() == null || event.getChild().namedTag == null) {
                return;
            }
            Level level = player1.getLevel();
            Room room = this.murderMystery.getRooms().getOrDefault(level.getName(), null);
            if (room == null || room.getMode() != 2) {
                return;
            }
            Entity child = event.getChild();
            if (child.namedTag.getBoolean("isMurderItem")) {
                if (child.namedTag.getInt("MurderType") == 20) {
                    Server.getInstance().getPluginManager().callEvent(new MurderPlayerDamageEvent(room, player1, player2));
                }else if (child.namedTag.getInt("MurderType") == 23) {
                    Tools.addSound(player2, Sound.RANDOM_ANVIL_LAND);
                    player2.sendMessage(this.language.damageSnowball);
                    Effect effect = Effect.getEffect(2);
                    effect.setAmplifier(2);
                    effect.setDuration(40);
                    player2.addEffect(effect);
                }
            }
            event.setCancelled();
        }
    }

    /**
     * 玩家被攻击事件(符合游戏条件的攻击)
     * @param event 事件
     */
    @EventHandler
    public void onPlayerDamage(MurderPlayerDamageEvent event) {
        if (!event.isCancelled()) {
            Player player1 = event.getDamage();
            Player player2 = event.getPlayer();
            Room room = event.getRoom();
            if (player1 == null || player2 == null || room == null) {
                return;
            }
            //攻击者是杀手
            if (room.getPlayerMode(player1) == 3) {
                player1.sendMessage(this.language.killPlayer);
                player2.sendTitle(this.language.deathTitle,
                        this.language.deathByKillerSubtitle, 20, 60, 20);
            }else { //攻击者是平民或侦探
                if (room.getPlayerMode(player2) == 3) {
                    player1.sendMessage(this.language.killKiller);
                    int money = this.murderMystery.getConfig().getInt("击杀杀手额外奖励", 0);
                    if (money > 0) {
                        EconomyAPI.getInstance().addMoney(player1, money);
                        player1.sendMessage(this.language.victoryKillKillerMoney.replace("%money%", money + ""));
                    }
                    player2.sendTitle(this.language.deathTitle,
                            this.language.killerDeathSubtitle, 10, 20, 20);
                } else {
                    player1.sendTitle(this.language.deathTitle,
                            this.language.deathByDamageTeammateSubtitle, 20, 60, 20);
                    player2.sendTitle(this.language.deathTitle,
                            this.language.deathByTeammateSubtitle, 20, 60, 20);
                    Server.getInstance().getPluginManager().callEvent(new MurderPlayerDeathEvent(room, player1));
                }
            }
            Server.getInstance().getPluginManager().callEvent(new MurderPlayerDeathEvent(room, player2));
        }
    }

}
