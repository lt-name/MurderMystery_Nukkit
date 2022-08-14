package cn.lanink.murdermystery.tasks;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.lanink.murdermystery.room.base.RoomStatus;
import cn.nukkit.scheduler.PluginTask;
import net.fap.stage.FStage;

/**
 * @author LT_Name
 */
public class FStageTask extends PluginTask<MurderMystery> {

    public FStageTask(MurderMystery murderMystery) {
        super(murderMystery);
    }

    @Override
    public void onRun(int i) {
        for (BaseRoom room : this.owner.getRooms().values()) {
            if (room.getStatus() == RoomStatus.TASK_NEED_INITIALIZED || room.getStatus() == RoomStatus.WAIT) {
                FStage.setLocalStatus("free");
                return;
            }
        }
        FStage.setLocalStatus("run");
    }

    @Override
    public void onCancel() {
        FStage.setLocalStatus("close");
    }

}
