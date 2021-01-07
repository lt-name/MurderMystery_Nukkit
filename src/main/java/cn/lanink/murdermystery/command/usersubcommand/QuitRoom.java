package cn.lanink.murdermystery.command.usersubcommand;

import cn.lanink.murdermystery.command.base.BaseSubCommand;
import cn.lanink.murdermystery.room.base.BaseRoom;
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
        for (BaseRoom room : this.murderMystery.getRooms().values()) {
            if (room.isPlaying(player) || room.isSpectator(player)) {
                room.quitRoom(player);
                sender.sendMessage(this.murderMystery.getLanguage(sender).translateString("quitRoom"));
                return true;
            }
        }
        sender.sendMessage(this.murderMystery.getLanguage(sender).translateString("quitRoomNotInRoom"));
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }
}
