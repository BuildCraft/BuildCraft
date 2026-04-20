/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package ct.buildcraft.api.statements;

/** Designates some sort of statement. Most of the time you should implement {@link ITriggerExternal},
 * {@link ITriggerInternal}, {@link IActionExternal} or {@link IActionInternal} though. */
public interface IStatement extends IGuiSlot {

    /** Return the maximum number of parameter this statement can have, 0 if none. */
    int maxParameters();

    /** Return the minimum number of parameter this statement can have, 0 if none. */
    int minParameters();

    /** Create parameters for the statement. */
    IStatementParameter createParameter(int index);

    /** Creates a parameter for the given index, optionally returning the old param if it is still valid. By default
     * this checks the classes of the old and new parameters, however it is sensible to override this check in case the
     * parameters given no longer match. For example if you return {@link StatementParameterItemStack} from
     * {@link #createParameter(int)} and require the stack to match a filter, but the incoming stack might not.
     * 
     * @param old
     * @param index
     * @return */
    default IStatementParameter createParameter(IStatementParameter old, int index) {
        IStatementParameter _new = createParameter(index);
        if (old == null || _new == null) {
            return _new;
        } else if (old.getClass() == _new.getClass()) {
            return old;
        }
        return _new;
    }

    /** This returns the statement after a left rotation. Used in particular in blueprints orientation. */
    IStatement rotateLeft();

    /** This returns a group of related statements. For example "redstone signal input" should probably return an array
     * of "RS_SIGNAL_ON" and "RS_SIGNAL_OFF". It is recommended to return an array containing this object. */
    IStatement[] getPossible();

    default boolean isPossibleOrdered() {
        return false;
    }
}
