package cn.lanink.murdermystery.api;

import cn.lanink.gamecore.api.Info;
import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.nukkit.Player;

/**
 * @author lt_name
 */
public class API {

    private API() {

    }

    @Info("返回玩家所在的房间")
    public static BaseRoom getRoom(Player player) {
        for (BaseRoom baseRoom : MurderMystery.getInstance().getRooms().values()) {
            if (baseRoom.isPlaying(player)) {
                return baseRoom;
            }
        }
        return null;
    }

}
