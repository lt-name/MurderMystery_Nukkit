package cn.lanink.murdermystery.command;

import cn.lanink.murdermystery.command.adminsubcommand.*;
import cn.lanink.murdermystery.command.base.BaseCommand;
import cn.lanink.murdermystery.utils.FormHelper;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

/**
 * @author lt_name
 */
public class AdminCommand extends BaseCommand {

    public AdminCommand(String name, String[] aliases) {
        super(name, "MurderMystery 管理命令");
        this.setAliases(aliases);
        this.setPermission("murdermystery.admin");
        this.addSubCommand(new CreateRoom("CreateRoom"));
        this.addSubCommand(new SetRoom("SetRoom"));
        this.addSubCommand(new StartRoom("StartRoom"));
        this.addSubCommand(new StopRoom("StopRoom"));
        this.addSubCommand(new ReloadRoom("ReloadRoom"));
        this.addSubCommand(new UnloadRoom("UnloadRoom"));
        this.addSubCommand(new Version("version"));

        this.addSubCommand(new SetRoomName("SetRoomName"));
        this.addSubCommand(new SetWaitSpawn("SetWaitSpawn"));
        this.addSubCommand(new AddRandomSpawn("AddRandomSpawn"));
        this.addSubCommand(new AddGoldSpawn("AddGoldSpawn"));
        this.addSubCommand(new SetGoldSpawnTime("SetGoldSpawnTime"));
        this.addSubCommand(new SetWaitTime("SetWaitTime"));
        this.addSubCommand(new SetGameTime("SetGameTime"));
        this.addSubCommand(new SetGameMode("SetGameMode"));
        this.addSubCommand(new SetMinPlayers("SetMinPlayers"));
        this.addSubCommand(new SetMaxPlayers("SetMaxPlayers"));

        this.loadCommandBase();
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(this.murderMystery.getLanguage(sender).translateString("adminHelp")
                .replace("%cmdName%", this.getName()));
    }

    @Override
    public void sendUi(CommandSender sender) {
        FormHelper.sendAdminMenu((Player) sender);
    }

}
