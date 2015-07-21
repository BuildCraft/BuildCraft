package buildcraft.transport.stripes;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandler;
import buildcraft.core.lib.utils.Utils;

public class StripesHandlerDispenser implements IStripesHandler {
    public static final List<Object> items = Lists.newArrayList();

    public class Source implements IBlockSource {
        private final World world;
        private final BlockPos pos;
        private final EnumFacing side;

        public Source(World world, BlockPos pos, EnumFacing side) {
            this.world = world;
            this.pos = pos;
            this.side = side;
        }

        @Override
        public double getX() {
            return (double) pos.getX() + 0.5D;
        }

        @Override
        public double getY() {
            return (double) pos.getY() + 0.5D;
        }

        @Override
        public double getZ() {
            return (double) pos.getZ() + 0.5D;
        }

        @Override
        public BlockPos getBlockPos() {
            return pos;
        }

        @Override
        public Block getBlock() {
            return world.getBlockState(pos).getBlock();
        }

        @Override
        public int getBlockMetadata() {
            return side.ordinal();
        }

        @Override
        public TileEntity getBlockTileEntity() {
            return world.getTileEntity(pos);
        }

        @Override
        public World getWorld() {
            return world;
        }
    }

    @Override
    public StripesHandlerType getType() {
        return StripesHandlerType.ITEM_USE;
    }

    @Override
    public boolean shouldHandle(ItemStack stack) {
        if (items.contains(stack.getItem())) {
            return true;
        }

        Class<?> c = stack.getItem().getClass();
        while (c != Item.class) {
            if (items.contains(c)) {
                return true;
            }
            c = c.getSuperclass();
        }
        return false;
    }

    @Override
    public boolean handle(World world, BlockPos pos, EnumFacing direction, ItemStack stack, EntityPlayer player, IStripesActivator activator) {
        Vec3 origin = Utils.convert(pos).add(Utils.convert(direction, -1));

        IBlockSource source = new Source(world, Utils.convertFloor(origin), direction);
        IBehaviorDispenseItem behaviour = (IBehaviorDispenseItem) BlockDispenser.dispenseBehaviorRegistry.getObject(stack.getItem());
        if (behaviour != null) {
            ItemStack output = behaviour.dispense(source, stack.copy());
            if (output.stackSize > 0) {
                activator.sendItem(output, direction.getOpposite());
            }
            return true;
        }
        return false;
    }
}
