package buildcraft.transport.pipe.behaviour;

import java.util.Collections;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.PipeEventActionActivate;
import buildcraft.api.transport.PipeEventHandler;
import buildcraft.api.transport.PipeEventItem;
import buildcraft.api.transport.PipeEventStatement;
import buildcraft.api.transport.neptune.IPipe;
import buildcraft.api.transport.neptune.IPipeHolder.PipeMessageReceiver;

import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.transport.BCTransportStatements;
import buildcraft.transport.statements.ActionPipeColor;

public class PipeBehaviourDaizuli extends PipeBehaviourDirectional {
    private EnumDyeColor colour = EnumDyeColor.WHITE;

    public PipeBehaviourDaizuli(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourDaizuli(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
        colour = NBTUtilBC.readEnum(nbt.getTag("colour"), EnumDyeColor.class);
        if (colour == null) {
            colour = EnumDyeColor.WHITE;
        }
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        nbt.setTag("colour", NBTUtilBC.writeEnum(colour));
        return nbt;
    }

    @Override
    public void writePayload(PacketBuffer buffer, Side side) {
        super.writePayload(buffer, side);
        if (side == Side.SERVER) {
            buffer.writeByte(colour.getMetadata());
        }
    }

    @Override
    public void readPayload(PacketBuffer buffer, Side side, MessageContext ctx) {
        super.readPayload(buffer, side, ctx);
        if (side == Side.CLIENT) {
            colour = EnumDyeColor.byMetadata(buffer.readUnsignedByte());
        }
    }

    @Override
    public int getTextureIndex(EnumFacing face) {
        if (face != currentDir.face && face != null) {
            return 16;
        }
        return colour.getMetadata();
    }

    @Override
    protected boolean canFaceDirection(EnumFacing dir) {
        return true;
    }

    @Override
    public boolean onPipeActivate(EntityPlayer player, RayTraceResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
        if (part != EnumPipePart.CENTER && part != currentDir) {
            // Activating the centre of a pipe always falls back to changing the colour
            // And so does clicking on the current facing side
            return super.onPipeActivate(player, trace, hitX, hitY, hitZ, part);
        }
        if (player.world.isRemote) {
            return EntityUtil.getWrenchHand(player) != null;
        }
        if (EntityUtil.getWrenchHand(player) != null) {
            EntityUtil.activateWrench(player);
            int n = colour.getMetadata() + (player.isSneaking() ? 15 : 1);
            colour = EnumDyeColor.byMetadata(n & 15);
            pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
            return true;
        }
        return false;
    }

    @PipeEventHandler
    public void sideCheck(PipeEventItem.SideCheck sideCheck) {
        if (colour == sideCheck.colour) {
            sideCheck.disallowAllExcept(currentDir.face);
        } else {
            sideCheck.disallow(currentDir.face);
        }
    }

    @PipeEventHandler
    public void addActions(PipeEventStatement.AddActionInternal event) {
        Collections.addAll(event.actions, BCTransportStatements.ACTION_PIPE_COLOUR);
    }

    @PipeEventHandler
    public void onActionActivated(PipeEventActionActivate event) {
        if (event.action instanceof ActionPipeColor) {
            ActionPipeColor action = ((ActionPipeColor) event.action);
            if (this.colour != action.color) {
                this.colour = action.color;
                pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
            }
        }
    }
}
