/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.button;

import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.pos.AreaCallable;
import buildcraft.lib.misc.GuiUtil;
import buildcraft.lib.misc.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.DoubleSupplier;

/** An image button that draws its states downwards, starting at baseU. */
@OnlyIn(Dist.CLIENT)
@Deprecated
public class GuiImageButton extends GuiAbstractButton {
    private final int u, v, baseU, baseV;
    private final ResourceLocation texture;

    // public GuiImageButton(BuildCraftGui gui, int id, int x, int y, int size, ResourceLocation texture, int u, int v)
    public GuiImageButton(BuildCraftGui gui, int id, DoubleSupplier x, DoubleSupplier y, DoubleSupplier size, ResourceLocation texture, int u, int v) {
        this(gui, id, x, y, size, texture, 0, 0, u, v);
    }

    // public GuiImageButton(BuildCraftGui gui, int id, int x, int y, int size, ResourceLocation texture, int baseU, int baseV, int u, int v)
    public GuiImageButton(BuildCraftGui gui, int id, DoubleSupplier x, DoubleSupplier y, DoubleSupplier size, ResourceLocation texture, int baseU, int baseV, int u, int v) {
        // super(gui, "" + id, new GuiRectangle(x, y, size, size));
        super(gui, "" + id, new AreaCallable(x, y, size, size));
        this.u = u;
        this.v = v;
        this.baseU = baseU;
        this.baseV = baseV;
        this.texture = texture;
    }

    @Override
    public void drawBackground(float partialTicks, GuiGraphics guiGraphics) {
        if (!visible) {
            return;
        }

//        gui.mc.renderEngine.bindTexture(texture);
//        SpriteUtil.bindTexture(texture);

//        GlStateManager.color(1, 1, 1, 1);
        RenderUtil.color(1, 1, 1, 1);
//        GlStateManager.enableAlpha();
        RenderUtil.enableAlpha();
//        GlStateManager.disableBlend();
        RenderUtil.disableBlend();

        int buttonState = getButtonState();

//        GuiUtil.drawTexturedModalRect(getX(), getY(), baseU + buttonState * getWidth(), baseV, getWidth(), getHeight());
        GuiUtil.drawTexturedModalRect(guiGraphics, texture, getX(), getY(), baseU + buttonState * getWidth(), baseV, getWidth(), getHeight());
//        GuiUtil.drawTexturedModalRect(getX() + 1, getY() + 1, u, v, getWidth() - 2, getHeight() - 2);
        GuiUtil.drawTexturedModalRect(guiGraphics, texture, getX() + 1, getY() + 1, u, v, getWidth() - 2, getHeight() - 2);
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
