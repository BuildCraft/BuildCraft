package buildcraft.lib.client.render.font;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

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
    public int drawString(GuiGraphics guiGraphics, String text, float x, float y, int color, boolean dropShadow) {
        if (forceShadow != null) {
            dropShadow = forceShadow;
        }
        return super.drawString(guiGraphics, text, x, y, color, dropShadow);
    }

}
