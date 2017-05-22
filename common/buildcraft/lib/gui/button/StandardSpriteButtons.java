package buildcraft.lib.gui.button;

import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.render.ISprite;

import buildcraft.lib.client.sprite.SpriteRaw;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.PositionAbsolute;
import buildcraft.lib.misc.GuiUtil;

public class StandardSpriteButtons {

    public static final GuiSpriteButton.Builder LARGE_BUTTON_DRAWABLE;
    public static final GuiSpriteButton.Builder SMALL_BUTTON_DRAWABLE;
    public static final GuiSpriteButton.Builder LEFT_BUTTON_DRAWABLE;
    public static final GuiSpriteButton.Builder RIGHT_BUTTON_DRAWABLE;
    public static final GuiSpriteButton.Builder TINY_BUTTON_DRAWABLE;
    public static final GuiSpriteButton.Builder LOCK_BUTTON_DRAWABLE;

    static {
        ResourceLocation buttonSheet = new ResourceLocation("buildcraftlib:textures/gui/buttons.png");

        ISprite sprite = new SpriteRaw(buttonSheet, 0, 0, 1, 1);

        GuiRectangle rect = new GuiRectangle(200, 20);
        LARGE_BUTTON_DRAWABLE = new GuiSpriteButton.Builder(rect, defineButton(sprite, rect.offset(0, 20)));
        LARGE_BUTTON_DRAWABLE.disabled = defineButton(sprite, rect);
        LARGE_BUTTON_DRAWABLE.hovered = defineButton(sprite, rect.offset(0, 2 * 20));
        LARGE_BUTTON_DRAWABLE.active = defineButton(sprite, rect.offset(0, 3 * 20));
        LARGE_BUTTON_DRAWABLE.activeHovered = defineButton(sprite, rect.offset(0, 4 * 20));

        rect = new GuiRectangle(0, 100, 200, 15);
        LARGE_BUTTON_DRAWABLE = new GuiSpriteButton.Builder(rect, defineButton(sprite, rect.offset(0, 15)));
        LARGE_BUTTON_DRAWABLE.disabled = defineButton(sprite, rect);
        LARGE_BUTTON_DRAWABLE.hovered = defineButton(sprite, rect.offset(0, 2 * 15));
        LARGE_BUTTON_DRAWABLE.active = defineButton(sprite, rect.offset(0, 3 * 15));
        LARGE_BUTTON_DRAWABLE.activeHovered = defineButton(sprite, rect.offset(0, 4 * 15));

        // TODO: Define the rest of these!
        // Replace GuiAbstractButton + subclasses with this + GuiSpriteButton + related methods for generating text elements + util methods for creating buttons like the old system + move everything to json.
        // But finish the filler gui before moving everything else to json

        SMALL_BUTTON_DRAWABLE = defineButtons(buttonSheet, new GuiRectangle(0, 100, 200, 15), new PositionAbsolute(0, 15), true);
        LEFT_BUTTON_DRAWABLE = defineButtons(buttonSheet, new GuiRectangle(204, 0, 10, 16), new PositionAbsolute(0, 16), false);
        RIGHT_BUTTON_DRAWABLE = defineButtons(buttonSheet, new GuiRectangle(214, 0, 10, 16), new PositionAbsolute(0, 16), false);
        TINY_BUTTON_DRAWABLE = defineButtons(buttonSheet, new GuiRectangle(214, 0, 10, 16), new PositionAbsolute(0, 16), true);
    }

    public static ISimpleDrawable defineButton(ISprite mainSprite, GuiRectangle rect) {
        return new GuiIcon(GuiUtil.subRelative(mainSprite, rect.x, rect.y, rect.width, rect.height, 256), 256);
    }
}
