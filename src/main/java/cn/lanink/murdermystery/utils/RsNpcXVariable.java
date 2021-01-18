package cn.lanink.murdermystery.utils;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.nukkit.Player;
import com.smallaswater.npc.variable.BaseVariable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lt_name
 */
public class RsNpcXVariable extends BaseVariable {

    @Override
    public String stringReplace(Player player, String s) {
        HashMap<String, Integer> map = new HashMap<>();
        int all = 0;
        for (BaseRoom room : MurderMystery.getInstance().getRooms().values()) {
            map.put(room.getGameMode(),
                    map.getOrDefault(room.getGameMode(), 0) + room.getPlayers().size());
            all += room.getPlayers().size();
        }
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            s = s.replace("{MurderMysteryRoomPlayerNumber" + entry.getKey() + "}", entry.getValue() + "");
        }
        return s.replace("{MurderMysteryRoomPlayerNumberAll}", all + "");
    }

}
