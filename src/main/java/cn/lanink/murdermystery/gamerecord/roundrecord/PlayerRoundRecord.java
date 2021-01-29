package cn.lanink.murdermystery.gamerecord.roundrecord;

import cn.lanink.murdermystery.room.base.PlayerIdentity;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lt_name
 */
@AllArgsConstructor
@Getter
public class PlayerRoundRecord {

    private final String name;
    private final PlayerIdentity identity;
    private final int score;
    private final int killCount; //击杀数

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static PlayerRoundRecord fromJson(String json) {
        return new Gson().fromJson(json, PlayerRoundRecord.class);
    }

}
