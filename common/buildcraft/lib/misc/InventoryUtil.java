package buildcraft.lib.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.transport.IInjectable;

import buildcraft.lib.inventory.ItemInjectableHelper;
import buildcraft.lib.inventory.ItemTransactorHelper;

public class InventoryUtil {
    // Drops

    public static void dropAll(World world, Vec3d vec, IItemHandlerModifiable handler) {
        dropAll(world, vec.xCoord, vec.yCoord, vec.zCoord, handler);
    }

    public static void dropAll(World world, BlockPos pos, IItemHandlerModifiable handler) {
        dropAll(world, pos.getX(), pos.getY(), pos.getZ(), handler);
    }

    public static void dropAll(World world, double x, double y, double z, IItemHandlerModifiable handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            drop(world, x, y, z, handler.extractItem(i, Integer.MAX_VALUE, false));
        }
    }

    public static void dropAll(World world, BlockPos pos, List<ItemStack> toDrop) {
        for (ItemStack stack : toDrop) {
            drop(world, pos, stack);
        }
    }

    public static void drop(World world, BlockPos pos, ItemStack stack) {
        drop(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
    }

    public static void drop(World world, double x, double y, double z, ItemStack stack) {
        if (StackUtil.isInvalid(stack)) {
            return;
        }
        EntityItem entity = new EntityItem(world, x, y, z, stack);
        world.spawnEntityInWorld(entity);
    }

    // Sending items around

    /** @return The leftover stack */
    public static ItemStack addToRandomInventory(World world, BlockPos pos, ItemStack stack) {
        if (StackUtil.isInvalid(stack)) {
            return StackUtil.INVALID_STACK;
        }
        List<EnumFacing> toTry = new ArrayList<>(6);
        Collections.addAll(toTry, EnumFacing.VALUES);
        Collections.shuffle(toTry);
        for (EnumFacing face : toTry) {
            TileEntity tile = world.getTileEntity(pos.offset(face));
            IItemTransactor transactor = ItemTransactorHelper.getTransactor(tile, face.getOpposite());
            stack = transactor.insert(stack, false, false);
            if (StackUtil.isInvalid(stack)) {
                return StackUtil.INVALID_STACK;
            }
        }
        return stack;
    }

    /** Look around the tile given in parameter in all 6 position, tries to add the items to a random injectable tile
     * around. Will make sure that the location from which the items are coming from (identified by the from parameter)
     * isn't used again so that entities doesn't go backwards. Returns true if successful, false otherwise. */
    public static ItemStack addToRandomInjectable(World world, BlockPos pos, EnumFacing ignore, ItemStack stack) {
        if (StackUtil.isInvalid(stack)) {
            return StackUtil.INVALID_STACK;
        }
        List<EnumFacing> toTry = new ArrayList<>(6);
        Collections.addAll(toTry, EnumFacing.VALUES);
        Collections.shuffle(toTry);
        for (EnumFacing face : toTry) {
            TileEntity tile = world.getTileEntity(pos.offset(face));
            IInjectable injectable = ItemInjectableHelper.getIjectable(tile, face.getOpposite());
            if (injectable != null) {
                stack = injectable.injectItem(stack, true, face.getOpposite(), null);
                if (StackUtil.isInvalid(stack)) {
                    return StackUtil.INVALID_STACK;
                }
            }
        }
        return stack;
    }

    /** Attempts to add the given stack to the best acceptor, in this order: {@link IItemHandler} instances,
     * {@link IInjectable} instances, and finally dropping it down on the ground. */
    public static void addToBestAcceptor(World world, BlockPos pos, EnumFacing ignore, ItemStack stack) {
        stack = addToRandomInventory(world, pos, stack);
        stack = addToRandomInjectable(world, pos, ignore, stack);
        drop(world, pos, stack);
    }

    // NBT migration
}
