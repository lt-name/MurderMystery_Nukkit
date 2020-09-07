package cn.lanink.murdermystery.ui;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.utils.Language;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;

/**
 * @author lt_name
 */
public class GuiListener implements Listener {

    private final Server server;
    private final MurderMystery murderMystery;
    private final Language language;

    public GuiListener(MurderMystery murderMystery) {
        this.server = murderMystery.getServer();
        this.murderMystery = murderMystery;
        this.language = murderMystery.getLanguage();
    }

    /**
     * 玩家操作ui事件
     * 直接执行现有命令，减小代码重复量，也便于维护
     * @param event 事件
     */
    @EventHandler
    public void onPlayerFormResponded(PlayerFormRespondedEvent event) {
        Player player = event.getPlayer();
        if (player == null || event.getWindow() == null || event.getResponse() == null) {
            return;
        }
        GuiType cache = GuiCreate.UI_CACHE.containsKey(player) ? GuiCreate.UI_CACHE.get(player).get(event.getFormID()) : null;
        if (cache == null) {
            return;
        }
        GuiCreate.UI_CACHE.get(player).remove(event.getFormID());
        String uName = this.murderMystery.getCmdUser();
        String aName = this.murderMystery.getCmdAdmin();
        if (event.getWindow() instanceof FormWindowSimple) {
            FormWindowSimple simple = (FormWindowSimple) event.getWindow();
            switch (cache) {
                case USER_MENU:
                    switch (simple.getResponse().getClickedButtonId()) {
                        case 0:
                            this.server.dispatchCommand(player, uName + " join");
                            break;
                        case 1:
                            this.server.dispatchCommand(player, uName + " quit");
                            break;
                        case 2:
                            GuiCreate.sendRoomListMenu(player);
                            break;
                    }
                    break;
                case ROOM_LIST_MENU:
                    if (simple.getResponse().getClickedButton().getText().equals(this.language.buttonReturn)) {
                        GuiCreate.sendUserMenu(player);
                    }else {
                        GuiCreate.sendRoomJoinOkMenu(player,
                                simple.getResponse().getClickedButton().getText().split("\n")[0]);
                    }
                    break;
                case ADMIN_MENU:
                    switch (simple.getResponse().getClickedButtonId()) {
                        case 0:
                            this.server.dispatchCommand(player, aName + " setwaitspawn");
                            break;
                        case 1:
                            this.server.dispatchCommand(player, aName + " addrandomspawn");
                            break;
                        case 2:
                            this.server.dispatchCommand(player, aName + " addgoldspawn");
                            break;
                        case 3:
                            GuiCreate.sendAdminTimeMenu(player);
                            break;
                        case 4:
                            GuiCreate.sendAdminPlayersMenu(player);
                            break;
                        case 5:
                            GuiCreate.sendAdminModeMenu(player);
                            break;
                        case 6:
                            this.server.dispatchCommand(player, aName + " reloadroom");
                            break;
                        case 7:
                            this.server.dispatchCommand(player, aName + " unloadroom");
                            break;
                    }
                    break;
            }
        }else if (event.getWindow() instanceof FormWindowCustom) {
            FormWindowCustom custom = (FormWindowCustom) event.getWindow();
            switch (cache) {
                case ADMIN_TIME_MENU:
                    this.server.dispatchCommand(player, aName + " setgoldspawntime " + custom.getResponse().getInputResponse(0));
                    this.server.dispatchCommand(player, aName + " setwaittime " + custom.getResponse().getInputResponse(1));
                    this.server.dispatchCommand(player, aName + " setgametime " + custom.getResponse().getInputResponse(2));
                    break;
                case ADMIN_PLAYERS_MENU:
                    this.server.dispatchCommand(player, aName + " setminplayers " + custom.getResponse().getInputResponse(0));
                    this.server.dispatchCommand(player, aName + " setmaxplayers " + custom.getResponse().getInputResponse(1));
                    break;
                case ADMIN_MODE_MENU:
                    this.server.dispatchCommand(player, this.murderMystery.getCmdAdmin() + " setgamemode " +
                            custom.getResponse().getDropdownResponse(0).getElementContent());
                    break;
            }
        }else if (event.getWindow() instanceof FormWindowModal) {
            FormWindowModal modal = (FormWindowModal) event.getWindow();
            if (cache == GuiType.ROOM_JOIN_OK) {
                try {
                    String roomName = modal.getContent().split("§7§k@")[1];
                    if (this.language.buttonOK.equals(modal.getResponse().getClickedButtonText())) {
                        this.server.dispatchCommand(player, uName + " join " + roomName);
                        return;
                    }else if (this.language.buttonSpectator.equals(modal.getResponse().getClickedButtonText())) {
                        this.server.dispatchCommand(player, uName + " joinspectator " + roomName);
                        return;
                    }
                } catch (Exception ignored) {

                }
                GuiCreate.sendRoomListMenu(player);
            }
        }
    }

}