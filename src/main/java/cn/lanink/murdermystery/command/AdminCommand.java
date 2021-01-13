package cn.lanink.murdermystery.command;

import cn.lanink.murdermystery.command.adminsubcommand.*;
import cn.lanink.murdermystery.command.base.BaseCommand;
import cn.lanink.murdermystery.form.GuiCreate;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

/**
 * @author lt_name
 */
public class AdminCommand extends BaseCommand {

    public AdminCommand(String name, String[] aliases) {
        super(name, "MurderMystery 管理命令");
        this.setAliases(aliases);
        this.setPermission("MurderMystery.command.admin");
        this.addSubCommand(new SetRoomName("setroomname"));
        this.addSubCommand(new SetWaitSpawn("setwaitspawn"));
        this.addSubCommand(new AddRandomSpawn("addrandomspawn"));
        this.addSubCommand(new AddGoldSpawn("addgoldspawn"));
        this.addSubCommand(new SetGoldSpawnTime("setgoldspawntime"));
        this.addSubCommand(new SetWaitTime("setwaittime"));
        this.addSubCommand(new SetGameTime("setgametime"));
        this.addSubCommand(new SetGameMode("setgamemode"));
        this.addSubCommand(new SetMinPlayers("setminplayers"));
        this.addSubCommand(new SetMaxPlayers("setmaxplayers"));
        this.addSubCommand(new CreateRoom("CreateRoom"));
        this.addSubCommand(new SetRoom("SetRoom"));
        this.addSubCommand(new StartRoom("startroom"));
        this.addSubCommand(new StopRoom("stoproom"));
        this.addSubCommand(new ReloadRoom("ReloadRoom"));
        this.addSubCommand(new UnloadRoom("UnloadRoom"));
        this.addSubCommand(new Version("version"));
        this.loadCommandBase();
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(this.murderMystery.getLanguage(sender).translateString("adminHelp")
                .replace("%cmdName%", this.getName()));
    }

    @Override
    public void sendUi(CommandSender sender) {
        GuiCreate.sendAdminMenu((Player) sender);
    }

}
