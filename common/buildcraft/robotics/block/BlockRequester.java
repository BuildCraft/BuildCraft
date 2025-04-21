/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.block;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.robotics.tile.TileRequester;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

// public class BlockRequester extends BlockBCTile_Neptune<TileRequester> implements IBlockWithFacing, IComparatorInventory
public class BlockRequester extends BlockBCTile_Neptune<TileRequester> implements IBlockWithFacing {
    public BlockRequester(String idBC, Properties props) {
        super(idBC, props);
    }

    @Override
//    public TileEntity createNewTileEntity(Level world, int meta)
    public TileBC_Neptune newBlockEntity(IBlockReader world) {
        return new TileRequester();
    }

    @Override
//    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumFacing face, float hitX, float hitY, float hitZ)
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity entityplayer, Hand hand, BlockRayTraceResult hitResult) {
//        if (super.onBlockActivated(world, pos, state, entityplayer, face, hitX, hitY, hitZ))
        ActionResultType su = super.use(state, world, pos, entityplayer, hand, hitResult);
        if (su.consumesAction()) {
            return su;
        }

        if (!world.isClientSide) {
            // entityplayer.openGui(BCRobotics.instance, GuiIds.REQUESTER, world, pos.getX(), pos.getY(), pos.getZ());
            TileEntity tile = world.getBlockEntity(pos);
            if (tile instanceof TileRequester) {
                MessageUtil.serverOpenTileGui(entityplayer, (TileRequester) tile, pos);
            }
        }

        return ActionResultType.SUCCESS;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    /** Just like {@link net.minecraft.world.inventory.AbstractContainerMenu#getRedstoneSignalFromContainer(Container)} */
    @Override
    // public boolean doesSlotCountComparator(BlockEntity tile, int slot, ItemStack stack)
    public int getAnalogOutputSignal(BlockState state, World level, BlockPos pos) {
        TileEntity tile = level.getBlockEntity(pos);
        if (tile instanceof TileRequester) {
            // return ((TileRequester) tile).getRequestTemplate(slot) != null;

            TileRequester requester = ((TileRequester) tile);
            int i = 0;
            float f = 0.0F;

            for (int j = 0; j < requester.inv.getSlots(); ++j) {
                ItemStack itemstack = requester.inv.getStackInSlot(j);
                if (!itemstack.isEmpty()) {
                    f += (float) itemstack.getCount() / (float) Math.min(itemstack.getMaxStackSize(), itemstack.getMaxStackSize());
                    ++i;
                }
            }

            f /= (float) requester.inv.getSlots();
            return MathHelper.floor(f * 14.0F) + (i > 0 ? 1 : 0);
        }
        return 0;
    }
}
