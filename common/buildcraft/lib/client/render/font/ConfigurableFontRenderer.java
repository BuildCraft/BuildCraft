package buildcraft.lib.client.render.font;

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
    public int drawString(String text, float x, float y, int color, boolean dropShadow) {
        if (forceShadow != null) {
            dropShadow = forceShadow;
        }
        return super.drawString(text, x, y, color, dropShadow);
    }

}
