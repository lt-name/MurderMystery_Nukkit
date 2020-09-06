package cn.lanink.murdermystery.command.usersubcommand;

import cn.lanink.murdermystery.command.base.BaseSubCommand;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.lanink.murdermystery.room.base.IRoomStatus;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

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
                sender.sendMessage(this.language.joinRoomOnRiding);
                return true;
            }
            for (BaseRoom room : this.murderMystery.getRooms().values()) {
                if (room.isPlaying(player) || room.isSpectator(player)) {
                    sender.sendMessage(this.language.joinRoomOnRoom);
                    return true;
                }
            }
            if (this.murderMystery.getRooms().containsKey(args[1])) {
                BaseRoom room = this.murderMystery.getRooms().get(args[1]);
                if (room.getStatus() != IRoomStatus.ROOM_STATUS_LEVEL_NOT_LOADED &&
                        room.getStatus() != IRoomStatus.ROOM_STATUS_VICTORY) {
                    room.joinRoom(player, true);
                }else {
                    sender.sendMessage(this.language.joinRoomIsNeedInitialized);
                }
            }else {
                sender.sendMessage(this.language.joinRoomIsNotFound);
            }
            return true;
        }
        sender.sendMessage(this.language.joinRoomNotAvailable);
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }

}
