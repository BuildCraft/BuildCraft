package buildcraft.transport.pluggable;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;

import buildcraft.BuildCraftTransport;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.pluggable.IPipePluggableStaticRenderer;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.lib.utils.MatrixTranformations;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.pipes.events.PipeEventItem;

import io.netty.buffer.ByteBuf;

public class LensPluggable extends PipePluggable {
    @Deprecated
    public int color;
    public boolean isFilter;
    protected IPipeTile container;
    private EnumFacing side;

    public LensPluggable() {

    }

    public LensPluggable(ItemStack stack) {
        color = stack.getItemDamage() & 15;
        isFilter = stack.getItemDamage() >= 16;
        if (stack.getItemDamage() >= 32) {
            isFilter = stack.getItemDamage() == 33;
            color = -1;
        }
    }

    @Override
    public void validate(IPipeTile pipe, EnumFacing direction) {
        this.container = pipe;
        this.side = direction;
    }

    @Override
    public void invalidate() {
        this.container = null;
        this.side = null;
    }

    @Override
    public ItemStack[] getDropItems(IPipeTile pipe) {
        int meta = color | (isFilter ? 16 : 0);
        if (color == -1) {
            meta = isFilter ? 33 : 32;
        }

        return new ItemStack[] { new ItemStack(BuildCraftTransport.lensItem, 1, meta) };
    }

    @Override
    public boolean isBlocking(IPipeTile pipe, EnumFacing direction) {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(EnumFacing side) {
        float[][] bounds = new float[3][2];
        // X START - END
        bounds[0][0] = 0.25F - 0.0625F;
        bounds[0][1] = 0.75F + 0.0625F;
        // Y START - END
        bounds[1][0] = 0.000F;
        bounds[1][1] = 0.125F;
        // Z START - END
        bounds[2][0] = 0.25F - 0.0625F;
        bounds[2][1] = 0.75F + 0.0625F;

        MatrixTranformations.transform(bounds, side);
        return new AxisAlignedBB(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
    }

    @Override
    public IPipePluggableStaticRenderer getRenderer() {
        return LensPluggableModel.INSTANCE;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        color = tag.getByte("c");
        isFilter = tag.getBoolean("f");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setByte("c", (byte) color);
        tag.setBoolean("f", isFilter);
    }

    @Override
    public void writeData(ByteBuf data) {
        data.writeByte(((color + 1) & 0x1F) | (isFilter ? 0x20 : 0));
    }

    @Override
    public void readData(ByteBuf data) {
        int flags = data.readUnsignedByte();
        color = (flags & 0x1F) - 1;
        isFilter = (flags & 0x20) > 0;
    }

    @Override
    public boolean requiresRenderUpdate(PipePluggable o) {
        LensPluggable other = (LensPluggable) o;
        return other.color != color || other.isFilter != isFilter;
    }

    private void color(TravelingItem item) {
        if ((item.toCenter && item.input.getOpposite() == side) || (!item.toCenter && item.output == side)) {
            if (color == -1) {
                item.color = null;
            } else {
                item.color = EnumDyeColor.byDyeDamage(color);
            }
        }
    }

    public void eventHandler(PipeEventItem.ReachedEnd event) {
        if (!isFilter) {
            color(event.item);
        }
    }

    public void eventHandler(PipeEventItem.Entered event) {
        if (!isFilter) {
            color(event.item);
        }
    }

    public EnumDyeColor getColour() {
        return EnumDyeColor.byDyeDamage(color);
    }
}
