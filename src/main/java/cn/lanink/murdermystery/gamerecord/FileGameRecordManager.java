package cn.lanink.murdermystery.gamerecord;

import cn.lanink.murdermystery.gamerecord.roundrecord.RoundRecord;
import cn.nukkit.utils.Config;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

/**
 * @author lt_name
 */
public class FileGameRecordManager extends GameRecordManager {

    private static final Gson GSON = new Gson();

    private final Config roundRecordFile;
    private final Config playerRecordFile;

    private final HashMap<Integer, RoundRecord> roundRecordCache = new HashMap<>();

    public FileGameRecordManager() {
        this.roundRecordFile = new Config(this.murderMystery.getDataFolder() + "/GameRecord/roundRecord.yml", Config.YAML);
        this.playerRecordFile = new Config(this.murderMystery.getDataFolder() + "/GameRecord/playerRecord.yml", Config.YAML);

        roundRecordCount = this.roundRecordFile.getAll().size();
    }

    @Override
    public void addRoundRecord(RoundRecord roundRecord) {
        this.roundRecordCache.put(roundRecord.getId(), roundRecord);
        this.roundRecordFile.set("id-" + roundRecord.getId(), roundRecord.toJson());
        this.roundRecordFile.save();
    }

    @Override
    public RoundRecord getRoundRecord(int id) {
        if (this.roundRecordCache.containsKey(id)) {
            return this.roundRecordCache.get(id);
        }
        String o = this.roundRecordFile.getString("id-" + id);
        RoundRecord roundRecord = GSON.fromJson(o, RoundRecord.class);
        this.roundRecordCache.put(id, roundRecord);
        return roundRecord;
    }

    @Override
    public List<RoundRecord> getRoundRecordList() {
        //TODO
        return null;
    }

    @Override
    public List<RoundRecord> getRoundRecordListByPlayer(@NotNull String name) {
        //TODO
        return null;
    }


}
