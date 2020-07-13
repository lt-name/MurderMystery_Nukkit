package cn.lanink.murdermystery.event;

import cn.lanink.murdermystery.room.RoomBase;
import cn.nukkit.event.player.PlayerEvent;


public abstract class RoomPlayerEvent extends PlayerEvent {

    protected RoomBase room;

    public RoomBase getRoom() {
        return this.room;
    }

}
