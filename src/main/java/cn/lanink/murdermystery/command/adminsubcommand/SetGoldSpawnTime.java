package cn.lanink.murdermystery.command.adminsubcommand;

import cn.lanink.murdermystery.command.base.BaseSubCommand;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;

public class SetGoldSpawnTime extends BaseSubCommand {

    public SetGoldSpawnTime(String name) {
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
            Player player = (Player) sender;
            if (args[1].matches("[0-9]*")) {
                Config config = this.murderMystery.getRoomConfig(player.getLevel());
                config.set("goldSpawnTime", Integer.parseInt(args[1]));
                config.save();
                sender.sendMessage(this.murderMystery.getLanguage(sender).adminSetGoldSpawnTime.replace("%time%", args[1]));
            }else {
                sender.sendMessage(this.murderMystery.getLanguage(sender).adminNotNumber);
            }
        }else {
            sender.sendMessage(this.murderMystery.getLanguage(sender).cmdHelp.replace("%cmdName%", this.getName()));
        }
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[] { new CommandParameter("time", CommandParamType.INT, false) };
    }

}
