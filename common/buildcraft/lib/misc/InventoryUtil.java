package buildcraft.lib.misc;

import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.items.IItemHandlerModifiable;

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
        if (stack == null || stack.stackSize <= 0) {
            return;
        }
        EntityItem entity = new EntityItem(world, x, y, z, stack);
        world.spawnEntityInWorld(entity);
    }

    // NBT migration
}
