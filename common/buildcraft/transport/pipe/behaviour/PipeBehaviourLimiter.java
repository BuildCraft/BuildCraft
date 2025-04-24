package buildcraft.transport.pipe.behaviour;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.*;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraft.api.transport.pipe.PipeApi.PowerTransferInfo;
import buildcraft.api.transport.pipe.PipeApi.RedstoneFluxTransferInfo;
import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.MathUtil;
import buildcraft.transport.pipe.flow.PipeFlowRedstoneFlux;
import buildcraft.transport.statements.ActionPowerLimit;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.IOException;

public class PipeBehaviourLimiter extends PipeBehaviour {

    public static final int MAX_SHIFT = 6;

    private int limitShift = 0;

    public PipeBehaviourLimiter(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourLimiter(IPipe pipe, CompoundNBT nbt) {
        super(pipe, nbt);
        limitShift = MathUtil.clamp(nbt.getInt("limitShift"), 0, MAX_SHIFT);
    }

    @Override
    public CompoundNBT writeToNbt() {
        CompoundNBT nbt = super.writeToNbt();
        nbt.putInt("limitShift", limitShift);
        return nbt;
    }

    @Override
    public void readPayload(PacketBuffer buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        limitShift = buffer.readUnsignedByte();
    }

    @Override
    public void writePayload(PacketBuffer buffer, Dist side) {
        super.writePayload(buffer, side);
        buffer.writeByte(limitShift);
    }

    @PipeEventHandler
    public void configurePower(PipeEventPower.Configure event) {
        if (limitShift == MAX_SHIFT) {
            event.disableTransfer();
        } else {
            event.setMaxPower(event.getMaxPower() >> limitShift);
        }
    }

    @PipeEventHandler
    public void configurePower(PipeEventRedstoneFlux.Configure event) {
        if (limitShift == MAX_SHIFT) {
            event.disableTransfer();
        } else {
            event.setMaxPower(event.getMaxPower() >> limitShift);
        }
    }

    @PipeEventHandler
    public void onActionActivate(PipeEventActionActivate event) {
        if (event.action instanceof ActionPowerLimit) {
            limitShift = ((ActionPowerLimit) event.action).limitShift;

            requestReconfigure();
        }
    }

    @Override
    public boolean onPipeActivate(
            PlayerEntity player, RayTraceResult trace, float hitX, float hitY, float hitZ, EnumPipePart part
    ) {
        if (EntityUtil.getWrenchHand(player) == null) {
            return false;
        }

        if (!player.level.isClientSide) {
            EntityUtil.activateWrench(player, trace);
            limitShift++;
            if (limitShift > MAX_SHIFT) {
                limitShift = 0;
            }

            boolean isRf = pipe.getFlow() instanceof PipeFlowRedstoneFlux;
            final int limit;
            if (limitShift == MAX_SHIFT) {
                limit = 0;
            } else if (isRf) {
                RedstoneFluxTransferInfo transferInfo = PipeApi.getRfTransferInfo(pipe.getDefinition());
                limit = transferInfo.transferPerTick >> limitShift;
            } else {
                PowerTransferInfo transferInfo = PipeApi.getPowerTransferInfo(pipe.getDefinition());
                limit = (int) ((transferInfo.transferPerTick >> limitShift) / MjAPI.MJ);
            }
            String key = "chat.pipe." + (isRf ? "rf" : "power") + ".iron.mode";
            TranslationTextComponent chat = new TranslationTextComponent(key, limit);
            // player.sendStatusMessage(chat, true);
            player.displayClientMessage(chat, true);

            requestReconfigure();
        }
        return true;
    }

    private void requestReconfigure() {
        if (pipe.getFlow() instanceof IFlowPowerLike) {
            ((IFlowPowerLike) pipe.getFlow()).reconfigure();
            pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
        }
    }

    @Override
    public int getTextureIndex(Direction face) {
        return MAX_SHIFT - limitShift;
    }
}
