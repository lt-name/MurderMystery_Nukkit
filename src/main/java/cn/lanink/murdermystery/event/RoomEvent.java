package cn.lanink.murdermystery.event;

import cn.lanink.murdermystery.room.Room;
import cn.nukkit.event.Event;

public abstract class RoomEvent extends Event {

    protected Room room;

    public Room getRoom() {
        return this.room;
    }

}
