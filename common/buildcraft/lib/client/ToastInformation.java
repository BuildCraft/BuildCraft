package buildcraft.lib.client;

import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import com.mojang.blaze3d.systems.RenderSystem;

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

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public Visibility draw(GuiToast toastGui, long delta) {
        toastGui.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        toastGui.drawTexturedModalRect(0, 0, 0, 0, 160, 32);
        int x = 10;
        if (icon != null) {
            icon.drawAt(0, 0);
            x = 30;
        }
        toastGui.getMinecraft().fontRenderer.draw(new net.minecraft.client.util.math.MatrixStack(), LocaleUtil.localize(localeKey), x, 13, -1);
        return delta >= 5000L ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public Object getType() {
        return type;
    }
}
