package cn.lanink.murdermystery.tasks.game;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.Room;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import cn.nukkit.scheduler.PluginTask;

/**
 * 金锭生成 金锭自动兑换
 */
public class GoldTask extends PluginTask<MurderMystery> {

    private final Room room;
    private int goldSpawnTime;

    public GoldTask(MurderMystery owner, Room room) {
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
            Tools.cleanEntity(room.getLevel());
            for (Position spawn : room.getGoldSpawn()) {
                room.getLevel().dropItem(spawn, Item.get(266, 0));
            }
        }else {
            this.goldSpawnTime--;
        }
        if (room.getPlayers().values().size() > 0) {
            for (Player player : room.getPlayers().keySet()) {
                int x = 0;
                boolean bow = true;
                for (Item item : player.getInventory().getContents().values()) {
                    if (item.getId() == 266) {
                        x += item.getCount();
                        continue;
                    }
                    if (item.getId() == 261) {
                        bow = false;
                    }
                }
                if (x > 9) {
                    player.getInventory().removeItem(Item.get(266, 0, 10));
                    player.getInventory().addItem(Item.get(262, 0, 1));
                    if (bow) {
                        player.getInventory().addItem(Item.get(261, 0, 1));
                    }
                }
            }
        }
    }

    @Override
    public void cancel() {
        while (owner.taskList.contains(this.getTaskId())) {
            owner.taskList.remove(this.getTaskId());
        }
        super.cancel();
    }

}
