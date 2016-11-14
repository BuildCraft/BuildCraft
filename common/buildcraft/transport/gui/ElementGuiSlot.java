package buildcraft.transport.gui;

import java.util.List;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import buildcraft.api.statements.IGuiSlot;

import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.GuiRectangle;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.misc.SpriteUtil;

public abstract class ElementGuiSlot<T extends IGuiSlot> extends GuiElementSimple<GuiGate> {

    public final T[] values;
    public final int index;

    public ElementGuiSlot(GuiGate gui, IGuiPosition parent, GuiRectangle rectangle, T[] values, int index) {
        super(gui, parent, rectangle);
        this.values = values;
        this.index = index;
    }

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        T value = values[index];
        if (value != null && contains(gui.mouse)) {
            tooltips.add(new ToolTip(value.getDescription()));
        }
    }

    @Override
    public void drawBackground(float partialTicks) {
        drawSprite(gui, values[index], this);
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
