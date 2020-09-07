package cn.lanink.murdermystery.tasks.game;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.base.IAsyncTipsTask;
import cn.nukkit.scheduler.PluginTask;

/**
 * 信息显示
 */
public class TipsTask extends PluginTask<MurderMystery> {

    private final IAsyncTipsTask task;

    public TipsTask(MurderMystery owner, IAsyncTipsTask task) {
        super(owner);
        this.task = task;
    }

    @Override
    public void onRun(int i) {
        if (this.task.getStatus() != IAsyncTipsTask.ROOM_STATUS_GAME) {
            this.cancel();
            return;
        }
        this.task.asyncTipsTask();
    }

}
