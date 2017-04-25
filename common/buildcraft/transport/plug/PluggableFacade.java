package buildcraft.transport.plug;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.facades.FacadeType;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableModelKey;

import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.RotationUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.client.model.key.KeyPlugBlocker;
import buildcraft.transport.client.model.key.KeyPlugFacade;
import buildcraft.transport.plug.FacadeStateManager.FacadePhasedState;
import buildcraft.transport.plug.FacadeStateManager.FullFacadeInstance;

public class PluggableFacade extends PipePluggable {
    public static final int SIZE = 2;
    public final FullFacadeInstance states;
    public int activeState;

    public PluggableFacade(PluggableDefinition definition, IPipeHolder holder, EnumFacing side, FullFacadeInstance states) {
        super(definition, holder, side);
        this.states = states;
    }

    public PluggableFacade(PluggableDefinition def, IPipeHolder holder, EnumFacing side, NBTTagCompound nbt) {
        super(def, holder, side);
        this.states = FullFacadeInstance.readFromNbt(nbt, "states");
        activeState = MathUtil.clamp(nbt.getInteger("activeState"), 0, states.phasedStates.length - 1);
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        states.writeToNbt(nbt, "states");
        nbt.setInteger("activeState", activeState);
        return nbt;
    }

    // Networking

    public PluggableFacade(PluggableDefinition def, IPipeHolder holder, EnumFacing side, PacketBuffer buffer) {
        super(def, holder, side);
        states = FullFacadeInstance.readFromBuffer(PacketBufferBC.asPacketBufferBc(buffer));
    }

    @Override
    public void writeCreationPayload(PacketBuffer buffer) {
        states.writeToBuffer(PacketBufferBC.asPacketBufferBc(buffer));
    }

    // Pluggable methods

    @Override
    public AxisAlignedBB getBoundingBox() {
        return RotationUtil.rotateAABB(new AxisAlignedBB(0 / 16D, 0 / 16D, 0 / 16D, 16 / 16D, SIZE / 16D, 16 / 16D), side);
    }

    @Override
    public boolean isBlocking() {
        return !states.phasedStates[activeState].isHollow;
    }

    @Override
    public void onRemove(NonNullList<ItemStack> toDrop) {
        toDrop.add(getPickStack());
    }

    @Override
    public ItemStack getPickStack() {
        return BCTransportItems.plugFacade.createItemStack(states);
    }

    @Override
    public PluggableModelKey getModelRenderKey(BlockRenderLayer layer) {
        if (states.type == FacadeType.Basic) {
            FacadePhasedState state = states.phasedStates[activeState];
            return new KeyPlugFacade(layer, side, state.stateInfo.state, state.isHollow);
        } else {
            return new KeyPlugBlocker(side);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getBlockColor(int tintIndex) {
        FacadePhasedState state = states.phasedStates[activeState];
        return Minecraft.getMinecraft().getBlockColors().colorMultiplier(state.stateInfo.state, holder.getPipeWorld(), holder.getPipePos(), tintIndex);
    }

    @Override
    public boolean canBeConnected() {
        return !states.phasedStates[activeState].isHollow;
    }
}
