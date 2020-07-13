package cn.lanink.murdermystery.event;

import cn.lanink.murdermystery.room.RoomBase;
import cn.nukkit.event.Event;

public abstract class RoomEvent extends Event {

    protected RoomBase room;

    public RoomBase getRoom() {
        return this.room;
    }

}
