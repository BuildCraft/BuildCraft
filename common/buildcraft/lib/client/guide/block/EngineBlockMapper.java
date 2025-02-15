/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.block;

import buildcraft.api.enums.EnumEngineType;
import buildcraft.lib.engine.BlockEngineBase_BC8;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

@Deprecated(forRemoval = true)
public class EngineBlockMapper implements IBlockGuidePageMapper {
    @Override
    public String getFor(Level world, BlockPos pos, BlockState state) {
//        EnumEngineType type = state.getValue(BuildCraftProperties.ENGINE_TYPE);
        EnumEngineType type = (EnumEngineType) ((BlockEngineBase_BC8) state.getBlock()).engineType;
        return "engine_" + type.getSerializedName();
    }

    @Override
    public List<String> getAllPossiblePages() {
        List<String> list = Lists.newArrayList();
        for (EnumEngineType type : EnumEngineType.values()) {
            list.add("engine_" + type.getSerializedName());
        }
        return list;
    }
}
