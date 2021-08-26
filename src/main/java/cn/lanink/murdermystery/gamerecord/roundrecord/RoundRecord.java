package cn.lanink.murdermystery.gamerecord.roundrecord;

import cn.lanink.murdermystery.room.base.PlayerIdentity;
import cn.nukkit.Player;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author lt_name
 */
@AllArgsConstructor
@Data
public class RoundRecord {

    protected static final Gson GSON = new Gson();

    private final int id;

    private final String gameMode;
    private PlayerIdentity win;
    private String killKiller; //击杀杀手的玩家 (平民或侦探)

    private List<PlayerRoundRecord> playerRoundRecordList;

    public PlayerRoundRecord getPlayerRoundRecord(@NotNull Player player) {
        return this.getPlayerRoundRecord(player.getName());
    }

    public PlayerRoundRecord getPlayerRoundRecord(@NotNull String name) {
        for (PlayerRoundRecord playerRoundRecord : this.playerRoundRecordList) {
            if (playerRoundRecord.getName().equals(name)) {
                return playerRoundRecord;
            }
        }
        return null;
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public static RoundRecord fromJson(String json) {
        return GSON.fromJson(json, RoundRecord.class);
    }

}
