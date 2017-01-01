package buildcraft.lib.gui;

import buildcraft.lib.client.sprite.ISprite;

/** An {@link ISimpleDrawable} that draws the specified {@link ISprite} as-is into the given width and height. */
public class GuiSpriteScaled implements ISimpleDrawable {
    public final ISprite sprite;
    public final int width, height;

    public GuiSpriteScaled(ISprite sprite, int width, int height) {
        this.sprite = sprite;
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawAt(int x, int y) {
        GuiIcon.draw(sprite, x, y, x + width, y + height);
    }
}
