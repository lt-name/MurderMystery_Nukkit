package cn.lanink.murdermystery.command.usersubcommand;

import cn.lanink.murdermystery.command.base.BaseSubCommand;
import cn.lanink.murdermystery.room.Room;
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
            for (Room room : this.murderMystery.getRooms().values()) {
                if (room.isPlaying(player)) {
                    sender.sendMessage(this.language.joinRoomOnRoom);
                    return true;
                }
            }
            if (player.riding != null) {
                sender.sendMessage(this.language.joinRoomOnRiding);
                return true;
            }
            if (args.length < 2) {
                for (Room room : this.murderMystery.getRooms().values()) {
                    if (room.getMode() == 0 || room.getMode() == 1) {
                        room.joinRoom(player);
                        sender.sendMessage(this.language.joinRandomRoom);
                        return true;
                    }
                }
            }else if (this.murderMystery.getRooms().containsKey(args[1])) {
                Room room = this.murderMystery.getRooms().get(args[1]);
                if (room.getMode() == 2 || room.getMode() == 3) {
                    sender.sendMessage(this.language.joinRoomIsPlaying);
                }else if (room.getPlayers().values().size() > 15) {
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
        sender.sendMessage(this.language.joinRoomNotAvailable);
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[] { new CommandParameter("roomName", CommandParamType.TEXT, false) };
    }

}
