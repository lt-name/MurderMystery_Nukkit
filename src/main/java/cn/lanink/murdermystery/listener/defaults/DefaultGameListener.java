package cn.lanink.murdermystery.listener.defaults;

import cn.lanink.gamecore.utils.Language;
import cn.lanink.murdermystery.item.ItemManager;
import cn.lanink.murdermystery.listener.BaseMurderMysteryListener;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.lanink.murdermystery.room.base.PlayerIdentity;
import cn.lanink.murdermystery.room.base.RoomStatus;
import cn.lanink.murdermystery.room.classic.ClassicModeRoom;
import cn.lanink.murdermystery.room.infected.InfectedModeRoom;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityShootBowEvent;
import cn.nukkit.event.entity.ItemSpawnEvent;
import cn.nukkit.event.entity.ProjectileLaunchEvent;
import cn.nukkit.event.inventory.InventoryClickEvent;
import cn.nukkit.event.inventory.InventoryOpenEvent;
import cn.nukkit.event.inventory.InventoryPickupItemEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerItemConsumeEvent;
import cn.nukkit.event.player.PlayerRespawnEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import cn.nukkit.network.protocol.LevelSoundEventPacketV1;
import cn.nukkit.network.protocol.LevelSoundEventPacketV2;
import cn.nukkit.network.protocol.SetSpawnPositionPacket;
import cn.nukkit.potion.Effect;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.scheduler.Task;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

/**
 * @author lt_name
 */
@SuppressWarnings("unused")
public class DefaultGameListener extends BaseMurderMysteryListener<BaseRoom> {

