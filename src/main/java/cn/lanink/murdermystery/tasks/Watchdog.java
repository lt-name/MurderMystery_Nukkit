package cn.lanink.murdermystery.tasks;

import cn.lanink.gamecore.room.IRoomStatus;
import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.nukkit.scheduler.PluginTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lt_name
 */
public class Watchdog extends PluginTask<MurderMystery> {

    public static final ConcurrentHashMap<BaseRoom, Integer> roomRunTime = new ConcurrentHashMap<>();

    public Watchdog() {
        super(MurderMystery.getInstance());
    }

    @Override
    public void onRun(int i) {
        if (MurderMystery.debug) {
            this.owner.getLogger().info("[debug] ==== Watchdog =====");
        }
        for (Map.Entry<BaseRoom, Integer> entry : roomRunTime.entrySet()) {
            int runTime = entry.getValue() + 1;
            if (MurderMystery.debug) {
                this.owner.getLogger().info("[debug] Room: " + entry.getKey().getLevelName() + " runTime: " + runTime);
            }
            entry.setValue(runTime);
            switch (entry.getKey().getStatus()) {
                case IRoomStatus.ROOM_STATUS_LEVEL_NOT_LOADED:
                case IRoomStatus.ROOM_STATUS_TASK_NEED_INITIALIZED:
                    entry.setValue(0);
                    break;
                case IRoomStatus.ROOM_STATUS_WAIT:
                    if (entry.getKey().getPlayers().size() < entry.getKey().getMinPlayers()) {
                        entry.setValue(0);
                    }else if (runTime > entry.getKey().setWaitTime * 1.5) {
                        entry.getKey().endGame();
                        entry.setValue(0);
                    }
                    break;
                case IRoomStatus.ROOM_STATUS_GAME:
                    if (runTime > entry.getKey().setGameTime * 1.5) {
                        entry.getKey().endGame();
                        entry.setValue(0);
                    }
                    break;
                case IRoomStatus.ROOM_STATUS_VICTORY:
                    if (runTime > 15) {
                        entry.getKey().endGame();
                        entry.setValue(0);
                    }
                    break;
            }
        }
        if (MurderMystery.debug) {
            this.owner.getLogger().info("[debug] ==== Watchdog =====");
        }
    }

    @Override
    public void onCancel() {
        roomRunTime.clear();
    }

    public static void add(BaseRoom baseRoom) {
        roomRunTime.put(baseRoom, 0);
    }

    public static void remove(BaseRoom baseRoom) {
        roomRunTime.remove(baseRoom);
    }

}
