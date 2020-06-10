package cn.lanink.murdermystery.event;

import cn.lanink.murdermystery.room.Room;
import cn.nukkit.event.player.PlayerEvent;


public abstract class RoomPlayerEvent extends PlayerEvent {

    protected Room room;

    public Room getRoom() {
        return this.room;
    }

}
