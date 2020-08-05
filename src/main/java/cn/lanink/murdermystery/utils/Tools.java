package cn.lanink.murdermystery.utils;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.entity.EntityPlayerCorpse;
import cn.lanink.murdermystery.entity.EntitySword;
import cn.lanink.murdermystery.entity.EntityText;
import cn.lanink.murdermystery.room.RoomBase;
import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.entity.item.EntityFirework;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemFirework;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.Sound;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.PlaySoundPacket;
import cn.nukkit.network.protocol.PlayerSkinPacket;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.DyeColor;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.lang.reflect.Field;
import java.util.List;


public class Tools {

    /**
     * 获取字符串房间模式
     * @param room 房间
     * @return 房间模式
     */
    public static String getStringRoomMode(RoomBase room) {
        switch (room.getGameMode()) {
            case "classic":
                return MurderMystery.getInstance().getLanguage().Classic;
            case "infected":
                return MurderMystery.getInstance().getLanguage().Infected;
            default:
                return room.getGameMode();
        }
    }

    /**
     * 执行命令
     * @param player 玩家
     * @param cmds 命令
     */
    public static void cmd(Player player, List<String> cmds) {
        if (player == null || cmds == null || cmds.size() == 0) {
            return;
        }
        for (String s : cmds) {
            String[] cmd = s.split("&");
            if ((cmd.length > 1) && (cmd[1].equals("con"))) {
                Server.getInstance().dispatchCommand(new ConsoleCommandSender(), cmd[0].replace("@p", player.getName()));
            } else {
                Server.getInstance().dispatchCommand(player, cmd[0].replace("@p", player.getName()));
            }
        }
    }

    /**
     * 给玩家道具
     * @param player 玩家
     * @param tagNumber 物品编号
     */
    public static void giveItem(Player player, int tagNumber) {
        switch (tagNumber) {
            case 1:
                player.getInventory().setItem(1, getMurderItem(tagNumber));
                player.getInventory().setItem(2, Item.get(262, 0, 1));
                break;
            case 2:
                player.getInventory().setItem(1, getMurderItem(tagNumber));
                player.getInventory().setItem(2, getMurderItem(3));
                break;
            case 10:
                player.getInventory().setItem(8, getMurderItem(tagNumber));
                break;
            case 21:
            case 22:
            case 23:
                player.getInventory().addItem(getMurderItem(tagNumber));
                break;
        }
    }

    /**
     * 根据编号获取物品
     * @param tagNumber 道具编号
     * @return 物品
     */
    public static Item getMurderItem(int tagNumber) {
        Item item;
        Language language = MurderMystery.getInstance().getLanguage();
        switch (tagNumber) {
            case 1:
                item = Item.get(261, 0, 1);
                item.setNamedTag(new CompoundTag()
                        .putBoolean("isMurderItem", true)
                        .putInt("MurderType", 1));
                item.setCustomName(language.itemDetectiveBow);
                item.setLore(language.itemDetectiveBowLore.split("\n"));
                return item;
            case 2:
                item = Item.get(267, 0, 1);
                item.setNamedTag(new CompoundTag()
                        .putBoolean("isMurderItem", true)
                        .putInt("MurderType", 2));
                item.setCustomName(language.itemKillerSword);
                item.setLore(language.itemKillerSwordLore.split("\n"));
                return item;
            case 3:
                item = Item.get(395, 0, 1);
                item.setNamedTag(new CompoundTag()
                        .putBoolean("isMurderItem", true)
                        .putInt("MurderType", 3));
                item.setCustomName(language.itemScan);
                item.setLore(language.itemScanLore.split("\n"));
                return item;
            case 10:
                item = Item.get(324, 0, 1);
                item.setNamedTag(new CompoundTag()
                        .putBoolean("isMurderItem", true)
                        .putInt("MurderType", 10));
                item.setCustomName(language.itemQuitRoom);
                item.setLore(language.itemQuitRoomLore.split("\n"));
                return item;
            case 20:
                item = Item.get(262, 0, 1);
                item.setNamedTag(new CompoundTag()
                        .putBoolean("isMurderItem", true)
                        .putInt("MurderType", 20));
                return item;
            case 21:
                item = Item.get(373, 0, 1);
                item.setNamedTag(new CompoundTag()
                        .putBoolean("isMurderItem", true)
                        .putInt("MurderType", 21));
                item.setCustomName(language.itemPotion);
                item.setLore(language.itemPotionLore.split("\n"));
                return item;
            case 22:
                item = Item.get(241, 3, 1);
                item.setNamedTag(new CompoundTag()
                        .putBoolean("isMurderItem", true)
                        .putInt("MurderType", 22));
                item.setCustomName(language.itemShieldWall);
                item.setLore(language.itemShieldWallLore.split("\n"));
                return item;
            case 23:
                item = Item.get(332, 0, 1);
                item.setNamedTag(new CompoundTag()
                        .putBoolean("isMurderItem", true)
                        .putInt("MurderType", 23));
                item.setCustomName(language.itemSnowball);
                item.setLore(language.itemSnowballLore.split("\n"));
                return item;
        }
        return null;
    }

