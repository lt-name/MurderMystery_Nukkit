package cn.lanink.murdermystery.room.base;

/**
 * @author lt_name
 */
public interface IRoomStatus {

    /**
     * @param status 房间状态
     */
    void setStatus(RoomStatus status);

    /**
     * @return 房间状态
     */
    RoomStatus getStatus();

}
