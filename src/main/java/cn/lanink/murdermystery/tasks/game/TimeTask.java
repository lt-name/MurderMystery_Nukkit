package cn.lanink.murdermystery.tasks.game;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.base.ITimeTask;
import cn.nukkit.scheduler.PluginTask;

/**
 * 游戏时间计算
 * @author lt_name
 */
public class TimeTask extends PluginTask<MurderMystery> {

    private final ITimeTask task;

    public TimeTask(MurderMystery owner, ITimeTask task) {
        super(owner);
        this.task = task;
    }

    @Override
    public void onRun(int i) {
        if (this.task.getStatus() != ITimeTask.ROOM_STATUS_GAME) {
            this.cancel();
            return;
        }
        this.task.timeTask();
    }

}
