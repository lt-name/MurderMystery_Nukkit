package cn.lanink.murdermystery.command.adminsubcommand;

import cn.lanink.murdermystery.command.base.BaseSubCommand;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;

import java.util.List;

public class AddGoldSpawn extends BaseSubCommand {

    public AddGoldSpawn(String name) {
        super(name);
    }

    @Override
    public boolean canUser(CommandSender sender) {
        return false;
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        Config config = this.murderMystery.getRoomConfig(player.getLevel());
        String s = player.getFloorX() + ":" + player.getFloorY() + ":" + player.getFloorZ();
        List<String> list = config.getStringList("goldSpawn");
        list.add(s);
        config.set("goldSpawn", list);
        config.save();
        sender.sendMessage(this.language.adminAddGoldSpawn);
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }

}
