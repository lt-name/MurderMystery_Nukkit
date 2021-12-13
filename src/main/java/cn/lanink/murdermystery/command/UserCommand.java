package cn.lanink.murdermystery.command;

import cn.lanink.murdermystery.command.base.BaseCommand;
import cn.lanink.murdermystery.command.usersubcommand.JoinRoom;
import cn.lanink.murdermystery.command.usersubcommand.JoinSpectator;
import cn.lanink.murdermystery.command.usersubcommand.QuitRoom;
import cn.lanink.murdermystery.command.usersubcommand.RoomList;
import cn.lanink.murdermystery.utils.FormHelper;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

/**
 * @author lt_name
 */
public class UserCommand extends BaseCommand {

    public UserCommand(String name, String[] aliases) {
        super(name, "MurderMystery 命令");
        this.setAliases(aliases);
        this.setPermission("murdermystery.user");
        this.addSubCommand(new JoinRoom("Join"));
        this.addSubCommand(new JoinSpectator("JoinSpectator"));
        this.addSubCommand(new QuitRoom("Quit"));
        this.addSubCommand(new RoomList("List"));
        this.loadCommandBase();
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(this.murderMystery.getLanguage(sender).translateString("userHelp")
                .replace("%cmdName%", this.getName()));
    }

    @Override
    public void sendUi(CommandSender sender) {
        FormHelper.sendUserMenu((Player) sender);
    }

}
