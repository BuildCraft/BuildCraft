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
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BlockMarkerConstruction extends BlockMarkerBase implements IBlockWithTickableTE<TileMarkerConstruction> {
    public BlockMarkerConstruction(String idBC, Properties properties) {
        super(idBC, properties);
    }

    @Override
    // public TileEntity createNewTileEntity(World world, int metadata)
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return BCBuildersBlocks.markerConstructionTile.get().create(pos, state);
    }

    @Override
    // public void breakBlock(Level world, BlockPos pos, BlockState state)
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        // Calen 1.18.2
        if (newState.getBlock() == state.getBlock()) {
            return;
        }
        // Utils.preDestroyBlock(world, pos); // Calen: removed in 1.18.2
        dropMarkerIfPresent(world, pos, true);
        // super.breakBlock(world, pos, state);
        super.onRemove(state, world, pos, newState, isMoving);
    }

    private InteractionResult dropMarkerIfPresent(Level world, BlockPos pos, boolean onBreak) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileMarkerConstruction) {
            TileMarkerConstruction marker = (TileMarkerConstruction) world.getBlockEntity(pos);
            // if (marker != null && marker.itemBlueprint != null && !world.isClientSide)
            if (marker != null && !marker.itemBlueprint.isEmpty() && !world.isClientSide) {
                BlockUtil.dropItem((ServerLevel) world, pos, 6000, marker.itemBlueprint);
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
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    // public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityliving, ItemStack stack)
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity entityliving, ItemStack stack) {
        // super.onBlockPlacedBy(world, pos, state, entityliving, stack);
        super.setPlacedBy(world, pos, state, entityliving, stack);

        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileMarkerConstruction) {
            TileMarkerConstruction marker = (TileMarkerConstruction) tile;
//            marker.direction = entityliving.getHorizontalFacing();
            marker.direction = entityliving.getDirection();
        }
    }

    @Override
    // public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumFacing face, float hitX, float hitY, float hitZ)
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player entityplayer, InteractionHand hand, BlockHitResult hitResult) {
        // if (super.onBlockActivated(world, pos, state, entityplayer, face, hitX, hitY, hitZ))
        InteractionResult superResult = super.use(state, world, pos, entityplayer, hand, hitResult);
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
                ItemStack stack = entityplayer.getInventory().getSelected().copy();
                stack.setCount(1);
                marker.setBlueprint(stack);
//                stack = null;
                stack = StackUtil.EMPTY;
                if (entityplayer.getInventory().getSelected().getCount() > 1) {
//                    stack = entityplayer.getCurrentEquippedItem().copy();
                    stack = entityplayer.getItemInHand(hand).copy();
//                    stack.getCount() = entityplayer.getCurrentEquippedItem().stackSize - 1;
                    stack.setCount(entityplayer.getItemInHand(hand).getCount() - 1);
                }
//                entityplayer.getInventory().setInventorySlotContents(entityplayer.inventory.currentItem, stack);
                entityplayer.getInventory().setItem(entityplayer.getInventory().selected, stack);

                return InteractionResult.SUCCESS;
            }
        } else if (equipped instanceof ItemMarkerConstruction) {
//            if (ItemMarkerConstruction.linkStarted(entityplayer.getCurrentEquippedItem()))
            if (ItemMarkerConstruction.linkStarted(entityplayer.getItemInHand(hand))) {
//                ItemMarkerConstruction.link(entityplayer.getCurrentEquippedItem(), world, pos);
                ItemMarkerConstruction.link(entityplayer.getItemInHand(hand), world, pos);
                return InteractionResult.SUCCESS;
            }
        }
        // else if ((equipped == null || equipped instanceof IToolWrench) && entityplayer.isShiftKeyDown())
        else if ((equipped instanceof AirItem || equipped instanceof IToolWrench) && entityplayer.isShiftKeyDown()) {
            return dropMarkerIfPresent(world, pos, false);
        }

        return InteractionResult.PASS;
    }
}
