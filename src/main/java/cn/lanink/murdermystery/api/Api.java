package cn.lanink.murdermystery.api;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.Room;
import cn.nukkit.Player;
import cn.nukkit.level.Level;

import java.util.LinkedHashMap;

public class Api {

    /**
     * @return 房间列表
     */
    public static LinkedHashMap<String, Room> getRooms() {
        return MurderMystery.getInstance().getRooms();
    }

    /**
     * @param level 世界
     * @return 房间
     */
    public static Room getRoomByLevel(Level level) {
        return getRoomByLevel(level.getName());
    }

    /**
     * @param level 世界名称
     * @return 房间
     */
    public static Room getRoomByLevel(String level) {
        return getRooms().getOrDefault(level, null);
    }

    /**
     * @param level 世界
     * @return 是否为游戏房间
     */
    public static boolean isRoomLevel(Level level) {
        return isRoomLevel(level.getName());
    }

    public static boolean isRoomLevel(String level) {
        return getRooms().containsKey(level);
    }

    /**
     * @deprecated
     * @param player 玩家
     * @return 玩家身份
     */
    public static String getPlayerMode(String player) {
        return getPlayerMode(MurderMystery.getInstance().getServer().getPlayer(player));
    }

    /**
     * @param player 玩家
     * @return 玩家身份
     */
    public static String getPlayerMode(Player player) {
        for (Room room : getRooms().values()) {
            if (room.getMode() == 2) {
                if (room.isPlaying(player)) {
                    switch (room.getPlayerMode(player)) {
                        case 1:
                            return "平民";
                        case 2:
                            return "侦探";
                        case 3:
                            return "杀手";
                        default:
                            return "死亡";
                    }
                }
            }
        }
        return "未分配";
    }

    /**
     * 根据玩家获取倒计时
     * @param player 玩家
     * @return 剩余时间
     */
    public static String getTime(Player player) {
        for (Room room : getRooms().values()) {
            if (room.isPlaying(player)) {
                if (room.getMode() == 1) {
                    return room.waitTime + "";
                }else if (room.getMode() == 2) {
                    return room.gameTime + "";
                }
            }
        }
        return "未加入房间";
    }

    /**
     * 获取存活玩家数量
     * @param player 玩家
     * @return 存活玩家数量
     */
    public static String getSurvivor(Player player) {
        for (Room room : getRooms().values()) {
            if (room.isPlaying(player)) {
                if (room.getMode() == 1) {
                    return room.getPlayers().size() + "";
                }else {
                    int playerNumber = 0;
                    for (Integer integer : room.getPlayers().values()) {
                        if (integer != 0) {
                            playerNumber++;
                        }
                    }
                    return playerNumber + "";
                }
            }
        }
        return "未加入房间";
    }

    /**
     * 获取房间状态
     * @param player 玩家
     * @return 房间状态
     */
    public static String getRoomMode(Player player) {
        if (getRoomByLevel(player.getLevel()) != null) {
            switch (getRoomByLevel(player.getLevel()).getMode()) {
                case 2:
                    return "游戏中";
                case 3:
                    return "胜利结算";
                default:
                    return "等待中";
            }
        }
        return "未加入房间";
    }

}
