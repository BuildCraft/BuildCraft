package buildcraft.lib.gui.json;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.render.ISprite;
import buildcraft.lib.client.sprite.SpriteRaw;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.gui.GuiSpriteScaled;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.button.GuiButtonDrawable;
import buildcraft.lib.gui.button.IButtonBehaviour;
import buildcraft.lib.gui.button.IButtonClickEventListener;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.misc.GuiUtil;
import buildcraft.lib.misc.SpriteUtil;
import net.minecraft.util.ResourceLocation;

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
    public IGuiElement deserialize0(BuildCraftJsonGui gui, IGuiPosition parent, JsonGuiInfo info, JsonGuiElement json) {
        FunctionContext ctx = createContext(json);
        // TODO: Find a sane way of making the position variable!
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

        String buttonId;
        if (json.properties.containsKey("button")) {
            buttonId = json.properties.get("button");
        } else {
            buttonId = resolveEquation(json, "button_expression", ctx);
        }

        ISimpleDrawable drEnabled = resolveDrawable(ctx, info, json, gui, sizeX, sizeY, "modes.enabled");
        GuiRectangle rect = new GuiRectangle(posX, posY, sizeX, sizeY);
        GuiButtonDrawable.Builder buttonBuilder = new GuiButtonDrawable.Builder(rect, drEnabled);

        buttonBuilder.active = resolveDrawable(ctx, info, json, gui, sizeX, sizeY, "modes.active");
        buttonBuilder.hovered = resolveDrawable(ctx, info, json, gui, sizeX, sizeY, "modes.hovered");
        buttonBuilder.activeHovered = resolveDrawable(ctx, info, json, gui, sizeX, sizeY, "modes.active_hovered");
        buttonBuilder.disabled = resolveDrawable(ctx, info, json, gui, sizeX, sizeY, "modes.disabled");
        buttonBuilder.disabledActive = resolveDrawable(ctx, info, json, gui, sizeX, sizeY, "modes.active_disabled");
        GuiButtonDrawable button = new GuiButtonDrawable(gui, json.name, parent, buttonBuilder);
        IButtonBehaviour behaviour = gui.properties.get(buttonId, IButtonBehaviour.class);
        if (behaviour != null) {
            button.setBehaviour(behaviour);
        }
        Boolean current = gui.properties.get(buttonId, Boolean.class);
        if (current != null) {
            button.setActive(current.booleanValue());
        }
        IButtonClickEventListener listener = gui.properties.get(buttonId, IButtonClickEventListener.class);
        if (listener == null) {
            BCLog.logger.warn("[lib.gui.json] Unknown button id '" + buttonId + "'");
        } else {
            button.registerListener(listener);
        }
        return button;
    }

    private static ISimpleDrawable resolveDrawable(FunctionContext ctx, JsonGuiInfo guiInfo, JsonGuiElement json,
                                                   BuildCraftJsonGui gui, int sizeX, int sizeY, String key) {
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
