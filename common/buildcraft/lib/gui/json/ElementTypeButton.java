package buildcraft.lib.gui.json;

import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.render.ISprite;

import buildcraft.lib.client.sprite.SpriteRaw;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.gui.GuiSpriteScaled;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.button.GuiButtonDrawable;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.misc.GuiUtil;
import buildcraft.lib.misc.SpriteUtil;

public class ElementTypeButton extends ElementType {
    public static final String NAME = "buildcraftlib:button";
    public static final ElementTypeButton INSTANCE = new ElementTypeButton();

    // Args:
    // - size[0-1]: The physical size of the button
    // - pos[0-1]: The position of the button
    // - modes.enabled[0-1]: The texture u, v of the "Enabled" button state
    // - modes.disabled[0-1]: The texture u, v of the "Disabled" button state
    // - modes.active[0-1]: The texture u, v of the "Active" button state
    // - modes.hovered[0-1]: The texture u, v of the "Active" button state
    // - modes.active_hovered[0-1]: The texture u, v of the "Active" + "Hovered" button states

    private ElementTypeButton() {
        super(NAME);
    }

    @Override
    public IGuiElement deserialize(GuiJson<?> gui, IGuiPosition parent, JsonGuiInfo info, JsonGuiElement json) {
        FunctionContext ctx = createContext(gui, json);
        inheritProperty(json, "area[0]", "pos[0]");
        inheritProperty(json, "area[1]", "pos[1]");
        inheritProperty(json, "area[2]", "size[0]");
        inheritProperty(json, "area[3]", "size[1]");

        inheritProperty(json, "size[0]", "modes.enabled[2]");
        inheritProperty(json, "size[1]", "modes.enabled[3]");
        inheritProperty(json, "sprite", "modes.enabled.sprite");
        inheritProperty(json, "texture", "modes.enabled.texture");

        inheritProperty(json, "size[0]", "modes.disabled[2]");
        inheritProperty(json, "size[1]", "modes.disabled[3]");
        inheritProperty(json, "sprite", "modes.disabled.sprite");
        inheritProperty(json, "texture", "modes.disabled.texture");

        inheritProperty(json, "size[0]", "modes.active[2]");
        inheritProperty(json, "size[1]", "modes.active[3]");
        inheritProperty(json, "sprite", "modes.active.sprite");
        inheritProperty(json, "texture", "modes.active.texture");

        inheritProperty(json, "size[0]", "modes.hovered[2]");
        inheritProperty(json, "size[1]", "modes.hovered[3]");
        inheritProperty(json, "sprite", "modes.hovered.sprite");
        inheritProperty(json, "texture", "modes.hovered.texture");

        inheritProperty(json, "size[0]", "modes.active_hovered[2]");
        inheritProperty(json, "size[1]", "modes.active_hovered[3]");
        inheritProperty(json, "sprite", "modes.active_hovered.sprite");
        inheritProperty(json, "texture", "modes.active_hovered.texture");

        int posX = resolveEquationInt(json, "pos[0]", ctx);
        int posY = resolveEquationInt(json, "pos[1]", ctx);
        int sizeX = resolveEquationInt(json, "size[0]", ctx);
        int sizeY = resolveEquationInt(json, "size[1]", ctx);

        ISimpleDrawable drEnabled = resolveDrawable(ctx, info, json, gui, sizeX, sizeY, "modes.enabled");
        GuiRectangle rect = new GuiRectangle(posX, posY, sizeX, sizeY);
        GuiButtonDrawable.Builder buttonBuilder = new GuiButtonDrawable.Builder(rect, drEnabled);
        String src = json.properties.get("source");

        buttonBuilder.active = resolveDrawable(ctx, info, json, gui, sizeX, sizeY, "modes.active");
        buttonBuilder.hovered = resolveDrawable(ctx, info, json, gui, sizeX, sizeY, "modes.hovered");
        buttonBuilder.activeHovered = resolveDrawable(ctx, info, json, gui, sizeX, sizeY, "modes.active_hovered");
        buttonBuilder.disabled = resolveDrawable(ctx, info, json, gui, sizeX, sizeY, "modes.disabled");
        buttonBuilder.disabledActive = resolveDrawable(ctx, info, json, gui, sizeX, sizeY, "modes.active_disabled");
        GuiButtonDrawable button = new GuiButtonDrawable(gui, json.name, parent, buttonBuilder);
        gui.properties.put(src, button);
        return button;
    }

    private static ISimpleDrawable resolveDrawable(FunctionContext ctx, JsonGuiInfo guiInfo, JsonGuiElement json,
        GuiJson<?> gui, int sizeX, int sizeY, String key) {
        double[] uvs = new double[4];
        for (int i = 0; i < 4; i++) {
            uvs[i] = resolveEquationDouble(json, key + "[" + i + "]", ctx);
        }
        SrcTexture texture = resolveTexture(guiInfo, json, key);
        ISprite sprite = gui.properties.get(texture.origin, ISprite.class);
        if (sprite != null) {
            sprite = GuiUtil.subRelative(sprite, uvs[0], uvs[1], uvs[2], uvs[3], texture.texSize);
        } else {
            ResourceLocation loc = SpriteUtil.transformLocation(new ResourceLocation(texture.origin));
            sprite = new SpriteRaw(loc, uvs[0], uvs[1], uvs[2], uvs[3], texture.texSize);
        }
        return new GuiSpriteScaled(sprite, sizeX, sizeY);
    }
}
