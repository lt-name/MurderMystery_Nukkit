package cn.lanink.murdermystery.command.usersubcommand;

import cn.lanink.gamecore.room.IRoomStatus;
import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.command.base.BaseSubCommand;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;

import java.util.LinkedList;
import java.util.Map;

public class JoinRoom extends BaseSubCommand {

    public JoinRoom(String name) {
        super(name);
    }

    @Override
    public boolean canUser(CommandSender sender) {
        return sender.isPlayer();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "加入" };
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (this.murderMystery.getRooms().size() > 0) {
            Player player = (Player) sender;
            if (player.riding != null) {
                sender.sendMessage(this.murderMystery.getLanguage(sender).translateString("joinRoomOnRiding"));
                return true;
            }
            for (BaseRoom room : this.murderMystery.getRooms().values()) {
                if (room.isPlaying(player)) {
                    sender.sendMessage(this.murderMystery.getLanguage(sender).translateString("joinRoomOnRoom"));
                    return true;
                }
            }
            if (args.length < 2) {
                LinkedList<BaseRoom> rooms = new LinkedList<>();
                for (BaseRoom room : this.murderMystery.getRooms().values()) {
                    if (room.canJoin()) {
                        if (room.getPlayers().size() > 0) {
                            room.joinRoom(player);
                            sender.sendMessage(this.murderMystery.getLanguage(sender).translateString("joinRandomRoom"));
                            return true;
                        }
                        rooms.add(room);
                    }
                }
                if (rooms.size() > 0) {
                    BaseRoom room = rooms.get(MurderMystery.RANDOM.nextInt(rooms.size()));
                    room.joinRoom(player);
                    sender.sendMessage(this.murderMystery.getLanguage(sender).translateString("joinRandomRoom"));
                    return true;
                }
            }else {
                String[] s = args[1].split(":");
                if (s.length == 2 && s[0].toLowerCase().trim().equals("mode")) {
                    String modeName = s[1].toLowerCase().trim();
                    LinkedList<BaseRoom> rooms = new LinkedList<>();
                    for (BaseRoom room : this.murderMystery.getRooms().values()) {
                        if (room.canJoin() && room.getGameMode().equals(modeName)) {
                            if (room.getPlayers().size() > 0) {
                                room.joinRoom(player);
                                sender.sendMessage(this.murderMystery.getLanguage(sender).translateString("joinRandomRoom"));
                                return true;
                            }
                            rooms.add(room);
                        }
                    }
                    if (rooms.size() > 0) {
                        BaseRoom room = rooms.get(MurderMystery.RANDOM.nextInt(rooms.size()));
                        room.joinRoom(player);
                        sender.sendMessage(this.murderMystery.getLanguage(sender).translateString("joinRandomRoom"));
                        return true;
                    }
                    sender.sendMessage(this.murderMystery.getLanguage(sender).translateString("joinRoomIsNotFound"));
                    return true;
                }else {
                    String world = args[1];
                    if (!this.murderMystery.getRooms().containsKey(world)) {
                        for (Map.Entry<String, String> entry : this.murderMystery.getRoomName().entrySet()) {
                            if (entry.getValue().equals(args[1])) {
                                world = entry.getKey();
                            }
                        }
                    }
                    BaseRoom room = this.murderMystery.getRooms().get(world);
                    if (room != null) {
                        if (room.getStatus() == IRoomStatus.ROOM_STATUS_LEVEL_NOT_LOADED) {
                            sender.sendMessage(this.murderMystery.getLanguage(sender).translateString("joinRoomIsNeedInitialized"));
                        }else if (room.getStatus() == IRoomStatus.ROOM_STATUS_GAME ||
                                room.getStatus() == IRoomStatus.ROOM_STATUS_VICTORY) {
                            sender.sendMessage(this.murderMystery.getLanguage(sender).translateString("joinRoomIsPlaying"));
                        }else if (room.getPlayers().size() >= room.getMaxPlayers()) {
                            sender.sendMessage(this.murderMystery.getLanguage(sender).translateString("joinRoomIsFull"));
                        }else {
                            room.joinRoom(player);
                        }
                    }else {
                        sender.sendMessage(this.murderMystery.getLanguage(sender).translateString("joinRoomIsNotFound"));
                    }
                    return true;
                }
            }
        }
        sender.sendMessage(this.murderMystery.getLanguage(sender).translateString("joinRoomNotAvailable"));
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[] { new CommandParameter("roomName", CommandParamType.TEXT, false) };
    }

}
