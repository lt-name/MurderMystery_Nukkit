package cn.lanink.murdermystery.command.adminsubcommand;

import cn.lanink.murdermystery.command.base.BaseSubCommand;
import cn.lanink.murdermystery.form.FormCreate;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.level.Level;

/**
 * @author lt_name
 */
public class CreateRoom extends BaseSubCommand {

    public CreateRoom(String name) {
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
        Player player = (Player) sender;
        if (args.length < 2) {
            FormCreate.sendCreateRoomMenu(player);
        }else {
            if (!this.murderMystery.getRoomConfigs().containsKey(args[1])) {
                Level level = Server.getInstance().getLevelByName(args[1]);
                if (level != null) {
                    if (this.murderMystery.setRoomTask.containsKey(player)) {
                        this.murderMystery.setRoomTask.get(player).cancel();
                    }
                    this.murderMystery.getRoomConfig(level);
                    sender.sendMessage(this.murderMystery.getLanguage(player).translateString("admin_createRoom_success", args[1]));
                    if (player.getLevel() != level) {
                        player.teleport(level.getSafeSpawn());
                    }
                    Server.getInstance().dispatchCommand(player,
                            this.murderMystery.getCmdAdmin() + " SetRoom " + args[1]);
                    if (this.murderMystery.setRoomTask.containsKey(player)) {
                        this.murderMystery.setRoomTask.get(player).setAutoNext(true);
                    }
                }else {
                    sender.sendMessage(this.murderMystery.getLanguage(player).translateString("world_doesNotExist", args[1]));
                }
            }else {
                sender.sendMessage(this.murderMystery.getLanguage(player).translateString("admin_createRoom_exist"));
            }
        }
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[]{ CommandParameter.newType("worldName", CommandParamType.TEXT) };
    }

}
