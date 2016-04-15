package buildcraft.lib.guide.font;

public interface IFontRenderer {
    int getStringWidth(String text);

    int getFontHeight();

    int drawString(String text, int x, int y, int shade);
}
