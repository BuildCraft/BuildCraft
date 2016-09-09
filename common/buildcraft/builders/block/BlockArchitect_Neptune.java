/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.block;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.builders.BCBuildersGuis;
import buildcraft.builders.tile.TileArchitect_Neptune;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
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

import java.util.List;

public class BlockArchitect_Neptune extends BlockBCTile_Neptune implements IBlockWithFacing {
    public static final IProperty<Boolean> PROP_VALID = BuildCraftProperties.VALID;

    private static final int META_VALID_INDEX = 4;

    public BlockArchitect_Neptune(Material material, String id) {
        super(material, id);
        setDefaultState(getDefaultState().withProperty(PROP_VALID, Boolean.TRUE));
    }

    @Override
    protected void addProperties(List<IProperty<?>> properties) {
        super.addProperties(properties);
        properties.add(PROP_VALID);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = super.getStateFromMeta(meta);
        state = state.withProperty(PROP_VALID, (meta & META_VALID_INDEX) == 0);
        return state;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return super.getMetaFromState(state) | (state.getValue(PROP_VALID) ? 0 : META_VALID_INDEX);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileArchitect_Neptune();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            BCBuildersGuis.ARCHITECT.openGUI(player, pos);
        }
        return true;
    }
}
