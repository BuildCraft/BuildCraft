package ct.buildcraft.api.statements.containers;

import net.minecraft.core.Direction;

public interface IRedstoneStatementContainer {
    /** Get the redstone input from a given side.
     * 
     * @param side The side - use "null" for maximum input.
     * @return The redstone input, from 0 to 15. */
    int getRedstoneInput(Direction side);

    /** Set the redstone input for a given side.
     * 
     * @param side The side - use "null" for all sides.
     * @return Whether the set was successful. */
    boolean setRedstoneOutput(Direction side, int value);
}
