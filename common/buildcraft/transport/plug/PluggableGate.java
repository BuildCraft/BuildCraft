package buildcraft.transport.plug;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.lib.net.command.IPayloadWriter;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.api_move.IPipeHolder;
import buildcraft.transport.api_move.IPipeHolder.PipeMessageReceiver;
import buildcraft.transport.api_move.IPluggableDynamicRenderer;
import buildcraft.transport.api_move.PipePluggable;
import buildcraft.transport.api_move.PluggableDefinition;
import buildcraft.transport.client.model.key.KeyPlugGate;
import buildcraft.transport.client.render.PlugGateRenderer;
import buildcraft.transport.gate.GateLogic;
import buildcraft.transport.gate.GateVariant;

public class PluggableGate extends PipePluggable {
    public static final int SET_TRIGGER = 0;
    public static final int SET_ACTION = 1;
    public static final int SET_TRIGGER_ARG = 2;
    public static final int SET_ACTION_ARG = 3;

    private static final AxisAlignedBB[] BOXES = new AxisAlignedBB[6];

    public final GateLogic logic;

    static {
        double ll = 2 / 16.0;
        double lu = 4 / 16.0;
        double ul = 12 / 16.0;
        double uu = 14 / 16.0;

        double min = 5 / 16.0;
        double max = 11 / 16.0;

        BOXES[EnumFacing.DOWN.getIndex()] = new AxisAlignedBB(min, ll, min, max, lu, max);
        BOXES[EnumFacing.UP.getIndex()] = new AxisAlignedBB(min, ul, min, max, uu, max);
        BOXES[EnumFacing.NORTH.getIndex()] = new AxisAlignedBB(min, min, ll, max, max, lu);
        BOXES[EnumFacing.SOUTH.getIndex()] = new AxisAlignedBB(min, min, ul, max, max, uu);
        BOXES[EnumFacing.WEST.getIndex()] = new AxisAlignedBB(ll, min, min, lu, max, max);
        BOXES[EnumFacing.EAST.getIndex()] = new AxisAlignedBB(ul, min, min, uu, max, max);
    }

    public PluggableGate(PluggableDefinition def, IPipeHolder holder, EnumFacing side, GateVariant variant) {
        super(def, holder, side);
        logic = new GateLogic(this, variant);
    }

    // Saving + Loading

    public PluggableGate(PluggableDefinition def, IPipeHolder holder, EnumFacing side, NBTTagCompound nbt) {
        super(def, holder, side);
        logic = new GateLogic(this, nbt.getCompoundTag("data"));
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        nbt.setTag("data", logic.writeToNbt());
        return nbt;
    }

    // Networking

    public PluggableGate(PluggableDefinition def, IPipeHolder holder, EnumFacing side, PacketBuffer buffer) {
        super(def, holder, side);
        logic = new GateLogic(this, buffer);
    }

    @Override
    public void writeCreationPayload(PacketBuffer buffer) {
        logic.writeCreationToBuf(buffer);
    }

    public void sendMessage(int id, IPayloadWriter writer) {
        PipeMessageReceiver to = PipeMessageReceiver.PLUGGABLES[side.ordinal()];
        holder.sendMessage(to, (buffer) -> {
            buffer.writeByte(id);
            writer.write(buffer);
        });
    }

    // PipePluggable

    @Override
    public AxisAlignedBB getBoundingBox() {
        return BOXES[side.getIndex()];
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    @Override
    public void onRemove(List<ItemStack> toDrop) {
        toDrop.add(getPickStack());
    }

    @Override
    public ItemStack getPickStack() {
        return BCTransportItems.plugGate.getStack(logic.variant);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public PluggableModelKey<?> getModelRenderKey(BlockRenderLayer layer) {
        if (layer == BlockRenderLayer.CUTOUT) {
            return new KeyPlugGate(side, logic.variant);
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IPluggableDynamicRenderer getDynamicRenderer() {
        return new PlugGateRenderer(this);
    }

    // Gate methods

    @Override
    public void onTick() {
        logic.onTick();
    }
}
