package cn.lanink.murdermystery.utils;

import cn.lanink.murdermystery.MurderMystery;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Config;

import java.io.File;
import java.util.*;

/**
 * @author 若水
 * @author lt_name
 */
public class SavePlayerInventory {

    /**
     * 保存玩家背包
     * @param player 玩家
     */
    public static void save(Player player) {
        File file = new File(MurderMystery.getInstance().getDataFolder() + "/PlayerInventory/" + player.getName() + ".json");
        Config config = new Config(file, 1);
        config.set("Inventory", inventoryToLinkedHashMap(player));
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
                player.getUIInventory().clearAll();
                putInventory(player, config.get("Inventory", null));
            }
        }
    }

    /**
     * 玩家背包内容转换为 LinkedHashMap
     * @param player 玩家
     * @return LinkedHashMap
     */
    public static LinkedHashMap<String, Object> inventoryToLinkedHashMap(Player player) {
        LinkedHashMap<String, Object> inventory = new LinkedHashMap<>();
        for (int i = -1; i < player.getInventory().getSize() + 4; i++) {
            LinkedList<String> list = new LinkedList<>();
            Item item;
            if (i == -1) {
                item = player.getOffhandInventory().getItem(0);
            }else {
                item = player.getInventory().getItem(i);
            }
            list.add(item.getId() + ":" + item.getDamage());
            list.add(item.getCount() + "");
            String tag = item.hasCompoundTag() ? bytesToBase64(item.getCompoundTag()) : "not";
            list.add(tag);
            inventory.put(i + "", list);
        }
        return inventory;
    }

    /**
     * 字节数组转base64
     * @param src 字节数组
     * @return base64字符串
     */
    public static String bytesToBase64(byte[] src) {
        if (src == null || src.length <= 0) {
            return "not";
        }
        return Base64.getEncoder().encodeToString(src);
    }

    /**
     * 物品还原到玩家背包
     * @param player 玩家
     * @param inventory 物品Map
     */
    public static void putInventory(Player player, Map<String, Object> inventory) {
        if (inventory == null || inventory.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : inventory.entrySet()) {
            List<String> list = null;
            try {
                list = (List<String>) entry.getValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (list == null || list.isEmpty()) {
                break;
            }
            Item item = Item.fromString(list.get(0));
            item.setCount(Integer.parseInt(list.get(1)));
            if (!"not".equals(String.valueOf(list.get(2)))) {
                CompoundTag tag = Item.parseCompoundTag(base64ToBytes(list.get(2)));
                item.setNamedTag(tag);
            }
            int index = Integer.parseInt(entry.getKey());
            if (index == -1) {
                player.getOffhandInventory().setItem(0, item);
            }else if (index > player.getInventory().getSize() + 4) {
                player.getInventory().addItem(item.clone());
            }else {
                player.getInventory().setItem(index, item.clone());
            }
        }
    }

    /**
     * base64转字节数组
     * @param hexString base64
     * @return 字节数组
     */
    public static byte[] base64ToBytes(String hexString) {
        if (hexString == null || "".equals(hexString)) {
            return null;
        }
        return Base64.getDecoder().decode(hexString);
    }

}
