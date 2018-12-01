package buildcraft.lib.client.guide.parts.contents;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import net.minecraft.util.text.TextFormatting;

import buildcraft.api.statements.IStatement;

import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.entry.PageEntryStatement;
import buildcraft.lib.client.guide.entry.PageValue;
import buildcraft.lib.client.guide.parts.GuidePage;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.statement.GuiElementStatementSource;

public class PageLinkStatement extends PageLink {

    public final IStatement statement;
    public final List<String> tooltip;
    public final String searchText;

    public PageLinkStatement(boolean startVisible, IStatement statement) {
        super(createPageLine(statement), startVisible);
        this.statement = statement;
        List<String> tip = statement.getTooltip();
        if (tip.isEmpty()) {
            String uniqueTag = statement.getUniqueTag();
            this.tooltip = ImmutableList.of(uniqueTag);
            this.searchText = uniqueTag.toLowerCase(Locale.ROOT);
        } else {
            this.tooltip = tip;
            String joinedTooltip = tip.stream().collect(Collectors.joining(" ", "", ""));
            this.searchText = TextFormatting.getTextWithoutFormattingCodes(joinedTooltip).toLowerCase(Locale.ROOT);
        }
    }

    private static PageLine createPageLine(IStatement statement) {
        ISimpleDrawable icon = (x, y) -> GuiElementStatementSource.drawGuiSlot(statement, x, y);

        List<String> tooltip = statement.getTooltip();
        String title = tooltip.isEmpty() ? statement.getUniqueTag() : tooltip.get(0);
        return new PageLine(icon, icon, 2, title, true);
    }

    @Override
    public String getSearchName() {
        return searchText;
    }

    @Override
    public List<String> getTooltip() {
        return tooltip.size() == 1 ? null : tooltip;
    }

    @Override
    public GuidePageFactory getFactoryLink() {
        // TODO: Populate this with useful information!
        return g -> new GuidePage(g, ImmutableList.of(), new PageValue<>(PageEntryStatement.INSTANCE, statement));
    }
}
