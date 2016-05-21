package buildcraft.transport.stripes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandler;
import buildcraft.core.lib.utils.BlockUtils;
import buildcraft.core.lib.utils.Utils;

public class StripesHandlerUse implements IStripesHandler {
    public static final List<Item> items = new ArrayList<>();

    @Override
    public StripesHandlerType getType() {
        return StripesHandlerType.ITEM_USE;
    }

    @Override
    public boolean shouldHandle(ItemStack stack) {
        return items.contains(stack.getItem());
    }

    @Override
    public boolean handle(World world, BlockPos pos, EnumFacing direction, ItemStack stack, EntityPlayer player, IStripesActivator activator) {
        Vec3d target = Utils.convert(pos).add(Utils.convert(direction));

        if (BlockUtils.useItemOnBlock(world, player, stack, Utils.convertFloor(target), direction.getOpposite())) {
            if (stack.stackSize > 0) {
                activator.sendItem(stack, direction.getOpposite());
            }
            return true;
        }
        return false;
    }

}
