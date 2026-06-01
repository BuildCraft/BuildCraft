/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.fluid;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FlowableFluid;

// STUB(R.Chen): original BCFluidBlock extended net.minecraftforge.fluids.BlockFluidClassic.
// Replaced with a thin Fabric FluidBlock shell. Forge-specific overrides (isEntityInsideMaterial,
// getFlammability, onEntityCollidedWithBlock) removed — TODO(R.Chen): re-implement via Fabric API.

public class BCFluidBlock extends FluidBlock {

    private boolean sticky = false;

    public BCFluidBlock(FlowableFluid fluid, AbstractBlock.Settings settings) {
        super(fluid, settings);
    }

    public void setSticky(boolean sticky) {
        this.sticky = sticky;
    }

    public boolean isSticky() {
        return sticky;
    }
}
