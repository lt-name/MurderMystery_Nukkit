package cn.lanink.murdermystery.playerdata;

import cn.nukkit.utils.Config;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

/**
 * @author LT_Name
 */
@Setter
@Getter
public class PlayerData {

    private final String name;
    private final Config config;

    private int noDetectiveCount;
    private int noKillerCount;

    public PlayerData(@NotNull String name, @NotNull Config config) {
        this.name = name;
        this.config = config;

        this.noDetectiveCount = this.config.getInt("noDetectiveCount");
        this.noKillerCount = this.config.getInt("noKillerCount");

    }

    public void addNoDetectiveCount() {
        this.noDetectiveCount++;
    }

    public void addNoKillerCount() {
        this.noKillerCount++;
    }

    public void save() {
        //TODO

        this.config.set("noKillerCount", this.noKillerCount);
        this.config.set("noDetectiveCount", this.noDetectiveCount);

        this.config.save();
    }

}
