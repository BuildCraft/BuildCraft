package buildcraft.lib.gui.json;

import buildcraft.api.core.render.ISprite;
import buildcraft.lib.client.sprite.SpriteRaw;
import buildcraft.lib.client.sprite.SubSpriteChanging;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.gui.GuiSpriteScaled;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.elem.GuiElementDrawable;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.gui.pos.PositionAbsolute;
import buildcraft.lib.misc.GuiUtil;
import buildcraft.lib.misc.SpriteUtil;
import net.minecraft.resources.ResourceLocation;

public class ElementTypeSprite extends ElementType {
    public static final String NAME = "buildcraftlib:drawable";
    public static final ElementTypeSprite INSTANCE = new ElementTypeSprite();

    // Args:
    // - pos[0], pos[1]: the position of the sprite (where it will be drawn, relative to the root of the gui). Defaults
    // to 0,0
    // - size[0], size[1]: the size of the sprite to be drawn.
    // - area[0-3]: mapping for pos[0], pos[1], size[0], size[1]
    // - source.sprite source.texture: the sprite where this texture will be drawn from. Defaults to the gui's texture
    // - source.area[0-3]: the area where the sprite will be taken from. 0-16 for sprites, 0-256 for textures

    private ElementTypeSprite() {
        super(NAME);
    }

    @Override
    public IGuiElement deserialize0(BuildCraftJsonGui gui, IGuiPosition parent, JsonGuiInfo info, JsonGuiElement json) {
        FunctionContext ctx = createContext(json);
        inheritProperty(json, "pos[0]", "area[0]");
        inheritProperty(json, "pos[1]", "area[1]");
        inheritProperty(json, "size[0]", "area[2]");
        inheritProperty(json, "size[1]", "area[3]");

        inheritProperty(json, "source.pos[0]", "source.area[0]");
        inheritProperty(json, "source.pos[1]", "source.area[1]");
        inheritProperty(json, "source.size[0]", "source.area[2]");
        inheritProperty(json, "source.size[1]", "source.area[3]");

        inheritProperty(json, "area", "source.area");
        inheritProperty(json, "area[0]", "source.area[0]");
        inheritProperty(json, "area[1]", "source.area[1]");
        inheritProperty(json, "area[2]", "source.area[2]");
        inheritProperty(json, "area[3]", "source.area[3]");

        IGuiArea area = resolveArea(json, "area", parent, ctx);
        IGuiArea srcArea = resolveArea(json, "source.area", PositionAbsolute.ORIGIN, ctx);

        INodeBoolean visible = getEquationBool(json, "visible", ctx, true);
        boolean foreground = resolveEquationBool(json, "foreground", ctx, false);

        // TODO: Allow the source sprite to be changing as well!
        SrcTexture tex = resolveTexture(info, json, "source");
        String origin = tex.origin;
        int texSize = tex.texSize;

        if (!json.properties.containsKey("source.area[2]")//
                && !json.properties.containsKey("source.area[3]")//
                && !json.properties.containsKey("source.area"))
        {
            srcArea = new GuiRectangle(texSize, texSize);
        }

        ISprite sprite = gui.properties.get(origin, ISprite.class);

        if (sprite == null) {
            ResourceLocation loc = SpriteUtil.transformLocation(new ResourceLocation(origin));
            sprite = new SpriteRaw(loc, 0, 0, 1, 1);
        }

        if (srcArea instanceof GuiRectangle) {
            double u = srcArea.getX();
            double v = srcArea.getY();
            double uSize = srcArea.getWidth();
            double vSize = srcArea.getHeight();
            sprite = GuiUtil.subRelative(sprite, u, v, uSize, vSize, texSize);
        } else {
            final IGuiArea a = srcArea;
            INodeDouble u = () -> a.getX() / texSize;
            INodeDouble v = () -> a.getY() / texSize;
            INodeDouble uSize = () -> a.getEndX() / texSize;
            INodeDouble vSize = () -> a.getEndY() / texSize;
            sprite = new SubSpriteChanging(sprite, u, v, uSize, vSize);
        }

        ISimpleDrawable icon = new GuiSpriteScaled(sprite, area.offsetToOrigin());
        GuiElementDrawable element = new GuiElementDrawable(gui, area, icon, foreground, visible);
        return element;
    }
}
