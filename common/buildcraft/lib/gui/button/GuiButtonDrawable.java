/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.gui.button;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.gui.DrawContext;
import com.mojang.blaze3d.systems.RenderSystem;

import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;

@Environment(EnvType.CLIENT)
public final class GuiButtonDrawable extends GuiAbstractButton {
    private final ISimpleDrawable drEnabled, drActive, drHovered, drActiveHovered, drDisabled, drDisabledActive;

    public static class Builder {
        public final IGuiArea rect;
        public final ISimpleDrawable enabled;
        public ISimpleDrawable active;
        public ISimpleDrawable hovered;
        public ISimpleDrawable activeHovered;
        public ISimpleDrawable disabled;
        public ISimpleDrawable disabledActive;

        public Builder(IGuiArea rect, ISimpleDrawable enabled) {
            this.rect = rect;
            this.enabled = enabled;
        }
    }

    public GuiButtonDrawable(BuildCraftGui gui, String id, IGuiPosition parent, Builder args) {
        super(gui, id, args.rect.offset(parent));
        this.drEnabled = args.enabled;
        this.drActive = firstNonnull(args.active, args.enabled);
        this.drHovered = firstNonnull(args.hovered, args.enabled);
        this.drActiveHovered = firstNonnull(args.activeHovered, args.hovered, args.active, args.enabled);
        this.drDisabled = firstNonnull(args.disabled, args.enabled);
        this.drDisabledActive = firstNonnull(args.disabledActive, args.disabled, args.enabled);
    }

    private static ISimpleDrawable firstNonnull(ISimpleDrawable... of) {
        for (ISimpleDrawable d : of) if (d != null) return d;
        throw new NullPointerException("No non-null drawable found!");
    }

    @Override
    public void drawBackground(DrawContext context, float partialTicks) {
        if (!visible) return;
        RenderSystem.setShaderColor(1, 1, 1, 1);
        if (enabled) {
            boolean hovered = isMouseOver();
            if (active) {
                (hovered ? drActiveHovered : drActive).drawAt(this);
            } else if (hovered) {
                drHovered.drawAt(this);
            } else {
                drEnabled.drawAt(this);
            }
        } else if (active) {
            drDisabledActive.drawAt(this);
        } else {
            drDisabled.drawAt(this);
        }
    }
}
