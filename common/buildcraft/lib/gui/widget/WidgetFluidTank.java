/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.lib.fluid.Tank;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.Widget_Neptune;

// STUB(R.Chen): GlStateManager/RenderHelper render — Phase 5. Retains container/tank binding so
// GUIs that wire it up still compile.
@Environment(EnvType.CLIENT)
public class WidgetFluidTank extends Widget_Neptune<ContainerBC_Neptune> {
    public final Tank tank;

    public WidgetFluidTank(ContainerBC_Neptune container, Tank tank) {
        super(container);
        this.tank = tank;
    }
}
