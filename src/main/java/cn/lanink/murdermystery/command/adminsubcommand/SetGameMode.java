package cn.lanink.murdermystery.command.adminsubcommand;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.command.base.BaseSubCommand;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;

public class SetGameMode extends BaseSubCommand {

    public SetGameMode(String name) {
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
            Config config = this.murderMystery.getRoomConfig(player.getLevel());
            config.set("gameMode", args[1]);
            config.save();
            if (MurderMystery.getRoomClass().containsKey(args[1])) {
                sender.sendMessage(this.language.adminSetGameMode
                        .replace("%roomMode%", args[1]));
            }else if (args[1].equals("infected")) {
                sender.sendMessage(this.language.adminSetGameMode
                        .replace("%roomMode%", this.language.Infected));
            }else {
                sender.sendMessage(this.language.adminSetGameMode
                        .replace("%roomMode%", this.language.Classic));
            }
        }else {
            sender.sendMessage(this.language.cmdHelp.replace("%cmdName%", this.getName()));
        }
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[] { new CommandParameter("gameMode", CommandParamType.INT, false) };
    }

}
