package buildcraft.lib.client.guide.entry;

import java.util.List;
import java.util.TreeMap;

import javax.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import buildcraft.api.registry.IScriptableRegistry.ISimpleEntryDeserializer;
import buildcraft.api.statements.IAction;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.ITrigger;
import buildcraft.api.statements.StatementManager;

import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.data.JsonTypeTags;
import buildcraft.lib.client.guide.parts.contents.PageLinkStatement;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.statement.GuiElementStatementSource;

public class PageEntryStatement extends PageEntry<IStatement> {

    private static final JsonTypeTags TRIGGER_TAGS = new JsonTypeTags("buildcraft.guide.contents.triggers");
    private static final JsonTypeTags ACTION_TAGS = new JsonTypeTags("buildcraft.guide.contents.actions");

    public static final IEntryIterable ITERABLE = PageEntryStatement::iterateAllDefault;
    public static final ISimpleEntryDeserializer<PageEntryStatement> DESERIALISER = PageEntryStatement::deserialize;

    private static void iterateAllDefault(IEntryLinkConsumer consumer) {
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

    private static PageEntryStatement deserialize(ResourceLocation name, JsonObject json,
        JsonDeserializationContext ctx) {
        String stmntName = JsonUtils.getString(json, "statement");
        IStatement stmnt = StatementManager.statements.get(stmntName);
        if (stmnt == null) {
            throw new JsonSyntaxException("Unknown statement '" + stmntName + "'");
        }
        List<String> tooltip = stmnt.getTooltip();
        ITextComponent title;
        if (tooltip.isEmpty()) {
            title = new TextComponentString(stmnt.getClass().toString());
        } else {
            title = new TextComponentString(tooltip.get(0));
        }
        return new PageEntryStatement(name, json, stmnt, title, ctx);
    }

    public PageEntryStatement(ResourceLocation name, JsonObject json, IStatement value, ITextComponent title,
        JsonDeserializationContext ctx) throws JsonParseException {
        super(name, json, value, title, ctx);
    }

    public PageEntryStatement(ResourceLocation name, JsonObject json, IStatement value, JsonDeserializationContext ctx)
        throws JsonParseException {
        super(name, json, value, ctx);
    }

    public PageEntryStatement(JsonTypeTags typeTags, ResourceLocation book, ITextComponent title, IStatement value) {
        super(typeTags, book, title, value);
    }

    @Override
    public List<String> getTooltip() {
        return value.getTooltip();
    }

    @Override
    @Nullable
    public ISimpleDrawable createDrawable() {
        return (x, y) -> GuiElementStatementSource.drawGuiSlot(value, x, y);
    }
}
