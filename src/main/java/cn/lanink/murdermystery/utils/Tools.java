package cn.lanink.murdermystery.utils;

import cn.lanink.gamecore.utils.Language;
import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.entity.EntityPlayerCorpse;
import cn.lanink.murdermystery.entity.EntitySword;
import cn.lanink.murdermystery.entity.EntityText;
import cn.lanink.murdermystery.room.base.BaseRoom;
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
import cn.nukkit.utils.DyeColor;

import java.util.HashSet;
import java.util.List;


/**
 * @author lt_name
 */
public class Tools {

    private Tools() {

    }

    /**
     * 显示玩家
     *
     * @param room 房间
     * @param player 玩家
     */
    public static void showPlayer(BaseRoom room, Player player) {
        for (Player p : room.getPlayers().keySet()) {
            p.showPlayer(player);
        }
        for (Player p : room.getSpectatorPlayers()) {
            p.showPlayer(player);
        }
    }

    /**
     * 隐藏玩家
     *
     * @param room 房间
     * @param player 玩家
     */
    public static void hidePlayer(BaseRoom room, Player player) {
        for (Player p : room.getPlayers().keySet()) {
            p.hidePlayer(player);
        }
        for (Player p : room.getSpectatorPlayers()) {
            p.hidePlayer(player);
        }
    }

    /**
     * 获取字符串房间模式
     *
     * @param player 玩家
     * @param room 房间
     * @return 房间模式
     */
    public static String getStringRoomMode(Player player, BaseRoom room) {
        switch (room.getGameMode()) {
            case "classic":
                return MurderMystery.getInstance().getLanguage(player).translateString("Classic");
            case "infected":
                return MurderMystery.getInstance().getLanguage(player).translateString("Infected");
            case "assassin":
                return MurderMystery.getInstance().getLanguage(player).translateString("assassin");
            default:
                return room.getGameMode();
        }
    }

    /**
     * 执行命令
     *
     * @param player 玩家
     * @param cmds 命令
     */
    public static void cmd(Player player, List<String> cmds) {
        if (player == null || cmds == null || cmds.size() == 0) {
            return;
        }
        for (String s : cmds) {
            String[] cmd = s.split("&");
            if ((cmd.length > 1) && ("con".equals(cmd[1]))) {
                Server.getInstance().dispatchCommand(new ConsoleCommandSender(), cmd[0].replace("@p", player.getName()));
            } else {
                Server.getInstance().dispatchCommand(player, cmd[0].replace("@p", player.getName()));
            }
        }
    }

    /**
     * 给玩家道具
     *
     * @param player 玩家
     * @param tagNumber 物品编号
     */
    public static void giveItem(Player player, int tagNumber) {
        switch (tagNumber) {
            case 1:
                player.getInventory().setItem(1, getMurderItem(player, tagNumber));
                player.getInventory().setItem(2, Item.get(262, 0, 1));
                break;
            case 2:
                player.getInventory().setItem(1, getMurderItem(player, tagNumber));
                player.getInventory().setItem(2, getMurderItem(player, 3));
                break;
            case 10:
                player.getInventory().setItem(8, getMurderItem(player, tagNumber));
                break;
            case 21:
            case 22:
            case 23:
                player.getInventory().addItem(getMurderItem(player, tagNumber));
                break;
            default:
                break;
        }
    }

