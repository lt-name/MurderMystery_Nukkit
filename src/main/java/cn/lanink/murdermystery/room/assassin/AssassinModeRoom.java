package cn.lanink.murdermystery.room.assassin;

import cn.lanink.gamecore.utils.Language;
import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.item.ItemMap;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.utils.Config;

import java.util.*;

/**
 * @author lt_name
 */
public class AssassinModeRoom extends BaseRoom {

    public HashMap<Player, Player> targetMap= new HashMap<>(); //玩家-目标玩家
    public HashSet<Player> targetWait = new HashSet<>();

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
                for (Player player : this.getPlayers().keySet()) {
                    player.getInventory().setItem(1, Tools.getMurderMysteryItem(player, 2));
                }
            }
        }
        //检查目标
        if (time < 0 && this.gameTime%2 == 0) {
            for (Player player : this.targetMap.keySet()) {
                if (!this.targetWait.contains(player) && this.getPlayers(this.targetMap.get(player)) != 3) {
                    this.assignTarget(player);
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
                //TODO 优化
                this.victory(3);
            }
        }else {
            this.victory(0);
        }
        this.goldSpawn();
    }

    @Override
    public void asyncTipsTask() {
        int time = this.setGameTime - this.gameTime;
        int playerNumber = this.getSurvivorPlayerNumber();
        String identity;
        for (Map.Entry<Player, Integer> entry : this.players.entrySet()) {
            entry.getKey().setNameTag("");
            Language language = this.murderMystery.getLanguage(entry.getKey());
            switch (entry.getValue()) {
                case 1:
                    identity = language.translateString("commonPeople");
                    break;
                case 2:
                    identity = language.translateString("detective");
                    break;
                case 3:
                    identity = language.translateString("killer");
                    break;
                default:
                    if (time <= 20) {
                        identity = "???";
                    }else {
                        identity = language.translateString("death");
                    }
                    break;
            }
            LinkedList<String> ms = new LinkedList<>(Arrays.asList(language.translateString("gameTimeScoreBoard")
                    .replace("%roomMode%", Tools.getStringRoomMode(entry.getKey(), this))
                    .replace("%identity%", identity)
                    .replace("%playerNumber%", playerNumber + "")
                    .replace("%time%", this.gameTime + "").split("\n")));
            ms.add(" ");
            this.murderMystery.getScoreboard().showScoreboard(entry.getKey(), language.translateString("scoreBoardTitle"), ms);
        }
        //旁观玩家只显示部分信息
        for (Player player : this.spectatorPlayers) {
            Language language = this.murderMystery.getLanguage(player);
            LinkedList<String> ms = new LinkedList<>(Arrays.asList(language.translateString("gameTimeScoreBoard")
                    .replace("%roomMode%", Tools.getStringRoomMode(player, this))
                    .replace("%identity%", language.translateString("spectator"))
                    .replace("%playerNumber%", playerNumber + "")
                    .replace("%time%", this.gameTime + "").split("\n")));
            ms.add(" ");
            this.murderMystery.getScoreboard().showScoreboard(player, language.translateString("scoreBoardTitle"), ms);
        }
    }

    @Override
    protected void assignIdentity() {
        for (Player player : this.getPlayers().keySet()) {
            this.getPlayers().put(player, 3);
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
        this.targetWait.remove(player);
        ItemMap item = this.getGameSkin(target).getItemMap();
        player.getOffhandInventory().setItem(0, item);
        item.sendImage(player);
        player.sendTitle("", this.murderMystery.getLanguage(player).translateString("game_assassin_assignTarget"));
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
