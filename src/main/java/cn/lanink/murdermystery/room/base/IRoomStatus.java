package cn.lanink.murdermystery.room.base;

/**
 * @author lt_name
 */
public interface IRoomStatus {

    int ROOM_STATUS_LEVEL_NOT_LOADED = -1; //地图未加载
    int ROOM_STATUS_TASK_NEED_INITIALIZED = 0; //Task未初始化
    int ROOM_STATUS_WAIT = 1; //等待玩家加入
    int ROOM_STATUS_GAME = 2; //游戏中
    int ROOM_STATUS_VICTORY = 3; //胜利结算中

    /**
     * @param status 房间状态
     */
    void setStatus(int status);

    /**
     * @return 房间状态
     */
    int getStatus();

}
