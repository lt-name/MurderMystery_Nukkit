package cn.lanink.murdermystery.utils;

import cn.lanink.gamecore.ranking.Ranking;
import cn.lanink.gamecore.ranking.RankingAPI;
import cn.lanink.gamecore.ranking.RankingFormat;
import cn.lanink.murdermystery.MurderMystery;
import cn.nukkit.Server;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.*;

/**
 * 排行榜管理
 *
 * @author LT_Name
 */
public class RankingManager {

    @Getter
    private static final ArrayList<RankingData> RANKING_DATA_LIST = new ArrayList<>();
    @Getter
    private static final HashMap<String, Ranking> RANKING_MAP = new HashMap<>();

    /**
     * 加载排行榜 (可当reload用)
     */
    public static void load() {
        clear();

        //读取排行榜数据
        Config rankingConfig = new Config(MurderMystery.getInstance().getDataFolder() + "/RankingConfig.yml", Config.YAML);
        //加载排行榜格式
        RankingFormat rankingFormat = RankingFormat.getDefaultFormat();
        rankingFormat.setTop(rankingConfig.getString("RankingFormat.Top"));
        rankingFormat.setLine(rankingConfig.getString("RankingFormat.Line"));
        rankingFormat.setLineSelf(rankingConfig.getString("RankingFormat.LineSelf"));
        rankingFormat.setBottom(rankingConfig.getString("RankingFormat.Bottom"));
        try { //ShowLine 配置不好很容易出错，这里捕获下
            if (rankingConfig.exists("RankingFormat.ShowLine")) {
                Map<String, Integer> map = (Map<String, Integer>) rankingConfig.get("RankingFormat.ShowLine");
                rankingFormat.getShowLine().clear();
                for (Map.Entry<String, Integer> entry : map.entrySet()) {
                    rankingFormat.getShowLine().put(Integer.parseInt(entry.getKey()), entry.getValue());
                }
            }
        }catch (Exception e) {
            MurderMystery.getInstance().getLogger().error("加载排行榜格式失败！请检查配置文件！", e);
        }

        List<Map> list = rankingConfig.getMapList("pos");
        for (Map map : list) {
            try {
                String levelName = (String) map.get("level");
                if (!Server.getInstance().loadLevel(levelName)) {
                    throw new RuntimeException("世界：" + levelName + " 加载失败！");
                }
                String stringType = (String) map.get("type");
                RecordType type = RecordType.of(stringType);
                if (type == null) {
                    throw new RuntimeException("排行榜类型：" + stringType + " 不存在！");
                }
                RANKING_DATA_LIST.add(
                        new RankingData(
                                (String) map.get("name"),
                                type,
                                Position.fromObject(Tools.mapToVector3(map), Server.getInstance().getLevelByName(levelName))
                        )
                );
            } catch (Exception e) {
                MurderMystery.getInstance().getLogger().error("读取排行榜数据出错！ 配置: " + map, e);
            }
        }
        if (MurderMystery.debug) {
            MurderMystery.getInstance().getLogger().info("[debug] " + RANKING_DATA_LIST);
        }
        //创建排行榜
        for (RankingData rankingData : RANKING_DATA_LIST) {
            Ranking ranking = RankingAPI.createRanking(MurderMystery.getInstance(), rankingData.getName(), rankingData.getPosition());
            //TODO 排行榜数据
            //ranking.setRankingList();
            ranking.setRankingFormat(rankingFormat);
            RANKING_MAP.put(rankingData.getName(), ranking);
        }
        MurderMystery.getInstance().getLogger().info("排行榜加载完成！成功创建 " + RANKING_MAP.size() + " 个排行榜！");
    }

    public static void addRanking(RankingData rankingData) {
        RANKING_DATA_LIST.add(rankingData);
    }

    /**
     * 保存排行榜数据
     */
    public static void save() {
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        for (RankingData rankingData : RANKING_DATA_LIST) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("name", rankingData.getName());
            map.put("type", rankingData.getType().name());
            map.putAll(Tools.vector3ToMap(rankingData.getPosition()));
            map.put("level", rankingData.getPosition().getLevel().getFolderName());
            list.add(map);
        }
        Config rankingConfig = new Config(MurderMystery.getInstance().getDataFolder() + "/RankingConfig.yml", Config.YAML);
        rankingConfig.set("pos", list);
        rankingConfig.save();
    }

    /**
     * 清理已加载的排行榜
     */
    public static void clear() {
        RANKING_DATA_LIST.clear();
        if (!RANKING_MAP.isEmpty()) {
            for (Ranking ranking : RANKING_MAP.values()) {
                ranking.close();
            }
            RANKING_MAP.clear();
        }
    }
    
    public enum RecordType {
        TODO; //TODO

        public static RecordType of(String name) {
            for (RecordType type : values()) {
                if (type.name().equalsIgnoreCase(name)) { //忽略大小写
                    return type;
                }
            }
            return null;
        }
    }

    @AllArgsConstructor
    @Data
    public static class RankingData {
        private String name;
        private RecordType type;
        private Position position;
    }

}
