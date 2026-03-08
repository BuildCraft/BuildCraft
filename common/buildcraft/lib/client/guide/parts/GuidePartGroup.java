package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.entry.PageValue;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.guide.ref.GuideGroupSet;
import buildcraft.lib.client.guide.ref.GuideGroupSet.GroupDirection;
import buildcraft.lib.gui.ISimpleDrawable;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class GuidePartGroup extends GuidePart {

    public final GuideGroupSet group;
    private final GuideText[] texts;
    private final Object[] values;

    public GuidePartGroup(GuiGuide gui, GuideGroupSet group, GroupDirection direction) {
        super(gui);
        this.group = group;
        List<PageValue<?>> groupValues = group.getValues(direction);
        values = new Object[groupValues.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = groupValues.get(i).value;
        }
        texts = new GuideText[1 + values.length];
//        texts[0] = new GuideText(gui, group.getTitle(direction));
        texts[0] = new GuideText(gui, group.getTitle(direction), ITextComponent.nullToEmpty(group.getTitle(direction)));
        int i = 1;
        for (PageValue<?> single : groupValues) {
            ISimpleDrawable icon = single.createDrawable();
            texts[i++] = new GuideText(gui, new PageLine(icon, icon, 1, single.titleKey, single.title, true, single::getTooltip));
        }
    }

    @Override
    public int hashCode() {
        return group.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        GuidePartGroup other = (GuidePartGroup) obj;
        return group == other.group;
    }

    @Override
    public void setFontRenderer(IFontRenderer fontRenderer) {
        super.setFontRenderer(fontRenderer);
        for (GuideText text : texts) {
            text.setFontRenderer(fontRenderer);
        }
    }

    @Override
    public PagePosition renderIntoArea(MatrixStack poseStack, int x, int y, int width, int height, PagePosition current, int index) {
        current = current.guaranteeSpace(getFontRenderer().getMaxFontHeight() * 4, height);
        for (GuideText text : texts) {
            current = text.renderIntoArea(poseStack, x, y, width, height, current, index);
        }
        return current;
    }

    @Override
    public PagePosition handleMouseClick(MatrixStack poseStack, int x, int y, int width, int height, PagePosition current, int index,
                                         double mouseX, double mouseY) {
        current = current.guaranteeSpace(getFontRenderer().getMaxFontHeight() * 4, height);
        for (int i = 0; i < texts.length; i++) {
            GuideText text = texts[i];
            current = text.handleMouseClick(poseStack, x, y, width, height, current, index, mouseX, mouseY);
            if (text.wasHovered && current.page == index && i > 0) {
                Object value = values[i - 1];
                GuidePageFactory factory = GuideManager.INSTANCE.getFactoryFor(value);
                if (factory != null) {
                    gui.openPage(factory.createNew(gui));
                    return new PagePosition(Integer.MAX_VALUE / 4, 0);
                }
            }
        }
        return current;
    }
}
