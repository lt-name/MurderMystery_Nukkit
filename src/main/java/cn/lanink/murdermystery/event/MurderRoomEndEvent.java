package cn.lanink.murdermystery.event;

import cn.lanink.murdermystery.room.Room;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

public class MurderRoomEndEvent extends RoomEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private int victoryMode;

    public static HandlerList getHandlers() {
        return handlers;
    }

    public MurderRoomEndEvent(Room room, int victoryMode) {
        this.room = room;
        this.victoryMode = victoryMode;
    }

    public int getVictoryMode() {
        return this.victoryMode;
    }

}
