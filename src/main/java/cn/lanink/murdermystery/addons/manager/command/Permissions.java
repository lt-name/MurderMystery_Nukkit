package cn.lanink.murdermystery.addons.manager.command;

/**
 * @author lt_name
 */
public enum Permissions {
    default_true("MurderMystery.command.user"),
    default_op("MurderMystery.command.admin");

    private final String permission;

    Permissions(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return this.permission;
    }

}
