package cn.lanink.murdermystery.event;

import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.nukkit.event.Event;

public abstract class MurderMysteryRoomEvent extends Event {

    protected BaseRoom room;

    public BaseRoom getRoom() {
        return this.room;
    }

}
