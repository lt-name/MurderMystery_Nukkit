package cn.lanink.murdermystery.command.usersubcommand;

import cn.lanink.murdermystery.command.base.BaseSubCommand;
import cn.lanink.murdermystery.room.BaseRoom;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;

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
                sender.sendMessage(this.language.joinRoomOnRiding);
                return true;
            }
            for (BaseRoom room : this.murderMystery.getRooms().values()) {
                if (room.isPlaying(player)) {
                    sender.sendMessage(this.language.joinRoomOnRoom);
                    return true;
                }
            }
            if (args.length < 2) {
                for (BaseRoom room : this.murderMystery.getRooms().values()) {
                    if ((room.getStatus() == 0 || room.getStatus() == BaseRoom.ROOM_STATUS_WAIT) &&
                            room.getPlayers().size() < room.getMaxPlayers()) {
                        room.joinRoom(player);
                        sender.sendMessage(this.language.joinRandomRoom);
                        return true;
                    }
                }
            }else {
                String[] s = args[1].split(":");
                if (s.length == 2 && s[0].toLowerCase().trim().equals("mode")) {
                    String modeName = s[1].toLowerCase().trim();
                    for (BaseRoom room : this.murderMystery.getRooms().values()) {
                        if ((room.getStatus() == 0 || room.getStatus() == BaseRoom.ROOM_STATUS_WAIT) &&
                                room.getPlayers().size() < room.getMaxPlayers()) {
                            if (room.getGameMode().equals(modeName)) {
                                room.joinRoom(player);
                                sender.sendMessage(this.language.joinRandomRoom);
                                return true;
                            }
                        }
                    }
                    sender.sendMessage(this.language.joinRoomIsNotFound);
                    return true;
                }else if (this.murderMystery.getRooms().containsKey(args[1])) {
                    BaseRoom room = this.murderMystery.getRooms().get(args[1]);
                    if (room.getStatus() == BaseRoom.ROOM_STATUS_GAME ||
                            room.getStatus() == BaseRoom.ROOM_STATUS_VICTORY) {
                        sender.sendMessage(this.language.joinRoomIsPlaying);
                    }else if (room.getPlayers().size() >= room.getMaxPlayers()) {
                        sender.sendMessage(this.language.joinRoomIsFull);
                    } else {
                        room.joinRoom(player);
                    }
                    return true;
                }else {
                    sender.sendMessage(this.language.joinRoomIsNotFound);
                    return true;
                }
            }
        }
        sender.sendMessage(this.language.joinRoomNotAvailable);
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[] { new CommandParameter("roomName", CommandParamType.TEXT, false) };
    }

}
