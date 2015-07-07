/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.factory.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.tools.IToolWrench;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.GuiIds;
import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.core.lib.fluids.TankUtils;
import buildcraft.factory.BuildCraftFactory;
import buildcraft.factory.tile.TileRefinery;

public class BlockRefinery extends BlockBuildCraft {
    public BlockRefinery() {
        super(Material.iron, FACING_PROP);

        setHardness(5F);
        setCreativeTab(BCCreativeTab.get("main"));
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileRefinery();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY,
            float hitZ) {
        if (super.onBlockActivated(world, pos, state, player, side, hitX, hitY, hitZ)) {
            return true;
        }

        TileEntity tile = world.getTileEntity(pos);

        if (!(tile instanceof TileRefinery)) {
            return false;
        }

        ItemStack current = player.getCurrentEquippedItem();
        Item equipped = current != null ? current.getItem() : null;
        if (player.isSneaking() && equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(player, pos)) {
            ((TileRefinery) tile).resetFilters();
            ((IToolWrench) equipped).wrenchUsed(player, pos);
            return true;
        }

        if (current != null) {
            if (!world.isRemote) {
                if (FluidContainerRegistry.isEmptyContainer(current)) {
                    if (TankUtils.handleRightClick((TileRefinery) tile, side, player, false, true)) {
                        return true;
                    }
                } else if (FluidContainerRegistry.isFilledContainer(current)) {
                    if (TankUtils.handleRightClick((TileRefinery) tile, side, player, true, false)) {
                        return true;
                    }
                }
            } else if (FluidContainerRegistry.isContainer(current)) {
                return true;
            }
        }

        if (!world.isRemote) {
            player.openGui(BuildCraftFactory.instance, GuiIds.REFINERY, world, pos.getX(), pos.getY(), pos.getZ());
        }

        return true;
    }

    @SideOnly(Side.CLIENT)
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.CUTOUT;
    }
}
