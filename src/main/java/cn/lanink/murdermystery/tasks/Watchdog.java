package cn.lanink.murdermystery.tasks;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.nukkit.scheduler.PluginTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lt_name
 */
public class Watchdog extends PluginTask<MurderMystery> {

    private static final ConcurrentHashMap<BaseRoom, Integer> roomRunTime = new ConcurrentHashMap<>();
    private int outTime = 10;

    public Watchdog() {
        super(MurderMystery.getInstance());
    }

    @Override
    public void onRun(int i) {
        for (Map.Entry<BaseRoom, Integer> entry : roomRunTime.entrySet()) {
            int runTime = entry.getValue() + 1;
            entry.setValue(runTime);
            switch (entry.getKey().getStatus()) {
                case LEVEL_NOT_LOADED:
                case TASK_NEED_INITIALIZED:
                    entry.setValue(0);
                    break;
                case WAIT:
                case GAME:
                case VICTORY:
                    if (runTime > this.outTime) {
                        try {
                            this.owner.getLogger().warning("[Watchdog] Room[" + entry.getKey().getFullRoomName() + "] stuck error! Try to close...");
                            entry.setValue(0);
                            entry.getKey().endGame();
                        } catch (Exception e) {
                            this.owner.unloadRoom(entry.getKey().getLevelName());
                            this.owner.getLogger().error("[Watchdog] The room[" + entry.getKey().getLevelName() + "] cannot end the game error", e);
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onCancel() {
        roomRunTime.clear();
    }

    public static void resetTime(BaseRoom baseRoom) {
        if (roomRunTime.containsKey(baseRoom)) {
            roomRunTime.put(baseRoom, 0);
        }
    }

    public static void add(BaseRoom baseRoom) {
        roomRunTime.put(baseRoom, 0);
    }

    public static void remove(BaseRoom baseRoom) {
        roomRunTime.remove(baseRoom);
    }

}
