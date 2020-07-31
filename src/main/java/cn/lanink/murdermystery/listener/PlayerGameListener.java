package cn.lanink.murdermystery.listener;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.RoomBase;
import cn.lanink.murdermystery.room.RoomClassicMode;
import cn.lanink.murdermystery.tasks.game.ScanTask;
import cn.lanink.murdermystery.tasks.game.SwordMoveTask;
import cn.lanink.murdermystery.utils.Language;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityShootBowEvent;
import cn.nukkit.event.entity.ItemSpawnEvent;
import cn.nukkit.event.entity.ProjectileLaunchEvent;
import cn.nukkit.event.inventory.InventoryPickupItemEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.potion.Effect;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.scheduler.Task;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

/**
 * 游戏监听器
 * @author lt_name
 */
public class PlayerGameListener implements Listener {

    private final MurderMystery murderMystery;
    private final Language language;

    public PlayerGameListener(MurderMystery murderMystery) {
        this.murderMystery = murderMystery;
        this.language = murderMystery.getLanguage();
    }

    /**
     * 生命实体射出箭 事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onShootBow(EntityShootBowEvent event) {
        if (event.isCancelled()) return;
        if (event.getEntity() instanceof Player) {
            Player player = ((Player) event.getEntity()).getPlayer();
            if (player == null || event.getProjectile() == null) {
                return;
            }
            RoomBase room = this.murderMystery.getRooms().getOrDefault(player.getLevel().getName(), null);
            if (room == null || room.getStatus() != 2) {
                return;
            }
            if (room.getPlayers(player) != 0 && room.getPlayers(player) != 3) {
                event.getProjectile().namedTag = new CompoundTag()
                        .putBoolean("isMurderItem", true)
                        .putInt("MurderType", 20);
                if (room.getPlayers(player) == 2) {
                    player.getInventory().addItem(Item.get(262, 0, 1));
                    return;
                }
            }
            //回收弓
            Server.getInstance().getScheduler().scheduleDelayedTask(this.murderMystery, new Task() {
                @Override
                public void onRun(int i) {
                    int j = 0; //箭的数量
                    boolean bow = false;
                    for (Item item : player.getInventory().slots.values()) {
                        if (item.getId() == 262) {
                            j += item.getCount();
                            continue;
                        }
                        if (item.getId() == 261) {
                            item.setDamage(0);
                            bow = true;
                        }
                    }
                    if (j < 1 && bow) {
                        player.getInventory().removeItem(Item.get(261, 0, 1));
                    }
                }
            }, 20);
        }
    }

    /**
     * 抛掷物被发射事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.isCancelled()) return;
        Entity entity = event.getEntity();
        if (entity == null || !this.murderMystery.getRooms().containsKey(entity.getLevel().getName()) ||
                this.murderMystery.getRooms().get(entity.getLevel().getName()).getStatus() != 2) {
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
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPickupItem(InventoryPickupItemEvent event) {
        if (event.isCancelled()) return;
        Level level = event.getItem() == null ? null : event.getItem().getLevel();
        if (level == null) return;
        RoomBase room = this.murderMystery.getRooms().getOrDefault(level.getName(), null);
        if (room == null) return;
        if (event.getInventory() != null && event.getInventory() instanceof PlayerInventory) {
            Player player = (Player) event.getInventory().getHolder();
            CompoundTag tag = event.getItem().getItem() == null ? null : event.getItem().getItem().getNamedTag();
            if (tag != null && tag.getBoolean("isMurderItem") && tag.getInt("MurderType") == 1) {
                if (room.getPlayers(player) != 1) {
                    event.setCancelled(true);
                    return;
                }
                if (room instanceof RoomClassicMode) {
                    ((RoomClassicMode) room).detectiveBow = null;
                }
                room.getPlayers().keySet().forEach(p -> p.sendMessage(this.language.commonPeopleBecomeDetective));
                room.getPlayers().put(player, 2);
                player.getInventory().addItem(Item.get(262, 0, 1));
            }
        }
    }

    /**
     * 玩家手持物品事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemHeld(PlayerItemHeldEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Item item = event.getItem();
        if (player == null || item == null) {
            return;
        }
        RoomBase room = this.murderMystery.getRooms().getOrDefault(player.getLevel().getName(), null);
        if (room == null || room.getGameMode().equals("infected")) return;
        CompoundTag tag = item.hasCompoundTag() ? item.getNamedTag() : null;
        if (room.getStatus() == 2 && room.isPlaying(player) && room.getPlayers(player) == 3) {
            if (tag != null && tag.getBoolean("isMurderItem") && tag.getInt("MurderType") == 2) {
                if (room.effectCD < 1) {
                    Effect effect = Effect.getEffect(1);
                    effect.setAmplifier(2);
                    effect.setVisible(false);
                    effect.setDuration(40);
                    player.addEffect(effect);
                    room.effectCD = 10;
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
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (player == null || block == null) {
            return;
        }
        RoomBase room = this.murderMystery.getRooms().getOrDefault(player.getLevel().getName(), null);
        if (room == null || !room.isPlaying(player)) {
            return;
        }
        if (event.getAction() == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK ||
                event.getAction() == PlayerInteractEvent.Action.LEFT_CLICK_AIR) {
            event.setCancelled(true);
            player.setAllowModifyWorld(false);
        }
        if (room.getStatus() == 2) {
            if (event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
                if (room.getPlayers(player) == 3 && !room.getGameMode().equals("infected")) {
                    CompoundTag tag = player.getInventory().getItemInHand() == null ? null : player.getInventory().getItemInHand().getNamedTag();
                    if (tag != null && tag.getBoolean("isMurderItem")) {
                        if (tag.getInt("MurderType") == 2) {
                            if (room.swordCD < 1) {
                                Server.getInstance().getScheduler().scheduleAsyncTask(this.murderMystery, new SwordMoveTask(room, player));
                                room.swordCD = 5;
                            }else {
                                player.sendMessage(this.language.useItemSwordCD);
                            }
                        }else if (tag.getInt("MurderType") == 3) {
                            if (room.scanCD < 1) {
                                Server.getInstance().getScheduler().scheduleAsyncTask(this.murderMystery, new ScanTask(room, player));
                                room.scanCD = 60;
                            }else {
                                player.sendMessage(this.language.useItemScanCD);
                            }
                        }
                    }
                }
                return;
            }
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
                            player.sendMessage(language.exchangeItem.replace("%name%", language.itemPotion));
                        }else {
                            player.sendMessage(language.exchangeUseGold.replace("%name%", language.itemPotion));
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
                                player.sendMessage(language.exchangeItem.replace("%name%", language.itemShieldWall));
                            }else {
                                player.sendMessage(language.exchangeItemsOnlyOne.replace("%name%", language.itemShieldWall));
                            }
                        }else {
                            player.sendMessage(language.exchangeUseGold.replace("%name%", language.itemShieldWall));
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
                            player.sendMessage(language.exchangeItem.replace("%name%", language.itemSnowball));
                        }else {
                            player.sendMessage(language.exchangeUseGold.replace("%name%", language.itemSnowball));
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
                player.sendMessage(this.language.quitRoom);
            }
        }
    }

    /**
     * 玩家使用消耗品事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemConsume(PlayerItemConsumeEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Item item = event.getItem();
        if (player == null || item == null || item.getNamedTag() == null) {
            return;
        }
        RoomBase room = this.murderMystery.getRooms().get(player.getLevel().getName());
        if (room == null || room.getStatus() != 2) {
            return;
        }
        CompoundTag tag = item.getNamedTag();
        if (room.isPlaying(player) &&
                tag.getBoolean("isMurderItem") &&
                tag.getInt("MurderType") == 21) {
            if (room.getPlayers(player) == 3) {
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
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Item item = event.getItem();
        Block block = event.getBlockReplace();
        if (player == null || item == null || block == null) {
            return;
        }
        Level level = player.getLevel();
        if (level == null || !this.murderMystery.getRooms().containsKey(level.getName())) {
            return;
        }
        RoomBase room = this.murderMystery.getRooms().get(level.getName());
        CompoundTag tag = item.getNamedTag();
        if (room.getStatus() == 2 && tag != null &&
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
                    Server.getInstance().getScheduler().scheduleDelayedTask(MurderMystery.getInstance(), new Task() {
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
        if (entityItem == null) return;
        RoomBase room = this.murderMystery.getRooms().get(entityItem.getLevel().getName());
        if (room == null) return;
        Item item = entityItem.getItem();
        CompoundTag tag = item.getNamedTag();
        if (tag != null && tag.getBoolean("isMurderItem") &&
                tag.getInt("MurderType") == 1) {
            if (room instanceof RoomClassicMode) {
                ((RoomClassicMode) room).detectiveBow = entityItem;
            }
            Server.getInstance().getScheduler().scheduleDelayedTask(this.murderMystery, new Task() {
                @Override
                public void onRun(int i) {
                    if (room.getStatus() != 2 || entityItem.isClosed()) return;
                    entityItem.setNameTag(language.itemDetectiveBow);
                    entityItem.setNameTagVisible(true);
                    entityItem.setNameTagAlwaysVisible(true);
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
        for (RoomBase room : this.murderMystery.getRooms().values()) {
            if (room.isPlaying(player)) {
                event.setRespawnPosition(room.getRandomSpawn().get(new Random().nextInt(room.getRandomSpawn().size())));
            }
        }
    }

    /**
     * 玩家执行命令事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player == null || event.getMessage() == null) return;
        RoomBase room = this.murderMystery.getRooms().getOrDefault(player.getLevel().getName(), null);
        if (room == null || !room.isPlaying(player)) {
            return;
        }
        if (event.getMessage().startsWith(this.murderMystery.getCmdUser(), 1) ||
                event.getMessage().startsWith(this.murderMystery.getCmdAdmin(), 1)) {
            return;
        }
        event.setCancelled(true);
        player.sendMessage(this.language.useCmdInRoom);
    }

    /**
     * 发送消息事件
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String string = event.getMessage();
        if (player == null || string == null) {
            return;
        }
        RoomBase room = this.murderMystery.getRooms().get(player.getLevel().getName());
        if (room == null || !room.isPlaying(player) || room.getStatus() != 2) return;
        if (room.getPlayers(player) == 0) {
            for (Player p : room.getPlayers().keySet()) {
                if (room.getPlayers(p) == 0) {
                    p.sendMessage(this.language.playerDeathChat
                            .replace("%player%", player.getName())
                            .replace("%message%", string));
                }
            }
        }else {
            for (Player p : room.getPlayers().keySet()) {
                p.sendMessage(this.language.playerChat
                        .replace("%player%", player.getName())
                        .replace("%message%", string));
            }
        }
        event.setMessage("");
        event.setCancelled(true);
    }

}
