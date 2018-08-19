package buildcraft.lib.client.guide.loader.entry;

import java.util.List;
import java.util.TreeMap;

import javax.annotation.Nullable;

import buildcraft.api.statements.IAction;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.ITrigger;
import buildcraft.api.statements.StatementManager;

import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.data.JsonTypeTags;
import buildcraft.lib.client.guide.parts.contents.PageLinkStatement;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.statement.GuiElementStatementSource;

public class EntryTypeStatement extends PageEntryType<IStatement> {
    public static final String ID = "buildcraft:statement";
    public static final EntryTypeStatement INSTANCE = new EntryTypeStatement();

    private static final JsonTypeTags TRIGGER_TAGS = new JsonTypeTags("buildcraft.guide.contents.triggers");
    private static final JsonTypeTags ACTION_TAGS = new JsonTypeTags("buildcraft.guide.contents.actions");

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

    @Override
    public void iterateAllDefault(IEntryLinkConsumer consumer) {
        for (IStatement statement : new TreeMap<>(StatementManager.statements).values()) {
            if (!GuideManager.INSTANCE.objectsAdded.add(statement)) {
                continue;
            }

            final JsonTypeTags parent;

            if (statement instanceof ITrigger) {
                parent = TRIGGER_TAGS;
            } else if (statement instanceof IAction) {
                parent = ACTION_TAGS;
            } else {
                continue;
            }

            consumer.addChild(parent, new PageLinkStatement(false, statement));
        }
    }
}
