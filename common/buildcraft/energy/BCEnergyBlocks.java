/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy;

import buildcraft.lib.compat.MaterialBC;
import net.minecraft.block.AbstractBlock;

import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.enums.EnumSpring;

import buildcraft.lib.item.ItemBlockBC_Neptune;
import buildcraft.lib.registry.RegistrationHelper;

import buildcraft.core.BCCoreBlocks;
import buildcraft.energy.blocks.BlockDynamoMJ;
import buildcraft.energy.tile.TileDynamoMJ;
import buildcraft.energy.tile.TileEngineIron_BC8;
import buildcraft.energy.tile.TileEngineRF;
import buildcraft.energy.tile.TileEngineStone_BC8;
import buildcraft.energy.tile.TileSpringOil;

public class BCEnergyBlocks {

    private static final RegistrationHelper HELPER = new RegistrationHelper();

    public static BlockDynamoMJ mjDynamo;

    public static void preInit() {

        if (BCCoreBlocks.engine != null) {
            // STUB(R.Chen): engine tiles now use the (BlockEntityType, BlockPos, BlockState) constructor and are
            // created via BlockEntityType; the legacy no-arg Supplier registry path is a no-op until the
            // engine BlockEntityTypes are wired up — Phase 10.
            BCCoreBlocks.engine.registerEngine(EnumEngineType.STONE, () -> null);
            BCCoreBlocks.engine.registerEngine(EnumEngineType.IRON, () -> null);
            if (BCEnergyConfig.enableRfEngine) {
                BCCoreBlocks.engine.registerEngine(EnumEngineType.RF, () -> null);
            }
        }

        if (BCEnergyConfig.enableMjDynamo) {
            mjDynamo = HELPER.addForcedBlock(new BlockDynamoMJ(MaterialBC.IRON, "block.mj_dynamo"));
            HELPER.addForcedItem(new ItemBlockBC_Neptune(mjDynamo));
            HELPER.registerTile(TileDynamoMJ.class, "tile.mj_dynamo");
        }

        EnumSpring.OIL.liquidBlock = BCEnergyFluids.crudeOil[0].getBlock().getDefaultState();
        EnumSpring.OIL.tileConstructor = TileSpringOil::new;

        HELPER.registerTile(TileSpringOil.class, "tile.spring.oil");
        HELPER.registerTile(TileEngineStone_BC8.class, "tile.engine.stone");
        HELPER.registerTile(TileEngineIron_BC8.class, "tile.engine.iron");
        HELPER.registerTile(TileEngineRF.class, "tile.engine.rf");
    }
}
