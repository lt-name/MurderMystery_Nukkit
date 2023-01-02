package cn.lanink.murdermystery.item;

import cn.lanink.gamecore.utils.Language;
import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * @author lt_name
 */
public class ItemManager {

    public static final String IS_MURDER_MYSTERY_TAG = Tools.dynamic("isMurderMystery");
    public static final String INTERNAL_ID_TAG = Tools.dynamic("MurderMysteryInternalID");
    public static final String NOT_CLICK_TAG = Tools.dynamic("MurderMysteryCanNotClick");
    public static final String NOT_CLICK_ON_INVENTORY_TAG = Tools.dynamic("MurderMysteryCanNotClickOnInventory");

    public static Item get(int internalID) {
        return get(null, internalID);
    }

    public static Item get(Player player, int internalID) {
        return get(player, internalID, 1);
    }

    public static Item get(Player player, int internalID, int count) {
        Item item;
        Language language = MurderMystery.getInstance().getLanguage(player);
        switch (internalID) {
            case 1: //侦探弓
                item = Item.get(261, 0, count);
                item.setNamedTag(new CompoundTag()
                        .putBoolean(ItemManager.IS_MURDER_MYSTERY_TAG, true)
                        .putInt(ItemManager.INTERNAL_ID_TAG, 1)
                        .putByte("Unbreakable", 1));
                item.setCustomName(language.translateString("itemDetectiveBow"));
                item.setLore(language.translateString("itemDetectiveBowLore").split("\n"));
                return item;
            case 2: //杀手剑
                item = Item.get(267, 0, count);
                item.setNamedTag(new CompoundTag()
                        .putBoolean(ItemManager.IS_MURDER_MYSTERY_TAG, true)
                        .putInt(ItemManager.INTERNAL_ID_TAG, 2)
                        .putByte("Unbreakable", 1));
                item.setCustomName(language.translateString("itemKillerSword"));
                item.setLore(language.translateString("itemKillerSwordLore").split("\n"));
                return item;
            case 3: //杀手 扫描器
                item = Item.get(395, 0, count);
                item.setNamedTag(new CompoundTag()
                        .putBoolean(ItemManager.IS_MURDER_MYSTERY_TAG, true)
                        .putInt(ItemManager.INTERNAL_ID_TAG, 3)
                        .putBoolean(ItemManager.NOT_CLICK_ON_INVENTORY_TAG, true));
                item.setCustomName(language.translateString("itemScan"));
                item.setLore(language.translateString("itemScanLore").split("\n"));
                return item;
            case 10: //退出房间
                item = Item.get(324, 0, count);
                item.setNamedTag(new CompoundTag()
                        .putBoolean(ItemManager.IS_MURDER_MYSTERY_TAG, true)
                        .putInt(ItemManager.INTERNAL_ID_TAG, 10));
                item.setCustomName(language.translateString("itemQuitRoom"));
                item.setLore(language.translateString("itemQuitRoomLore").split("\n"));
                return item;
            case 20:
                item = Item.get(262, 0, count);
                item.setNamedTag(new CompoundTag()
                        .putBoolean(ItemManager.IS_MURDER_MYSTERY_TAG, true)
                        .putInt(ItemManager.INTERNAL_ID_TAG, 20));
                return item;
            case 21: //随机药水
                item = Item.get(373, 0, count);
                item.setNamedTag(new CompoundTag()
                        .putBoolean(ItemManager.IS_MURDER_MYSTERY_TAG, true)
                        .putInt(ItemManager.INTERNAL_ID_TAG, 21));
                item.setCustomName(language.translateString("itemPotion"));
                item.setLore(language.translateString("itemPotionLore").split("\n"));
                return item;
            case 22: //护盾墙
                item = Item.get(241, 3, count);
                item.setNamedTag(new CompoundTag()
                        .putBoolean(ItemManager.IS_MURDER_MYSTERY_TAG, true)
                        .putInt(ItemManager.INTERNAL_ID_TAG, 22));
                item.setCustomName(language.translateString("itemShieldWall"));
                item.setLore(language.translateString("itemShieldWallLore").split("\n"));
                return item;
            case 23: //眩晕雪球
                item = Item.get(332, 0, count);
                item.setNamedTag(new CompoundTag()
                        .putBoolean(ItemManager.IS_MURDER_MYSTERY_TAG, true)
                        .putInt(ItemManager.INTERNAL_ID_TAG, 23));
                item.setCustomName(language.translateString("itemSnowball"));
                item.setLore(language.translateString("itemSnowballLore").split("\n"));
                return item;
            case 266: //金锭
                item = Item.get(266, 0, count);
                item.setNamedTag(new CompoundTag().putBoolean(ItemManager.NOT_CLICK_ON_INVENTORY_TAG, true));
                return item;
            case 345: //指南针
                item = Item.get(345);
                return item;
            default:
                return Item.get(0);
        }
    }

}
