package cn.lanink.murdermystery.item;

import cn.lanink.gamecore.utils.Language;
import cn.lanink.murdermystery.MurderMystery;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * @author lt_name
 */
public class ItemManager {

    public static Item get(int tagNumber) {
        return get(null, tagNumber);
    }

    public static Item get(Player player, int tagNumber) {
        Item item;
        Language language = MurderMystery.getInstance().getLanguage(player);
        switch (tagNumber) {
            case 1:
                item = Item.get(261, 0, 1);
                item.setNamedTag(new CompoundTag()
                        .putBoolean("isMurderItem", true)
                        .putInt("MurderType", 1)
                        .putByte("Unbreakable", 1));
                item.setCustomName(language.translateString("itemDetectiveBow"));
                item.setLore(language.translateString("itemDetectiveBowLore").split("\n"));
                return item;
            case 2:
                item = Item.get(267, 0, 1);
                item.setNamedTag(new CompoundTag()
                        .putBoolean("isMurderItem", true)
                        .putInt("MurderType", 2)
                        .putByte("Unbreakable", 1));
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
            case 266:
                item = Item.get(266, 0, 1); //金锭
                item.setNamedTag(new CompoundTag().putBoolean("cannotClickOnInventory", true));
                return item;
            default:
                return Item.get(0);
        }
    }

}
