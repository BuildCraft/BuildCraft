package ct.buildcraft.api.facades;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public interface IFacadeState {
    boolean isTransparent();

    BlockState getBlockState();

    ItemStack getRequiredStack();
}
