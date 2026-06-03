/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): ContainerList deferred
package buildcraft.core.list;

import net.minecraft.entity.player.PlayerEntity;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.widget.WidgetPhantomSlot;

public class ContainerList extends ContainerBC_Neptune {

    public class WidgetListSlot extends WidgetPhantomSlot {
        public final int lineIndex;
        public final int slotIndex;

        public WidgetListSlot(int lineIndex, int slotIndex) {
            this.lineIndex = lineIndex;
            this.slotIndex = slotIndex;
        }
    }

    public ContainerList(PlayerEntity player) {
        super(player, 0);
    }
}
