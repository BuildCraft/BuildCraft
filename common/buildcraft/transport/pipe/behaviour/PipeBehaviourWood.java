package buildcraft.transport.pipe.behaviour;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;

import buildcraft.core.lib.inventory.filters.StackFilter;
import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.NBTUtils;
import buildcraft.transport.api_move.IFlowItems;
import buildcraft.transport.api_move.IPipe;
import buildcraft.transport.api_move.IPipeHolder.PipeMessageReceiver;
import buildcraft.transport.api_move.PipeBehaviour;
import buildcraft.transport.api_move.PipeFlow;

public class PipeBehaviourWood extends PipeBehaviour implements IMjRedstoneReceiver {
    private EnumPipePart currentDir = EnumPipePart.CENTER;

    public PipeBehaviourWood(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourWood(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
        setCurrentDir(NBTUtils.readEnum(nbt.getTag("currentDir"), EnumFacing.class));
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        nbt.setTag("currentDir", NBTUtils.writeEnum(getCurrentDir()));
        return nbt;
    }

    @Override
    public int getTextureIndex(EnumFacing face) {
        return (face != null && face == getCurrentDir()) ? 1 : 0;
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
        currentDir = EnumPipePart.fromMeta(buffer.readUnsignedByte());
    }

    @Override
    public void onTick() {
        if (pipe.getHolder().getPipeWorld().isRemote) {
            return;
        }
    }

    @Override
    public boolean onPipeActivate(EntityPlayer player, RayTraceResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
        if (EntityUtil.getWrenchHand(player) != null) {
            EntityUtil.activateWrench(player);
            if (part.face != getCurrentDir()) {
                if (part == EnumPipePart.CENTER) {
                    // TODO: Advance the currentDir
                    return false;
                } else {
                    setCurrentDir(part.face);
                }
                if (!player.worldObj.isRemote) {
                    pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
                }
            }
            return true;
        }
        return false;
    }

    @Nullable
    private EnumFacing getCurrentDir() {
        return currentDir.face;
    }

    private void setCurrentDir(EnumFacing setTo) {
        this.currentDir = EnumPipePart.fromFacing(setTo);
    }

    // IMjRedstoneReceiver

    @Override
    public boolean canConnect(IMjConnector other) {
        return true;
    }

    @Override
    public long getPowerRequested() {
        return MjAPI.MJ;
    }

    @Override
    public long receivePower(long microJoules, boolean simulate) {
        // TODO: Make this require more or less than 1 Mj Per item
        // Also make this extract different numbers of items depending
        // on how much power was put in

        PipeFlow flow = pipe.getFlow();
        if (flow instanceof IFlowItems) {
            ((IFlowItems) flow).tryExtractStack(1, getCurrentDir(), StackFilter.ALL);
        }
        return 0;
    }
}
