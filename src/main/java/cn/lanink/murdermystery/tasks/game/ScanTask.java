package cn.lanink.murdermystery.tasks.game;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.entity.EntityText;
import cn.lanink.murdermystery.room.RoomBase;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.scheduler.Task;

import java.util.ArrayList;
import java.util.Map;

public class ScanTask extends AsyncTask {

    private final RoomBase room;
    private final Player player;

    public ScanTask(RoomBase room , Player player) {
        this.room = room;
        this.player = player;
    }

    @Override
    public void onRun() {
        ArrayList<EntityText> texts = new ArrayList<>();
        for (Map.Entry<Player, Integer> entry : this.room.getPlayers().entrySet()) {
            if (entry.getValue() == 1 || entry.getValue() == 2) {
                EntityText text = new EntityText(entry.getKey().getChunk(), EntityText.getDefaultNBT(entry.getKey()), entry.getKey());
                text.spawnTo(player);
                texts.add(text);
            }
        }
        this.player.sendMessage(MurderMystery.getInstance().getLanguage().useItemScan);
        Server.getInstance().getScheduler().scheduleDelayedTask(MurderMystery.getInstance(), new Task() {
            @Override
            public void onRun(int i) {
                if (texts.size() > 0) {
                    for (EntityText text : texts) {
                        text.close();
                    }
                }
            }
        }, 100);
    }

}
