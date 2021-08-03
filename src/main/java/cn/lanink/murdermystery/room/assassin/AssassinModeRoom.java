package cn.lanink.murdermystery.room.assassin;

import cn.lanink.gamecore.utils.Language;
import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.item.ItemManager;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.lanink.murdermystery.room.base.PlayerIdentity;
import cn.lanink.murdermystery.tasks.Watchdog;
import cn.lanink.murdermystery.tasks.game.assassin.AssassinDistanceTip;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.item.ItemMap;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.Config;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author lt_name
 */
public class AssassinModeRoom extends BaseRoom {

    public HashMap<Player, Player> targetMap = new HashMap<>(); //玩家-目标玩家
    public HashSet<Player> targetWait = new HashSet<>();
    public HashMap<Player, Integer> killCount = new HashMap<>();

    /**
     * 初始化
     *
     * @param level  世界
     * @param config 配置文件
     */
    public AssassinModeRoom(@NotNull Level level, @NotNull Config config) throws RoomLoadException {
        super(level, config);
    }

    @Override
    public void initData() {
        super.initData();
        if (this.targetMap != null) {
            this.targetMap.clear();
        }
        if (this.targetWait != null) {
            this.targetWait.clear();
        }
        if (this.killCount != null) {
            this.killCount.clear();
        }
    }

    /**
     * 启用监听器
     */
    @Override
    @SuppressWarnings("unchecked")
    public void enableListener() {
        this.murderMystery.getMurderMysteryListeners().get("RoomLevelProtection").addListenerRoom(this);
        this.murderMystery.getMurderMysteryListeners().get("DefaultGameListener").addListenerRoom(this);
        this.murderMystery.getMurderMysteryListeners().get("DefaultChatListener").addListenerRoom(this);
        this.murderMystery.getMurderMysteryListeners().get("DefaultDamageListener").addListenerRoom(this);
        this.murderMystery.getMurderMysteryListeners().get("AssassinDamageListener").addListenerRoom(this);
        this.murderMystery.getMurderMysteryListeners().get("AssassinGameListener").addListenerRoom(this);
    }

    @Override
    public synchronized void startGame() {
        super.startGame();
        for (Map.Entry<Player, PlayerIdentity> entry : this.getPlayers().entrySet()) {
            entry.setValue(PlayerIdentity.ASSASSIN);
            Language language = this.murderMystery.getLanguage(entry.getKey());
            entry.getKey().sendTitle(language.translateString("game_assassin_title_assassinTitle"),
                    language.translateString("game_assassin_title_assassinSubtitle"),
                    10, 40, 10);
        }
    }

