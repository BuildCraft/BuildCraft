package buildcraft.transport.stripes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;

import buildcraft.lib.misc.BlockUtil;

public enum StripesHandlerUse implements IStripesHandlerItem {
    INSTANCE;

    public static final List<Item> items = new ArrayList<>();

    @Override
    public boolean handle(World world, BlockPos pos, EnumFacing direction, ItemStack stack, EntityPlayer player, IStripesActivator activator) {
        if (!items.contains(stack.getItem())) {
            return false;
        }
        return BlockUtil.useItemOnBlock(world, player, stack, pos.offset(direction), direction.getOpposite());
    }
}
