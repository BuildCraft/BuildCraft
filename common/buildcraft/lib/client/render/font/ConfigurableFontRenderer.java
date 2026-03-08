package buildcraft.lib.client.render.font;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;

public class ConfigurableFontRenderer extends DelegateFontRenderer {

    private Boolean forceShadow = null;

    public ConfigurableFontRenderer(FontRenderer delegate) {
        super(delegate);
    }

    public ConfigurableFontRenderer leaveShadow() {
        forceShadow = null;
        return this;
    }

    public ConfigurableFontRenderer disableShadow() {
        forceShadow = false;
        return this;
    }

    public ConfigurableFontRenderer forceShadow() {
        forceShadow = true;
        return this;
    }

    @Override
    public int drawString(MatrixStack poseStack, String text, float x, float y, int color, boolean dropShadow) {
        if (forceShadow != null) {
            dropShadow = forceShadow;
        }
        return super.drawString(poseStack, text, x, y, color, dropShadow);
    }

}
