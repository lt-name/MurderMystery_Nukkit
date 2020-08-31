package cn.lanink.murdermystery.command;

import cn.lanink.murdermystery.command.base.BaseCommand;
import cn.lanink.murdermystery.command.usersubcommand.JoinRoom;
import cn.lanink.murdermystery.command.usersubcommand.JoinSpectator;
import cn.lanink.murdermystery.command.usersubcommand.QuitRoom;
import cn.lanink.murdermystery.command.usersubcommand.RoomList;
import cn.lanink.murdermystery.ui.GuiCreate;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

/**
 * @author lt_name
 */
public class UserCommand extends BaseCommand {

    public UserCommand(String name, String[] aliases) {
        super(name, "MurderMystery 命令");
        this.setAliases(aliases);
        this.setPermission("MurderMystery.command.user");
        this.addSubCommand(new JoinRoom("join"));
        this.addSubCommand(new JoinSpectator("joinspectator"));
        this.addSubCommand(new QuitRoom("quit"));
        this.addSubCommand(new RoomList("list"));
        this.loadCommandBase();
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(this.language.userHelp.replace("%cmdName%", this.getName()));
    }

    @Override
    public void sendUi(CommandSender sender) {
        GuiCreate.sendUserMenu((Player) sender);
    }

}
