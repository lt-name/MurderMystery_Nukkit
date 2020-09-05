package cn.lanink.murdermystery.ui;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.lanink.murdermystery.room.base.IRoomStatus;
import cn.lanink.murdermystery.utils.Language;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.scheduler.Task;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GuiCreate {

    private static final MurderMystery MURDER_MYSTERY = MurderMystery.getInstance();
    private static final Language LANGUAGE = MURDER_MYSTERY.getLanguage();
    public static final String PLUGIN_NAME = "§l§7[§1M§2u§3r§4d§5e§6r§aM§cy§bs§dt§9e§6r§2y§7]";
    public static final HashMap<Player, HashMap<Integer, GuiType>> UI_CACHE = new HashMap<>();

    /**
     * 显示用户菜单
     * @param player 玩家
     */
    public static void sendUserMenu(Player player) {
        FormWindowSimple simple = new FormWindowSimple(PLUGIN_NAME, "");
        simple.addButton(new ElementButton(LANGUAGE.userMenuButton1, new ElementButtonImageData("path", "textures/ui/switch_start_button")));
        simple.addButton(new ElementButton(LANGUAGE.userMenuButton2, new ElementButtonImageData("path", "textures/ui/switch_select_button")));
        simple.addButton(new ElementButton(LANGUAGE.userMenuButton3, new ElementButtonImageData("path", "textures/ui/servers")));
        showFormWindow(player, simple, GuiType.USER_MENU);
    }

    /**
     * 显示管理菜单
     * @param player 玩家
     */
    public static void sendAdminMenu(Player player) {
        FormWindowSimple simple = new FormWindowSimple(PLUGIN_NAME, LANGUAGE.adminMenuSetLevel.replace("%name%", player.getLevel().getFolderName()));
        simple.addButton(new ElementButton(LANGUAGE.adminMenuButton1, new ElementButtonImageData("path", "textures/ui/World")));
        simple.addButton(new ElementButton(LANGUAGE.adminMenuButton2, new ElementButtonImageData("path", "textures/ui/World")));
        simple.addButton(new ElementButton(LANGUAGE.adminMenuButton3, new ElementButtonImageData("path", "textures/ui/World")));
        simple.addButton(new ElementButton(LANGUAGE.adminMenuButton4, new ElementButtonImageData("path", "textures/ui/timer")));
        simple.addButton(new ElementButton(LANGUAGE.adminMenuButton5, new ElementButtonImageData("path", "textures/ui/dev_glyph_color")));
        simple.addButton(new ElementButton(LANGUAGE.adminMenuButton6,  new ElementButtonImageData("path", "textures/ui/refresh_light")));
        simple.addButton(new ElementButton(LANGUAGE.adminMenuButton7, new ElementButtonImageData("path", "textures/ui/redX1")));
        showFormWindow(player, simple, GuiType.ADMIN_MENU);
    }

    /**
     * 显示设置时间菜单
     * @param player 玩家
     */
    public static void sendAdminTimeMenu(Player player) {
        FormWindowCustom custom = new FormWindowCustom(PLUGIN_NAME);
        custom.addElement(new ElementInput(LANGUAGE.adminTimeMenuInputText1, "", "20"));
        custom.addElement(new ElementInput(LANGUAGE.adminTimeMenuInputText2, "", "60"));
        custom.addElement(new ElementInput(LANGUAGE.adminTimeMenuInputText3, "", "300"));
        showFormWindow(player, custom, GuiType.ADMIN_TIME_MENU);
    }

    /**
     * 设置房间模式菜单
     * @param player 玩家
     */
    public static void sendAdminModeMenu(Player player) {
        FormWindowCustom custom = new FormWindowCustom(PLUGIN_NAME);
        custom.addElement(new ElementDropdown("\n\n\n" +
                LANGUAGE.adminMenuSetLevel.replace("%name%", player.getLevel().getName()),
                Arrays.asList(MurderMystery.getRoomClass().keySet().toArray(new String[0]))));
        showFormWindow(player, custom, GuiType.ADMIN_MODE_MENU);
    }

    /**
     * 显示房间列表菜单
     * @param player 玩家
     */
    public static void sendRoomListMenu(Player player) {
        FormWindowSimple simple = new FormWindowSimple(PLUGIN_NAME, "");
        for (Map.Entry<String, BaseRoom> entry : MurderMystery.getInstance().getRooms().entrySet()) {
            simple.addButton(new ElementButton("§e§l" + entry.getKey() +
                    "\n§r§eMode: " + Tools.getStringRoomMode(entry.getValue()) +
                    " Player: " + entry.getValue().getPlayers().size() + "/" + entry.getValue().getMaxPlayers(),
                    new ElementButtonImageData("path", "textures/ui/switch_start_button")));
        }
        simple.addButton(new ElementButton(LANGUAGE.buttonReturn, new ElementButtonImageData("path", "textures/ui/cancel")));
        showFormWindow(player, simple, GuiType.ROOM_LIST_MENU);
    }

    /**
     * 加入房间确认(自选)
     * @param player 玩家
     * @param roomName 房间名字
     */
    public static void sendRoomJoinOkMenu(Player player, String roomName) {
        //TODO 观战
        FormWindowModal modal;
        roomName = roomName.replace("§e§l", "").trim();
        BaseRoom room = MurderMystery.getInstance().getRooms().get(roomName);
        if (room != null) {
            if (room.getStatus() == IRoomStatus.ROOM_STATUS_LEVEL_NOT_LOADED) {
                modal = new FormWindowModal(PLUGIN_NAME, LANGUAGE.joinRoomIsNeedInitialized, LANGUAGE.buttonReturn, LANGUAGE.buttonReturn);
            }else if (room.getStatus() == IRoomStatus.ROOM_STATUS_GAME ||
                    room.getStatus() == IRoomStatus.ROOM_STATUS_VICTORY) {
                modal = new FormWindowModal(
                        PLUGIN_NAME, LANGUAGE.joinRoomIsPlaying + "§7§k@" + roomName,
                        LANGUAGE.buttonSpectator, LANGUAGE.buttonReturn);
            }else if (room.getPlayers().size() >= room.getMaxPlayers()) {
                modal = new FormWindowModal(
                        PLUGIN_NAME, LANGUAGE.joinRoomIsFull + "§7§k@" + roomName,
                        LANGUAGE.buttonSpectator, LANGUAGE.buttonReturn);
            }else {
                modal = new FormWindowModal(
                        PLUGIN_NAME,
                        LANGUAGE.joinRoomOK.replace("%name%", "\"" + roomName + "\"") + "§7§k@" + roomName,
                        LANGUAGE.buttonOK, LANGUAGE.buttonReturn);
            }
        }else {
            modal = new FormWindowModal(PLUGIN_NAME, LANGUAGE.joinRoomIsNotFound, LANGUAGE.buttonReturn, LANGUAGE.buttonReturn);
        }
        showFormWindow(player, modal, GuiType.ROOM_JOIN_OK);
    }

    public static void showFormWindow(Player player, FormWindow window, GuiType guiType) {
        HashMap<Integer, GuiType> map;
        if (!UI_CACHE.containsKey(player)) {
            map = new HashMap<>();
            UI_CACHE.put(player, map);
        }else {
            map = UI_CACHE.get(player);
        }
        int id = player.showFormWindow(window);
        map.put(id, guiType);
        Server.getInstance().getScheduler().scheduleDelayedTask(MURDER_MYSTERY, new Task() {
            @Override
            public void onRun(int i) {
                if (UI_CACHE.containsKey(player))
                    UI_CACHE.get(player).remove(id);
            }
        }, 2400);
    }

}