    @Override
    public void scheduleTask() {
        super.scheduleTask();
        Server.getInstance().getScheduler().scheduleRepeatingTask(this.murderMystery,
                new AssassinDistanceTip(this), 5, true);
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
                            .translateString("game_assassin_wantedCountdown").replace("%time%", time + ""));
                }
                for (Player player : this.getSpectatorPlayers()) {
                    player.sendMessage(this.murderMystery.getLanguage(player)
                            .translateString("game_assassin_wantedCountdown").replace("%time%", time + ""));
                }
                Tools.playSound(this, Sound.RANDOM_CLICK);
            }
            if (time == 0) {
                this.assignIdentity();
                for (Player player : this.getPlayers().keySet()) {
                    player.sendMessage(this.murderMystery.getLanguage(player).translateString("game_assassin_wanted"));
                }
                for (Player player : this.getSpectatorPlayers()) {
                    player.sendMessage(this.murderMystery.getLanguage(player).translateString("game_assassin_wanted"));
                }
                for (Player player : this.getPlayers().keySet()) {
                    player.getInventory().setItem(1, ItemManager.get(player, 2));
                }
            }
        }

        //检查目标
        if (time < 0 && this.gameTime%2 == 0) {
            for (Player player : this.targetMap.keySet()) {
                if (!this.targetWait.contains(player) && this.getPlayers(this.targetMap.get(player)) == PlayerIdentity.DEATH) {
                    this.assignTarget(player);
                }
            }
        }

        //技能CD计算
        for (Map.Entry<Player, Integer> entry : this.killerSwordCD.entrySet()) {
            if (entry.getValue() > 0) {
                entry.setValue(entry.getValue() - 1);
            }
        }

        //计时与胜利判断
        if (this.gameTime > 0) {
            this.gameTime--;
            int playerNumber = 0;
            for (Map.Entry<Player, PlayerIdentity> entry : this.getPlayers().entrySet()) {
                if (entry.getValue() == PlayerIdentity.ASSASSIN) {
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

        Watchdog.resetTime(this);
    }

    @Override
    public void asyncTipsTask() {
        int playerNumber = this.getSurvivorPlayerNumber();
        String identity;
        for (Map.Entry<Player, PlayerIdentity> entry : this.players.entrySet()) {
            entry.getKey().setNameTag("");
            Language language = this.murderMystery.getLanguage(entry.getKey());
            if (entry.getValue() == PlayerIdentity.ASSASSIN) {
                identity = language.translateString("player_identity_assassin");
            } else {
                identity = language.translateString("death");
            }
            LinkedList<String> ms = new LinkedList<>(Arrays.asList(language.translateString("gameTimeScoreBoard")
                    .replace("%roomMode%", Tools.getStringRoomMode(entry.getKey(), this))
                    .replace("%identity%", identity)
                    .replace("%playerNumber%", playerNumber + "")
                    .replace("%time%", this.gameTime + "").split("\n")));
            ms.add(" ");
            ms.add(language.translateString("game_assassin_scoreboard_killCount")
                    .replace("%count%", this.killCount.getOrDefault(entry.getKey(), 0) + ""));
            ms.add("  ");
            Integer cd = this.killerSwordCD.getOrDefault(entry.getKey(), 0);
            if (cd > 0) {
                ms.add(language.translateString("gameSwordCDScoreBoard")
                        .replace("%time%", cd + ""));
            }
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
            this.assignTarget(player);
        }
    }

    /**
     * 分配目标
     *
     * @param player 需要分配目标的玩家
     */
    public void assignTarget(@NotNull Player player) {
        if (this.getPlayers(player) != PlayerIdentity.ASSASSIN) {
            return;
        }
        ArrayList<Player> survivingPlayers = new ArrayList<>();
        for (Map.Entry<Player, PlayerIdentity> entry : this.getPlayers().entrySet()) {
            if (entry.getKey() != player && entry.getValue() == PlayerIdentity.ASSASSIN) {
                survivingPlayers.add(entry.getKey());
            }
        }
        if (survivingPlayers.isEmpty()) {
            if (MurderMystery.debug) {
                this.murderMystery.getLogger().error("[debug] 分配目标时 存活玩家为空");
            }
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
        player.getInventory().setItem(3, item);
        item.sendImage(player);
        player.sendTitle("", this.murderMystery.getLanguage(player).translateString("game_assassin_assignTarget"));
        if (MurderMystery.debug) {
            String message = "[debug] " + player.getName() + " 的目标是：" + target.getName();
            this.murderMystery.getLogger().info(message);
            player.sendMessage(message);
        }
    }

    @Override
    public void playerDamage(@NotNull Player damager, @NotNull Player player) {
        if (this.targetMap.get(damager) == player) { //杀死目标
            damager.sendTitle("",
                    this.murderMystery.getLanguage(damager).translateString("game_assassin_killTarget"));
            damager.getOffhandInventory().clearAll();
            this.targetWait.add(damager);
            this.killCount.put(damager, this.killCount.getOrDefault(damager, 0) + 1);
            Server.getInstance().getScheduler().scheduleDelayedTask(this.murderMystery,
                    () -> this.assignTarget(damager), 60);
            player.sendTitle(this.murderMystery.getLanguage(player).translateString("deathTitle"),
                    this.murderMystery.getLanguage(player).translateString("game_assassin_deathByAssassin"));
            this.playerDeath(player);
        }else if (this.targetMap.get(player) == damager) { //反杀
            damager.sendTitle("", this.murderMystery.getLanguage(damager).translateString("game_assassin_targetKillAssassin"));
            this.killCount.put(damager, this.killCount.getOrDefault(damager, 0) + 1);
            player.sendTitle(this.murderMystery.getLanguage(player).translateString("deathTitle"),
                    this.murderMystery.getLanguage(player).translateString("game_assassin_deathByTarget"));
            this.playerDeath(player);
        }else { //找错
            damager.addEffect(Effect.getEffect(2).setAmplifier(2).setDuration(100).setVisible(false));//缓慢
            damager.addEffect(Effect.getEffect(15).setDuration(100).setVisible(false));//失明
            Tools.playSound(damager, Sound.RANDOM_ANVIL_LAND);
            damager.sendTitle("", this.murderMystery.getLanguage(damager).translateString("game_assassin_errorTarget"));
            damager.getInventory().remove(ItemManager.get(damager, 2));
            Server.getInstance().getScheduler().scheduleDelayedTask(this.murderMystery,
                    () -> damager.getInventory().setItem(1, ItemManager.get(damager, 2)), 100);
        }
    }

    @Override
    public void playerDeath(@NotNull Player player) {
        super.playerDeath(player);
        this.targetMap.remove(player);
        if (this.targetMap.containsValue(player)) {
            for (Map.Entry<Player, Player> entry : this.targetMap.entrySet()) {
                if (entry.getValue() == player && this.getPlayers(entry.getKey()) == PlayerIdentity.ASSASSIN) {
                    this.assignTarget(entry.getKey());
                }
            }
        }
    }

}
