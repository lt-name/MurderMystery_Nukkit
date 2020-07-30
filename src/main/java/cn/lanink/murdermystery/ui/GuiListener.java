package cn.lanink.murdermystery.ui;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.utils.Language;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;

public class GuiListener implements Listener {

    private final MurderMystery murderMystery;
    private final Language language;

    public GuiListener(MurderMystery murderMystery) {
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
        GuiType cache = this.murderMystery.getGuiCache().get(event.getFormID());
        if (cache == null) return;
        this.murderMystery.getGuiCache().remove(event.getFormID());
        String uName = this.murderMystery.getCmdUser();
        String aName = this.murderMystery.getCmdAdmin();
        if (event.getWindow() instanceof FormWindowSimple) {
            FormWindowSimple simple = (FormWindowSimple) event.getWindow();
            switch (cache) {
                case USER_MENU:
                    switch (simple.getResponse().getClickedButtonId()) {
                        case 0:
                            this.murderMystery.getServer().dispatchCommand(player, uName + " join");
                            break;
                        case 1:
                            this.murderMystery.getServer().dispatchCommand(player, uName + " quit");
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
                            this.murderMystery.getServer().dispatchCommand(player, aName + " setwaitspawn");
                            break;
                        case 1:
                            this.murderMystery.getServer().dispatchCommand(player, aName + " addrandomspawn");
                            break;
                        case 2:
                            this.murderMystery.getServer().dispatchCommand(player, aName + " addgoldspawn");
                            break;
                        case 3:
                            GuiCreate.sendAdminTimeMenu(player);
                            break;
                        case 4:
                            GuiCreate.sendAdminModeMenu(player);
                            break;
                        case 5:
                            this.murderMystery.getServer().dispatchCommand(player, aName + " reloadroom");
                            break;
                        case 6:
                            this.murderMystery.getServer().dispatchCommand(player, aName + " unloadroom");
                            break;
                    }
                    break;
            }
        }else if (event.getWindow() instanceof FormWindowCustom) {
            FormWindowCustom custom = (FormWindowCustom) event.getWindow();
            switch (cache) {
                case ADMIN_TIME_MENU:
                    MurderMystery.getInstance().getServer().dispatchCommand(player, aName + " setgoldspawntime " + custom.getResponse().getInputResponse(0));
                    MurderMystery.getInstance().getServer().dispatchCommand(player, aName + " setwaittime " + custom.getResponse().getInputResponse(1));
                    MurderMystery.getInstance().getServer().dispatchCommand(player, aName + " setgametime " + custom.getResponse().getInputResponse(2));
                    break;
                case ADMIN_MODE_MENU:
                    this.murderMystery.getServer().dispatchCommand(player, this.murderMystery.getCmdAdmin() + " setgamemode " +
                            custom.getResponse().getDropdownResponse(0).getElementContent());
                    break;
            }
        }else if (event.getWindow() instanceof FormWindowModal) {
            FormWindowModal modal = (FormWindowModal) event.getWindow();
            if (cache == GuiType.ROOM_JOIN_OK) {
                if (modal.getResponse().getClickedButtonId() == 0 && !modal.getButton1().equals(this.language.buttonReturn)) {
                    String[] s = modal.getContent().split("\"");
                    MurderMystery.getInstance().getServer().dispatchCommand(
                            player, uName + " join " + s[1].replace("§e§l", "").trim());
                }else {
                    GuiCreate.sendRoomListMenu(player);
                }
            }
        }
    }

}