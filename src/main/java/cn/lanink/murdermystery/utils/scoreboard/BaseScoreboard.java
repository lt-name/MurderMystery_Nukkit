package cn.lanink.murdermystery.utils.scoreboard;

import cn.nukkit.Player;

import java.util.LinkedList;

public abstract class BaseScoreboard {

    public abstract void showScoreboard(Player player, LinkedList<String> message);

    public abstract void closeScoreboard(Player player);

    public abstract void delCache(Player player);

}
