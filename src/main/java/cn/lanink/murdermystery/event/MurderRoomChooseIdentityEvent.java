package cn.lanink.murdermystery.event;

import cn.lanink.murdermystery.room.Room;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

public class MurderRoomChooseIdentityEvent extends RoomEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public MurderRoomChooseIdentityEvent(Room room) {
        this.room = room;
    }

}
