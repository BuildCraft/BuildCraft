package buildcraft.lib.gui;

import buildcraft.api.core.render.ISprite;

/** An {@link ISimpleDrawable} that draws the specified {@link ISprite} as-is into the given width and height. */
public class GuiSpriteScaled implements ISimpleDrawable {
    public final ISprite sprite;
    public final double width, height;

    public GuiSpriteScaled(ISprite sprite, double width, double height) {
        this.sprite = sprite;
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawAt(int x, int y) {
        GuiIcon.draw(sprite, x, y, x + width, y + height);
    }
}
