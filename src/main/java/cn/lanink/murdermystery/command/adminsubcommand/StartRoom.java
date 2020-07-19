package cn.lanink.murdermystery.command.adminsubcommand;

import cn.lanink.murdermystery.command.base.BaseSubCommand;
import cn.lanink.murdermystery.event.MurderRoomStartEvent;
import cn.lanink.murdermystery.room.Room;
import cn.nukkit.Player;
import cn.nukkit.Server;
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
                if (room.getMode() == 1) {
                    Server.getInstance().getPluginManager().callEvent(new MurderRoomStartEvent(room));
                    sender.sendMessage(this.language.adminStartRoom);
                }else {
                    sender.sendMessage(this.language.adminStartRoomIsPlaying);
                }
            }else {
                sender.sendMessage(this.language.adminStartRoomNoPlayer);
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
