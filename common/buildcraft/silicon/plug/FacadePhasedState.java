package buildcraft.silicon.plug;

import buildcraft.api.facades.IFacadePhasedState;
import buildcraft.api.facades.IFacadeState;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.net.PacketBufferBC;
import net.minecraft.block.BlockState;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;

public class FacadePhasedState implements IFacadePhasedState {
    public final FacadeBlockStateInfo stateInfo;

    @Nullable
    public final DyeColor activeColour;

    public FacadePhasedState(FacadeBlockStateInfo stateInfo, DyeColor activeColour) {
        this.stateInfo = stateInfo;
        this.activeColour = activeColour;
    }

    public static FacadePhasedState readFromNbt(CompoundNBT nbt) {
        FacadeBlockStateInfo stateInfo = FacadeStateManager.defaultState;
        if (nbt.contains("state")) {
            try {
                BlockState blockState = NBTUtil.readBlockState(nbt.getCompound("state"));
                stateInfo = FacadeStateManager.validFacadeStates.get(blockState);
                if (stateInfo == null) {
                    stateInfo = FacadeStateManager.defaultState;
                }
            } catch (Throwable t) {
                throw new RuntimeException("Failed badly when reading a facade state!", t);
            }
        }
        DyeColor colour = NBTUtilBC.readEnum(nbt.get("activeColour"), DyeColor.class);
        return new FacadePhasedState(stateInfo, colour);
    }

    public CompoundNBT writeToNbt() {
        CompoundNBT nbt = new CompoundNBT();
        try {
            nbt.put("state", NBTUtil.writeBlockState(stateInfo.state));
        } catch (Throwable t) {
            throw new IllegalStateException("Writing facade block state"//
                    + "\n\tState = " + stateInfo//
                    + "\n\tBlock = " + stateInfo.state.getBlock() + "\n\tBlock Class = "
                    + stateInfo.state.getBlock().getClass(), t);
        }
        if (activeColour != null) {
            nbt.put("activeColour", NBTUtilBC.writeEnum(activeColour));
        }
        return nbt;
    }

    public static FacadePhasedState readFromBuffer(PacketBufferBC buf) {
        BlockState state = MessageUtil.readBlockState(buf);
        DyeColor colour = MessageUtil.readEnumOrNull(buf, DyeColor.class);
        FacadeBlockStateInfo info = FacadeStateManager.validFacadeStates.get(state);
        if (info == null) {
            info = FacadeStateManager.defaultState;
        }
        return new FacadePhasedState(info, colour);
    }

    public void writeToBuffer(PacketBufferBC buf) {
        try {
            MessageUtil.writeBlockState(buf, stateInfo.state);
        } catch (Throwable t) {
            throw new IllegalStateException("Writing facade block state\n\tState = " + stateInfo.state, t);
        }
        MessageUtil.writeEnumOrNull(buf, activeColour);
    }

    public FacadePhasedState withColour(DyeColor colour) {
        return new FacadePhasedState(stateInfo, colour);
    }

    public boolean isSideSolid(Direction side) {
        return stateInfo.isSideSolid[side.ordinal()];
    }

//    public BlockFaceShape getBlockFaceShape(EnumFacing side) {
//        return stateInfo.blockFaceShape[side.ordinal()];
//    }

    @Override
    public String toString() {
        return (activeColour == null ? "" : activeColour + " ") + getState();
    }

    // IFacadePhasedState

    @Override
    public IFacadeState getState() {
        return stateInfo;
    }

    @Override
    public DyeColor getActiveColor() {
        return activeColour;
    }
}
