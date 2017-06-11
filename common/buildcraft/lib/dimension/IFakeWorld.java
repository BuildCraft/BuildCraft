/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.dimension;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.schematics.ISchematicBlock;

import buildcraft.builders.snapshot.Blueprint;

/**
 * Delegate interface for fake world logic, implementing classes MUST extend world or a subclass of world
 */
public interface IFakeWorld {


    FakeChunkProvider getFakeChunkProvider();

    default void clear(BlueprintLocationManager.BlueprintLocation location) {
        for (int z = -1; z <= location.size.getZ() + 1; z++) {
            for (int y = -1; y <= location.size.getY() + 1; y++) {
                for (int x = -1; x <= location.size.getX() + 1; x++) {
                    setBlockState(new BlockPos(x, y, z).add(location.startPos), Blocks.AIR.getDefaultState());
                }
            }
        }
    }

    boolean setBlockState(BlockPos pos, IBlockState state);

    default void uploadBlueprint(Blueprint blueprint, BlueprintLocationManager.BlueprintLocation location, boolean useStone) {
        for (int z = -1; z <= blueprint.size.getZ(); z++) {
            for (int y = -1; y <= blueprint.size.getY(); y++) {
                for (int x = -1; x <= blueprint.size.getX(); x++) {
                    BlockPos pos = new BlockPos(x, y, z).add(location.startPos);
                    if (x == -1 || y == -1 || z == -1 ||
                            x == blueprint.size.getX() ||
                            y == blueprint.size.getY() ||
                            z == blueprint.size.getZ()) {
                        setBlockState(pos, useStone ? Blocks.STONE.getDefaultState() : Blocks.AIR.getDefaultState());
                    } else {
                        ISchematicBlock<?> schematicBlock = blueprint.palette.get(blueprint.data[x][y][z]);
                        schematicBlock.buildWithoutChecks((World) this, pos);
                    }
                }
            }
        }
    }

}
