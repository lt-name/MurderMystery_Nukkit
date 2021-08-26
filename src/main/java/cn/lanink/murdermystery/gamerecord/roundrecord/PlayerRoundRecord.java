package cn.lanink.murdermystery.gamerecord.roundrecord;

import cn.lanink.murdermystery.room.base.PlayerIdentity;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author lt_name
 */
@AllArgsConstructor
@Data
public class PlayerRoundRecord {

    private final String name;
    private PlayerIdentity identity;
    private int score;
    private int killCount; //击杀数

    public Map<String, Object> toSaveMap() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();

        map.put("name", this.name);
        map.put("identity", identity.toString());
        map.put("score", this.score);
        map.put("killCount", this.killCount);

        return map;
    }

    public static PlayerRoundRecord fromSaveMap(Map<String, Object> map) {
        return new PlayerRoundRecord(
                (String) map.get("name"),
                PlayerIdentity.valueOf((String) map.get("identity")),
                (int) map.get("score"),
                (int) map.get("killCount")
        );
    }

}
