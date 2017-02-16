package buildcraft.transport.stripes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.BlockDispenser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;

public enum StripesHandlerDispenser implements IStripesHandlerItem {
    INSTANCE;

    public static final List<Item> items = new ArrayList<>();
    public static final List<Class<? extends Item>> itemClasses = new ArrayList<>();

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
            return pos.getX() + 0.5D;
        }

        @Override
        public double getY() {
            return pos.getY() + 0.5D;
        }

        @Override
        public double getZ() {
            return pos.getZ() + 0.5D;
        }

        @Override
        public BlockPos getBlockPos() {
            return pos;
        }

        @Override
        public IBlockState getBlockState() {
            return Blocks.DISPENSER.getDefaultState().withProperty(BlockDispenser.FACING, side);
        }

        @Override
        public <T extends TileEntity> T getBlockTileEntity() {
            return (T) world.getTileEntity(pos);
        }

        @Override
        public World getWorld() {
            return world;
        }
    }

    private static boolean shouldHandle(ItemStack stack) {
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
        IBehaviorDispenseItem behaviour = BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(stack.getItem());
        if (behaviour == IBehaviorDispenseItem.DEFAULT_BEHAVIOR) {
            return false;
        }
        // Temp: for testing
//        if (!shouldHandle(stack)) {
//            return false;
//        }

        IBlockSource source = new Source(world, pos, direction);
        ItemStack output = behaviour.dispense(source, stack.copy());
        if (!output.isEmpty()) {
            activator.sendItem(output, direction);
        }
        return true;
    }
}
