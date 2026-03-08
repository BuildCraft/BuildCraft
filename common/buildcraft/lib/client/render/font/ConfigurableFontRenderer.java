package buildcraft.lib.client.render.font;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;

public class ConfigurableFontRenderer extends DelegateFontRenderer {

    private Boolean forceShadow = null;

    public ConfigurableFontRenderer(Font delegate) {
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
    public int drawString(PoseStack poseStack, String text, float x, float y, int color, boolean dropShadow) {
        if (forceShadow != null) {
            dropShadow = forceShadow;
        }
        return super.drawString(poseStack, text, x, y, color, dropShadow);
    }

}
