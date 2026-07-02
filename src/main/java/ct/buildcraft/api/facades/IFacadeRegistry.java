package ct.buildcraft.api.facades;

import java.util.Collection;

import javax.annotation.Nullable;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface IFacadeRegistry {

    Collection<? extends IFacadeState> getValidFacades();

    IFacadePhasedState createPhasedState(IFacadeState state, @Nullable DyeColor activeColor);

    IFacade createPhasedFacade(IFacadePhasedState[] states, boolean isHollow);

    default void disableBlock(Block block) {
    }

    default void mapStateToStack(BlockState state, ItemStack stack) {
    }

    default IFacade createBasicFacade(IFacadeState state, boolean isHollow) {
        return createPhasedFacade(new IFacadePhasedState[] { createPhasedState(state, null) }, isHollow);
    }
}
