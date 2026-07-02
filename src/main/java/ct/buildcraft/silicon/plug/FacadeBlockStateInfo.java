package ct.buildcraft.silicon.plug;

import java.util.Objects;

import com.google.common.collect.ImmutableSet;

import ct.buildcraft.api.facades.IFacadeState;
import ct.buildcraft.lib.world.SingleBlockAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;

public class FacadeBlockStateInfo implements IFacadeState {
    public final BlockState state;
    public final ItemStack requiredStack;
    public final ImmutableSet<Property<?>> varyingProperties;
    public final boolean isTransparent;
    public final boolean isVisible;
    public final boolean[] isSideSolid = new boolean[6];
//    public final BlockFaceShape[] blockFaceShape = new BlockFaceShape[6];

    public FacadeBlockStateInfo(BlockState state, ItemStack requiredStack,
        ImmutableSet<Property<?>> varyingProperties) {
        this.state = Objects.requireNonNull(state, "state must not be null!");
        Objects.requireNonNull(state.getBlock(), "state.getBlock must not be null!");
        Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(state.getBlock()),
            "ForgeRegistries.BLOCKS.getKey(state.getBlock() must not be null!");
        BlockGetter access = new SingleBlockAccess(state);
        this.requiredStack = requiredStack;
        this.varyingProperties = varyingProperties;
        this.isTransparent = !state.propagatesSkylightDown(access, BlockPos.ZERO);
        this.isVisible = !requiredStack.isEmpty();
        for (Direction side : Direction.values()) {
            isSideSolid[side.ordinal()] = state.isFaceSturdy(access, BlockPos.ZERO, side);
//            blockFaceShape[side.ordinal()] = state.getBlockFaceShape(access, BlockPos.ORIGIN, side);
        }
    }

    // Helper methods

    public FacadePhasedState createPhased(DyeColor activeColour) {
        return new FacadePhasedState(this, activeColour);
    }

    @Override
    public String toString() {
        return "StateInfo [id=" + System.identityHashCode(this) + ", block = " + state.getBlock() + ", state =  "
            + state.toString() + "]";
    }

    // IFacadeState

    @Override
    public BlockState getBlockState() {
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
