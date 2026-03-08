/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.block;

import buildcraft.api.tools.IToolWrench;
import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.item.ItemMarkerConstruction;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.tile.TileMarkerConstruction;
import buildcraft.lib.block.BlockMarkerBase;
import buildcraft.lib.block.IBlockWithTickableTE;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AirItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class BlockMarkerConstruction extends BlockMarkerBase implements IBlockWithTickableTE<TileMarkerConstruction> {
    public BlockMarkerConstruction(String idBC, AbstractBlock.Properties properties) {
        super(idBC, properties);
    }

    @Override
    // public TileEntity createNewTileEntity(World world, int metadata)
    public TileBC_Neptune newBlockEntity(IBlockReader world) {
        return BCBuildersBlocks.markerConstructionTile.get().create();
    }

    @Override
    // public void breakBlock(Level world, BlockPos pos, BlockState state)
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        // Calen 1.18.2
        if (newState.getBlock() == state.getBlock()) {
            return;
        }
        // Utils.preDestroyBlock(world, pos); // Calen: removed in 1.18.2
        dropMarkerIfPresent(world, pos, true);
        // super.breakBlock(world, pos, state);
        super.onRemove(state, world, pos, newState, isMoving);
    }

    private ActionResultType dropMarkerIfPresent(World world, BlockPos pos, boolean onBreak) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileMarkerConstruction) {
            TileMarkerConstruction marker = (TileMarkerConstruction) world.getBlockEntity(pos);
            // if (marker != null && marker.itemBlueprint != null && !world.isClientSide)
            if (marker != null && !marker.itemBlueprint.isEmpty() && !world.isClientSide) {
                BlockUtil.dropItem((ServerWorld) world, pos, 6000, marker.itemBlueprint);
                // marker.itemBlueprint = null;
                if (!onBreak) {
                    if (marker.bluePrintBuilder != null) {
                        marker.bluePrintBuilder.invalidate();
                    }
                    marker.bluePrintBuilder = null;
                    marker.bptContext = null;
                    marker.box.reset();
                }
                marker.setBlueprint(StackUtil.EMPTY);
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    // public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityliving, ItemStack stack)
    public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity entityliving, ItemStack stack) {
        // super.onBlockPlacedBy(world, pos, state, entityliving, stack);
        super.setPlacedBy(world, pos, state, entityliving, stack);

        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileMarkerConstruction) {
            TileMarkerConstruction marker = (TileMarkerConstruction) tile;
//            marker.direction = entityliving.getHorizontalFacing();
            marker.direction = entityliving.getDirection();
        }
    }

    @Override
    // public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumFacing face, float hitX, float hitY, float hitZ)
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity entityplayer, Hand hand, BlockRayTraceResult hitResult) {
        // if (super.onBlockActivated(world, pos, state, entityplayer, face, hitX, hitY, hitZ))
        ActionResultType superResult = super.use(state, world, pos, entityplayer, hand, hitResult);
        if (superResult.consumesAction()) {
            return superResult;
        }

        TileMarkerConstruction marker = (TileMarkerConstruction) world.getBlockEntity(pos);

//        Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
        Item equipped = entityplayer.getItemInHand(hand).getItem();

        if (equipped instanceof ItemSnapshot) {
            // if (marker.itemBlueprint == null)
            if (marker.itemBlueprint.isEmpty()) {
//                ItemStack stack = entityplayer.inventory.getCurrentItem().copy();
                ItemStack stack = entityplayer.inventory.getSelected().copy();
                stack.setCount(1);
                marker.setBlueprint(stack);
//                stack = null;
                stack = StackUtil.EMPTY;
                if (entityplayer.inventory.getSelected().getCount() > 1) {
//                    stack = entityplayer.getCurrentEquippedItem().copy();
                    stack = entityplayer.getItemInHand(hand).copy();
//                    stack.getCount() = entityplayer.getCurrentEquippedItem().stackSize - 1;
                    stack.setCount(entityplayer.getItemInHand(hand).getCount() - 1);
                }
//                entityplayer.getInventory().setInventorySlotContents(entityplayer.inventory.currentItem, stack);
                entityplayer.inventory.setItem(entityplayer.inventory.selected, stack);

                return ActionResultType.SUCCESS;
            }
        } else if (equipped instanceof ItemMarkerConstruction) {
//            if (ItemMarkerConstruction.linkStarted(entityplayer.getCurrentEquippedItem()))
            if (ItemMarkerConstruction.linkStarted(entityplayer.getItemInHand(hand))) {
//                ItemMarkerConstruction.link(entityplayer.getCurrentEquippedItem(), world, pos);
                ItemMarkerConstruction.link(entityplayer.getItemInHand(hand), world, pos);
                return ActionResultType.SUCCESS;
            }
        }
        // else if ((equipped == null || equipped instanceof IToolWrench) && entityplayer.isShiftKeyDown())
        else if ((equipped instanceof AirItem || equipped instanceof IToolWrench) && entityplayer.isShiftKeyDown()) {
            return dropMarkerIfPresent(world, pos, false);
        }

        return ActionResultType.PASS;
    }
}
