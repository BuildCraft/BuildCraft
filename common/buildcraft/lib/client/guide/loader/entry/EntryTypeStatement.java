package buildcraft.lib.client.guide.loader.entry;

import java.util.List;

import javax.annotation.Nullable;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.StatementManager;

import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.statement.GuiElementStatementSource;

public class EntryTypeStatement extends PageEntryType<IStatement> {
    public static final String ID = "buildcraft:statement";
    public static final EntryTypeStatement INSTANCE = new EntryTypeStatement();

    @Override
    @Nullable
    public IStatement deserialise(String source) {
        return StatementManager.statements.get(source);
    }

    @Override
    public List<String> getTooltip(IStatement value) {
        return value.getTooltip();
    }

    @Override
    @Nullable
    public ISimpleDrawable createDrawable(IStatement value) {
        return (x, y) -> GuiElementStatementSource.drawGuiSlot(value, x, y);
    }
}
