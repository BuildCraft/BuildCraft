/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.block;


import buildcraft.api.enums.EnumEngineType;
import buildcraft.lib.engine.BlockEngineBase_BC8;
import buildcraft.lib.engine.TileEngineBase_BC8;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;

public class BlockEngine_BC8 extends BlockEngineBase_BC8<EnumEngineType> {
    public BlockEngine_BC8(String idBC, BlockBehaviour.Properties properties, EnumEngineType type, BiFunction<BlockPos, BlockState, ? extends TileEngineBase_BC8> engineTileConstructor) {
        super(idBC, properties, type, engineTileConstructor);
    }

//    @Override
//    public Property<EnumEngineType> getEngineProperty() {
//        return BuildCraftProperties.ENGINE_TYPE;
//    }

//    @Override
//    public EnumEngineType getEngineType(int meta) {
//        return EnumEngineType.fromMeta(meta);
//    }

//    @Override
//    public String getUnlocalizedName() {
////        return TagManager.getTag("block.engine.bc." + engine.unlocalizedTag, TagManager.EnumTagType.UNLOCALIZED_NAME);
//        return TagManager.getTag("block.engine.bc." + this.engineType.unlocalizedTag, TagManager.EnumTagType.UNLOCALIZED_NAME);
//    }
}
