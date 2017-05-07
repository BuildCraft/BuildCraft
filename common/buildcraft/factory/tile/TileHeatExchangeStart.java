package buildcraft.factory.tile;

import java.io.IOException;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.block.BlockHeatExchange;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.fluid.Tank;
import buildcraft.lib.fluid.TankManager;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;

public class TileHeatExchangeStart extends TileBC_Neptune implements ITickable, IDebuggable {
    public final Tank tankHeatableIn = new Tank("heatable_in", 2 * Fluid.BUCKET_VOLUME, this);
    public final Tank tankCoolableOut = new Tank("coolable_out", 2 * Fluid.BUCKET_VOLUME, this);
    private final TankManager<Tank> tankManager = new TankManager<>(tankHeatableIn, tankCoolableOut);

    private TileHeatExchangeEnd tileEnd;

    public TileHeatExchangeStart() {
        caps.addCapability(CapUtil.CAP_FLUIDS, tankHeatableIn, EnumPipePart.DOWN);
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
        return tankCoolableOut;
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
        if (world.isRemote) {
            // TODO: Client stuffs
            return;
        }
        // TODO (AlexIIL): Make this check passive, not active.
        tileEnd = null;
        IBlockState state = getCurrentStateForBlock(BCFactoryBlocks.heatExchangeStart);
        if (state == null) {
            return;
        }
        BlockHeatExchange block = (BlockHeatExchange) state.getBlock();
        EnumFacing facing = state.getValue(BlockBCBase_Neptune.PROP_FACING);
        int middles = 0;
        BlockPos search = getPos();
        for (int i = 0; i < 3; i++) {
            search = search.offset(facing);
            state = getLocalState(search);
            if (state.getBlock() != BCFactoryBlocks.heatExchangeMiddle) {
                break;
            }
            block = BCFactoryBlocks.heatExchangeMiddle;
            if (block.part.getAxis(state) != facing.getAxis()) {
                return;
            }
            middles++;
        }
        if (middles == 0) {
            return;
        }
        search = search.offset(facing);
        state = getLocalState(search);
        if (state.getBlock() != BCFactoryBlocks.heatExchangeEnd) {
            return;
        }
        if (state.getValue(BlockBCBase_Neptune.PROP_FACING) != facing.getOpposite()) {
            return;
        }
        TileEntity tile = getLocalTile(search);
        if (tile instanceof TileHeatExchangeEnd) {
            tileEnd = (TileHeatExchangeEnd) tile;
        }
        // TODO: Ticks!
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("heatable_in = " + tankHeatableIn.getDebugString());
        left.add("coolable_out = " + tankCoolableOut.getDebugString());
    }
}
