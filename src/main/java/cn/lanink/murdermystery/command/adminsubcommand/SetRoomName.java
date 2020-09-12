package cn.lanink.murdermystery.command.adminsubcommand;

import cn.lanink.murdermystery.command.base.BaseSubCommand;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;

/**
 * @author lt_name
 */
public class SetRoomName extends BaseSubCommand {

    public SetRoomName(String name) {
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
        if (args.length == 2) {
            if (this.murderMystery.getRooms().containsKey(args[1]) ||
                    this.murderMystery.getRoomName().containsValue(args[1])) {
                sender.sendMessage(this.murderMystery.getLanguage(sender).adminSetRoomNameExist.replace("%roomName%", args[1]));
            }else {
                Config config = this.murderMystery.getRoomConfig(((Player) sender).getLevel());
                config.set("roomName", args[1]);
                config.save();
                sender.sendMessage(this.murderMystery.getLanguage(sender).adminSetRoomName.replace("%roomName%", args[1]));
            }
        }else {
            sender.sendMessage(this.murderMystery.getLanguage(sender).cmdHelp.replace("%cmdName%", this.getName()));
        }
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[] { new CommandParameter("name", CommandParamType.TEXT, false) };
    }

}
