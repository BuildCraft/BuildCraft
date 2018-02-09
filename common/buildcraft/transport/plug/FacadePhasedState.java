package buildcraft.transport.plug;

import javax.annotation.Nullable;

import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;

import buildcraft.api.facades.IFacadePhasedState;
import buildcraft.api.facades.IFacadeState;

import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.net.PacketBufferBC;

public class FacadePhasedState implements IFacadePhasedState {
    public final FacadeBlockStateInfo stateInfo;

    @Nullable
    public final EnumDyeColor activeColour;

    public FacadePhasedState(FacadeBlockStateInfo stateInfo, EnumDyeColor activeColour) {
        this.stateInfo = stateInfo;
        this.activeColour = activeColour;
    }

    public static FacadePhasedState readFromNbt(NBTTagCompound nbt) {
        FacadeBlockStateInfo stateInfo = FacadeStateManager.defaultState;
        if (nbt.hasKey("state")) {
            try {
                IBlockState blockState = NBTUtil.readBlockState(nbt.getCompoundTag("state"));
                stateInfo = FacadeStateManager.validFacadeStates.get(blockState);
                if (stateInfo == null) {
                    stateInfo = FacadeStateManager.defaultState;
                }
            } catch (Throwable t) {
                throw new RuntimeException("Failed badly when reading a facade state!", t);
            }
        }
        EnumDyeColor colour = NBTUtilBC.readEnum(nbt.getTag("activeColour"), EnumDyeColor.class);
        return new FacadePhasedState(stateInfo, colour);
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = new NBTTagCompound();
        try {
            nbt.setTag("state", NBTUtil.writeBlockState(new NBTTagCompound(), stateInfo.state));
        } catch (Throwable t) {
            throw new IllegalStateException("Writing facade block state"//
                + "\n\tState = " + stateInfo//
                + "\n\tBlock = " + stateInfo.state.getBlock() + "\n\tBlock Class = "
                + stateInfo.state.getBlock().getClass(), t);
        }
        if (activeColour != null) {
            nbt.setTag("activeColour", NBTUtilBC.writeEnum(activeColour));
        }
        return nbt;
    }

    public static FacadePhasedState readFromBuffer(PacketBufferBC buf) {
        IBlockState state = MessageUtil.readBlockState(buf);
        EnumDyeColor colour = MessageUtil.readEnumOrNull(buf, EnumDyeColor.class);
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

    public FacadePhasedState withColour(EnumDyeColor colour) {
        return new FacadePhasedState(stateInfo, colour);
    }

    public boolean isSideSolid(EnumFacing side) {
        return stateInfo.isSideSolid[side.ordinal()];
    }

    public BlockFaceShape getBlockFaceShape(EnumFacing side) {
        return stateInfo.blockFaceShape[side.ordinal()];
    }

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
    public EnumDyeColor getActiveColor() {
        return activeColour;
    }
}
