package cn.lanink.murdermystery.event;

import cn.lanink.murdermystery.room.BaseRoom;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

public class MurderMysteryRoomAssignIdentityEvent extends MurderMysteryRoomEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public MurderMysteryRoomAssignIdentityEvent(BaseRoom room) {
        this.room = room;
    }

}
