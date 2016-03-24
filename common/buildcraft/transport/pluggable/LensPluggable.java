package buildcraft.transport.pluggable;

import io.netty.buffer.ByteBuf;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.BuildCraftTransport;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.lib.utils.MatrixTranformations;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.client.model.ModelKeyLens;
import buildcraft.transport.pipes.events.PipeEventItem;

public class LensPluggable extends PipePluggable {
    public EnumDyeColor dyeColor;
    public boolean isFilter;
    protected IPipeTile container;
    private EnumFacing side;

    public LensPluggable() {

    }

    public LensPluggable(ItemStack stack) {
        dyeColor = EnumDyeColor.byMetadata(stack.getItemDamage() & 15);
        isFilter = stack.getItemDamage() >= 16;
        if (stack.getItemDamage() >= 32) {
            isFilter = stack.getItemDamage() == 33;
            dyeColor = null;
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
        int colourMeta;
        if (dyeColor == null) {
            colourMeta = isFilter ? 33 : 32;
        } else {
            colourMeta = dyeColor.getMetadata() | (isFilter ? 16 : 0);
        }
        return new ItemStack[] { new ItemStack(BuildCraftTransport.lensItem, 1, colourMeta) };
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
    @SideOnly(Side.CLIENT)
    public ModelKeyLens getModelRenderKey(EnumWorldBlockLayer layer, EnumFacing side) {
        if (layer == EnumWorldBlockLayer.CUTOUT) {
            return new ModelKeyLens.Cutout(side, isFilter);
        } else if (layer == EnumWorldBlockLayer.TRANSLUCENT) {
            return new ModelKeyLens.Translucent(side, isFilter, dyeColor);
        } else {
            return null;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        if (tag.hasKey("colour")) {
            dyeColor = NBTUtils.readEnum(tag.getTag("colour"), EnumDyeColor.class);
        } else {
            dyeColor = EnumDyeColor.byMetadata(tag.getByte("c"));
        }
        isFilter = tag.getBoolean("f");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setTag("colour", NBTUtils.writeEnum(dyeColor));
        tag.setBoolean("f", isFilter);
    }

    @Override
    public void writeData(ByteBuf data) {
        int col = dyeColor == null ? 0 : dyeColor.getMetadata() + 1;
        data.writeByte((col & 0x1F) | (isFilter ? 0x20 : 0));
    }

    @Override
    public void readData(ByteBuf data) {
        int flags = data.readUnsignedByte();
        int col = (flags & 0x1F);
        if (col == 0) dyeColor = null;
        else dyeColor = EnumDyeColor.byMetadata(col - 1);
        isFilter = (flags & 0x20) > 0;
    }

    @Override
    public boolean requiresRenderUpdate(PipePluggable o) {
        LensPluggable other = (LensPluggable) o;
        return other.dyeColor != dyeColor || other.isFilter != isFilter;
    }

    private void color(TravelingItem item) {
        if ((item.toCenter && item.input.getOpposite() == side) || (!item.toCenter && item.output == side)) {
            item.color = dyeColor;
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
        return dyeColor;
    }
}
