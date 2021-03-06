package cn.lanink.murdermystery.form;

import cn.lanink.gamecore.utils.Language;
import cn.lanink.murdermystery.MurderMystery;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;

import java.util.ArrayList;

/**
 * @author lt_name
 */
public class GuiListener implements Listener {

    private final Server server;
    private final MurderMystery murderMystery;

    public GuiListener(MurderMystery murderMystery) {
        this.server = murderMystery.getServer();
        this.murderMystery = murderMystery;
    }

    /**
     * 玩家操作ui事件
     * 直接执行现有命令，减小代码重复量，也便于维护
     * @param event 事件
     */
    @EventHandler
    public void onPlayerFormResponded(PlayerFormRespondedEvent event) {
        Player player = event.getPlayer();
        if (player == null || event.getWindow() == null) {
            return;
        }
        Language language = this.murderMystery.getLanguage(player);
        GuiType cache = GuiCreate.UI_CACHE.containsKey(player) ? GuiCreate.UI_CACHE.get(player).get(event.getFormID()) : null;
        if (cache == null) {
            return;
        }
        GuiCreate.UI_CACHE.get(player).remove(event.getFormID());
        if (event.getResponse() == null) {
            return;
        }
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
                    if (simple.getResponse().getClickedButton().getText().equals(language.translateString("buttonReturn"))) {
                        GuiCreate.sendUserMenu(player);
                    }else {
                        ArrayList<String> rooms = new ArrayList<>(this.murderMystery.getRooms().keySet());
                        if (rooms.size() >= simple.getResponse().getClickedButtonId()) {
                            GuiCreate.sendRoomJoinOkMenu(player, rooms.get(simple.getResponse().getClickedButtonId()));
                        }
                    }
                    break;
                case ADMIN_MENU:
                    switch (simple.getResponse().getClickedButtonId()) {
                        case 0:
                            this.server.dispatchCommand(player, aName + " CreateRoom");
                            break;
                        case 1:
                            this.server.dispatchCommand(player, aName + " SetRoom");
                            break;
                        case 3:
                            this.server.dispatchCommand(player, aName + " ReloadRoom");
                            break;
                        case 4:
                            this.server.dispatchCommand(player, aName + " UnloadRoom");
                            break;
                    }
                    break;
                case ADMIN_CREATE_ROOM_MENU:
                    this.server.dispatchCommand(player, aName + " CreateRoom " +
                            simple.getResponse().getClickedButton().getText());
                    break;
                case ADMIN_SET_ROOM_MENU:
                    this.server.dispatchCommand(player, aName + " SetRoom " +
                            simple.getResponse().getClickedButton().getText());
                    break;
            }
        }else if (event.getWindow() instanceof FormWindowCustom) {
            FormWindowCustom custom = (FormWindowCustom) event.getWindow();
            switch (cache) {
                case ADMIN_ROOM_NAME_MENU:
                    this.server.dispatchCommand(player, aName + " setroomname " + custom.getResponse().getInputResponse(0));
                    break;
                case ADMIN_MORE_MENU:
                    this.server.dispatchCommand(player, aName + " setgoldspawntime " + custom.getResponse().getInputResponse(0));
                    this.server.dispatchCommand(player, aName + " setwaittime " + custom.getResponse().getInputResponse(1));
                    this.server.dispatchCommand(player, aName + " setgametime " + custom.getResponse().getInputResponse(2));
                    this.server.dispatchCommand(player, aName + " setminplayers " + custom.getResponse().getInputResponse(3));
                    this.server.dispatchCommand(player, aName + " setmaxplayers " + custom.getResponse().getInputResponse(4));
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
                    if (language.translateString("buttonOK").equals(modal.getResponse().getClickedButtonText())) {
                        this.server.dispatchCommand(player, uName + " join " + roomName);
                        return;
                    }else if (language.translateString("buttonSpectator").equals(modal.getResponse().getClickedButtonText())) {
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