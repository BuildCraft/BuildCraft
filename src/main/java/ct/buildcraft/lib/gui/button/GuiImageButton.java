/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.gui.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.lib.gui.BuildCraftGui;
import ct.buildcraft.lib.gui.pos.GuiRectangle;
import ct.buildcraft.lib.misc.GuiUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** An image button that draws its states downwards, starting at baseU. */
@OnlyIn(Dist.CLIENT)
@Deprecated
public class GuiImageButton extends GuiAbstractButton {
    private final int u, v, baseU, baseV;
    private final ResourceLocation texture;

    public GuiImageButton(BuildCraftGui gui, int id, int x, int y, int size, ResourceLocation texture, int u, int v) {
        this(gui, id, x, y, size, texture, 0, 0, u, v);
    }

    public GuiImageButton(BuildCraftGui gui, int id, int x, int y, int size, ResourceLocation texture, int baseU, int baseV, int u, int v) {
        super(gui, "" + id, new GuiRectangle(x, y, size, size));
        this.u = u;
        this.v = v;
        this.baseU = baseU;
        this.baseV = baseV;
        this.texture = texture;
    }

    @Override
    public void drawBackground(PoseStack pose, float partialTicks) {
        if (!visible) {
            return;
        }

        RenderSystem.setShaderTexture(0, texture);

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();

        int buttonState = getButtonState();

        GuiUtil.drawTexturedModalRect(pose, getX(), getY(), baseU + buttonState * getWidth(), baseV, getWidth(), getHeight());
        GuiUtil.drawTexturedModalRect(pose, getX() + 1, getY() + 1, u, v, getWidth() - 2, getHeight() - 2);
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