    /**
     * 生命实体射出箭 事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (event.getProjectile() == null) {
                return;
            }
            BaseRoom room = this.getListenerRooms().get(player.getLevel().getFolderName());
            if (room == null || room.getStatus() != RoomStatus.GAME) {
                return;
            }
            if (event.getForce() < 2) {
                event.setCancelled(true);
                return;
            }
            if (room.getPlayers(player) == PlayerIdentity.COMMON_PEOPLE || room.getPlayers(player) == PlayerIdentity.DETECTIVE) {
                event.getProjectile().namedTag = new CompoundTag()
                        .putBoolean("isMurderItem", true)
                        .putInt("MurderType", 20);
                if (room.getPlayers(player) == PlayerIdentity.DETECTIVE) {
                    if (!(room instanceof InfectedModeRoom)) {
                        player.getInventory().addItem(Item.get(262, 0, 1));
                    }
                    return;
                }
            }
            //回收弓
            Server.getInstance().getScheduler().scheduleDelayedTask(this.murderMystery, () -> {
                int arrow = 0;
                boolean hasBow = false;
                for (Item item : player.getInventory().slots.values()) {
                    if (item.getId() == 262) {
                        arrow += item.getCount();
                    }else if (item.getId() == 261) {
                        hasBow = true;
                    }
                }
                if (arrow < 1 && hasBow) {
                    player.getInventory().removeItem(Item.get(261, 0, 1));
                }
            }, 1);
        }
    }

    /**
     * 抛掷物被发射事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Entity entity = event.getEntity();
        if (entity == null) {
            return;
        }
        BaseRoom room = this.getListenerRooms().get(entity.getLevel().getFolderName());
        if (room == null || room.getStatus() != RoomStatus.GAME) {
            return;
        }
        if (entity.getNetworkId() == 81) {
            event.getEntity().namedTag = new CompoundTag()
                    .putBoolean("isMurderItem", true)
                    .putInt("MurderType", 23);
        }

    }

    /**
     * 收起掉落的物品时
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPickupItem(InventoryPickupItemEvent event) {
        Item item = event.getItem().getItem();
        Level level = event.getItem().getLevel();
        BaseRoom room = this.getListenerRooms().get(level.getFolderName());
        if (room == null) {
            return;
        }
        if (room.getStatus() != RoomStatus.GAME) {
            event.setCancelled(true);
            return;
        }
        if (event.getInventory() != null && event.getInventory() instanceof PlayerInventory) {
            Player player = (Player) event.getInventory().getHolder();
            if (player.getGamemode() != 0) {
                event.setCancelled(true);
                return;
            }
            if (item.getId() == Item.GOLD_INGOT) {
                event.setCancelled(true);
                event.getItem().close();

                Item playerHasItem = player.getInventory().getItem(8);
                if (playerHasItem.getId() == Item.GOLD_INGOT) {
                    item.setCount(playerHasItem.getCount() + 1);
                }
                player.getInventory().setItem(8, item);
                if (playerHasItem.getId() != 0 && playerHasItem.getId() != Item.GOLD_INGOT) {
                    player.getInventory().addItem(playerHasItem);
                }

                Tools.playSound(player, Sound.RANDOM_ORB);
                return;
            }

            CompoundTag tag = item.getNamedTag();
            if (tag != null && tag.getBoolean("isMurderItem") && tag.getInt("MurderType") == 1) {
                if (room.getPlayers(player) != PlayerIdentity.COMMON_PEOPLE) {
                    event.setCancelled(true);
                    return;
                }
                if (room instanceof ClassicModeRoom) {
                    room.detectiveBow = null;
                }
                for (Player p : room.getPlayers().keySet()) {
                    p.sendMessage(this.murderMystery.getLanguage(p).translateString("commonPeopleBecomeDetective"));
                    p.getInventory().remove(ItemManager.get(p, 345));
                }
                room.getPlayers().put(player, PlayerIdentity.DETECTIVE);
                player.getInventory().addItem(Item.get(262, 0, 1));
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
        if (room == null || (!room.isPlaying(player) && !room.isSpectator(player))) {
            return;
        }
        if (event.getAction() == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK ||
                event.getAction() == PlayerInteractEvent.Action.LEFT_CLICK_AIR) {
            event.setCancelled(true);
            player.setAllowModifyWorld(false);
        }
        if (room.getStatus() == RoomStatus.GAME && room.isPlaying(player) && player.getGamemode() == 0) {
            Language language = this.murderMystery.getLanguage(player);
            int id1 = block.getId();
            int id2 = block.getLevel().getBlock(block.getFloorX(), block.getFloorY() - 1, block.getFloorZ()).getId();
            if (id1 == 118 && id2 == 138) {
                Server.getInstance().getScheduler().scheduleAsyncTask(this.murderMystery, new AsyncTask() {
                    @Override
                    public void onRun() {
                        int x = 0; //金锭数量
                        for (Item item : player.getInventory().getContents().values()) {
                            if (item.getId() == 266) {
                                x += item.getCount();
                            }
                        }
                        if (x > 0) {
                            player.getInventory().removeItem(Item.get(266, 0, 1));
                            Tools.giveItem(player, 21);
                            player.sendMessage(language.translateString("exchangeItem")
                                    .replace("%name%", language.translateString("itemPotion")));
                        }else {
                            player.sendMessage(language.translateString("exchangeUseGold")
                                    .replace("%name%", language.translateString("itemPotion")));
                        }
                    }
                });
                event.setCancelled(true);
            }else if (id1 == 116 && id2 == 169) {
                Server.getInstance().getScheduler().scheduleAsyncTask(this.murderMystery, new AsyncTask() {
                    @Override
                    public void onRun() {
                        int x = 0; //金锭数量
                        boolean notHave = true;
                        for (Item item : player.getInventory().getContents().values()) {
                            if (item.getId() == 266) {
                                x += item.getCount();
                                continue;
                            }
                            CompoundTag tag = item.getNamedTag();
                            if (tag != null && tag.getBoolean("isMurderItem") && tag.getInt("MurderType") == 22) {
                                notHave = false;
                            }
                        }
                        if (x > 0) {
                            if (notHave) {
                                player.getInventory().removeItem(Item.get(266, 0, 1));
                                Tools.giveItem(player, 22);
                                player.sendMessage(language.translateString("exchangeItem")
                                        .replace("%name%", language.translateString("itemShieldWall")));
                            }else {
                                player.sendMessage(language.translateString("exchangeItemsOnlyOne")
                                        .replace("%name%", language.translateString("itemShieldWall")));
                            }
                        }else {
                            player.sendMessage(language.translateString("exchangeUseGold")
                                    .replace("%name%", language.translateString("itemShieldWall")));
                        }
                    }
                });
                event.setCancelled(true);
            }else if (id1 == 80 && id2 == 79) {
                Server.getInstance().getScheduler().scheduleAsyncTask(this.murderMystery, new AsyncTask() {
                    @Override
                    public void onRun() {
                        int x = 0; //金锭数量
                        for (Item item : player.getInventory().getContents().values()) {
                            if (item.getId() == 266) {
                                x += item.getCount();
                            }
                        }
                        if (x > 0) {
                            player.getInventory().removeItem(Item.get(266, 0, 1));
                            Tools.giveItem(player, 23);
                            player.sendMessage(language.translateString("exchangeItem")
                                    .replace("%name%", language.translateString("itemSnowball")));
                        }else {
                            player.sendMessage(language.translateString("exchangeUseGold")
                                    .replace("%name%", language.translateString("itemSnowball")));
                        }
                    }
                });
                event.setCancelled(true);
            }
        }else if (event.getItem() != null && event.getItem().getNamedTag() != null) {
            CompoundTag tag = event.getItem().getNamedTag();
            if (tag.getBoolean("isMurderItem") && tag.getInt("MurderType") == 10) {
                event.setCancelled(true);
                room.quitRoom(player);
            }
        }
    }

    /**
     * 玩家使用消耗品事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();
        if (player == null || item == null || item.getNamedTag() == null) {
            return;
        }
        BaseRoom room = this.getListenerRooms().get(player.getLevel().getFolderName());
        if (room == null || room.getStatus() != RoomStatus.GAME) {
            return;
        }
        CompoundTag tag = item.getNamedTag();
        if (room.isPlaying(player) &&
                tag.getBoolean("isMurderItem") &&
                tag.getInt("MurderType") == 21) {
            if (room.getPlayers(player) == PlayerIdentity.KILLER) {
                Effect effect = Effect.getEffect(2);
                effect.setDuration(100);
                player.addEffect(effect);
            }else {
                int random = new Random().nextInt(100);
                Effect effect = null;
                if (random >= 70) {
                    effect = Effect.getEffect(1); //速度
                }else if (random >= 60) {
                    effect = Effect.getEffect(16); //夜视
                }else if (random >= 50) {
                    effect = Effect.getEffect(14); //隐身
                }else if (random >= 30) {
                    effect = Effect.getEffect(8); //跳跃提升2
                    effect.setAmplifier(2);
                }else if (random >= 10) {
                    effect = Effect.getEffect(2); //缓慢
                }
                if (effect != null) {
                    effect.setDuration(100);
                    player.addEffect(effect);
                }
            }
        }
    }

    /**
     * 方块放置事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();
        Block block = event.getBlockReplace();
        if (player == null || item == null || block == null) {
            return;
        }
        Level level = player.getLevel();
        if (level == null) {
            return;
        }
        BaseRoom room = this.getListenerRooms().get(level.getFolderName());
        if (room == null) {
            return;
        }
        CompoundTag tag = item.getNamedTag();
        if (room.getStatus() == RoomStatus.GAME && tag != null &&
                tag.getBoolean("isMurderItem") && tag.getInt("MurderType") == 22) {
            level.addSound(block, Sound.RANDOM_ANVIL_USE);
            //>315 <45  X
            //>135 <225 X
            double yaw = player.getYaw();
            Server.getInstance().getScheduler().scheduleAsyncTask(this.murderMystery, new AsyncTask() {
                @Override
                public void onRun() {
                    LinkedList<Vector3> blockList = new LinkedList<>();
                    blockList.add(block);
                    for (int y = block.getFloorY() ; y < (block.getFloorY() + 6); y++) {
                        if ((yaw > 315 || yaw < 45) || (yaw > 135 && yaw < 225)) {
                            for (int x = block.getFloorX() ; x < (block.getFloorX() + 4); x++) {
                                Vector3 vector3 = new Vector3(x, y, block.getFloorZ());
                                if (level.getBlock(vector3).getId() == 0) {
                                    level.setBlock(vector3, Block.get(241, 3));
                                    blockList.add(vector3);
                                }
                            }
                            for (int x = block.getFloorX() ; x > (block.getFloorX() - 4); x--) {
                                Vector3 vector3 = new Vector3(x, y, block.getFloorZ());
                                if (level.getBlock(vector3).getId() == 0) {
                                    level.setBlock(vector3, Block.get(241, 3));
                                    blockList.add(vector3);
                                }
                            }
                        }else {
                            for (int z = block.getFloorZ() ; z < (block.getFloorZ() + 4); z++) {
                                Vector3 vector3 = new Vector3(block.getFloorX(), y, z);
                                if (level.getBlock(vector3).getId() == 0) {
                                    level.setBlock(vector3, Block.get(241, 3));
                                    blockList.add(vector3);
                                }
                            }
                            for (int z = block.getFloorZ() ; z > (block.getFloorZ() - 4); z--) {
                                Vector3 vector3 = new Vector3(block.getFloorX(), y, z);
                                if (level.getBlock(vector3).getId() == 0) {
                                    level.setBlock(vector3, Block.get(241, 3));
                                    blockList.add(vector3);
                                }
                            }
                        }
                    }
                    room.placeBlocks.add(blockList);
                    Server.getInstance().getScheduler().scheduleDelayedTask(murderMystery, new Task() {
                        @Override
                        public void onRun(int i) {
                            room.placeBlocks.remove(blockList);
                            Iterator<Vector3> it = blockList.iterator();
                            while (it.hasNext()) {
                                level.setBlock(it.next(), Block.get(0));
                                it.remove();
                            }
                        }
                    }, 100);
                }
            });
        }else {
            event.setCancelled(true);
        }
    }

    /**
     * 掉落物生成事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onItemSpawn(ItemSpawnEvent event) {
        final EntityItem entityItem = event.getEntity();
        if (entityItem == null) {
            return;
        }
        BaseRoom room = this.getListenerRooms().get(entityItem.getLevel().getFolderName());
        if (room == null) {
            return;
        }
        Item item = entityItem.getItem();
        CompoundTag tag = item.getNamedTag();
        if (tag != null && tag.getBoolean("isMurderItem") &&
                tag.getInt("MurderType") == 1) {
            if (room instanceof ClassicModeRoom) {
                room.detectiveBow = entityItem;
            }

            SetSpawnPositionPacket pk = new SetSpawnPositionPacket();
            pk.spawnType = SetSpawnPositionPacket.TYPE_WORLD_SPAWN;
            pk.x = entityItem.getFloorX();
            pk.y = entityItem.getFloorY();
            pk.z = entityItem.getFloorZ();
            pk.dimension = 0;
            room.getPlayers().keySet().forEach(p -> p.dataPacket(pk));

            Server.getInstance().getScheduler().scheduleDelayedTask(this.murderMystery, () -> {
                if (room.getStatus() != RoomStatus.GAME || entityItem.isClosed()) {
                    return;
                }
                entityItem.setNameTag(murderMystery.getLanguage(null).translateString("itemDetectiveBow"));
                entityItem.setNameTagVisible(true);
                entityItem.setNameTagAlwaysVisible(true);

                for (Player player : room.getPlayers().keySet()) {
                    player.getInventory().addItem(ItemManager.get(player, 345));
                }
            }, 100);
        }
    }

    /**
     * 玩家重生事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        for (BaseRoom room : this.getListenerRooms().values()) {
            if (room.isPlaying(player) || room.isSpectator(player)) {
                event.setRespawnPosition(room.getRandomSpawn().get(new Random().nextInt(room.getRandomSpawn().size())));
            }
        }
    }

    /**
     * 玩家打开库存事件
     * @param event 事件
     */
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = event.getPlayer();
        if (player == null || event.getInventory() == null) {
            return;
        }
        BaseRoom room = this.getListenerRooms().get(player.getLevel().getFolderName());
        if (room == null || (!room.isPlaying(player) && !room.isSpectator(player))) {
            return;
        }
        switch (event.getInventory().getType()) {
            case UI:
            case PLAYER:
                break;
            default:
                event.setCancelled(true);
                break;
        }
    }

    /**
     * 玩家点击背包栏格子事件
     * @param event 事件
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = event.getPlayer();
        if (player == null || event.getInventory() == null) {
            return;
        }
        BaseRoom room = this.getListenerRooms().get(player.getLevel().getFolderName());
        if (room == null || (!room.isPlaying(player) && !room.isSpectator(player))) {
            return;
        }
        if ((event.getSourceItem().hasCompoundTag() && event.getSourceItem().getNamedTag().getBoolean("cannotClickOnInventory")) ||
                (event.getHeldItem().hasCompoundTag() && event.getHeldItem().getNamedTag().getBoolean("cannotClickOnInventory"))) {
            event.setCancelled(true);
            return;
        }
        switch (event.getInventory().getType()) {
            case UI:
            case PLAYER:
                break;
            default:
                event.setCancelled(true);
                break;
        }
    }

    /**
     * 数据包接收事件
     * 不接收已死亡玩家的操作声音
     *
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDataPacketReceive(DataPacketReceiveEvent event) {
        if (event.getPacket() instanceof LevelSoundEventPacket ||
                event.getPacket() instanceof LevelSoundEventPacketV1 ||
                event.getPacket() instanceof LevelSoundEventPacketV2) {
            Player player = event.getPlayer();
            BaseRoom room = this.getListenerRooms().get(player.getLevel().getFolderName());
            if (room == null || (!room.isPlaying(player) && !room.isSpectator(player))) {
                return;
            }
            if (room.getPlayers(player) == PlayerIdentity.DEATH) {
                player.dataPacket(event.getPacket());
                event.setCancelled(true);
            }else if (room instanceof InfectedModeRoom) {
                //拦截未复活玩家的声音
                InfectedModeRoom infectedModeRoom = (InfectedModeRoom) room;
                if (infectedModeRoom.getPlayerRespawnTime().getOrDefault(player, 0) > 0) {
                    player.dataPacket(event.getPacket());
                    event.setCancelled(true);
                }
            }
        }
    }

}
