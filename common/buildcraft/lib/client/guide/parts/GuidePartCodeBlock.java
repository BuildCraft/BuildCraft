package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.font.IFontRenderer;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class GuidePartCodeBlock extends GuidePart {

    public final List<String> lines;

    public GuidePartCodeBlock(GuiGuide gui, List<String> lines) {
        super(gui);
        this.lines = lines;
    }

    @Override
    public PagePosition renderIntoArea(GuiGraphics guiGraphics, int x, int y, int width, int height, PagePosition current, int index) {
        IFontRenderer font = gui.getCurrentFont();

        List<String> wrappedLines = new ArrayList<>();
        IntList lineNumbers = new IntArrayList();

        int lineNumberWidth = font.getStringWidth(Integer.toString(lines.size() - 1));
        int widthForDecoration = 8 + lineNumberWidth;
        int innerMaxWidth = 0;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            List<String> wrapped = font.wrapString(line, width - widthForDecoration, false, 1);
            wrappedLines.addAll(wrapped);
            for (int j = 0; j < wrapped.size(); j++) {
                lineNumbers.add(j == 0 ? (i + 1) : -1);
                innerMaxWidth = Math.max(innerMaxWidth, font.getStringWidth(wrapped.get(j)));
            }
        }

        int innerHeight = wrappedLines.size() * (font.getMaxFontHeight() + 2);
        int outerHeight = innerHeight + 6;
        current = current.guaranteeSpace(outerHeight, height);
        if (index == current.page) {
            int outerWidth = innerMaxWidth + 8;

            // FIXME: this displays too low! (or maybe text is too high?)
            int _y = y + current.pixel;
            GuiGuide.BOX_CODE_SLICED.draw(guiGraphics, x + lineNumberWidth + 5, _y, outerWidth, outerHeight);
            _y += 4;
            // try (AutoGlScissor scissor = GuiUtil.scissor(x, _y, width, height)) {
            boolean darken = true;
            for (int i = 0; i < wrappedLines.size(); i++) {
                String line = wrappedLines.get(i);
                int number = lineNumbers.get(i);
                if (number != -1) {
                    darken = !darken;
                    if (wrappedLines.size() > 1) {
                        String ns = Integer.toString(number);
                        int addX = lineNumberWidth - font.getStringWidth(ns);
                        font.drawString(guiGraphics, ns, x + 4 + addX, _y, 0);
                    }
                }
                int _x = x + 8 + lineNumberWidth;
                if (darken) {
                    guiGraphics.fill(_x - 2, _y - 1, _x + innerMaxWidth + 4, _y + font.getMaxFontHeight() + 1,
                            0xFF_F0_F0_F0);
                }
                font.drawString(guiGraphics, line, _x, _y, 0);
                _y += font.getMaxFontHeight() + 2;
            }

            // }
        }
        current = current.nextLine(outerHeight, height);
        return current;
    }

    @Override
    public PagePosition handleMouseClick(GuiGraphics guiGraphics, int x, int y, int width, int height, PagePosition current, int index,
            double mouseX, double mouseY) {
        return renderIntoArea(guiGraphics, x, y, width, height, current, -1);
    }
}
