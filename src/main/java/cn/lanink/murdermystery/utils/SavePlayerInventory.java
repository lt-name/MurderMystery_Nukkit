package cn.lanink.murdermystery.utils;

import cn.lanink.murdermystery.MurderMystery;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Config;
import com.sun.istack.internal.NotNull;

import java.io.File;
import java.util.*;

/**
 * @author 若水
 */
public class SavePlayerInventory {

    /**
     * 保存玩家背包
     * @param player 玩家
     */
    public static void save(Player player) {
        File file = new File(MurderMystery.getInstance().getDataFolder() + "/PlayerInventory/" + player.getName() + ".json");
        Config config = new Config(file, 1);
        config.set("Inventory", InventoryToJson(player));
        config.save();
        player.getInventory().clearAll();
    }

    /**
     * 还原玩家背包
     * @param player 玩家
     */
    public static void restore(Player player) {
        File file = new File(MurderMystery.getInstance().getDataFolder() + "/PlayerInventory/" + player.getName() + ".json");
        if (file.exists()) {
            Config config = new Config(file, 1);
            if (file.delete()) {
                player.getInventory().clearAll();
                PutInventory(player, config.get("Inventory", null));
            }
        }
    }

    public static LinkedHashMap<String, Object> InventoryToJson(@NotNull Player player) {
        LinkedHashMap<String, Object> Inventory = new LinkedHashMap<>();
        for (int i = 0; i < player.getInventory().getSize() + 4; i++) {
            LinkedList<String> list = new LinkedList<>();
            Item item = player.getInventory().getItem(i);
            list.add(item.getId() + ":" + item.getDamage());
            list.add(item.getCount() + "");
            String tag = item.hasCompoundTag() ? bytesToBase64(item.getCompoundTag()) : "not";
            list.add(tag);
            Inventory.put(i + "", list);
        }
        return Inventory;
    }

    public static String bytesToBase64(byte[] src) {
        if (src == null || src.length <= 0) {
            return "not";
        }
        return Base64.getEncoder().encodeToString(src);
    }

    public static void PutInventory(Player player, Map inventory) {
        if (inventory == null || inventory.isEmpty()) {
            return;
        }
        for (int i = 0; i < player.getInventory().getSize() + 4; i++) {
            List list = (List)inventory.get(i + "");
            Item item = Item.fromString((String) list.get(0));
            item.setCount(Integer.parseInt((String) list.get(1)));
            if (!String.valueOf(list.get(2)).equals("not")) {
                CompoundTag tag = Item.parseCompoundTag(base64ToBytes((String) list.get(2)));
                item.setNamedTag(tag);
            }
            if (player.getInventory().getSize() + 4 < i) {
                player.getInventory().addItem(item.clone());
            } else {
                player.getInventory().setItem(i, item.clone());
            }
        }
    }

    public static byte[] base64ToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        return Base64.getDecoder().decode(hexString);
    }

}