    /**
     * 设置Human实体皮肤
     * @param human 实体
     * @param skin 皮肤
     */
    public static void setHumanSkin(EntityHuman human, Skin skin) {
        setHumanSkin(human, skin, false);
    }

    public static void setHumanSkin(EntityHuman human, Skin skin, boolean needACK) {
        human.setSkin(skin);
        if (human.getLevel() != null) {
            for (Player player : human.getLevel().getPlayers().values()) {
                Server.getInstance().getScheduler().scheduleAsyncTask(MurderMystery.getInstance(), new AsyncTask() {
                    @Override
                    public void onRun() {
                        setHumanSkin(human, skin, player, needACK, 0);
                    }
                });
            }
        }
    }

    private static void setHumanSkin(EntityHuman human, Skin skin, Player player, boolean needACK, int retransmission) {
        PlayerSkinPacket packet = new PlayerSkinPacket();
        packet.skin = skin;
        packet.newSkinName = skin.getSkinId();
        packet.oldSkinName = human.getSkin().getSkinId();
        packet.uuid = human.getUniqueId();
        int id = player.directDataPacket(packet, true);
        if (needACK && retransmission < 3) {
            Server.getInstance().getScheduler().scheduleDelayedTask(MurderMystery.getInstance(), new Task() {
                @Override
                public void onRun(int i) {
                    try {
                        Field field = player.getClass().getDeclaredField("needACK");
                        field.setAccessible(true);
                        Int2ObjectOpenHashMap<Boolean> o = (Int2ObjectOpenHashMap<Boolean>) field.get(player);
                        if (o == null || !o.get(id)) {
                            setHumanSkin(human, skin, player, true, retransmission + 1);
                        }
                    } catch (Exception ignored) {

                    }
                }
            }, 60, true);
        }
    }

    /**
     * 设置玩家是否隐身
     * @param player 玩家
     * @param invisible 是否隐身
     */
    public static void setPlayerInvisible(Player player, boolean invisible) {
        player.setDataFlag(0, 5, invisible);
    }

    /**
     * 重置玩家状态
     * @param player 玩家
     * @param joinRoom 是否为加入房间
     */
    public static void rePlayerState(Player player, boolean joinRoom) {
        player.setGamemode(0);
        player.removeAllEffects();
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setLevel(player.getFoodData().getMaxLevel());
        if (joinRoom) {
            player.setNameTag("");
            player.setNameTagVisible(false);
            player.setNameTagAlwaysVisible(false);
            player.setAllowModifyWorld(false);
        }else {
            player.setNameTag(player.getName());
            player.setNameTagVisible(true);
            player.setNameTagAlwaysVisible(true);
            setPlayerInvisible(player, false);
            player.setAllowModifyWorld(true);
        }
        player.setAdventureSettings((new AdventureSettings(player)).set(AdventureSettings.Type.ALLOW_FLIGHT, false));
    }

    public static void sendMessage(RoomBase roomBase, String string) {
        for (Player player : roomBase.getPlayers().keySet()) {
            player.sendMessage(string);
        }
    }

    /**
     * 添加声音
     * @param room 房间
     * @param sound 声音
     */
    public static void addSound(RoomBase room, Sound sound) {
        for (Player player : room.getPlayers().keySet()) {
            addSound(player, sound);
        }
    }

