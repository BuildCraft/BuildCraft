package buildcraft.lib.tile.item;

import net.minecraftforge.items.IItemHandler;

/** A form of {@link IItemHandler} that provides insertion-checking functionality via {@link StackInsertionChecker} */
public interface IItemHandlerAdv extends IItemHandler, StackInsertionChecker {}
