package buildcraft.transport.pipe.behaviour;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.EnumPipePart;

import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.NBTUtils;
import buildcraft.transport.api_move.IPipe;
import buildcraft.transport.api_move.IPipeHolder.PipeMessageReceiver;
import buildcraft.transport.api_move.PipeBehaviour;

public abstract class PipeBehaviourDirectional extends PipeBehaviour {
    protected EnumPipePart currentDir = EnumPipePart.CENTER;

    public PipeBehaviourDirectional(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourDirectional(IPipe pipe, NBTTagCompound nbt) {
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
    public boolean onPipeActivate(EntityPlayer player, RayTraceResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
        if (EntityUtil.getWrenchHand(player) != null) {
            EntityUtil.activateWrench(player);
            if (part.face != getCurrentDir()) {
                if (part == EnumPipePart.CENTER) {
                    // TODO: Advance the currentDir
                    return false;
                } else {
                    if (canFaceDirection(part.face)) {
                        setCurrentDir(part.face);
                    }
                }
                if (!player.worldObj.isRemote) {
                    pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
                }
            }
            return true;
        }
        return false;
    }

    protected abstract boolean canFaceDirection(EnumFacing dir);

    @Nullable
    protected EnumFacing getCurrentDir() {
        return currentDir.face;
    }

    protected void setCurrentDir(EnumFacing setTo) {
        this.currentDir = EnumPipePart.fromFacing(setTo);
    }
}
