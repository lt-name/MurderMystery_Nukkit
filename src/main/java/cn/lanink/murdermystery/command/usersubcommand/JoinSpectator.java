package cn.lanink.murdermystery.command.usersubcommand;

import cn.lanink.murdermystery.command.base.BaseSubCommand;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.lanink.murdermystery.room.base.RoomStatus;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

import java.util.Map;

/**
 * @author lt_name
 */
public class JoinSpectator extends BaseSubCommand {

    public JoinSpectator(String name) {
        super(name);
    }

    @Override
    public boolean canUser(CommandSender sender) {
        return sender.isPlayer();
    }

    @Override
    public String[] getAliases() {
        return new String[0];
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
                if (room.isPlaying(player) || room.isSpectator(player)) {
                    sender.sendMessage(this.murderMystery.getLanguage(sender).translateString("joinRoomOnRoom"));
                    return true;
                }
            }
            if (args.length < 2) {
                for (BaseRoom room : this.murderMystery.getRooms().values()) {
                    if (room.getStatus() == RoomStatus.GAME) {
                        room.joinRoom(player, true);
                        return true;
                    }
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
                    if (room.getStatus() != RoomStatus.LEVEL_NOT_LOADED &&
                            room.getStatus() != RoomStatus.VICTORY) {
                        room.joinRoom(player, true);
                    } else {
                        sender.sendMessage(this.murderMystery.getLanguage(sender).translateString("joinRoomIsNeedInitialized"));
                    }
                } else {
                    sender.sendMessage(this.murderMystery.getLanguage(sender).translateString("joinRoomIsNotFound"));
                }
            }
            return true;
        }
        sender.sendMessage(this.murderMystery.getLanguage(sender).translateString("joinRoomNotAvailable"));
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }

}
