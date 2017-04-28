package buildcraft.factory.tile;

import java.io.IOException;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.lib.fluid.Tank;
import buildcraft.lib.fluid.TankManager;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;

public class TileHeatExchangeStart extends TileBC_Neptune implements ITickable {
    public final Tank tankHeatableIn = new Tank("heatable_in", 2 * Fluid.BUCKET_VOLUME, this);
    public final Tank tankCoolableOut = new Tank("heatable_in", 2 * Fluid.BUCKET_VOLUME, this);
    private final TankManager<Tank> tankManager = new TankManager<>(tankHeatableIn, tankCoolableOut);

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

    }
}
