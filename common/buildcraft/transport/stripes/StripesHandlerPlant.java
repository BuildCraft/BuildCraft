package buildcraft.transport.stripes;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

public class StripesHandlerPlant implements IStripesHandler {
    @Override
    public StripesHandlerType getType() {
        return StripesHandlerType.ITEM_USE;
    }

    @Override
    public boolean shouldHandle(ItemStack stack) {
        return stack.getItem() instanceof IPlantable;
    }

    @Override
    public boolean handle(World world, int x, int y, int z, ForgeDirection direction, ItemStack stack, EntityPlayer player, IStripesActivator activator) {
        Block b = world.getBlock(x, y - 1, z);
        IPlantable plant = (IPlantable) stack.getItem();
        if (b.canSustainPlant(world, x, y - 1, z, ForgeDirection.UP, (IPlantable) stack.getItem())) {
            world.setBlock(x, y, z, plant.getPlant(world, x, y, z), plant.getPlantMetadata(world, x, y, z), 3);
            return true;
        }
        return false;
    }
}
