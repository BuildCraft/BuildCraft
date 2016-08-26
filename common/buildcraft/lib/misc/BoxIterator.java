package buildcraft.lib.misc;

import buildcraft.api.core.IBox;
import buildcraft.api.core.INetworkLoadable_BC8;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.lib.utils.Utils.AxisOrder;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;

public class BoxIterator implements INetworkLoadable_BC8<BoxIterator> {
    private final BlockPos min, max;
    private final boolean invert, repeat;
    private AxisOrder order;
    private BlockPos current;

    public BoxIterator() {
        min = max = null;
        invert = repeat = false;
    }

    private BoxIterator(BlockPos min, BlockPos max, boolean invert, boolean repeat, AxisOrder order, BlockPos current) {
        this.min = min;
        this.max = max;
        this.invert = invert;
        this.repeat = repeat;
        this.order = order;
        this.current = current;
    }

    public BoxIterator(BoxIterator old) {
        this.min = old.min;
        this.max = old.max;
        this.invert = old.invert;
        this.repeat = old.repeat;
        this.order = old.order;
        this.current = old.current;
    }

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

    public void advance() {
        if (current == null) {
            current = getStart();
            return;
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

    @Override
    public BoxIterator readFromByteBuf(ByteBuf buf) {
        return new BoxIterator(NetworkUtils.readBlockPos(buf), NetworkUtils.readBlockPos(buf), buf.readBoolean(), buf.readBoolean(), new AxisOrder().readFromByteBuf(buf), NetworkUtils.readBlockPos(buf));
    }

    @Override
    public void writeToByteBuf(ByteBuf buf) {
        NetworkUtils.writeBlockPos(buf, min);
        NetworkUtils.writeBlockPos(buf, max);
        buf.writeBoolean(invert);
        buf.writeBoolean(repeat);
        order.writeToByteBuf(buf);
        NetworkUtils.writeBlockPos(buf, current);
    }

    public BoxIterator readFromNBT(NBTTagCompound tag) {
        if(tag == null) {
            return null;
        }
        return new BoxIterator(NBTUtils.readBlockPos(tag.getTag("min")), NBTUtils.readBlockPos(tag.getTag("max")), tag.getBoolean("invert"), tag.getBoolean("repeat"), new AxisOrder().readFromNBT(tag.getCompoundTag("order")), NBTUtils.readBlockPos(tag.getTag("current")));
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("min", NBTUtils.writeBlockPos(min));
        tag.setTag("max", NBTUtils.writeBlockPos(max));
        tag.setBoolean("invert", invert);
        tag.setBoolean("repeat", repeat);
        tag.setTag("order", order.writeToNBT());
        tag.setTag("current", NBTUtils.writeBlockPos(current));
        return tag;
    }
}
