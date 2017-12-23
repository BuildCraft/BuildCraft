package buildcraft.lib.tile.craft;

import net.minecraft.item.ItemStack;

import buildcraft.lib.tile.item.ItemHandlerSimple;

/** Used by compat to provide information aboue the current recipe that is being made in an auto-crafter. */
public interface IAutoCraft {
    ItemStack getCurrentRecipeOutput();

    ItemHandlerSimple getInvBlueprint();
}
