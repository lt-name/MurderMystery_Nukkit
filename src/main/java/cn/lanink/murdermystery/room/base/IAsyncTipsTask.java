package cn.lanink.murdermystery.room.base;

import cn.lanink.gamecore.room.IRoomStatus;

/**
 * 信息显示Task
 * 具体执行查看{@link cn.lanink.murdermystery.tasks.game.TipsTask }
 *
 * @author lt_name
 */
public interface IAsyncTipsTask extends IRoomStatus {

    /**
     * 玩家信息显示
     */
    void asyncTipsTask();

}
