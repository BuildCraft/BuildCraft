package ct.buildcraft.lib.tile.craft;

import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.world.item.ItemStack;

/** Used by compat to provide information aboue the current recipe that is being made in an auto-crafter. */
public interface IAutoCraft {
    ItemStack getCurrentRecipeOutput();

    ItemHandlerSimple getInvBlueprint();
}
