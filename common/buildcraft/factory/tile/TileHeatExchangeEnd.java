package buildcraft.factory.tile;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.factory.BCFactoryBlocks;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.fluid.Tank;
import buildcraft.lib.fluid.TankManager;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;

public class TileHeatExchangeEnd extends TileBC_Neptune implements IDebuggable {
    public final Tank tankHeatableOut = new Tank("heatable_out", 2 * Fluid.BUCKET_VOLUME, this);
    public final Tank tankCoolableIn = new Tank("coolable_in", 2 * Fluid.BUCKET_VOLUME, this);
    private final TankManager<Tank> tankManager = new TankManager<>(tankHeatableOut, tankCoolableIn);

    public TileHeatExchangeEnd() {
        caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, tankHeatableOut, EnumPipePart.UP);
        caps.addCapability(CapUtil.CAP_FLUIDS, this::getTankForSide, EnumPipePart.HORIZONTALS);
    }

    private IFluidHandler getTankForSide(EnumFacing side) {
        IBlockState state = getCurrentStateForBlock(BCFactoryBlocks.heatExchangeEnd);
        if (state == null) {
            return null;
        }
        EnumFacing thisFacing = state.getValue(BlockBCBase_Neptune.PROP_FACING);
        if (side != thisFacing.getOpposite()) {
            return null;
        }
        return tankCoolableIn;
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
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("heatable_out = " + tankHeatableOut.getDebugString());
        left.add("coolable_in = " + tankCoolableIn.getDebugString());
    }

    @Nullable
    public IFluidHandler getFluidAutoOutputTarget() {
        IBlockState state = getCurrentStateForBlock(BCFactoryBlocks.heatExchangeEnd);
        if (state == null) {
            return null;
        }
        EnumFacing facing = state.getValue(BlockBCBase_Neptune.PROP_FACING);
        TileEntity tile = getNeighbourTile(facing.getOpposite());
        if (tile == null) {
            return null;
        }
        return tile.getCapability(CapUtil.CAP_FLUIDS, facing);
    }
}
