package cn.lanink.murdermystery.event;

import cn.lanink.murdermystery.room.RoomBase;
import cn.nukkit.Player;
import cn.nukkit.event.HandlerList;

import java.util.LinkedList;
import java.util.Map;

public class MurderMysteryRoomEndEvent extends MurderMysteryRoomEvent {

    private static final HandlerList handlers = new HandlerList();
    private int victoryMode;
    private LinkedList<Player> victoryPlayers, defeatPlayers;

    public static HandlerList getHandlers() {
        return handlers;
    }

    public MurderMysteryRoomEndEvent(RoomBase room, int victoryMode) {
        this.room = room;
        this.victoryMode = victoryMode;
        for (Map.Entry<Player, Integer> entry : room.getPlayers().entrySet()) {
            if (this.victoryMode == 3) {
                if (entry.getValue() == 3) {
                    this.victoryPlayers.add(entry.getKey());
                }else {
                    this.defeatPlayers.add(entry.getKey());
                }
            }else {
                if (entry.getValue() == 1 || entry.getValue() == 2) {
                    this.victoryPlayers.add(entry.getKey());
                }else {
                    this.defeatPlayers.add(entry.getKey());
                }
            }
        }
    }

    public MurderMysteryRoomEndEvent(RoomBase room, int victoryMode, LinkedList<Player> victoryPlayers, LinkedList<Player> defeatPlayers) {
        this.room = room;
        this.victoryMode = victoryMode;
        this.victoryPlayers = victoryPlayers;
        this.defeatPlayers = defeatPlayers;
    }

    public int getVictoryMode() {
        return this.victoryMode;
    }

    public LinkedList<Player> getVictoryPlayers() {
        return this.victoryPlayers;
    }

    public LinkedList<Player> getDefeatPlayers() {
        return this.defeatPlayers;
    }

}
