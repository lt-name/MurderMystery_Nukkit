package cn.lanink.murdermystery.event;

import cn.lanink.murdermystery.room.RoomBase;
import cn.nukkit.event.HandlerList;

public class MurderMysteryRoomStartEvent extends MurderMysteryRoomEvent {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public MurderMysteryRoomStartEvent(RoomBase room) {
        this.room = room;
    }

}
