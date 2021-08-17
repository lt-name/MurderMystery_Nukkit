package cn.lanink.murdermystery.event;

import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.nukkit.Player;
import cn.nukkit.event.HandlerList;

/**
 * @author LT_Name
 */
public class MurderMysteryRoomPlayerJoinEvent extends MurderMysteryRoomPlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public MurderMysteryRoomPlayerJoinEvent(BaseRoom baseRoom, Player player) {
        this.room = baseRoom;
        this.player = player;
    }

}
