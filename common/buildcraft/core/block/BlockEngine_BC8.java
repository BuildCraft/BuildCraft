/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.engine.BlockEngineBase_BC8;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;

public class BlockEngine_BC8 extends BlockEngineBase_BC8<EnumEngineType> {
    public BlockEngine_BC8(Material material, String id) {
        super(material, id);
    }

    @Override
    public IProperty<EnumEngineType> getEngineProperty() {
        return BuildCraftProperties.ENGINE_TYPE;
    }

    @Override
    public EnumEngineType getEngineType(int meta) {
        return EnumEngineType.fromMeta(meta);
    }

    @Override
    public String getUnlocalizedName(EnumEngineType engine) {
        return TagManager.getTag("block.engine.bc." + engine.unlocalizedTag, EnumTagType.UNLOCALIZED_NAME);
    }
}
