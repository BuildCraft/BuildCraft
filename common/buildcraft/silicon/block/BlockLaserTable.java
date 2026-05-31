/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.silicon.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.enums.EnumLaserTableType;
import buildcraft.api.mj.ILaserTargetBlock;

import buildcraft.lib.block.BlockBCBase_Neptune;

import buildcraft.silicon.tile.TileAdvancedCraftingTable;
import buildcraft.silicon.tile.TileAssemblyTable;
import buildcraft.silicon.tile.TileChargingTable;
import buildcraft.silicon.tile.TileIntegrationTable;
import buildcraft.silicon.tile.TileProgrammingTable_Neptune;

/**
 * STUB(R.Chen): BlockBCTile_Neptune not available in libLeaf — extends BlockBCBase_Neptune directly.
 * onUse (was onBlockActivated), BlockEntityProvider, VoxelShape deferred.
 * BCSiliconGuis.openGui deferred until ExtendedScreenHandlerType is registered.
 * isOpaqueCube/isFullCube/getBlockLayer do not exist in 1.20.1 — removed.
 */
public class BlockLaserTable extends BlockBCBase_Neptune implements ILaserTargetBlock {
    private final EnumLaserTableType type;

    public BlockLaserTable(EnumLaserTableType type, Settings settings, String id) {
        super(settings, id);
        this.type = type;
    }

    // STUB(R.Chen): createBlockEntity deferred until BlockEntityType is registered.
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        switch (type) {
            case ASSEMBLY_TABLE:
                return new TileAssemblyTable(null, pos, state);
            case ADVANCED_CRAFTING_TABLE:
                return new TileAdvancedCraftingTable(null, pos, state);
            case INTEGRATION_TABLE:
                return new TileIntegrationTable(null, pos, state);
            case CHARGING_TABLE:
                return new TileChargingTable(null, pos, state);
            case PROGRAMMING_TABLE:
                return new TileProgrammingTable_Neptune(null, pos, state);
            default:
                return null;
        }
    }

    // STUB(R.Chen): onUse (was onBlockActivated) deferred — BCSiliconGuis.openGui needs
    // ExtendedScreenHandlerType registration first.
    // STUB(R.Chen): getBoundingBox/VoxelShape (was Box) deferred to lib.block phase.
}
