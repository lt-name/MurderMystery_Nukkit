package cn.lanink.murdermystery.tasks.game;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.RoomClassicMode;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.scheduler.Task;

/**
 * 金锭生成 金锭自动兑换
 */
public class GoldTask extends PluginTask<MurderMystery> {

    private final RoomClassicMode room;
    private int goldSpawnTime;

    public GoldTask(MurderMystery owner, RoomClassicMode room) {
        super(owner);
        owner.taskList.add(this.getTaskId());
        this.room = room;
        this.goldSpawnTime = room.getSetGoldSpawnTime();
    }

    @Override
    public void onRun(int i) {
        if (this.room.getMode() != 2) {
            Tools.cleanEntity(this.room.getLevel());
            this.cancel();
            return;
        }
        if (this.goldSpawnTime < 1) {
            this.goldSpawnTime = this.room.getSetGoldSpawnTime();
            //主线程操作掉落物
            owner.getServer().getScheduler().scheduleDelayedTask(owner, new Task() {
                @Override
                public void onRun(int i) {
                    room.goldSpawn();
                }
            }, 1);
        }else {
            this.goldSpawnTime--;
        }
        this.room.asyncGoldTask();
    }

    @Override
    public void cancel() {
        while (owner.taskList.contains(this.getTaskId())) {
            owner.taskList.remove(this.getTaskId());
        }
        super.cancel();
    }

}
