package ct.buildcraft.lib.statement;

import java.util.List;

import javax.annotation.Nullable;

import ct.buildcraft.api.statements.IGuiSlot;
import ct.buildcraft.lib.gui.ISimpleDrawable;

/** Provides a set of possible {@link IGuiSlot}'s for showing in GUI's as possible values, to be dragged into waiting
 * statement slots. */
public interface StatementContext<S extends IGuiSlot> {
    /** @return A separated list of all the possible values. The lists should be separated into groups, which identify
     *         where the statement came from. If groups return null from {@link StatementGroup#getSourceIcon()} then
     *         they won't be drawn with an icon. */
    List<? extends StatementGroup<S>> getAllPossible();

    public interface StatementGroup<S extends IGuiSlot> {
        List<S> getValues();

        /** @return Something that can be drawn to identify what this is, or null if nothing exists that could identify
         *         a source. */
        @Nullable
        ISimpleDrawable getSourceIcon();

        /** @return The colour for the ledger (Must include alpha), or 0 if this shouldn't have a colour. */
        default int getLedgerColour() {
            return 0;
        }
    }
}
