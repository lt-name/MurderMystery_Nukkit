package cn.lanink.murdermystery.form;

import cn.lanink.gamecore.room.IRoomStatus;
import cn.lanink.gamecore.utils.Language;
import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.base.BaseRoom;
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
import cn.nukkit.level.Level;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GuiCreate {

    private static final MurderMystery MURDER_MYSTERY = MurderMystery.getInstance();
    public static final String PLUGIN_NAME = "§l§7[§1M§2u§3r§4d§5e§6r§aM§cy§bs§dt§9e§6r§2y§7]";
    public static final ConcurrentHashMap<Player, ConcurrentHashMap<Integer, GuiType>> UI_CACHE = new ConcurrentHashMap<>();

    /**
     * 显示用户菜单
     * @param player 玩家
     */
    public static void sendUserMenu(Player player) {
        Language language = MURDER_MYSTERY.getLanguage(player);
        FormWindowSimple simple = new FormWindowSimple(PLUGIN_NAME, "");
        simple.addButton(new ElementButton(language.translateString("userMenuButton1"), new ElementButtonImageData("path", "textures/ui/switch_start_button")));
        simple.addButton(new ElementButton(language.translateString("userMenuButton2"), new ElementButtonImageData("path", "textures/ui/switch_select_button")));
        simple.addButton(new ElementButton(language.translateString("userMenuButton3"), new ElementButtonImageData("path", "textures/ui/servers")));
        showFormWindow(player, simple, GuiType.USER_MENU);
    }

    /**
     * 显示管理菜单
     * @param player 玩家
     */
    public static void sendAdminMenu(Player player) {
        Language language = MURDER_MYSTERY.getLanguage(player);
        FormWindowSimple simple = new FormWindowSimple(PLUGIN_NAME, language.translateString("adminMenuSetLevel").replace("%name%", player.getLevel().getFolderName()));
        simple.addButton(new ElementButton(language.translateString("adminMenuButton1"), new ElementButtonImageData("path", "textures/ui/copy")));
        simple.addButton(new ElementButton(language.translateString("adminMenuButton2"), new ElementButtonImageData("path", "textures/ui/World")));
        simple.addButton(new ElementButton(language.translateString("adminMenuButton3"), new ElementButtonImageData("path", "textures/ui/World")));
        simple.addButton(new ElementButton(language.translateString("adminMenuButton4"), new ElementButtonImageData("path", "textures/ui/World")));
        simple.addButton(new ElementButton(language.translateString("adminMenuButton5"), new ElementButtonImageData("path", "textures/ui/timer")));
        simple.addButton(new ElementButton(language.translateString("adminMenuButton6"), new ElementButtonImageData("path", "textures/ui/FriendsDiversity")));
        simple.addButton(new ElementButton(language.translateString("adminMenuButton7"), new ElementButtonImageData("path", "textures/ui/dev_glyph_color")));
        simple.addButton(new ElementButton(language.translateString("adminMenuButton8"),  new ElementButtonImageData("path", "textures/ui/refresh_light")));
        simple.addButton(new ElementButton(language.translateString("adminMenuButton9"), new ElementButtonImageData("path", "textures/ui/redX1")));
        showFormWindow(player, simple, GuiType.ADMIN_MENU);
    }

    /**
     * 显示创建房间菜单（选择地图）
     * @param player 玩家
     */
    public static void sendCreateRoomMenu(Player player) {
        FormWindowSimple simple = new FormWindowSimple(PLUGIN_NAME,
                MURDER_MYSTERY.getLanguage(player).translateString("gui_admin_room_selectWorld"));
        for (Level level : Server.getInstance().getLevels().values()) {
            simple.addButton(new ElementButton(level.getFolderName()));
        }
        showFormWindow(player, simple, GuiType.ADMIN_CREATE_ROOM_MENU);
    }

    /**
     * 显示设置房间菜单（选择房间）
     * @param player 玩家
     */
    public static void sendSetRoomMenu(Player player) {
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
    public static void sendAdminRoomNameMenu(Player player) {
        FormWindowCustom custom = new FormWindowCustom(PLUGIN_NAME);
        custom.addElement(new ElementInput("RoomName", "", player.getLevel().getFolderName()));
        showFormWindow(player, custom, GuiType.ADMIN_ROOM_NAME_MENU);
    }

    /**
     * 显示设置时间菜单
     * @param player 玩家
     */
    public static void sendAdminMoreMenu(Player player) {
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
     * 设置房间游戏人数菜单
     * @param player 玩家
     */
    @Deprecated
    public static void sendAdminPlayersMenu(Player player) {
        /*Language language = MURDER_MYSTERY.getLanguage(player);
        FormWindowCustom custom = new FormWindowCustom(PLUGIN_NAME);
        custom.addElement(new ElementInput(language.translateString("adminPlayersMenuInputText1"), "", "5"));
        custom.addElement(new ElementInput(language.translateString("adminPlayersMenuInputText2"), "", "16"));
        showFormWindow(player, custom, GuiType.ADMIN_PLAYERS_MENU);*/
    }

    /**
     * 设置房间模式菜单
     * @param player 玩家
     */
    public static void sendAdminModeMenu(Player player) {
        Language language = MURDER_MYSTERY.getLanguage(player);
        FormWindowCustom custom = new FormWindowCustom(PLUGIN_NAME);
        custom.addElement(new ElementDropdown(language.translateString("adminMenuSetLevel")
                .replace("%name%", player.getLevel().getName()),
                Arrays.asList(MurderMystery.getRoomClass().keySet().toArray(new String[0]))));
        showFormWindow(player, custom, GuiType.ADMIN_MODE_MENU);
    }

    /**
     * 显示房间列表菜单
     * @param player 玩家
     */
    public static void sendRoomListMenu(Player player) {
        Language language = MURDER_MYSTERY.getLanguage(player);
        FormWindowSimple simple = new FormWindowSimple(PLUGIN_NAME, "");
        for (Map.Entry<String, BaseRoom> entry : MurderMystery.getInstance().getRooms().entrySet()) {
            simple.addButton(new ElementButton("§e§l" + MURDER_MYSTERY.getRoomName().get(entry.getKey()) +
                    "\n§r§eMode: " + Tools.getStringRoomMode(player, entry.getValue()) +
                    " Player: " + entry.getValue().getPlayers().size() + "/" + entry.getValue().getMaxPlayers(),
                    new ElementButtonImageData("path", "textures/ui/switch_start_button")));
        }
        simple.addButton(new ElementButton(language.translateString("buttonReturn"), new ElementButtonImageData("path", "textures/ui/cancel")));
        showFormWindow(player, simple, GuiType.ROOM_LIST_MENU);
    }

    /**
     * 加入房间确认(自选)
     * @param player 玩家
     * @param world 房间名字
     */
    public static void sendRoomJoinOkMenu(Player player, String world) {
        Language language = MURDER_MYSTERY.getLanguage(player);
        FormWindowModal modal;
        world = world.replace("§e§l", "").trim();
        BaseRoom room = MurderMystery.getInstance().getRooms().get(world);
        if (room != null) {
            if (room.getStatus() == IRoomStatus.ROOM_STATUS_LEVEL_NOT_LOADED) {
                modal = new FormWindowModal(PLUGIN_NAME, language.translateString("joinRoomIsNeedInitialized"),
                        language.translateString("buttonReturn"), language.translateString("buttonReturn"));
            }else if (room.getStatus() == IRoomStatus.ROOM_STATUS_GAME ||
                    room.getStatus() == IRoomStatus.ROOM_STATUS_VICTORY) {
                String button1 = language.translateString("buttonSpectator");
                if (room.getStatus() == IRoomStatus.ROOM_STATUS_VICTORY) {
                    button1 = language.translateString("buttonReturn");
                }
                modal = new FormWindowModal(
                        PLUGIN_NAME, language.translateString("joinRoomIsPlaying") + "§7§k@" + world,
                        button1, language.translateString("buttonReturn"));
            }else if (room.getPlayers().size() >= room.getMaxPlayers()) {
                modal = new FormWindowModal(
                        PLUGIN_NAME, language.translateString("joinRoomIsFull") + "§7§k@" + world,
                        language.translateString("buttonSpectator"), language.translateString("buttonReturn"));
            }else {
                modal = new FormWindowModal(
                        PLUGIN_NAME,
                        language.translateString("joinRoomOK").replace("%name%", "\"" +
                                MURDER_MYSTERY.getRoomName().get(world) + "\"") + "§7§k@" + world,
                        language.translateString("buttonOK"), language.translateString("buttonReturn"));
            }
        }else {
            modal = new FormWindowModal(PLUGIN_NAME, language.translateString("joinRoomIsNotFound"),
                    language.translateString("buttonReturn"), language.translateString("buttonReturn"));
        }
        showFormWindow(player, modal, GuiType.ROOM_JOIN_OK);
    }

    public static void showFormWindow(Player player, FormWindow window, GuiType guiType) {
        ConcurrentHashMap<Integer, GuiType> map = UI_CACHE.computeIfAbsent(player, i -> new ConcurrentHashMap<>());
        int id = player.showFormWindow(window);
        map.put(id, guiType);
        Server.getInstance().getScheduler().scheduleDelayedTask(MURDER_MYSTERY, () -> {
            if (UI_CACHE.containsKey(player)) {
                UI_CACHE.get(player).remove(id);
            }
        }, 2400, true);
    }

}
