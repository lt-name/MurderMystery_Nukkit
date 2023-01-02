package cn.lanink.murdermystery.playerdata;

import cn.lanink.murdermystery.MurderMystery;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LT_Name
 */
public class PlayerDataManager {

    private final MurderMystery murderMystery;

    private final ConcurrentHashMap<String, PlayerData> playerDataMap = new ConcurrentHashMap<>();

    public PlayerDataManager(MurderMystery murderMystery) {
        this.murderMystery = murderMystery;
    }

    /**
     * 保存到文件
     */
    public void saveAll() {
        //TODO
    }

    /**
     * 清除缓存
     */
    public void clearAll() {
        this.playerDataMap.clear();
    }

}
