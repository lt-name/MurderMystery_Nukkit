package cn.lanink.murdermystery.command.adminsubcommand;

import cn.lanink.murdermystery.command.base.BaseSubCommand;
import cn.lanink.murdermystery.form.GuiCreate;
import cn.lanink.murdermystery.tasks.admin.SetRoomTask;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.level.Level;

/**
 * @author lt_name
 */
public class SetRoom extends BaseSubCommand {

    public SetRoom(String name) {
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
        if (this.murderMystery.setRoomTask.containsKey(player)) {
            this.murderMystery.setRoomTask.get(player).cancel();
        }else {
            if (args.length < 2) {
                GuiCreate.sendSetRoomMenu(player);
            }else {
                if (this.murderMystery.getRoomConfigs().containsKey(args[1])) {
                    Level level = Server.getInstance().getLevelByName(args[1]);
                    if (player.getLevel() != level) {
                        player.teleport(level.getSafeSpawn());
                    }
                    SetRoomTask task = new SetRoomTask(player, level);
                    this.murderMystery.setRoomTask.put(player, task);
                    Server.getInstance().getScheduler().scheduleRepeatingTask(this.murderMystery, task, 10);
                    sender.sendMessage(this.murderMystery.getLanguage(player).translateString("admin_setRoom_start", args[1]));
                }else {
                    sender.sendMessage(this.murderMystery.getLanguage(player).translateString("admin_setRoom_noExist"));
                }
            }
        }
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[]{ new CommandParameter("roomName", CommandParamType.TEXT, false) };
    }

}
