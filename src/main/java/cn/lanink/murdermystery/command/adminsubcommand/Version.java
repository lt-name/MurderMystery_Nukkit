package cn.lanink.murdermystery.command.adminsubcommand;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.command.base.BaseSubCommand;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;

/**
 * @author lt_name
 */
public class Version extends BaseSubCommand {

    public Version(String name) {
        super(name);
    }

    @Override
    public boolean canUser(CommandSender sender) {
        return sender.isOp();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "ver" };
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        sender.sendMessage("§eMurderMystery Version: " + MurderMystery.VERSION);
        sender.sendMessage("Thank you for using");
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }
}
