package buildcraft.transport.plug;

import java.util.Objects;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import buildcraft.api.facades.IFacadeState;

import buildcraft.lib.world.SingleBlockAccess;

public class FacadeBlockStateInfo implements IFacadeState {
    public final IBlockState state;
    public final ItemStack requiredStack;
    public final ImmutableSet<IProperty<?>> varyingProperties;
    public final boolean isTransparent;
    public final boolean isVisible;
    public final boolean[] isSideSolid = new boolean[6];

    public FacadeBlockStateInfo(IBlockState state, ItemStack requiredStack,
        ImmutableSet<IProperty<?>> varyingProperties) {
        this.state = Objects.requireNonNull(state, "state must not be null!");
        Objects.requireNonNull(state.getBlock(), "state.getBlock must not be null!");
        Objects.requireNonNull(state.getBlock().getRegistryName(),
            "state.getBlock.getRegistryName() must not be null!");
        this.requiredStack = requiredStack;
        this.varyingProperties = varyingProperties;
        this.isTransparent = !state.isOpaqueCube();
        this.isVisible = requiredStack != null;
        IBlockAccess access = new SingleBlockAccess(state);
        for (EnumFacing side : EnumFacing.VALUES) {
            isSideSolid[side.ordinal()] = state.isSideSolid(access, BlockPos.ORIGIN, side);

        }
    }

    // Helper methods

    public FacadePhasedState createPhased(boolean isHollow, EnumDyeColor activeColour) {
        return new FacadePhasedState(this, isHollow, activeColour);
    }

    @Override
    public String toString() {
        return "StateInfo [id=" + System.identityHashCode(this) + ", block = " + state.getBlock() + ", state =  "
            + state.toString() + "]";
    }

    // IFacadeState

    @Override
    public IBlockState getBlockState() {
        return state;
    }

    @Override
    public boolean isTransparent() {
        return isTransparent;
    }

    @Override
    public ItemStack getRequiredStack() {
        return requiredStack;
    }
}
