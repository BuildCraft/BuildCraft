// STUB(R.Chen): ICommandSender → CommandSource/ServerCommandSource shim for 1.12.2 compat
package net.minecraft.command;

public interface ICommandSender {
    String getCommandSenderName();
    default void sendMessage(net.minecraft.text.Text component) {}
}
