package cn.lanink.murdermystery.command.adminsubcommand;

import cn.lanink.murdermystery.command.base.BaseSubCommand;
import cn.lanink.murdermystery.event.MurderRoomStartEvent;
import cn.lanink.murdermystery.room.Room;
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
        Room room = this.murderMystery.getRooms().get(player.getLevel().getName());
        if (room != null) {
            //少于三人将进入死循环！
            if (room.getPlayers().size() >= 3) {
                murderMystery.getServer().getPluginManager().callEvent(new MurderRoomStartEvent(room));
            }else {
                sender.sendMessage(this.language.adminStartNoPlayer);
            }
        }else {
            sender.sendMessage(this.language.adminStartNoRoom);
        }
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }

}
