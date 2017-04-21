package buildcraft.factory.tile;

import java.io.IOException;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.tiles.IDebuggable;

import buildcraft.factory.BCFactoryBlocks;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.node.value.*;
import buildcraft.lib.expression.node.value.NodeStateful.Instance;
import buildcraft.lib.fluids.Tank;
import buildcraft.lib.fluids.TankManager;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;

public class TileDistiller_BC8 extends TileBC_Neptune implements ITickable, IDebuggable {
    public static final FunctionContext MODEL_FUNC_CTX;
    private static final NodeVariableString MODEL_FACING;
    private static final NodeVariableBoolean MODEL_ACTIVE;
    private static final NodeVariableLong MODEL_POWER_AVG;

    static {
        MODEL_FUNC_CTX = DefaultContexts.createWithAll();
        MODEL_FACING = MODEL_FUNC_CTX.putVariableString("facing");
        MODEL_POWER_AVG = MODEL_FUNC_CTX.putVariableLong("power_average"); // 0 - 4 (0 for not active, 1 - 4 for active)
        MODEL_ACTIVE = MODEL_FUNC_CTX.putVariableBoolean("active");
    }

    public final Tank tankIn = new Tank("in", 4 * Fluid.BUCKET_VOLUME, this);
    public final Tank tankOutGas = new Tank("out_gas", 4 * Fluid.BUCKET_VOLUME, this);
    public final Tank tankOutLiquid = new Tank("out_liquid", 4 * Fluid.BUCKET_VOLUME, this);
    public final TankManager<Tank> tankManager = new TankManager<>(tankIn, tankOutGas, tankOutLiquid);

    /** The model variables, used to keep track of the various state-based variables. */
    public ITickableNode[] clientRenderVariables;

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

    public void setClientModelVariables(float partialTicks) {
        DefaultContexts.RENDER_PARTIAL_TICKS.value = partialTicks;

        // TEMP for testing

        long totalWorldTime = world.getTotalWorldTime();
        boolean active = (totalWorldTime / 400) % 2 == 1;
        MODEL_ACTIVE.value = active;
        MODEL_POWER_AVG.value = active ? (totalWorldTime % 400 / 150) + 1 : 0;
        MODEL_FACING.value = "west";

        IBlockState state = world.getBlockState(getPos());
        if (state.getBlock() == BCFactoryBlocks.distiller) {
            MODEL_FACING.value = state.getValue(BlockBCBase_Neptune.PROP_FACING).getName();
        }
    }

    @Override
    public void update() {
        if (world.isRemote) {
            if (clientRenderVariables != null) {
                setClientModelVariables(1);
                for (ITickableNode node : clientRenderVariables) {
                    node.tick();
                }
            }
            return;
        }
        if (Math.random() < 0.1) {// TEMP!
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
        if (world.isRemote) {
            left.add("model_facing = " + MODEL_FACING.value);
            left.add("model_active = " + MODEL_ACTIVE.value);
            left.add("model_power = " + MODEL_POWER_AVG.value);
            if (clientRenderVariables != null) {
                left.add("model_custom:");
                for (ITickableNode node : clientRenderVariables) {
                    if (node instanceof NodeUpdatable) {
                        NodeUpdatable nU = (NodeUpdatable) node;
                        left.add("  " + nU.name + " = " + nU.variable.valueToString());
                    } else if (node instanceof NodeStateful.Instance) {
                        NodeStateful.Instance nS = (Instance) node;
                        left.add("  " + nS.getContainer().name + " = " + nS.storedVar.valueToString());
                    }
                }
            }
        }
    }
}
