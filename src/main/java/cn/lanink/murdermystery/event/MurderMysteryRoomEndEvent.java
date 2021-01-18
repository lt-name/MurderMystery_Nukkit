package cn.lanink.murdermystery.event;

import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.lanink.murdermystery.room.base.PlayerIdentity;
import cn.nukkit.Player;
import cn.nukkit.event.HandlerList;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MurderMysteryRoomEndEvent extends MurderMysteryRoomEvent {

    private static final HandlerList handlers = new HandlerList();
    private final int victoryMode;
    private Set<Player> victoryPlayers = new HashSet<>();
    private Set<Player> defeatPlayers = new HashSet<>();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public MurderMysteryRoomEndEvent(BaseRoom room, int victoryMode) {
        this.room = room;
        this.victoryMode = victoryMode;
        if (victoryMode == 0) return;
        for (Map.Entry<Player, PlayerIdentity> entry : room.getPlayers().entrySet()) {
            if (this.victoryMode == 3) {
                if (entry.getValue() == PlayerIdentity.KILLER) {
                    this.victoryPlayers.add(entry.getKey());
                }else {
                    this.defeatPlayers.add(entry.getKey());
                }
            }else {
                if (entry.getValue() == PlayerIdentity.COMMON_PEOPLE || entry.getValue() == PlayerIdentity.DETECTIVE) {
                    this.victoryPlayers.add(entry.getKey());
                }else {
                    this.defeatPlayers.add(entry.getKey());
                }
            }
        }
    }

    public MurderMysteryRoomEndEvent(BaseRoom room, int victoryMode, Set<Player> victoryPlayers, Set<Player> defeatPlayers) {
        this.room = room;
        this.victoryMode = victoryMode;
        if (victoryMode == 0) {
            this.victoryPlayers = new HashSet<>();
            this.defeatPlayers = new HashSet<>();
        }else {
            this.victoryPlayers = victoryPlayers;
            this.defeatPlayers = defeatPlayers;
        }
    }

    public int getVictoryMode() {
        return this.victoryMode;
    }

    public Set<Player> getVictoryPlayers() {
        return this.victoryPlayers;
    }

    public Set<Player> getDefeatPlayers() {
        return this.defeatPlayers;
    }

}
