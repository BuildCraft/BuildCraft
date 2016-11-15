package buildcraft.transport.gui;

import java.util.List;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import buildcraft.api.statements.IGuiSlot;

import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.gui.pos.IPositionedElement;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.SpriteUtil;
import buildcraft.lib.misc.StringUtilBC;
import buildcraft.lib.misc.data.IReference;
import buildcraft.transport.gate.StatementWrapper;

public abstract class ElementGuiSlot<T extends IGuiSlot> extends GuiElementSimple<GuiGate> {

    public final IReference<T> reference;

    public ElementGuiSlot(GuiGate gui, IPositionedElement element, IReference<T> reference) {
        super(gui, element);
        this.reference = reference;
    }

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        if (gui.currentHover != null && gui.currentHover != this) return;
        T value = reference.get();
        if (value != null && contains(gui.mouse)) {
            String[] arr = { value.getDescription() };
            if (value instanceof StatementWrapper) {
                EnumFacing face = ((StatementWrapper) value).sourcePart.face;
                if (face != null) {
                    String translated = ColourUtil.getTextFullTooltip(face);
                    translated = StringUtilBC.localize("gate.side", translated);
                    arr = new String[] { arr[0], translated };
                }
            }
            tooltips.add(new ToolTip(arr));
        }
    }

    @Override
    public void drawBackground(float partialTicks) {
        drawSprite(gui, reference.get(), this);
    }

    public static void drawSprite(GuiGate gui, IGuiSlot slot, IGuiPosition element) {
        if (slot != null) {
            TextureAtlasSprite sprite = slot.getGuiSprite();
            if (sprite != null) {
                SpriteUtil.bindBlockTextureMap();
                gui.drawTexturedModalRect(element.getX() + 1, element.getY() + 1, sprite, 16, 16);
            }
        }
    }
}
