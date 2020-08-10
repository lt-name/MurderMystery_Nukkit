package cn.lanink.murdermystery.listener;

import cn.lanink.murdermystery.MurderMystery;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.ItemFrameDropItemEvent;
import cn.nukkit.event.entity.EntityExplodeEvent;
import cn.nukkit.event.entity.ProjectileHitEvent;
import cn.nukkit.event.inventory.CraftItemEvent;
import cn.nukkit.event.inventory.InventoryPickupArrowEvent;
import cn.nukkit.event.inventory.StartBrewEvent;
import cn.nukkit.event.level.ChunkUnloadEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.event.player.PlayerDropItemEvent;
import cn.nukkit.event.player.PlayerFoodLevelChangeEvent;
import cn.nukkit.event.player.PlayerGameModeChangeEvent;
import cn.nukkit.level.Level;


/**
 * 游戏世界保护
 * @author lt_name
 */
public class RoomLevelProtection implements Listener {

    private final MurderMystery murderMystery;

    public RoomLevelProtection(MurderMystery murderMystery) {
        this.murderMystery = murderMystery;
    }

    /**
     * 物品合成事件
     * @param event 事件
     */
    @EventHandler
    public void onCraft(CraftItemEvent event) {
        Level level = event.getPlayer() == null ? null : event.getPlayer().getLevel();
        if (level != null && this.murderMystery.getRooms().containsKey(level.getName())) {
            event.setCancelled();
        }
    }

    /**
     * 开始酿造事件
     * @param event 事件
     */
    @EventHandler
    public void onStartBrew(StartBrewEvent event) {
        Level level = event.getBrewingStand() == null ? null : event.getBrewingStand().getLevel();
        if (level != null && this.murderMystery.getRooms().containsKey(level.getName())) {
            event.setCancelled();
        }
    }

    /**
     * 方块破坏事件
     * @param event 事件
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        Level level = event.getPlayer().getLevel();
        if (level != null && this.murderMystery.getRooms().containsKey(level.getName())) {
            event.setCancelled();
            player.setAllowModifyWorld(false);
        }
    }

    /**
     * 实体爆炸事件
     * @param event 事件
     */
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Level level = event.getEntity() == null ? null : event.getEntity().getLevel();
        if (level != null && this.murderMystery.getRooms().containsKey(level.getName())) {
            event.setCancelled();
        }
    }

    /**
     * 物品展示框丢出事件
     * @param event 事件
     */
    @EventHandler
    public void onFrameDropItem(ItemFrameDropItemEvent event) {
        Level level = event.getItemFrame() == null ? null : event.getItemFrame().getLevel();
        if (level != null && this.murderMystery.getRooms().containsKey(level.getName())) {
            event.setCancelled();
        }
    }

    /**
     * 饥饿值变化事件
     * @param event 事件
     */
    @EventHandler
    public void onFoodLevelChange(PlayerFoodLevelChangeEvent event) {
        Level level = event.getPlayer() == null ? null : event.getPlayer().getLevel();
        if (level != null && this.murderMystery.getRooms().containsKey(level.getName())) {
            event.setCancelled();
        }
    }

    /**
     * 丢出物品事件
     * @param event 事件
     */
    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        Level level = event.getPlayer() == null ? null : event.getPlayer().getLevel();
        if (level != null && this.murderMystery.getRooms().containsKey(level.getName())) {
            event.setCancelled();
        }
    }

    /**
     * 收起发射出去的箭事件
     * @param event 事件
     */
    @EventHandler
    public void onPickupArrow(InventoryPickupArrowEvent event) {
        Level level = event.getArrow() == null ? null : event.getArrow().getLevel();
        if (level != null && this.murderMystery.getRooms().containsKey(level.getName())) {
            event.setCancelled();
        }
    }

    /**
     * 当一个抛射物击中物体时
     * @param event 事件
     */
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Level level = event.getEntity() == null ? null : event.getEntity().getLevel();
        if (level != null && this.murderMystery.getRooms().containsKey(level.getName())) {
            event.getEntity().close();
        }
    }

    /**
     * 玩家死亡事件
     * @param event 事件
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Level level = event.getEntity() == null ? null : event.getEntity().getLevel();
        if (level != null && this.murderMystery.getRooms().containsKey(level.getName())) {
            event.setKeepInventory(true);
            event.setKeepExperience(true);
        }
    }

    /**
     * 玩家游戏模式改变事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Level level = event.getPlayer() == null ? null : event.getPlayer().getLevel();
        if (level != null && this.murderMystery.getRooms().containsKey(level.getName())) {
            event.setCancelled(false);
        }
    }

    /**
     * 区块卸载事件
     *
     * @param event 事件
     */
    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (event.getLevel() != null && this.murderMystery.getRooms().containsKey(event.getLevel().getName())) {
            for (Entity entity : event.getChunk().getEntities().values()) {
                if (!(entity instanceof Player)) {
                    entity.close();
                }
            }
        }
    }

}
