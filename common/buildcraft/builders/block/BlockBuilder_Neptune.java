/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.enums.EnumBlueprintType;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.lib.block.BlockBCTile_Neptune;

public class BlockBuilder_Neptune extends BlockBCTile_Neptune {
    public static final IProperty<EnumFacing> PROP_FACING = BuildCraftProperties.BLOCK_FACING;
    public static final IProperty<EnumBlueprintType> PROP_BPT = BuildCraftProperties.BLUEPRINT_TYPE;

    public BlockBuilder_Neptune(Material material, String id) {
        super(material, id);
        IBlockState state = getDefaultState();
        state = state.withProperty(PROP_FACING, EnumFacing.NORTH);
        state = state.withProperty(PROP_BPT, EnumBlueprintType.NONE);
        setDefaultState(state);
    }

    // IBlockState

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, PROP_FACING, PROP_BPT);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = 0;
        meta |= state.getValue(PROP_FACING).getHorizontalIndex() & 3;
        return meta;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = getDefaultState();
        state = state.withProperty(PROP_FACING, EnumFacing.getHorizontal(meta & 3));
        return state;
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        EnumFacing facing = state.getValue(PROP_FACING);
        state = state.withProperty(PROP_FACING, rot.rotate(facing));
        return state;
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        EnumFacing facing = state.getValue(PROP_FACING);
        state = state.withProperty(PROP_FACING, mirror.mirror(facing));
        return state;
    }

    // Others

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        EnumFacing orientation = placer.getHorizontalFacing();
        world.setBlockState(pos, state.withProperty(PROP_FACING, orientation.getOpposite()));
        super.onBlockPlacedBy(world, pos, state, placer, stack);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return null;
    }

}
