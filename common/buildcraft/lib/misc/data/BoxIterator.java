package buildcraft.lib.misc.data;

import buildcraft.api.core.IBox;
import buildcraft.lib.misc.NBTUtils;
import buildcraft.lib.misc.VecUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;

public class BoxIterator implements Iterator<BlockPos> {
    private final BlockPos min, max;
    private final boolean invert, repeat;// TODO: remove repeat if its not used in the future
    private AxisOrder order;
    private BlockPos current;

    public BoxIterator(IBox box, AxisOrder order, boolean invert) {
        this(box.min(), box.max(), order, invert);
    }

    public BoxIterator(BlockPos min, BlockPos max, AxisOrder order, boolean invert) {
        this.min = min;
        this.max = max;
        this.invert = invert;
        this.repeat = false;
        this.order = order;
        this.current = getStart();
    }

    public BoxIterator(NBTTagCompound nbt) {
        min = NBTUtils.readBlockPos(nbt.getTag("min"));
        max = NBTUtils.readBlockPos(nbt.getTag("max"));
        invert = nbt.getBoolean("invert");
        repeat = false;
        order = AxisOrder.readNbt(nbt.getCompoundTag("order"));
        current = NBTUtils.readBlockPos(nbt.getTag("current"));
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("min", NBTUtils.writeBlockPos(min));
        nbt.setTag("max", NBTUtils.writeBlockPos(max));
        nbt.setBoolean("invert", invert);
        // repeat
        nbt.setTag("order", order.writeNBT());
        nbt.setTag("current", NBTUtils.writeBlockPos(current));
        return nbt;
    }

    private BlockPos getStart() {
        BlockPos pos = BlockPos.ORIGIN;
        pos = replace(pos, order.first);
        pos = replace(pos, order.second);
        return replace(pos, order.third);
    }

    private BlockPos replace(BlockPos toReplace, EnumFacing facing) {
        BlockPos with = facing.getAxisDirection() == AxisDirection.POSITIVE ? min : max;
        return VecUtil.replaceValue(toReplace, facing.getAxis(), VecUtil.getValue(with, facing.getAxis()));
    }

    public BlockPos getCurrent() {
        return current;
    }

    public BlockPos getMin() {
        return min;
    }

    public BlockPos getMax() {
        return max;
    }

    public boolean isInvert() {
        return invert;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public AxisOrder getOrder() {
        return order;
    }

    public BlockPos advance() {
        if (current == null) {
            current = getStart();
            return getCurrent();
        }
        current = increment(current, order.first);
        if (shouldReset(current, order.first)) {
            if (invert) {
                order = order.invertFirst();
            }
            current = replace(current, order.first);
            current = increment(current, order.second);
            if (shouldReset(current, order.second)) {
                if (invert) {
                    order = order.invertSecond();
                }
                current = replace(current, order.second);
                current = increment(current, order.third);
                if (shouldReset(current, order.third)) {
                    if (repeat) {
                        if (invert) {
                            order = order.invertThird();
                        }
                        current = replace(current, order.third);
                    } else {
                        current = null;
                    }
                }
            }
        }
        return getCurrent();
    }

    private static BlockPos increment(BlockPos pos, EnumFacing facing) {
        int diff = facing.getAxisDirection().getOffset();
        int value = VecUtil.getValue(pos, facing.getAxis()) + diff;
        return VecUtil.replaceValue(pos, facing.getAxis(), value);
    }

    private boolean shouldReset(BlockPos current, EnumFacing facing) {
        int lstReturned = VecUtil.getValue(current, facing.getAxis());
        BlockPos goingTo = facing.getAxisDirection() == AxisDirection.POSITIVE ? max : min;
        int to = VecUtil.getValue(goingTo, facing.getAxis());
        if (facing.getAxisDirection() == AxisDirection.POSITIVE) return lstReturned > to;
        return lstReturned < to;
    }

    public boolean hasFinished() {
        return current == null;
    }

    // Iterator

    @Override
    public boolean hasNext() {
        return !hasFinished();
    }

    @Override
    public BlockPos next() {
        BlockPos c = current;
        advance();
        return c;
    }
}
