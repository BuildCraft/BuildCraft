package buildcraft.transport.pipe.behaviour;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.EnumPipePart;

import buildcraft.core.lib.inventory.filters.StackFilter;
import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.NBTUtils;
import buildcraft.transport.api_move.IFlowItems;
import buildcraft.transport.api_move.IPipe;
import buildcraft.transport.api_move.IPipeHolder.PipeMessageReceiver;
import buildcraft.transport.api_move.PipeBehaviour;
import buildcraft.transport.api_move.PipeFlow;

public class PipeBehaviourWood extends PipeBehaviour {
    private EnumFacing currentDir = EnumFacing.UP;

    // Currently we auto-extract every 2 seconds
    private int lastExtract = 0;

    public PipeBehaviourWood(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourWood(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
        nbt.setTag("currentDir", NBTUtils.writeEnum(currentDir));
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        currentDir = NBTUtils.readEnum(nbt.getTag("currentDir"), EnumFacing.class);
        return nbt;
    }

    @Override
    public int getTextureIndex(EnumFacing face) {
        return (face != null && face == currentDir) ? 1 : 0;
    }

    @Override
    public boolean canConnect(EnumFacing face, PipeBehaviour other) {
        return !(other instanceof PipeBehaviourWood);
    }

    @Override
    public boolean canConnect(EnumFacing face, TileEntity oTile) {
        return true;
    }

    @Override
    public void writePayload(PacketBuffer buffer, Side side) {
        super.writePayload(buffer, side);
        buffer.writeByte(currentDir.getIndex());
    }

    @Override
    public void readPayload(PacketBuffer buffer, Side side, MessageContext ctx) {
        super.readPayload(buffer, side, ctx);
        currentDir = EnumFacing.getFront(buffer.readUnsignedByte());
    }

    @Override
    public void onTick() {
        if (pipe.getHolder().getPipeWorld().isRemote) {
            return;
        }

        lastExtract++;
        if (lastExtract >= 40) {
            lastExtract = 0;
            PipeFlow flow = pipe.getFlow();
            if (flow instanceof IFlowItems) {
                ((IFlowItems) flow).tryExtractStack(1, currentDir, StackFilter.ALL);
            }
        }
    }

    @Override
    public boolean onPipeActivate(EntityPlayer player, RayTraceResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
        if (EntityUtil.getWrenchHand(player) != null) {
            EntityUtil.activateWrench(player);
            if (part.face != currentDir) {
                if (part == EnumPipePart.CENTER) {
                    // TODO: Advance the currentDir
                    return false;
                } else {
                    currentDir = part.face;
                }
                if (!player.worldObj.isRemote) {
                    pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
                }
            }
            return true;
        }
        return false;
    }
}
