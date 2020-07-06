package cn.lanink.murdermystery.addons.manager.autoregister;

import cn.lanink.murdermystery.addons.manager.command.Permissions;

import java.lang.annotation.*;

/**
 * @author lt_name
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RegisterCommands.class)
public @interface RegisterCommand {
    String command();
    String description() default "";
    String usageMessage() default "";
    String[] aliases() default "";
    Permissions permissions() default Permissions.default_true;
    String permissionMessage() default "你没有权限执行此命令！";
    String fallbackPrefix() default "";

}
