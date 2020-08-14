package cn.lanink.murdermystery.addons.manager.command;

import cn.lanink.murdermystery.addons.AddonsBase;
import cn.lanink.murdermystery.addons.manager.autoregister.RegisterCommand;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;

/**
 * @author lt_name
 */
public class AddonsCommand extends Command {

    private final AddonsBase addonsBase;
    private final Permissions permissions;

    public AddonsCommand(AddonsBase addonsBase, RegisterCommand registerCommand) {
        super(registerCommand.command(), registerCommand.description(), registerCommand.usageMessage(), registerCommand.aliases());
        this.addonsBase = addonsBase;
        this.permissions = registerCommand.permissions();
        this.setPermission(this.permissions.getPermission());
        this.setPermissionMessage(registerCommand.permissionMessage());
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (!this.addonsBase.isEnabled()) {
            return true;
        }
        if (this.permissions == Permissions.default_op) {
            if (!commandSender.isOp()) {
                commandSender.sendMessage(this.getPermissionMessage());
                return true;
            }
        }
        return this.addonsBase.onCommand(commandSender, this, s, strings);
    }

}
