package cn.lanink.murdermystery.event;

import cn.lanink.murdermystery.room.BaseRoom;
import cn.nukkit.event.HandlerList;

public class MurderMysteryRoomStartEvent extends MurderMysteryRoomEvent {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public MurderMysteryRoomStartEvent(BaseRoom room) {
        this.room = room;
    }

}
