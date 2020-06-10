package cn.lanink.murdermystery.command.adminsubcommand;

import cn.lanink.murdermystery.command.base.BaseSubCommand;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

public class ReloadRoom extends BaseSubCommand {

    public ReloadRoom(String name) {
        super(name);
    }

    @Override
    public boolean canUser(CommandSender sender) {
        return sender.isOp();
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        this.murderMystery.reLoadRooms();
        sender.sendMessage(this.language.adminReload);
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }
}
