package cn.lanink.murdermystery.utils.update;

import cn.lanink.gamecore.utils.VersionUtils;
import cn.lanink.murdermystery.MurderMystery;
import cn.nukkit.utils.Config;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;

/**
 * @author LT_Name
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigUpdateUtils {

    public static void updateConfig() {
        update1_X_X_To_1_4_2();
    }

    @NotNull
    private static Config getConfig() {
        return new Config(MurderMystery.getInstance().getDataFolder() + "/config.yml", Config.YAML);
    }

    private static void update1_X_X_To_1_4_2() {
        Config config = getConfig();
        if (VersionUtils.compareVersion(config.getString("ConfigVersion", "1.0.0"), "1.4.2") >= 0) {
            return;
        }

        config.set("ConfigVersion", "1.4.2");

        if (!config.exists("AutomaticJoinGame")) {
            config.set("AutomaticJoinGame", false);
        }

        config.save();
    }

    // 需要在NsGB加载后检查，放到onEnable里
    public static void checkFapNsGB(MurderMystery murderMystery) {
        try {
            Class.forName("cn.nsgamebase.NsGameBaseMain");

            Config config = murderMystery.getConfig();

            LinkedHashMap<String, Object> fapIntegral = new LinkedHashMap<>();
            fapIntegral.put("money", 10);
            fapIntegral.put("point", 0);
            fapIntegral.put("exp", 10);
            fapIntegral.put("maxMultiplier", 1);

            if (!config.exists("fapIntegral")) {
                config.set("fapIntegral", fapIntegral);
                config.save();
            }
        } catch (Exception ignored) {

        }
    }

}
