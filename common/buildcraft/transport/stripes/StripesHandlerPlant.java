package buildcraft.transport.stripes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.crops.CropManager;
import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;

public enum StripesHandlerPlant implements IStripesHandlerItem {
    INSTANCE;

    @Override
    public boolean handle(World world, BlockPos pos, EnumFacing direction, ItemStack stack, EntityPlayer player, IStripesActivator activator) {
        if (CropManager.plantCrop(world, player, stack, pos.offset(direction).down())) {
            if (!stack.isEmpty()) {
                activator.sendItem(stack, direction.getOpposite());
            }
            return true;
        } else if (CropManager.plantCrop(world, player, stack, pos.offset(direction))) {
            if (!stack.isEmpty()) {
                activator.sendItem(stack, direction.getOpposite());
            }
            return true;
        }
        return false;
    }
}
