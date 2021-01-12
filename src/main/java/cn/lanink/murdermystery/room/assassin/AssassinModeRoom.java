package cn.lanink.murdermystery.room.assassin;

import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.item.ItemMap;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.utils.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lt_name
 */
public class AssassinModeRoom extends BaseRoom {

    public HashMap<Player, Player> targetMap= new HashMap<>(); //玩家-目标玩家

    /**
     * 初始化
     *
     * @param level  世界
     * @param config 配置文件
     */
    public AssassinModeRoom(Level level, Config config) throws RoomLoadException {
        super(level, config);
    }

    /**
     * 启用监听器
     */
    @SuppressWarnings("unchecked")
    public void enableListener() {
        this.murderMystery.getMurderMysteryListeners().get("RoomLevelProtection").addListenerRoom(this);
        this.murderMystery.getMurderMysteryListeners().get("DefaultGameListener").addListenerRoom(this);
        this.murderMystery.getMurderMysteryListeners().get("DefaultChatListener").addListenerRoom(this);
        this.murderMystery.getMurderMysteryListeners().get("AssassinDamageListener").addListenerRoom(this);
    }

    @Override
    protected void victoryReward(int victory) {

    }

    /**
     * 计时Task
     */
    @Override
    public void timeTask() {
        //开局20秒后给物品
        int time = this.gameTime - (this.setGameTime - 20);
        if (time >= 0) {
            if ((time%5 == 0 && time != 0) || (time <= 5 && time != 0)) {
                for (Player player : this.getPlayers().keySet()) {
                    player.sendMessage(this.murderMystery.getLanguage(player)
                            .translateString("killerGetSwordTime").replace("%time%", time + ""));
                }
                for (Player player : this.getSpectatorPlayers()) {
                    player.sendMessage(this.murderMystery.getLanguage(player)
                            .translateString("killerGetSwordTime").replace("%time%", time + ""));
                }
                Tools.playSound(this, Sound.RANDOM_CLICK);
            }
            if (time == 0) {
                this.assignIdentity();
                for (Player player : this.getPlayers().keySet()) {
                    player.sendMessage(this.murderMystery.getLanguage(player).translateString("killerGetSword"));
                }
                for (Player player : this.getSpectatorPlayers()) {
                    player.sendMessage(this.murderMystery.getLanguage(player).translateString("killerGetSword"));
                }
                for (Map.Entry<Player, Integer> entry : this.getPlayers().entrySet()) {
                    if (entry.getValue() == 2) {
                        Tools.giveItem(entry.getKey(), 1);
                    }else if (entry.getValue() == 3) {
                        Tools.giveItem(entry.getKey(), 2);
                    }
                }
            }
        }
        //计时与胜利判断
        if (this.gameTime > 0) {
            this.gameTime--;
            int playerNumber = 0;
            for (Map.Entry<Player, Integer> entry : this.getPlayers().entrySet()) {
                if (entry.getValue() == 3) {
                    playerNumber++;
                }
            }
            if (playerNumber <= 1) {
                //TODO 胜利


            }
        }else {
            this.victory(0);
        }
        this.goldSpawn();
    }

    @Override
    protected void assignIdentity() {
        for (Player player : this.getPlayers().keySet()) {
            this.getPlayers().put(player, 3);
            player.getInventory().setItem(1, Tools.getMurderItem(player, 2));
        }
        for (Player player : this.getPlayers().keySet()) {
            this.assignTarget(player);
        }
    }

    public void assignTarget(Player player) {
        if (this.getPlayers(player) != 3) {
            return;
        }
        ArrayList<Player> survivingPlayers = new ArrayList<>();
        for (Map.Entry<Player, Integer> entry : this.getPlayers().entrySet()) {
            if (entry.getKey() != player && entry.getValue() == 3) {
                survivingPlayers.add(entry.getKey());
            }
        }
        if (survivingPlayers.isEmpty()) {
            return;
        }
        Collections.shuffle(survivingPlayers, MurderMystery.RANDOM);

        Player target = null;
        for (Player p : survivingPlayers) {
            if (!this.targetMap.containsValue(p)) {
                target = p;
                break;
            }
        }
        if (target == null) {
            target = survivingPlayers.get(MurderMystery.RANDOM.nextInt(survivingPlayers.size()));
        }

        this.targetMap.put(player, target);
        ItemMap item = this.getGameSkin(target).getItemMap();
        player.getOffhandInventory().setItem(0, item);
        item.sendImage(player);
        //TODO
        player.sendTitle("", "已分配新的刺杀目标！");
        if (MurderMystery.debug) {
            String message = "[debug] " + player.getName() + " 的目标是：" + target.getName();
            this.murderMystery.getLogger().info(message);
            player.sendMessage(message);
        }

    }

    public void playerDeath(Player player) {
        super.playerDeath(player);
        this.targetMap.remove(player);
    }

}
