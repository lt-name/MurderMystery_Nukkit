package cn.lanink.murdermystery.playerdata;

import cn.lanink.murdermystery.MurderMystery;
import cn.nukkit.Player;
import cn.nukkit.utils.Config;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LT_Name
 */
public class PlayerDataManager {

    private static final String[] Default_First_Name = new String[]{
            "a","b","c","d","e","f","g","h",
            "i","j","k", "l","m","n","o","p","q",
            "r","s","t","u","v","w","x","y","z","0","1","2",
            "3","4","5","6","7","8","9","#"
    };

    private final MurderMystery murderMystery;

    private final ConcurrentHashMap<String, PlayerData> playerDataMap = new ConcurrentHashMap<>();

    public PlayerDataManager(MurderMystery murderMystery) {
        this.murderMystery = murderMystery;
    }

    public PlayerData getPlayerData(Player player) {
        return this.getPlayerData(player.getName());
    }

    public PlayerData getPlayerData(String name) {
        if (!this.playerDataMap.containsKey(name)) {
            String file = null;
            for(String string: Default_First_Name) {
                if(string.equals(name.substring(0,1).toLowerCase())) {
                    file = this.murderMystery.getDataFolder() + "/Players/" + string + "/" + name + ".yml";
                    break;
                }
            }
            if (file == null) {
                file = this.murderMystery.getDataFolder() + "/Players/#/" + name + ".yml";
            }
            this.playerDataMap.put(name, new PlayerData(name, new Config(file, Config.YAML)));
        }
        return this.playerDataMap.get(name);
    }

    /**
     * 保存到文件
     */
    public void saveAll() {
        for (PlayerData playerData : this.playerDataMap.values()) {
            playerData.save();
        }
    }

    /**
     * 清除缓存
     */
    public void clearAll() {
        this.playerDataMap.clear();
    }

}
