package cn.lanink.murdermystery.command.usersubcommand;

import cn.lanink.murdermystery.command.base.BaseSubCommand;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

public class RoomList extends BaseSubCommand {

    public RoomList(String name) {
        super(name);
    }

    @Override
    public boolean canUser(CommandSender sender) {
        return true;
    }

    @Override
    public String[] getAliases() {
        return new String[] { "列表" };
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        StringBuilder list = new StringBuilder();
        for (String string : this.murderMystery.getRooms().keySet()) {
            list.append(this.murderMystery.getRoomName().get(string)).append(" ");
        }
        sender.sendMessage(this.murderMystery.getLanguage(sender).listRoom.replace("%list%", list.toString()));
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }

}
