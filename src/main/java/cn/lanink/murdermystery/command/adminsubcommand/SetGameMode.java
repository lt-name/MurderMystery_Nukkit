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
            String gameMode = args[1];
            if (MurderMystery.hasRoomClass(gameMode)) {
                Config config = this.murderMystery.getRoomConfig(player.getLevel());
                config.set("gameMode", gameMode);
                config.save();
                sender.sendMessage(this.murderMystery.getLanguage(sender).adminSetGameMode
                        .replace("%roomMode%", gameMode));
            }else {
                sender.sendMessage(this.murderMystery.getLanguage(sender).adminSetGameModeNotFound
                        .replace("%roomMode%", gameMode));
            }
        }else {
            sender.sendMessage(this.murderMystery.getLanguage(sender).cmdHelp.replace("%cmdName%", this.getName()));
        }
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[] { CommandParameter.newType("gameMode", CommandParamType.TEXT) };
    }

}
