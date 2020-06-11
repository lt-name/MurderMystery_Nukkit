package cn.lanink.murdermystery.listener;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.entity.EntityPlayerCorpse;
import cn.lanink.murdermystery.event.MurderPlayerCorpseSpawnEvent;
import cn.lanink.murdermystery.event.MurderPlayerDeathEvent;
import cn.lanink.murdermystery.event.MurderRoomChooseIdentityEvent;
import cn.lanink.murdermystery.event.MurderRoomStartEvent;
import cn.lanink.murdermystery.room.Room;
import cn.lanink.murdermystery.tasks.game.GoldTask;
import cn.lanink.murdermystery.tasks.game.TimeTask;
import cn.lanink.murdermystery.tasks.game.TipsTask;
import cn.lanink.murdermystery.utils.Language;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

import java.util.LinkedHashMap;
import java.util.Random;

/**
 * 游戏监听器（插件事件）
 * @author lt_name
 */
public class MurderListener implements Listener {

    private final MurderMystery murderMystery;
    private final Language language;

    public MurderListener(MurderMystery murderMystery) {
        this.murderMystery = murderMystery;
        this.language = murderMystery.getLanguage();
    }

    /**
     * 房间开始事件
     * @param event 事件
     */
    @EventHandler
    public void onRoomStart(MurderRoomStartEvent event) {
        Room room = event.getRoom();
        room.setMode(2);
        Server.getInstance().getPluginManager().callEvent(new MurderRoomChooseIdentityEvent(room));
        int x=0;
        for (Player player : room.getPlayers().keySet()) {
            if (x >= room.getRandomSpawn().size()) {
                x = 0;
            }
            player.teleport(room.getRandomSpawn().get(x));
            x++;
        }
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                MurderMystery.getInstance(), new TimeTask(this.murderMystery, room), 20,true);
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                MurderMystery.getInstance(), new GoldTask(this.murderMystery, room), 20, true);
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                MurderMystery.getInstance(), new TipsTask(this.murderMystery, room), 18, true);
    }

    /**
     * 玩家分配身份事件
     * @param event 事件
     */
    @EventHandler
    public void onChooseIdentity(MurderRoomChooseIdentityEvent event) {
        if (event.isCancelled()) return;
        Room room = event.getRoom();
        LinkedHashMap<Player, Integer> players = room.getPlayers();
        Random random = new Random();
        int random1 = random.nextInt(players.size()) + 1;
        int random2;
        do {
            random2 = random.nextInt(players.size()) + 1;
        }while (random1 == random2);
        int j = 0;
        for (Player player : players.keySet()) {
            player.getInventory().clearAll();
            j++;
            //侦探
            if (j == random1) {
                room.addPlaying(player, 2);
                player.sendTitle(this.language.titleDetectiveTitle,
                        this.language.titleDetectiveSubtitle, 10, 40, 10);
                continue;
            }
            //杀手
            if (j == random2) {
                room.addPlaying(player, 3);
                player.sendTitle(this.language.titleKillerTitle,
                        this.language.titleKillerSubtitle, 10, 40, 10);
                continue;
            }
            room.addPlaying(player, 1);
            player.sendTitle(this.language.titleCommonPeopleTitle,
                    this.language.titleCommonPeopleSubtitle, 10, 40, 10);
        }
    }

    /**
     * 玩家死亡事件（游戏中死亡）
     * @param event 事件
     */
    @EventHandler
    public void onPlayerDeath(MurderPlayerDeathEvent event) {
        if (!event.isCancelled()) {
            Player player = event.getPlayer();
            Room room = event.getRoom();
            if (player == null || room == null) {
                return;
            }
            player.getInventory().clearAll();
            player.setAllowModifyWorld(true);
            player.setAdventureSettings((new AdventureSettings(player)).set(AdventureSettings.Type.ALLOW_FLIGHT, true));
            player.setGamemode(3);
            if (room.getPlayerMode(player) == 2) {
                room.getLevel().dropItem(player, Tools.getMurderItem(1));
            }
            room.addPlaying(player, 0);
            Tools.setPlayerInvisible(player, true);
            Tools.addSound(room, Sound.GAME_PLAYER_HURT);
            Server.getInstance().getPluginManager().callEvent(new MurderPlayerCorpseSpawnEvent(room, player));
        }
    }

    /**
     * 尸体生成事件
     * @param event 事件
     */
    @EventHandler
    public void onCorpseSpawn(MurderPlayerCorpseSpawnEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Room room = event.getRoom();
        if (player == null || room == null) return;
        Skin skin = room.getPlayerSkin(player);
        switch(skin.getSkinData().data.length) {
            case 8192:
            case 16384:
            case 32768:
            case 65536:
                break;
            default:
                skin = this.murderMystery.getCorpseSkin();
        }
        CompoundTag nbt = EntityPlayerCorpse.getDefaultNBT(player);
        nbt.putCompound("Skin", new CompoundTag()
                .putByteArray("Data", skin.getSkinData().data)
                .putString("ModelId", skin.getSkinId()));
        nbt.putFloat("Scale", -1.0F);
        EntityPlayerCorpse ent = new EntityPlayerCorpse(player.getChunk(), nbt);
        ent.setSkin(skin);
        ent.setPosition(new Vector3(player.getFloorX(), Tools.getFloorY(player), player.getFloorZ()));
        ent.setGliding(true);
        ent.setRotation(player.getYaw(), 0);
        ent.spawnToAll();
        ent.updateMovement();
    }

}
