package cn.lanink.murdermystery.event;

import cn.lanink.murdermystery.room.Room;
import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

public class MurderPlayerDeathEvent extends RoomPlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public MurderPlayerDeathEvent(Room room, Player player) {
        this.room = room;
        this.player = player;
    }

}
