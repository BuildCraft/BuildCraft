/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */

package buildcraft.lib.gui.button;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;

/** An image button that draws its states downwards, starting at baseU. */
@SideOnly(Side.CLIENT)
@Deprecated
public class GuiImageButton extends GuiAbstractButton<GuiBC8<?>> {
    private final int u, v, baseU, baseV;
    private final ResourceLocation texture;

    public GuiImageButton(GuiBC8<?> gui, String id, IGuiPosition parent, GuiRectangle rect, ResourceLocation texture, int u, int v) {
        this(gui, id, rect.offset(parent), texture, 0, 0, u, v);
    }

    public GuiImageButton(GuiBC8<?> gui, String id, IGuiArea area, ResourceLocation texture, int baseU, int baseV, int u, int v) {
        super(gui, id, area);
        this.u = u;
        this.v = v;
        this.baseU = baseU;
        this.baseV = baseV;
        this.texture = texture;
        throw new Error("Can't use this! Is deprecated!");
    }

    @Override
    public void drawBackground(float partialTicks) {
        if (!visible) {
            return;
        }

        gui.mc.renderEngine.bindTexture(texture);

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();

        int buttonState = getButtonState();

        gui.drawTexturedModalRect(getX(), getY(), baseU + buttonState * getWidth(), baseV, getWidth(), getHeight());
        gui.drawTexturedModalRect(getX() + 1, getY() + 1, u, v, getWidth() - 2, getHeight() - 2);
    }

    private int getButtonState() {
        if (!this.enabled) {
            return 0;
        }

        if (isMouseOver()) {
            if (!this.active) {
                return 2;
            } else {
                return 4;
            }
        }

        if (!this.active) {
            return 1;
        } else {
            return 3;
        }
    }
}
