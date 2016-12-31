package buildcraft.builders.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.enums.EnumFillerPattern;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.BoundingBoxUtil;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.io.IOException;
import java.util.List;

public class TileFiller extends TileBC_Neptune implements ITickable, IDebuggable {
    public final IItemHandlerModifiable invResources = itemManager.addInvHandler("resources", 27, EnumAccess.NONE, EnumPipePart.VALUES);
    public Box box = null;
    public final MjBattery battery = new MjBattery(1000 * MjAPI.MJ);
    public EnumFillerPattern pattern = EnumFillerPattern.NONE;

    @Override
    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        EnumFacing thisFacing = getWorld().getBlockState(getPos()).getValue(BlockBCBase_Neptune.PROP_FACING);
        TileEntity inFront = getWorld().getTileEntity(getPos().offset(thisFacing.getOpposite()));
        if (inFront instanceof IAreaProvider) {
            IAreaProvider provider = (IAreaProvider) inFront;
            BlockPos min = provider.min();
            BlockPos max = provider.max();
            if (min != null && max != null && !min.equals(max)) {
                box = new Box(min, max);
                provider.removeFromWorld();
                sendNetworkUpdate(NET_RENDER_DATA);
            }
        }
    }

    @Override
    public void update() {
        battery.tick(getWorld(), getPos());
        if (world.isRemote) {
            return;
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
            }
        }
    }

    // Read-write

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        NBTTagCompound boxTag = nbt.getCompoundTag("box");
        if (!boxTag.hasNoTags()) {
            box = new Box();
            box.initialize(boxTag);
        }
        battery.deserializeNBT(nbt.getCompoundTag("mj_battery"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (box != null) {
            nbt.setTag("box", box.writeToNBT());
        }
        nbt.setTag("mj_battery", battery.serializeNBT());
        return nbt;
    }

    // Rendering

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasFastRenderer() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return BoundingBoxUtil.makeFrom(getPos(), box);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return Double.MAX_VALUE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("box = " + box);
    }
}
