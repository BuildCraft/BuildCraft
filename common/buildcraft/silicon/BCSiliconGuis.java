/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.silicon;

/**
 * STUB(R.Chen): BCSiliconGuis enum stub. Full openGui() implementation deferred until
 * ExtendedScreenHandlerType is registered for each GUI type.
 */
public enum BCSiliconGuis {
    ASSEMBLY_TABLE,
    ADVANCED_CRAFTING_TABLE,
    INTEGRATION_TABLE,
    GATE;

    public void openGui(net.minecraft.entity.player.PlayerEntity player, net.minecraft.util.math.BlockPos pos, int data) {
        // STUB(R.Chen): ExtendedScreenHandlerType.open() deferred until GUI types are registered.
    }
}
