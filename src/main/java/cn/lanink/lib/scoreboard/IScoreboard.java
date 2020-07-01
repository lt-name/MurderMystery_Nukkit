package cn.lanink.lib.scoreboard;

import cn.nukkit.Player;

import java.util.LinkedList;

/**
 * @author lt_name
 */
public interface IScoreboard {

    /**
     * 计分板显示信息
     * @param player 玩家
     * @param title 标题
     * @param message 信息
     */
    void showScoreboard(Player player, String title, LinkedList<String> message);

    /**
     * 关闭计分板显示
     * @param player 玩家
     */
    void closeScoreboard(Player player);

    /**
     * 清除计分板缓存
     * @param player 玩家
     */
    void delCache(Player player);

}
