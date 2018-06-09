package buildcraft.lib.client;

import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;

import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.misc.LocaleUtil;

public class ToastInformation implements IToast {

    public final String localeKey;
    public final ISimpleDrawable icon;
    private final Object type;

    public ToastInformation(String localeKey, ISimpleDrawable icon, Object type) {
        this.localeKey = localeKey;
        this.icon = icon;
        this.type = type;
    }

    public ToastInformation(String localeKey, ISimpleDrawable icon) {
        this(localeKey, icon, NO_TOKEN);
    }

    @Override
    public Visibility draw(GuiToast toastGui, long delta) {
        toastGui.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        toastGui.drawTexturedModalRect(0, 0, 0, 0, 160, 32);
        int x = 10;
        if (icon != null) {
            icon.drawAt(0, 0);
            x = 30;
        }
        toastGui.getMinecraft().fontRenderer.drawString(LocaleUtil.localize(localeKey), x, 13, -1);
        return delta >= 5000L ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public Object getType() {
        return type;
    }
}
