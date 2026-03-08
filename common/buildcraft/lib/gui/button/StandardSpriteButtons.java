package buildcraft.lib.gui.button;

import buildcraft.api.core.render.ISprite;
import buildcraft.lib.client.sprite.SpriteRaw;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.misc.GuiUtil;
import net.minecraft.util.ResourceLocation;

public class StandardSpriteButtons {

    public static final GuiButtonDrawable.Builder LARGE_BUTTON_DRAWABLE;
    public static final GuiButtonDrawable.Builder SMALL_BUTTON_DRAWABLE;
    public static final GuiButtonDrawable.Builder HALF_BUTTON_DRAWABLE;
    public static final GuiButtonDrawable.Builder QUARTER_BUTTON_DRAWABLE;
    public static final GuiButtonDrawable.Builder EIGHTH_BUTTON_DRAWABLE;
    public static final GuiButtonDrawable.Builder LEFT_BUTTON_DRAWABLE;
    public static final GuiButtonDrawable.Builder RIGHT_BUTTON_DRAWABLE;
    public static final GuiButtonDrawable.Builder LOCK_BUTTON_DRAWABLE;
    public static final GuiButtonDrawable.Builder TINY_BUTTON_DRAWABLE;

    static {
        ResourceLocation buttonSheet = new ResourceLocation("buildcraftlib:textures/gui/buttons.png");

        ISprite sprite = new SpriteRaw(buttonSheet, 0, 0, 1, 1);

        GuiRectangle size = new GuiRectangle(200, 20);
        GuiRectangle rect = new GuiRectangle(200, 20);
        LARGE_BUTTON_DRAWABLE = new GuiButtonDrawable.Builder(size, defineButton(sprite, rect.offset(0, 20)));
        LARGE_BUTTON_DRAWABLE.disabled = defineButton(sprite, rect);
        LARGE_BUTTON_DRAWABLE.hovered = defineButton(sprite, rect.offset(0, 2 * 20));
        LARGE_BUTTON_DRAWABLE.active = defineButton(sprite, rect.offset(0, 3 * 20));
        LARGE_BUTTON_DRAWABLE.activeHovered = defineButton(sprite, rect.offset(0, 4 * 20));

        rect = new GuiRectangle(0, 100, 200, 15);
        size = new GuiRectangle(200, 15);
        SMALL_BUTTON_DRAWABLE = new GuiButtonDrawable.Builder(size, defineButton(sprite, rect.offset(0, 15)));
        SMALL_BUTTON_DRAWABLE.disabled = defineButton(sprite, rect);
        SMALL_BUTTON_DRAWABLE.hovered = defineButton(sprite, rect.offset(0, 2 * 15));
        SMALL_BUTTON_DRAWABLE.active = defineButton(sprite, rect.offset(0, 3 * 15));
        SMALL_BUTTON_DRAWABLE.activeHovered = defineButton(sprite, rect.offset(0, 4 * 15));

        rect = new GuiRectangle(0, 175, 100, 15);
        size = new GuiRectangle(100, 15);
        HALF_BUTTON_DRAWABLE = new GuiButtonDrawable.Builder(size, defineButton(sprite, rect.offset(0, 15)));
        HALF_BUTTON_DRAWABLE.disabled = defineButton(sprite, rect);
        HALF_BUTTON_DRAWABLE.hovered = defineButton(sprite, rect.offset(0, 2 * 15));
        HALF_BUTTON_DRAWABLE.active = defineButton(sprite, rect.offset(0, 3 * 15));
        HALF_BUTTON_DRAWABLE.activeHovered = defineButton(sprite, rect.offset(0, 4 * 15));

        rect = new GuiRectangle(100, 175, 50, 15);
        size = new GuiRectangle(50, 15);
        QUARTER_BUTTON_DRAWABLE = new GuiButtonDrawable.Builder(size, defineButton(sprite, rect.offset(0, 15)));
        QUARTER_BUTTON_DRAWABLE.disabled = defineButton(sprite, rect);
        QUARTER_BUTTON_DRAWABLE.hovered = defineButton(sprite, rect.offset(0, 2 * 15));
        QUARTER_BUTTON_DRAWABLE.active = defineButton(sprite, rect.offset(0, 3 * 15));
        QUARTER_BUTTON_DRAWABLE.activeHovered = defineButton(sprite, rect.offset(0, 4 * 15));

        rect = new GuiRectangle(150, 175, 25, 15);
        size = new GuiRectangle(25, 15);
        EIGHTH_BUTTON_DRAWABLE = new GuiButtonDrawable.Builder(size, defineButton(sprite, rect.offset(0, 15)));
        EIGHTH_BUTTON_DRAWABLE.disabled = defineButton(sprite, rect);
        EIGHTH_BUTTON_DRAWABLE.hovered = defineButton(sprite, rect.offset(0, 2 * 15));
        EIGHTH_BUTTON_DRAWABLE.active = defineButton(sprite, rect.offset(0, 3 * 15));
        EIGHTH_BUTTON_DRAWABLE.activeHovered = defineButton(sprite, rect.offset(0, 4 * 15));

        rect = new GuiRectangle(204, 0, 10, 16);
        size = new GuiRectangle(10, 16);
        LEFT_BUTTON_DRAWABLE = new GuiButtonDrawable.Builder(size, defineButton(sprite, rect.offset(0, 16)));
        LEFT_BUTTON_DRAWABLE.disabled = defineButton(sprite, rect);
        LEFT_BUTTON_DRAWABLE.hovered = defineButton(sprite, rect.offset(0, 2 * 16));

        rect = rect.offset(10, 0);
        RIGHT_BUTTON_DRAWABLE = new GuiButtonDrawable.Builder(size, defineButton(sprite, rect.offset(0, 16)));
        RIGHT_BUTTON_DRAWABLE.disabled = defineButton(sprite, rect);
        RIGHT_BUTTON_DRAWABLE.hovered = defineButton(sprite, rect.offset(0, 2 * 16));

        rect = new GuiRectangle(224, 0, 16, 16);
        size = new GuiRectangle(16, 16);
        LOCK_BUTTON_DRAWABLE = new GuiButtonDrawable.Builder(size, defineButton(sprite, rect.offset(0, 16)));
        LOCK_BUTTON_DRAWABLE.disabled = defineButton(sprite, rect);
        LOCK_BUTTON_DRAWABLE.disabledActive = defineButton(sprite, rect.offset(16, 0));
        LOCK_BUTTON_DRAWABLE.active = defineButton(sprite, rect.offset(16, 16));
        LOCK_BUTTON_DRAWABLE.hovered = defineButton(sprite, rect.offset(0, 32));
        LOCK_BUTTON_DRAWABLE.activeHovered = defineButton(sprite, rect.offset(16, 32));

        rect = new GuiRectangle(226, 48, 10, 10);
        size = new GuiRectangle(10, 10);
        TINY_BUTTON_DRAWABLE = new GuiButtonDrawable.Builder(size, defineButton(sprite, rect.offset(10, 0)));
        TINY_BUTTON_DRAWABLE.disabled = defineButton(sprite, rect);
        TINY_BUTTON_DRAWABLE.hovered = defineButton(sprite, rect.offset(20, 0));

        // Replace GuiAbstractButton + subclasses with this + GuiSpriteButton + related methods for generating text
        // elements + util methods for creating buttons like the old system + move everything to json.
        // But finish the filler gui before moving everything else to json
    }

    public static ISimpleDrawable defineButton(ISprite mainSprite, GuiRectangle rect) {
        return new GuiIcon(GuiUtil.subRelative(mainSprite, rect.x, rect.y, rect.width, rect.height, 256), 256);
    }
}
