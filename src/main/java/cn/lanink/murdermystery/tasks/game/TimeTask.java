package cn.lanink.murdermystery.tasks.game;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.RoomBase;
import cn.nukkit.scheduler.PluginTask;

/**
 * 游戏时间计算
 */
public class TimeTask extends PluginTask<MurderMystery> {

    private final RoomBase room;

    public TimeTask(MurderMystery owner, RoomBase room) {
        super(owner);
        owner.taskList.add(this.getTaskId());
        this.room = room;
    }

    public void onRun(int i) {
        if (this.room.getStatus() != 2) {
            this.cancel();
            return;
        }
        this.room.asyncTimeTask();
    }

    @Override
    public void cancel() {
        while (owner.taskList.contains(this.getTaskId())) {
            owner.taskList.remove(this.getTaskId());
        }
        super.cancel();
    }

}
