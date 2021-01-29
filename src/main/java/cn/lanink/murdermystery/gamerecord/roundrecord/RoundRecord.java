package cn.lanink.murdermystery.gamerecord.roundrecord;

import cn.lanink.murdermystery.room.base.PlayerIdentity;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @author lt_name
 */
@AllArgsConstructor
@Getter
public class RoundRecord {

    private final int id;

    private final String gameMode;
    private final PlayerIdentity win;
    private final String killKiller; //击杀杀手的玩家 (平民或侦探)

    private final List<PlayerRoundRecord> playerRoundRecordList;

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static RoundRecord fromJson(String json) {
        return new Gson().fromJson(json, RoundRecord.class);
    }

}
