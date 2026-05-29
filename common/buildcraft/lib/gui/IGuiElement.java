/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.lib.gui.pos.IGuiArea;

// STUB(R.Chen): GUI render — Phase 5. Forge @SideOnly → @Environment(EnvType.CLIENT).
@Environment(EnvType.CLIENT)
public interface IGuiElement extends IGuiArea, ITooltipElement, IHelpElement {
    default void drawBackground(float partialTicks) {}
    default void drawForeground(float partialTicks) {}
    default void tick() {}
    default String getDebugInfo(java.util.List<String> info) { return toString(); }
    default java.util.List<IGuiElement> getThisAndChildrenAt(double x, double y) {
        if (contains(x, y)) return com.google.common.collect.ImmutableList.of(this);
        return com.google.common.collect.ImmutableList.of();
    }
}
