package cn.lanink.murdermystery.event;

import cn.lanink.murdermystery.room.Room;
import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

public class MurderPlayerCorpseSpawnEvent extends RoomPlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public MurderPlayerCorpseSpawnEvent(Room room, Player player) {
        this.room = room;
        this.player = player;
    }

}
