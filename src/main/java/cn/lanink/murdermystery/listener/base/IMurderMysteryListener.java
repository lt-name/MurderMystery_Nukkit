package cn.lanink.murdermystery.listener.base;

import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.nukkit.event.Listener;

import java.util.Map;

/**
 * @author lt_name
 */
public interface IMurderMysteryListener extends Listener {

    String getListenerName();

    Map<String, BaseRoom> getListenerRooms();

    void addListenerRoom(BaseRoom baseRoom);

    void removeListenerRoom(String roomName);

    void removeListenerRoom(BaseRoom baseRoom);

}
