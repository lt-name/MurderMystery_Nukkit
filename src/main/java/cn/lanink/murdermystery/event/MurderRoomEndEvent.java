package cn.lanink.murdermystery.event;

import cn.lanink.murdermystery.room.Room;
import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

import java.util.LinkedList;
import java.util.Map;

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

    public LinkedList<Player> getVictoryPlayers() {
        LinkedList<Player> victoryPlayers= new LinkedList<>();
        for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
            if (this.victoryMode == 3) {
                if (entry.getValue() == 3) {
                    victoryPlayers.add(entry.getKey());
                }
            }else {
                if (entry.getValue() == 1 || entry.getValue() == 2) {
                    victoryPlayers.add(entry.getKey());
                }
            }
        }
        return victoryPlayers;
    }

    public LinkedList<Player> getDefeatPlayers() {
        LinkedList<Player> defeatPlayers = new LinkedList<>();
        for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
            if (this.victoryMode == 3) {
                if (entry.getValue() != 3) {
                    defeatPlayers.add(entry.getKey());
                }
            }else {
                if (entry.getValue() == 3 || entry.getValue() == 0) {
                    defeatPlayers.add(entry.getKey());
                }
            }
        }
        return defeatPlayers;
    }

}
