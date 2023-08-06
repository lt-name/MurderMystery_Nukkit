package cn.lanink.murdermystery.listener.defaults;

import cn.lanink.gamecore.utils.PlayerDataUtils;
import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.lanink.murdermystery.tasks.admin.SetRoomTask;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerLocallyInitializedEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.player.PlayerTeleportEvent;

import java.io.File;
import java.util.LinkedHashMap;

/**
 * 玩家进入/退出服务器 或传送到其他世界时，退出房间
 */
@SuppressWarnings("unused")
public class PlayerJoinAndQuit implements Listener {

    private final MurderMystery murderMystery;

    public PlayerJoinAndQuit(MurderMystery murderMystery) {
        this.murderMystery = murderMystery;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        String languageCode = player.getLoginChainData().getLanguageCode();
        this.murderMystery.getPlayerLanguage().put(player, languageCode);
        if (MurderMystery.debug) {
            this.murderMystery.getLogger().info("[debug] Player: " + player.getName() + " LanguageCode: " + languageCode);
        }
        if (this.murderMystery.getRooms().containsKey(player.getLevel().getFolderName())) {
            Server.getInstance().getScheduler().scheduleDelayedTask(this.murderMystery, () -> {
                if (player.isOnline()) {
                    Tools.rePlayerState(player ,false);
                    File file = new File(this.murderMystery.getDataFolder() + "/PlayerInventory/" + player.getName() + ".json");
                    if (file.exists()) {
                        PlayerDataUtils.PlayerData playerData = PlayerDataUtils.create(player, file);
                        if (file.delete()) {
                            playerData.restoreAll();
                        }
                    }
                    player.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
                }
            }, 1);
        }
    }

    @EventHandler
    public void onPlayerLocallyInitialized(PlayerLocallyInitializedEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        if (this.murderMystery.isAutomaticJoinGame()) {
            Server.getInstance().dispatchCommand(player, this.murderMystery.getCmdUser() + " join");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        for (BaseRoom room : this.murderMystery.getRooms().values()) {
            if (room.isPlaying(player) || room.isSpectator(player)) {
                room.quitRoom(player);
            }
        }
        SetRoomTask task = this.murderMystery.setRoomTask.get(player);
        if (task != null) {
            task.cancel();
        }
        this.murderMystery.getPlayerLanguage().remove(player);

    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerTp(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        String fromLevel = event.getFrom().getLevel() == null ? null : event.getFrom().getLevel().getFolderName();
        String toLevel = event.getTo().getLevel()== null ? null : event.getTo().getLevel().getFolderName();
        if (player == null || fromLevel == null || toLevel == null) return;
        if (!fromLevel.equals(toLevel)) {
            LinkedHashMap<String, BaseRoom> rooms = this.murderMystery.getRooms();
            if (rooms.containsKey(fromLevel) &&
                    (rooms.get(fromLevel).isPlaying(player) || rooms.get(fromLevel).isSpectator(player))) {
                rooms.get(fromLevel).quitRoom(player);
            }else if (!player.isOp() && rooms.containsKey(toLevel) &&
                    !rooms.get(toLevel).isPlaying(player) && !rooms.get(toLevel).isSpectator(player)) {
                event.setCancelled(true);
                player.sendMessage(this.murderMystery.getLanguage(player).translateString("tpJoinRoomLevel"));
            }
        }
    }

}
