package cn.lanink.murdermystery.tasks.game;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.entity.EntityText;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.lanink.murdermystery.room.base.PlayerIdentity;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.PluginTask;

import java.util.LinkedList;
import java.util.Map;

public class ScanTask extends PluginTask<MurderMystery> {

    private final BaseRoom room;
    private final Player player;

    public ScanTask(MurderMystery owner, BaseRoom room , Player player) {
        super(owner);
        this.room = room;
        this.player = player;
    }

    @Override
    public void onRun(int i) {
        LinkedList<EntityText> texts = new LinkedList<>();
        for (Map.Entry<Player, PlayerIdentity> entry : this.room.getPlayers().entrySet()) {
            if (entry.getValue() == PlayerIdentity.COMMON_PEOPLE || entry.getValue() == PlayerIdentity.DETECTIVE) {
                EntityText text = new EntityText(entry.getKey().getChunk(), EntityText.getDefaultNBT(entry.getKey()), entry.getKey());
                text.spawnTo(this.player);
                texts.add(text);
            }
        }
        this.player.sendMessage(this.owner.getLanguage(this.player).translateString("useItemScan"));
        Server.getInstance().getScheduler().scheduleDelayedTask(this.owner, () -> {
            if (texts.size() > 0) {
                for (EntityText text : texts) {
                    text.close();
                }
            }
        }, 100);
    }

}
