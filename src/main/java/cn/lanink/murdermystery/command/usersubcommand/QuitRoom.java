package cn.lanink.murdermystery.command.usersubcommand;

import cn.lanink.murdermystery.command.base.BaseSubCommand;
import cn.lanink.murdermystery.room.RoomBase;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

public class QuitRoom extends BaseSubCommand {

    public QuitRoom(String name) {
        super(name);
    }

    @Override
    public boolean canUser(CommandSender sender) {
        return sender.isPlayer();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "退出" };
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        for (RoomBase room : this.murderMystery.getRooms().values()) {
            if (room.isPlaying(player)) {
                room.quitRoom(player);
                sender.sendMessage(this.language.quitRoom);
                return true;
            }
        }
        sender.sendMessage(this.language.quitRoomNotInRoom);
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }
}
