package buildcraft.factory.tile;

import java.io.IOException;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.fluids.Tank;
import buildcraft.lib.fluids.TankManager;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;

public class TileDistiller_BC8 extends TileBC_Neptune implements ITickable, IDebuggable {
    public final Tank tankIn = new Tank("in", 4 * Fluid.BUCKET_VOLUME, this);
    public final Tank tankOutGas = new Tank("out_gas", 4 * Fluid.BUCKET_VOLUME, this);
    public final Tank tankOutLiquid = new Tank("out_liquid", 4 * Fluid.BUCKET_VOLUME, this);
    public final TankManager<Tank> tankManager = new TankManager<>(tankIn, tankOutGas, tankOutLiquid);

    public TileDistiller_BC8() {
        tankIn.setCanDrain(false);
        tankOutGas.setCanFill(false);
        tankOutLiquid.setCanFill(false);

        caps.addCapability(CapUtil.CAP_FLUIDS, tankIn, EnumFacing.HORIZONTALS);
        caps.addCapability(CapUtil.CAP_FLUIDS, tankOutGas, EnumFacing.UP);
        caps.addCapability(CapUtil.CAP_FLUIDS, tankOutLiquid, EnumFacing.DOWN);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setTag("tanks", tankManager.serializeNBT());
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        tankManager.deserializeNBT(nbt.getCompoundTag("tanks"));
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                tankManager.writeData(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                tankManager.readData(buffer);
            }
        }
    }

    @Override
    public void update() {
        if (!world.isRemote && Math.random() < 0.1) {// TEMP!
            sendNetworkUpdate(NET_RENDER_DATA);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("In = " + tankIn.getDebugString());
        left.add("OutGas = " + tankOutGas.getDebugString());
        left.add("OutLiquid = " + tankOutLiquid.getDebugString());
        tankIn.getDebugString();
    }
}
