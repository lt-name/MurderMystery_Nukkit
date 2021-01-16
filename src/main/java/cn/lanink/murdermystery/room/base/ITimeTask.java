package cn.lanink.murdermystery.room.base;

/**
 * 房间时间计算任务, 每秒调用一次
 * 具体执行查看{@link cn.lanink.murdermystery.tasks.game.TimeTask }
 *
 * @author lt_name
 */
public interface ITimeTask extends IRoomStatus {

    /**
     * 房间游戏状态下，每秒调用一次
     */
    void timeTask();

}
