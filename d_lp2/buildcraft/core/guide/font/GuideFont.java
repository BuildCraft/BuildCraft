package buildcraft.core.guide.font;

import java.io.InputStream;

public class GuideFont implements IFontRenderer {
    private final SimpleTextureMap texture = new SimpleTextureMap();

    public GuideFont(InputStream stream) throws Exception {

    }

    @Override
    public int getStringWidth(String text) {
        return 0;
    }

    @Override
    public int getFontHeight() {
        return 0;
    }

    @Override
    public int drawString(String text, int x, int y, int shade) {
        return 0;
    }
}