    public static void addSound(Player player, Sound sound) {
        PlaySoundPacket packet = new PlaySoundPacket();
        packet.name = sound.getSound();
        packet.volume = 1.0F;
        packet.pitch = 1.0F;
        packet.x = player.getFloorX();
        packet.y = player.getFloorY();
        packet.z = player.getFloorZ();
        player.dataPacket(packet);
    }

    public static void cleanEntity(Level level) {
        cleanEntity(level, false);
    }

    /**
     * 清理实体
     * @param level 世界
     * @param cleanAll 是否清理全部
     */
    public static void cleanEntity(Level level, boolean cleanAll) {
        for (Entity entity : level.getEntities()) {
            if (!(entity instanceof Player)) {
                if (entity instanceof EntityPlayerCorpse || entity instanceof EntitySword || entity instanceof EntityText) {
                    if (cleanAll) {
                        entity.close();
                    }
                }else if (entity instanceof EntityItem) {
                    Item item = ((EntityItem) entity).getItem();
                    CompoundTag tag = item.getNamedTag();
                    if (tag != null && tag.getBoolean("isMurderItem") &&
                            tag.getInt("MurderType") == 1) {
                        if (cleanAll) {
                            entity.close();
                        }
                    }else {
                        entity.close();
                    }
                }else {
                    entity.close();
                }
            }
        }
    }

    /**
     * 获取底部 Y
     * 调用前应判断非空
     * @param player 玩家
     * @return Y
     */
    public static double getFloorY(Player player) {
        if (player.getFloorY() <= 0) return 1;
        for (int y = 0; y < 15; y++) {
            Level level = player.getLevel();
            Block block = level.getBlock(player.getFloorX(), player.getFloorY() - y, player.getFloorZ());
            if (block.getId() != 0) {
                if (block.getBoundingBox() != null) {
                    return block.getBoundingBox().getMaxY() + 0.2;
                }
                return block.getMinY() + 0.2;
            }
        }
        return player.getFloorY();
    }

    /**
     * 放烟花
     * GitHub：https://github.com/PetteriM1/FireworkShow
     * @param position 位置
     */
    public static void spawnFirework(Position position) {
        ItemFirework item = new ItemFirework();
        CompoundTag tag = new CompoundTag();
        CompoundTag ex = new CompoundTag();
        ex.putByteArray("FireworkColor",new byte[]{
                (byte) DyeColor.values()[MurderMystery.RANDOM.nextInt(ItemFirework.FireworkExplosion.ExplosionType.values().length)].getDyeData()
        });
        ex.putByteArray("FireworkFade",new byte[0]);
        ex.putBoolean("FireworkFlicker",MurderMystery.RANDOM.nextBoolean());
        ex.putBoolean("FireworkTrail",MurderMystery.RANDOM.nextBoolean());
        ex.putByte("FireworkType",ItemFirework.FireworkExplosion.ExplosionType.values()
                [MurderMystery.RANDOM.nextInt(ItemFirework.FireworkExplosion.ExplosionType.values().length)].ordinal());
        tag.putCompound("Fireworks",(new CompoundTag("Fireworks"))
                .putList(new ListTag<CompoundTag>("Explosions").add(ex)).putByte("Flight",1));
        item.setNamedTag(tag);
        CompoundTag nbt = new CompoundTag();
        nbt.putList(new ListTag<DoubleTag>("Pos")
                .add(new DoubleTag("",position.x+0.5D))
                .add(new DoubleTag("",position.y+0.5D))
                .add(new DoubleTag("",position.z+0.5D))
        );
        nbt.putList(new ListTag<DoubleTag>("Motion")
                .add(new DoubleTag("",0.0D))
                .add(new DoubleTag("",0.0D))
                .add(new DoubleTag("",0.0D))
        );
        nbt.putList(new ListTag<FloatTag>("Rotation")
                .add(new FloatTag("",0.0F))
                .add(new FloatTag("",0.0F))

        );
        nbt.putCompound("FireworkItem", NBTIO.putItemHelper(item));
        EntityFirework entity = new EntityFirework(position.getLevel().getChunk((int)position.x >> 4, (int)position.z >> 4), nbt);
        entity.spawnToAll();
    }

}
