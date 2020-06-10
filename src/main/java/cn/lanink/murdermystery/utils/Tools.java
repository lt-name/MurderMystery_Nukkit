package cn.lanink.murdermystery.utils;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.entity.EntityPlayerCorpse;
import cn.lanink.murdermystery.entity.EntitySword;
import cn.lanink.murdermystery.entity.EntityText;
import cn.lanink.murdermystery.room.Room;
import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.entity.item.EntityFirework;
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
import cn.nukkit.utils.DyeColor;
import tip.messages.BossBarMessage;
import tip.messages.NameTagMessage;
import tip.messages.ScoreBoardMessage;
import tip.messages.TipMessage;
import tip.utils.Api;

import java.util.LinkedList;
import java.util.Random;


public class Tools {

    /**
     * 移除显示信息
     * @param level 地图
     */
    public static void removePlayerShowMessage(String level, Player player) {
        Api.removePlayerShowMessage(player.getName(),
                new NameTagMessage(level, true, ""));
        Api.removePlayerShowMessage(player.getName(),
                new TipMessage(level, true, 0, ""));
        Api.removePlayerShowMessage(player.getName(),
                new ScoreBoardMessage(level, true, "", new LinkedList<>()));
        Api.removePlayerShowMessage(player.getName(),
                new BossBarMessage(level, false, 5, false, new LinkedList<>()));
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
     * 设置玩家皮肤
     * @param player 玩家
     * @param skin 皮肤
     */
    public static void setPlayerSkin(Player player, Skin skin) {
        PlayerSkinPacket packet = new PlayerSkinPacket();
        packet.skin = skin;
        packet.newSkinName = skin.getSkinId();
        packet.oldSkinName = player.getSkin().getSkinId();
        packet.uuid = player.getUniqueId();
        player.setSkin(skin);
        player.dataPacket(packet);
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
            player.setNameTagVisible(false);
            player.setNameTagAlwaysVisible(false);
            player.setAllowModifyWorld(false);
        }else {
            player.setNameTagVisible(true);
            player.setNameTagAlwaysVisible(true);
            setPlayerInvisible(player, false);
            player.setAllowModifyWorld(true);
        }
        player.setAdventureSettings((new AdventureSettings(player)).set(AdventureSettings.Type.ALLOW_FLIGHT, false));
    }

    /**
     * 添加声音
     * @param room 房间
     * @param sound 声音
     */
    public static void addSound(Room room, Sound sound) {
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
                } else {
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
        for (int y = 0; y < 10; y++) {
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
     * GitHub：https://github.com/SmallasWater/LuckDraw/blob/master/src/main/java/smallaswater/luckdraw/utils/Tools.java
     * @param player 玩家
     */
    public static void spawnFirework(Position player) {
        Level level = player.getLevel();
        ItemFirework item = new ItemFirework();
        CompoundTag tag = new CompoundTag();
        Random random = new Random();
        CompoundTag ex = new CompoundTag();
        ex.putByteArray("FireworkColor",new byte[]{
                (byte) DyeColor.values()[random.nextInt(ItemFirework.FireworkExplosion.ExplosionType.values().length)].getDyeData()
        });
        ex.putByteArray("FireworkFade",new byte[0]);
        ex.putBoolean("FireworkFlicker",random.nextBoolean());
        ex.putBoolean("FireworkTrail",random.nextBoolean());
        ex.putByte("FireworkType",ItemFirework.FireworkExplosion.ExplosionType.values()
                [random.nextInt(ItemFirework.FireworkExplosion.ExplosionType.values().length)].ordinal());
        tag.putCompound("Fireworks",(new CompoundTag("Fireworks")).putList(new ListTag<CompoundTag>("Explosions").add(ex)).putByte("Flight",1));
        item.setNamedTag(tag);
        CompoundTag nbt = new CompoundTag();
        nbt.putList(new ListTag<DoubleTag>("Pos")
                .add(new DoubleTag("",player.x+0.5D))
                .add(new DoubleTag("",player.y+0.5D))
                .add(new DoubleTag("",player.z+0.5D))
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
        EntityFirework entity = new EntityFirework(level.getChunk((int)player.x >> 4, (int)player.z >> 4), nbt);
        entity.spawnToAll();
    }

}
