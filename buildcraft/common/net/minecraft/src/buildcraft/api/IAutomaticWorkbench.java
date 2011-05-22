package net.minecraft.src.buildcraft.api;

import net.minecraft.src.IInventory;

/**
 * To be implemented by TileEntities that behave like automatic workbenches. 
 * Pipes connected putting objects to these will only stack items on top of 
 * already existing stacks. Wooden pipes will extract the result of the recipe
 * when one item has been place on top of each stack.
 */
public interface IAutomaticWorkbench extends IInventory {

}
