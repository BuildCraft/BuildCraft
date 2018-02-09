package buildcraft.transport.plug;

import java.util.Arrays;

import javax.annotation.Nullable;

import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.util.Constants;

import buildcraft.api.facades.FacadeType;
import buildcraft.api.facades.IFacade;
import buildcraft.api.facades.IFacadePhasedState;

import buildcraft.lib.net.PacketBufferBC;

public class FacadeInstance implements IFacade {
    public final FacadePhasedState[] phasedStates;
    public final FacadeType type;
    public final boolean isHollow;

    public FacadeInstance(FacadePhasedState[] phasedStates, boolean isHollow) {
        if (phasedStates == null) throw new NullPointerException("phasedStates");
        if (phasedStates.length == 0) throw new IllegalArgumentException("phasedStates.length was 0");
        // Maximum of 17 states - 16 for each colour, 1 for no colour
        if (phasedStates.length > 17) throw new IllegalArgumentException("phasedStates.length was > 17");
        this.phasedStates = phasedStates;
        if (phasedStates.length == 1) {
            type = FacadeType.Basic;
        } else {
            type = FacadeType.Phased;
        }
        this.isHollow = isHollow;
    }

    public static FacadeInstance createSingle(FacadeBlockStateInfo info, boolean isHollow) {
        return new FacadeInstance(new FacadePhasedState[] { new FacadePhasedState(info, null) }, isHollow);
    }

    public static FacadeInstance readFromNbt(NBTTagCompound nbt) {
        NBTTagList list = nbt.getTagList("states", Constants.NBT.TAG_COMPOUND);
        if (list.hasNoTags()) {
            return FacadeInstance.createSingle(FacadeStateManager.defaultState, false);
        }
        FacadePhasedState[] states = new FacadePhasedState[list.tagCount()];
        for (int i = 0; i < list.tagCount(); i++) {
            states[i] = FacadePhasedState.readFromNbt(list.getCompoundTagAt(i));
        }
        boolean hollow = nbt.getBoolean("isHollow");
        return new FacadeInstance(states, hollow);
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        for (FacadePhasedState state : phasedStates) {
            list.appendTag(state.writeToNbt());
        }
        nbt.setTag("states", list);
        nbt.setBoolean("isHollow", isHollow);
        return nbt;
    }

    public static FacadeInstance readFromBuffer(PacketBufferBC buf) {
        boolean isHollow = buf.readBoolean();
        int count = buf.readFixedBits(5);
        FacadePhasedState[] states = new FacadePhasedState[count];
        for (int i = 0; i < count; i++) {
            states[i] = FacadePhasedState.readFromBuffer(buf);
        }
        return new FacadeInstance(states, isHollow);
    }

    public void writeToBuffer(PacketBufferBC buf) {
        buf.writeBoolean(isHollow);
        buf.writeFixedBits(phasedStates.length, 5);
        for (FacadePhasedState phasedState : phasedStates) {
            phasedState.writeToBuffer(buf);
        }
    }

    public boolean canAddColour(EnumDyeColor colour) {
        for (FacadePhasedState state : phasedStates) {
            if (state.activeColour == colour) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    public FacadeInstance withState(FacadePhasedState state) {
        if (canAddColour(state.activeColour)) {
            FacadePhasedState[] newStates = Arrays.copyOf(phasedStates, phasedStates.length + 1);
            newStates[newStates.length - 1] = state;
            return new FacadeInstance(newStates, isHollow);
        } else {
            return null;
        }
    }

    public FacadePhasedState getCurrentStateForStack() {
        int count = phasedStates.length;
        if (count == 1) {
            return phasedStates[0];
        } else {
            int now = (int) (System.currentTimeMillis() % 100_000);
            return phasedStates[(now / 500) % count];
        }
    }

    public FacadeInstance withSwappedIsHollow() {
        return new FacadeInstance(phasedStates, !isHollow);
    }

    public boolean areAllStatesSolid(EnumFacing side) {
        for (FacadePhasedState state : phasedStates) {
            if (!state.isSideSolid(side)) {
                return false;
            }
        }
        return true;
    }

    public BlockFaceShape getBlockFaceShape(EnumFacing side) {
        if (isHollow()) {
            return BlockFaceShape.UNDEFINED;
        }
        switch (type) {
            case Basic:
                return phasedStates[0].getBlockFaceShape(side);
            case Phased: {
                BlockFaceShape shape = null;
                for (FacadePhasedState state : phasedStates) {
                    if (shape == null) {
                        shape = state.getBlockFaceShape(side);
                    } else if (shape != state.getBlockFaceShape(side)) {
                        return BlockFaceShape.UNDEFINED;
                    }
                }
                if (shape == null) {
                    return BlockFaceShape.UNDEFINED;
                }
                return shape;
            }
            default:
                throw new IllegalStateException("Unknown FacadeType " + type);
        }
    }

    // IFacade

    @Override
    public FacadeType getType() {
        return type;
    }

    @Override
    public boolean isHollow() {
        return isHollow;
    }

    @Override
    public IFacadePhasedState[] getPhasedStates() {
        return phasedStates;
    }
}
