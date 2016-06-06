package buildcraft.lib.misc;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.core.IBox;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.lib.utils.Utils.AxisOrder;

public class BoxIterator {
    private final BlockPos min, max;
    private final AxisOrder order;
    private BlockPos current;

    public BoxIterator(IBox box, AxisOrder order) {
        this(box.min(), box.max(), order);
    }

    public BoxIterator(BlockPos min, BlockPos max, AxisOrder order) {
        this.min = min;
        this.max = max;
        this.order = order;
        this.current = getStart();
    }

    private BlockPos getStart() {
        BlockPos pos = BlockPos.ORIGIN;
        pos = replace(pos, order.first);
        pos = replace(pos, order.second);
        return replace(pos, order.third);
    }

    private BlockPos replace(BlockPos toReplace, EnumFacing facing) {
        BlockPos with = facing.getAxisDirection() == AxisDirection.POSITIVE ? min : max;
        return Utils.withValue(toReplace, facing.getAxis(), Utils.getValue(with, facing.getAxis()));
    }

    public BlockPos getCurrent() {
        return current;
    }

    public void advance() {
        if (current == null) {
            current = getStart();
            return;
        }
        current = increment(current, order.first);
        if (shouldReset(current, order.first)) {
            current = replace(current, order.first);
            current = increment(current, order.second);
            if (shouldReset(current, order.second)) {
                current = replace(current, order.second);
                current = increment(current, order.third);
                if (shouldReset(current, order.third)) {
                    current = null;
                }
            }
        }
    }

    private static BlockPos increment(BlockPos pos, EnumFacing facing) {
        int diff = facing.getAxisDirection().getOffset();
        int value = Utils.getValue(pos, facing.getAxis()) + diff;
        return Utils.withValue(pos, facing.getAxis(), value);
    }

    private boolean shouldReset(BlockPos current, EnumFacing facing) {
        int lstReturned = Utils.getValue(current, facing.getAxis());
        BlockPos goingTo = facing.getAxisDirection() == AxisDirection.POSITIVE ? max : min;
        int to = Utils.getValue(goingTo, facing.getAxis());
        if (facing.getAxisDirection() == AxisDirection.POSITIVE) return lstReturned > to;
        return lstReturned < to;
    }

    public boolean hasFinished() {
        return current == null;
    }
}