    /**
     * 根据编号获取物品
     *
     * @param tagNumber 道具编号
     * @return 物品
     */
    public static Item getMurderItem(Player player, int tagNumber) {
        Item item;
        Language language = MurderMystery.getInstance().getLanguage(player);
        switch (tagNumber) {
            case 1:
                item = Item.get(261, 0, 1);
                item.setNamedTag(new CompoundTag()
                        .putBoolean("isMurderItem", true)
                        .putInt("MurderType", 1));
                item.setCustomName(language.translateString("itemDetectiveBow"));
                item.setLore(language.translateString("itemDetectiveBowLore").split("\n"));
                return item;
            case 2:
                item = Item.get(267, 0, 1);
                item.setNamedTag(new CompoundTag()
                        .putBoolean("isMurderItem", true)
                        .putInt("MurderType", 2));
                item.setCustomName(language.translateString("itemKillerSword"));
                item.setLore(language.translateString("itemKillerSwordLore").split("\n"));
                return item;
            case 3:
                item = Item.get(395, 0, 1);
                item.setNamedTag(new CompoundTag()
                        .putBoolean("isMurderItem", true)
                        .putInt("MurderType", 3));
                item.setCustomName(language.translateString("itemScan"));
                item.setLore(language.translateString("itemScanLore").split("\n"));
                return item;
            case 10:
                item = Item.get(324, 0, 1);
                item.setNamedTag(new CompoundTag()
                        .putBoolean("isMurderItem", true)
                        .putInt("MurderType", 10));
                item.setCustomName(language.translateString("itemQuitRoom"));
                item.setLore(language.translateString("itemQuitRoomLore").split("\n"));
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
                item.setCustomName(language.translateString("itemPotion"));
                item.setLore(language.translateString("itemPotionLore").split("\n"));
                return item;
            case 22:
                item = Item.get(241, 3, 1);
                item.setNamedTag(new CompoundTag()
                        .putBoolean("isMurderItem", true)
                        .putInt("MurderType", 22));
                item.setCustomName(language.translateString("itemShieldWall"));
                item.setLore(language.translateString("itemShieldWallLore").split("\n"));
                return item;
            case 23:
                item = Item.get(332, 0, 1);
                item.setNamedTag(new CompoundTag()
                        .putBoolean("isMurderItem", true)
                        .putInt("MurderType", 23));
                item.setCustomName(language.translateString("itemSnowball"));
                item.setLore(language.translateString("itemSnowballLore").split("\n"));
                return item;
            default:
                return Item.get(0);
        }
    }

    /**
     * 设置Human实体皮肤
     *
     * @param human 实体
     * @param skin 皮肤
     */
    public static void setHumanSkin(EntityHuman human, Skin skin) {
        PlayerSkinPacket packet = new PlayerSkinPacket();
        packet.skin = skin;
        packet.newSkinName = skin.getSkinId();
        packet.oldSkinName = human.getSkin().getSkinId();
        packet.uuid = human.getUniqueId();
        HashSet<Player> players = new HashSet<>(human.getViewers().values());
        if (human instanceof Player) {
            players.add((Player) human);
        }
        if (!players.isEmpty()) {
            Server.broadcastPacket(players, packet);
        }
        human.setSkin(skin);
    }

    /**
     * 重置玩家状态
     *
     * @param player 玩家
     * @param joinRoom 是否为加入房间
     */
    public static void rePlayerState(Player player, boolean joinRoom) {
        player.removeAllEffects();
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setLevel(player.getFoodData().getMaxLevel());
        player.setGamemode(0);
        player.getAdventureSettings().set(AdventureSettings.Type.FLYING, false)
                .set(AdventureSettings.Type.ALLOW_FLIGHT, false)
                .update();
        if (joinRoom) {
            player.setNameTag("");
            player.setNameTagVisible(false);
            player.setNameTagAlwaysVisible(false);
            player.setAllowModifyWorld(false);
        }else {
            player.setNameTag(player.getName());
            player.setNameTagVisible(true);
            player.setNameTagAlwaysVisible(true);
        }
    }

    public static void sendMessage(BaseRoom room, String string) {
        for (Player player : room.getPlayers().keySet()) {
            player.sendMessage(string);
        }
    }

    /**
     * 播放声音
     *
     * @param room 房间
     * @param sound 声音
     */
    public static void playSound(BaseRoom room, Sound sound) {
        for (Player player : room.getPlayers().keySet()) {
            playSound(player, sound);
        }
    }

    public static void playSound(Player player, Sound sound) {
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
     *
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
     *
     * @param player 玩家
     * @return Y
     */
    public static double getFloorY(Player player) {
        if (player.getFloorY() <= 0) {
            return 1;
        }
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
     *
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
