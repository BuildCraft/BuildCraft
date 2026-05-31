/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.energy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

// STUB(R.Chen): Forge player.openGui(modInstance, guiId, world, x, y, z) →
// Fabric ExtendedScreenHandlerType. GUI opening is deferred until each engine's
// ScreenHandlerType is registered in BCEnergyInitializer.
public enum BCEnergyGuis {
    ENGINE_STONE,
    ENGINE_IRON,
    ENGINE_RF,
    DYNAMO_MJ;

    public static final BCEnergyGuis[] VALUES = values();

    public static BCEnergyGuis get(int id) {
        if (id < 0 || id >= VALUES.length) return null;
        return VALUES[id];
    }

    public void openGUI(PlayerEntity player) {
        // STUB(R.Chen): Forge player.openGui → Fabric ExtendedScreenHandlerType.openHandledScreen.
    }

    public void openGUI(PlayerEntity player, BlockPos pos) {
        // STUB(R.Chen): Forge player.openGui → Fabric ExtendedScreenHandlerType.openHandledScreen.
    }
}
