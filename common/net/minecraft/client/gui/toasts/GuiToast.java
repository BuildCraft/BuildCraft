// STUB(R.Chen): Minecraft 1.12 GuiToast — removed. TODO: use ToastManager.
package net.minecraft.client.gui.toasts;

import net.minecraft.client.MinecraftClient;

public class GuiToast {
    public static void add(net.minecraft.client.toast.Toast toast) {
        MinecraftClient.getInstance().getToastManager().add(toast);
    }
}
