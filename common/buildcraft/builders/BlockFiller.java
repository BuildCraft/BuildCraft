/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.BuildCraftBuilders;
import buildcraft.core.GuiIds;
import buildcraft.core.builders.patterns.FillerPattern;
import buildcraft.core.lib.block.BlockBuildCraft;

public class BlockFiller extends BlockBuildCraft {
    public BlockFiller() {
        super(Material.iron, FACING_PROP, LED_DONE, LED_POWER, FILLER_PATTERN);
        setHardness(5F);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing face, float par7, float par8,
            float par9) {
        if (super.onBlockActivated(world, pos, state, player, face, par7, par8, par9)) {
            return true;
        }

        if (player.isSneaking()) {
            return false;
        }

        if (!world.isRemote) {
            player.openGui(BuildCraftBuilders.instance, GuiIds.FILLER, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;

    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileFiller();
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public int getLightValue(IBlockAccess world, BlockPos pos) {
        return 1;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess access, BlockPos pos) {
        state = super.getActualState(state, access, pos);
        TileEntity tile = access.getTileEntity(pos);
        if (tile instanceof TileFiller) {
            FillerPattern pattern = ((TileFiller) tile).currentPattern;
            if (pattern == null) {
                return state;
            } else {
                state = state.withProperty(FILLER_PATTERN, pattern.type);
                return state;
            }
        } else {
            return state;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.CUTOUT;
    }
}
