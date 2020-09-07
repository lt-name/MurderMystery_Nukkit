package cn.lanink.murdermystery.event;

import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.nukkit.event.player.PlayerEvent;


public abstract class MurderMysteryRoomPlayerEvent extends PlayerEvent {

    protected BaseRoom room;

    public BaseRoom getRoom() {
        return this.room;
    }

}
