/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.lib.gui.pos.IGuiPosition;

@FunctionalInterface
public interface ISimpleDrawable {
    void drawAt(PoseStack pose, double x, double y);

    default void drawAt(PoseStack pose, IGuiPosition element) {
        drawAt(pose, element.getX(), element.getY());
    }

    default ISimpleDrawable andThen(ISimpleDrawable after) {
        ISimpleDrawable t = this;
        return (p, x, y) -> {
            t.drawAt(p, x, y);
            after.drawAt(p, x, y);
        };
    }
}
