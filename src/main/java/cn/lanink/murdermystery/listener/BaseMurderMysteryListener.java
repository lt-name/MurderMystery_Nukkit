package cn.lanink.murdermystery.listener;

import cn.lanink.gamecore.utils.exception.GameListenerInitException;
import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.nukkit.event.Listener;
import cn.nukkit.level.Level;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lt_name
 */
public abstract class BaseMurderMysteryListener<T extends BaseRoom> implements Listener {

    protected final MurderMystery murderMystery = MurderMystery.getInstance();
    private String listenerName = null;
    private final ConcurrentHashMap<String, T> listenerRooms = new ConcurrentHashMap<>();

    public final void init(String listenerName) throws GameListenerInitException {
        if (this.listenerName == null) {
            if (listenerName == null || listenerName.trim().isEmpty()) {
                throw new GameListenerInitException("空参数");
            }
            this.listenerName = listenerName;
        }else {
            throw new GameListenerInitException("重复初始化");
        }
    }

    public String getListenerName() {
        return this.listenerName;
    }

    public Map<String, T> getListenerRooms() {
        return this.listenerRooms;
    }

    public T getListenerRoom(Level level) {
        return this.getListenerRoom(level.getFolderName());
    }

    public T getListenerRoom(String level) {
        return this.listenerRooms.get(level);
    }

    public void addListenerRoom(T room) {
        this.listenerRooms.put(room.getLevelName(), room);
    }

    public void removeListenerRoom(T room) {
        this.removeListenerRoom(room.getLevelName());
    }

    public void removeListenerRoom(String level) {
        this.listenerRooms.remove(level);
    }

}
