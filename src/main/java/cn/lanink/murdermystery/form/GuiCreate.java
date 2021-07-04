package cn.lanink.murdermystery.form;

import cn.lanink.gamecore.form.element.ResponseElementButton;
import cn.lanink.gamecore.form.windows.AdvancedFormWindowModal;
import cn.lanink.gamecore.form.windows.AdvancedFormWindowSimple;
import cn.lanink.gamecore.utils.Language;
import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.lanink.murdermystery.room.base.RoomStatus;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GuiCreate {

    private static final MurderMystery MURDER_MYSTERY = MurderMystery.getInstance();
    public static final String PLUGIN_NAME = "§l§7[§1M§2u§3r§4d§5e§6r§aM§cy§bs§dt§9e§6r§2y§7]";
    public static final HashMap<Player, HashMap<Integer, GuiType>> UI_CACHE = new HashMap<>();

    /**
     * 显示用户菜单
     * @param player 玩家
     */
    public static void sendUserMenu(@NotNull Player player) {
        Language language = MURDER_MYSTERY.getLanguage(player);
        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(PLUGIN_NAME);
        simple.addButton(new ResponseElementButton(language.translateString("userMenuButton1"),
                new ElementButtonImageData("path", "textures/ui/switch_start_button"))
                .onClicked(p -> Server.getInstance().dispatchCommand(p, MURDER_MYSTERY.getCmdUser() + " join")));
        simple.addButton(new ResponseElementButton(language.translateString("userMenuButton2"),
                new ElementButtonImageData("path", "textures/ui/switch_select_button"))
                .onClicked(p -> Server.getInstance().dispatchCommand(p, MURDER_MYSTERY.getCmdUser() + " quit")));
        simple.addButton(new ResponseElementButton(language.translateString("userMenuButton3"),
                new ElementButtonImageData("path", "textures/ui/servers"))
                .onClicked(GuiCreate::sendRoomListMenu));
        player.showFormWindow(simple);
    }

    /**
     * 显示管理菜单
     * @param player 玩家
     */
    public static void sendAdminMenu(@NotNull Player player) {
        Language language = MURDER_MYSTERY.getLanguage(player);
        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(PLUGIN_NAME);
        simple.addButton(new ResponseElementButton(language.translateString("gui_admin_main_createRoom"),
                new ElementButtonImageData("path", "textures/ui/World"))
                .onClicked(p -> Server.getInstance().dispatchCommand(p, MURDER_MYSTERY.getCmdAdmin() + " CreateRoom")));
        simple.addButton(new ResponseElementButton(language.translateString("gui_admin_main_setRoom"),
                new ElementButtonImageData("path", "textures/ui/World"))
                .onClicked(p -> Server.getInstance().dispatchCommand(p, MURDER_MYSTERY.getCmdAdmin() + " SetRoom")));
        simple.addButton(new ResponseElementButton(language.translateString("gui_admin_main_reloadAllRoom"),
                new ElementButtonImageData("path", "textures/ui/refresh_light"))
                .onClicked(p -> Server.getInstance().dispatchCommand(p, MURDER_MYSTERY.getCmdAdmin() + " ReloadRoom")));
        simple.addButton(new ResponseElementButton(language.translateString("gui_admin_main_unloadAllRoom"),
                new ElementButtonImageData("path", "textures/ui/redX1"))
                .onClicked(p -> Server.getInstance().dispatchCommand(p, MURDER_MYSTERY.getCmdAdmin() + " UnloadRoom")));
        player.showFormWindow(simple);
    }

    /**
     * 显示创建房间菜单（选择地图）
     * @param player 玩家
     */
    public static void sendCreateRoomMenu(@NotNull Player player) {
        FormWindowSimple simple = new FormWindowSimple(PLUGIN_NAME,
                MURDER_MYSTERY.getLanguage(player).translateString("gui_admin_room_selectWorld"));
        for (Level level : Server.getInstance().getLevels().values()) {
            if (!MURDER_MYSTERY.getRoomConfigs().containsKey(level.getFolderName())) {
                simple.addButton(new ElementButton(level.getFolderName()));
            }
        }
        showFormWindow(player, simple, GuiType.ADMIN_CREATE_ROOM_MENU);
    }

    /**
     * 显示设置房间菜单（选择房间）
     * @param player 玩家
     */
    public static void sendSetRoomMenu(@NotNull Player player) {
        FormWindowSimple simple = new FormWindowSimple(PLUGIN_NAME,
                MURDER_MYSTERY.getLanguage(player).translateString("gui_admin_room_selectRoom"));
        for (String roomName : MURDER_MYSTERY.getRoomConfigs().keySet()) {
            simple.addButton(new ElementButton(roomName));
        }
        showFormWindow(player, simple, GuiType.ADMIN_SET_ROOM_MENU);
    }

    /**
     * 显示设置房间名称菜单
     * @param player 玩家
     */
    public static void sendAdminRoomNameMenu(@NotNull Player player) {
        FormWindowCustom custom = new FormWindowCustom(PLUGIN_NAME);
        custom.addElement(new ElementInput("RoomName", "", player.getLevel().getFolderName()));
        showFormWindow(player, custom, GuiType.ADMIN_ROOM_NAME_MENU);
    }

    /**
     * 显示设置时间菜单
     * @param player 玩家
     */
    public static void sendAdminMoreMenu(@NotNull Player player) {
        Language language = MURDER_MYSTERY.getLanguage(player);
        FormWindowCustom custom = new FormWindowCustom(PLUGIN_NAME);
        custom.addElement(new ElementInput(language.translateString("adminTimeMenuInputText1"), "", "20"));
        custom.addElement(new ElementInput(language.translateString("adminTimeMenuInputText2"), "", "60"));
        custom.addElement(new ElementInput(language.translateString("adminTimeMenuInputText3"), "", "300"));
        custom.addElement(new ElementInput(language.translateString("adminPlayersMenuInputText1"), "", "5"));
        custom.addElement(new ElementInput(language.translateString("adminPlayersMenuInputText2"), "", "16"));
        showFormWindow(player, custom, GuiType.ADMIN_MORE_MENU);
    }

    /**
     * 设置房间模式菜单
     * @param player 玩家
     */
    public static void sendAdminModeMenu(@NotNull Player player) {
        Language language = MURDER_MYSTERY.getLanguage(player);
        FormWindowCustom custom = new FormWindowCustom(PLUGIN_NAME);
        custom.addElement(new ElementDropdown(language.translateString("gui_admin_room_selectGameMode"),
                Arrays.asList(MurderMystery.getRoomClass().keySet().toArray(new String[0]))));
        showFormWindow(player, custom, GuiType.ADMIN_MODE_MENU);
    }

    /**
     * 显示房间列表菜单
     * @param player 玩家
     */
    public static void sendRoomListMenu(@NotNull Player player) {
        Language language = MURDER_MYSTERY.getLanguage(player);
        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(PLUGIN_NAME);
        for (Map.Entry<String, BaseRoom> entry : MurderMystery.getInstance().getRooms().entrySet()) {
            simple.addButton(new ResponseElementButton("§e§l" + MURDER_MYSTERY.getRoomName().get(entry.getKey()) +
                    "\n§r§eMode: " + Tools.getStringRoomMode(player, entry.getValue()) +
                    " Player: " + entry.getValue().getPlayers().size() + "/" + entry.getValue().getMaxPlayers(),
                    new ElementButtonImageData("path", "textures/ui/switch_start_button"))
                    .onClicked(p -> sendRoomJoinOkMenu(p, entry.getKey())));
        }
        simple.addButton(new ResponseElementButton(language.translateString("buttonReturn"),
                new ElementButtonImageData("path", "textures/ui/cancel"))
                .onClicked(GuiCreate::sendUserMenu));
        player.showFormWindow(simple);
    }

    /**
     * 加入房间确认(自选)
     *
     * @param player 玩家
     * @param world 房间名字
     */
    public static void sendRoomJoinOkMenu(@NotNull Player player, @NotNull String world) {
        Language language = MURDER_MYSTERY.getLanguage(player);
        AdvancedFormWindowModal modal;
        world = world.replace("§e§l", "").trim();
        BaseRoom room = MurderMystery.getInstance().getRooms().get(world);
        if (room != null) {
            if (room.getStatus() == RoomStatus.LEVEL_NOT_LOADED) {
                modal = new AdvancedFormWindowModal(PLUGIN_NAME,
                        language.translateString("joinRoomIsNeedInitialized"),
                        language.translateString("buttonReturn"),
                        language.translateString("buttonReturn"));
                modal.onClickedTrue(GuiCreate::sendRoomListMenu);
                modal.onClickedFalse(GuiCreate::sendRoomListMenu);
            }else if (room.getStatus() == RoomStatus.GAME ||
                    room.getStatus() == RoomStatus.VICTORY) {
                modal = new AdvancedFormWindowModal(PLUGIN_NAME,
                        language.translateString("joinRoomIsPlaying"),
                        "",
                        language.translateString("buttonReturn"));
                if (room.getStatus() == RoomStatus.VICTORY) {
                    modal.setButton1(language.translateString("buttonReturn"));
                    modal.onClickedTrue(GuiCreate::sendRoomListMenu);
                }else {
                    modal.setButton1(language.translateString("buttonSpectator"));
                    modal.onClickedTrue((p) ->
                            Server.getInstance().dispatchCommand(p, MURDER_MYSTERY.getCmdUser() + " joinspectator " + room.getLevelName()));
                }
                modal.onClickedFalse(GuiCreate::sendRoomListMenu);
            }else if (room.getPlayers().size() >= room.getMaxPlayers()) {
                modal = new AdvancedFormWindowModal(PLUGIN_NAME,
                        language.translateString("joinRoomIsFull"),
                        language.translateString("buttonSpectator"),
                        language.translateString("buttonReturn"));
            }else {
                modal = new AdvancedFormWindowModal(PLUGIN_NAME,
                        language.translateString("joinRoomOK")
                                .replace("%name%", "\"" + MURDER_MYSTERY.getRoomName().get(world) + "\""),
                        language.translateString("buttonOK"),
                        language.translateString("buttonReturn"));
                modal.onClickedTrue((p) ->
                        Server.getInstance().dispatchCommand(p, MURDER_MYSTERY.getCmdUser() + " join " + room.getLevelName()));
                modal.onClickedFalse(GuiCreate::sendRoomListMenu);
            }
        }else {
            modal = new AdvancedFormWindowModal(PLUGIN_NAME,
                    language.translateString("joinRoomIsNotFound"),
                    language.translateString("buttonReturn"),
                    language.translateString("buttonReturn"));
            modal.onClickedTrue(GuiCreate::sendRoomListMenu);
            modal.onClickedFalse(GuiCreate::sendRoomListMenu);
        }
        player.showFormWindow(modal);
    }

    public static void showFormWindow(@NotNull Player player, @NotNull FormWindow window, @NotNull GuiType guiType) {
        UI_CACHE.computeIfAbsent(player, i -> new HashMap<>()).put(player.showFormWindow(window), guiType);
    }

}
