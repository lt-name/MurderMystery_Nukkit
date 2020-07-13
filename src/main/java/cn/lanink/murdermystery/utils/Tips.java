package cn.lanink.murdermystery.utils;

import cn.nukkit.Player;
import tip.messages.BossBarMessage;
import tip.messages.NameTagMessage;
import tip.messages.ScoreBoardMessage;
import tip.messages.TipMessage;
import tip.utils.Api;

import java.util.LinkedList;

public class Tips {

    /**
     * 关闭Tips显示
     * @param level 世界
     * @param player 玩家
     */
    public static void closeTipsShow(String level, Player player) {
        Api.setPlayerShowMessage(player.getName(),
                new NameTagMessage(level, true, ""));
        Api.setPlayerShowMessage(player.getName(),
                new TipMessage(level, false, 0, ""));
        Api.setPlayerShowMessage(player.getName(),
                new ScoreBoardMessage(level, false, "", new LinkedList<>()));
        Api.setPlayerShowMessage(player.getName(),
                new BossBarMessage(level, false, 5, false, new LinkedList<>()));
    }

    /**
     * 移除Tips设置
     * @param level 世界
     * @param player 玩家
     */
    public static void removeTipsConfig(String level, Player player) {
        Api.removePlayerShowMessage(player.getName(),
                new NameTagMessage(level, true, ""));
        Api.removePlayerShowMessage(player.getName(),
                new TipMessage(level, false, 0, ""));
        Api.removePlayerShowMessage(player.getName(),
                new ScoreBoardMessage(level, false, "", new LinkedList<>()));
        Api.removePlayerShowMessage(player.getName(),
                new BossBarMessage(level, false, 5, false, new LinkedList<>()));
    }

}
