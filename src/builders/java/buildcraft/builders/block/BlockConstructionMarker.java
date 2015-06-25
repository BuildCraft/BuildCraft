/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import buildcraft.builders.item.ItemBlueprint;
import buildcraft.builders.item.ItemConstructionMarker;
import buildcraft.builders.tile.TileConstructionMarker;
import buildcraft.core.lib.utils.Utils;

public class BlockConstructionMarker extends BlockMarker {
    public BlockConstructionMarker() {}

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileConstructionMarker();
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        Utils.preDestroyBlock(world, pos);
        TileConstructionMarker marker = (TileConstructionMarker) world.getTileEntity(pos);
        if (marker != null && marker.itemBlueprint != null && !world.isRemote) {
            float f1 = 0.7F;
            double d = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
            double d1 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
            double d2 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
            EntityItem itemToDrop = new EntityItem(world, pos.getX() + d, pos.getY() + d1, pos.getZ() + d2, marker.itemBlueprint);
            itemToDrop.setDefaultPickupDelay();
            world.spawnEntityInWorld(itemToDrop);
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityliving, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, entityliving, stack);

        TileConstructionMarker tile = (TileConstructionMarker) world.getTileEntity(pos);
        tile.direction = entityliving.getHorizontalFacing();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumFacing face, float hitX, float hitY,
            float hitZ) {
        if (super.onBlockActivated(world, pos, state, entityplayer, face, hitX, hitY, hitZ)) {
            return true;
        }

        TileConstructionMarker marker = (TileConstructionMarker) world.getTileEntity(pos);

        Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;

        if (equipped instanceof ItemBlueprint) {
            if (marker.itemBlueprint == null) {
                ItemStack stack = entityplayer.inventory.getCurrentItem().copy();
                stack.stackSize = 1;
                marker.setBlueprint(stack);
                stack = null;
                if (entityplayer.inventory.getCurrentItem().stackSize > 1) {
                    stack = entityplayer.getCurrentEquippedItem().copy();
                    stack.stackSize = entityplayer.getCurrentEquippedItem().stackSize - 1;
                }
                entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, stack);

                return true;
            }
        } else if (equipped instanceof ItemConstructionMarker) {
            if (ItemConstructionMarker.linkStarted(entityplayer.getCurrentEquippedItem())) {
                ItemConstructionMarker.link(entityplayer.getCurrentEquippedItem(), world, pos);
                return true;
            }
        }

        return false;
    }
}
