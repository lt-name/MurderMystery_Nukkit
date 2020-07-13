package cn.lanink.murdermystery.event;

import cn.lanink.murdermystery.room.RoomBase;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

public class MurderRoomAssignIdentityEvent extends RoomEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public MurderRoomAssignIdentityEvent(RoomBase room) {
        this.room = room;
    }

}
