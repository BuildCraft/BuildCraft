package buildcraft.transport.plug;

import java.util.Arrays;

import javax.annotation.Nullable;

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

    public FacadeInstance(FacadePhasedState[] phasedStates) {
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
    }

    public static FacadeInstance createSingle(FacadeBlockStateInfo info, boolean isHollow) {
        return new FacadeInstance(new FacadePhasedState[] { new FacadePhasedState(info, isHollow, null) });
    }

    public static FacadeInstance readFromNbt(NBTTagCompound nbt, String subTag) {
        NBTTagList list = nbt.getTagList(subTag, Constants.NBT.TAG_COMPOUND);
        if (list.hasNoTags()) {
            return FacadeInstance.createSingle(FacadeStateManager.defaultState, false);
        }
        FacadePhasedState[] states = new FacadePhasedState[list.tagCount()];
        for (int i = 0; i < list.tagCount(); i++) {
            states[i] = FacadePhasedState.readFromNbt(list.getCompoundTagAt(i));
        }
        return new FacadeInstance(states);
    }

    public void writeToNbt(NBTTagCompound nbt, String subTag) {
        NBTTagList list = new NBTTagList();
        for (FacadePhasedState state : phasedStates) {
            list.appendTag(state.writeToNbt());
        }
        nbt.setTag(subTag, list);
    }

    public static FacadeInstance readFromBuffer(PacketBufferBC buf) {
        int count = buf.readFixedBits(5);
        FacadePhasedState[] states = new FacadePhasedState[count];
        for (int i = 0; i < count; i++) {
            states[i] = FacadePhasedState.readFromBuffer(buf);
        }
        return new FacadeInstance(states);
    }

    public void writeToBuffer(PacketBufferBC buf) {
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
            return new FacadeInstance(newStates);
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
        FacadePhasedState[] newStates = Arrays.copyOf(phasedStates, phasedStates.length);
        for (int i = 0; i < newStates.length; i++) {
            newStates[i] = newStates[i].withSwappedIsHollow();
        }
        return new FacadeInstance(newStates);
    }

    public boolean areAllStatesSolid(EnumFacing side) {
        for (FacadePhasedState state : phasedStates) {
            if (!state.isSideSolid(side)) {
                return false;
            }
        }
        return true;
    }

    // IFacade

    @Override
    public FacadeType getType() {
        return type;
    }

    @Override
    public IFacadePhasedState[] getPhasedStates() {
        return phasedStates;
    }
}
