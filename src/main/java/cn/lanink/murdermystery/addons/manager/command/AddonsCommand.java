package cn.lanink.murdermystery.addons.manager.command;

import cn.lanink.murdermystery.addons.BaseAddons;
import cn.lanink.murdermystery.addons.manager.autoregister.RegisterCommand;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;

/**
 * @author lt_name
 */
public class AddonsCommand extends Command {

    private final BaseAddons baseAddons;
    private final Permissions permissions;

    public AddonsCommand(BaseAddons baseAddons, RegisterCommand registerCommand) {
        super(registerCommand.command(), registerCommand.description(), registerCommand.usageMessage(), registerCommand.aliases());
        this.baseAddons = baseAddons;
        this.permissions = registerCommand.permissions();
        this.setPermission(this.permissions.getPermission());
        this.setPermissionMessage(registerCommand.permissionMessage());
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (this.permissions == Permissions.default_op) {
            if (!commandSender.isOp()) {
                commandSender.sendMessage(this.getPermissionMessage());
                return true;
            }
        }
        return this.baseAddons.onCommand(commandSender, this, s, strings);
    }

}
