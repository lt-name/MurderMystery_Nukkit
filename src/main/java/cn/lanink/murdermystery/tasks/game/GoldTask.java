package cn.lanink.murdermystery.tasks.game;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.BaseRoom;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.scheduler.Task;

/**
 * 金锭生成 金锭自动兑换
 * @author lt_name
 */
public class GoldTask extends PluginTask<MurderMystery> {

    private final BaseRoom room;
    private int goldSpawnTime;

    public GoldTask(MurderMystery owner, BaseRoom room) {
        super(owner);
        this.room = room;
        this.goldSpawnTime = room.setGoldSpawnTime;
    }

    @Override
    public void onRun(int i) {
        if (this.room.getStatus() != BaseRoom.ROOM_STATUS_GAME) {
            Tools.cleanEntity(this.room.getLevel());
            this.cancel();
            return;
        }
        if (this.goldSpawnTime < 1) {
            this.goldSpawnTime = this.room.setGoldSpawnTime;
            //主线程操作掉落物
            owner.getServer().getScheduler().scheduleTask(owner, new Task() {
                @Override
                public void onRun(int i) {
                    room.goldSpawn();
                }
            });
        }else {
            this.goldSpawnTime--;
        }
        this.room.asyncGoldTask();
    }

}
