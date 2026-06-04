/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.gui;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.gui.DrawContext;

import buildcraft.lib.gui.pos.IGuiArea;

@Environment(EnvType.CLIENT)
public interface IGuiElement extends IGuiArea, ITooltipElement, IHelpElement {
    default void drawBackground(DrawContext context, float partialTicks) {}
    default void drawForeground(DrawContext context, float partialTicks) {}
    default void tick() {}
    default String getDebugInfo(List<String> info) { return toString(); }
    default List<IGuiElement> getThisAndChildrenAt(double x, double y) {
        if (contains(x, y)) return com.google.common.collect.ImmutableList.of(this);
        return com.google.common.collect.ImmutableList.of();
    }
}
