package cn.lanink.murdermystery.command.adminsubcommand;

import cn.lanink.murdermystery.command.base.BaseSubCommand;
import cn.lanink.murdermystery.room.BaseRoom;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

public class StartRoom extends BaseSubCommand {

    public StartRoom(String name) {
        super(name);
    }

    @Override
    public boolean canUser(CommandSender sender) {
        return sender.isPlayer() && sender.isOp();
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        BaseRoom room = this.murderMystery.getRooms().get(player.getLevel().getName());
        if (room != null) {
            if (room.getPlayers().size() >= room.getMinPlayers()) {
                if (room.getStatus() == BaseRoom.ROOM_STATUS_WAIT) {
                    room.gameStartEvent();
                    sender.sendMessage(this.language.adminStartRoom);
                }else {
                    sender.sendMessage(this.language.adminStartRoomIsPlaying);
                }
            }else {
                sender.sendMessage(this.language.adminStartRoomNoPlayer
                        .replace("%minPlayers%", room.getMinPlayers() + ""));
            }
        }else {
            sender.sendMessage(this.language.adminLevelNoRoom);
        }
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }

}
