package buildcraft.lib.client.guide.parts;

import java.util.List;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.misc.GuiUtil;
import buildcraft.lib.misc.GuiUtil.AutoGlScissor;

public class GuidePartNote extends GuidePartMulti {

    public static final int WIDTH = 140;
    public static final int HEIGHT = 20;
    public static final int MAX_OPEN_STAGE = 100;

    public final String id;
    private int openStage, lastOpenStage;
    private GuiRectangle thisRect = new GuiRectangle(0, 0, 1, 1);
    private int timeSinceRender = 20;

    public GuidePartNote(GuiGuide gui, String id, List<GuidePart> parts) {
        super(gui, parts, () -> true);
        this.id = id;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        int diff = 10;
        lastOpenStage = openStage;
        if (timeSinceRender < 20) {
            timeSinceRender++;
            if (thisRect.contains(gui.mouse)) {
                openStage += diff;
                if (openStage > MAX_OPEN_STAGE) {
                    openStage = MAX_OPEN_STAGE;
                }
            } else {
                openStage -= diff;
                if (openStage < 0) {
                    openStage = 0;
                }
            }
        }
    }

    @Override
    public PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index) {
        current = current.guaranteeSpace(HEIGHT + 10, height);
        if (current.page == index) {
            timeSinceRender = 0;
            int _x = x + (width - WIDTH) / 2;
            int _y = y + current.pixel + 5;
            int _w = WIDTH;
            int _h = HEIGHT;

            float partialTicks = gui.getLastPartialTicks();
            if (partialTicks < 0) partialTicks = 0;
            if (partialTicks > 1) partialTicks = 1;
            int interpOpenStage = (int) ((openStage * partialTicks) + lastOpenStage * (1 - partialTicks));
            if (openStage == lastOpenStage) {
                interpOpenStage = openStage;
            }

            thisRect = new GuiRectangle(_x, _y - interpOpenStage, _w, _h + interpOpenStage);
            _x = x + (width - GuiGuide.NOTE_PAGE.width) / 2;
            _y = y + current.pixel + 5 - interpOpenStage;
            _w = GuiGuide.NOTE_PAGE.width;
            _h = HEIGHT + interpOpenStage - 5;
            try (AutoGlScissor scissor = GuiUtil.scissor(_x, _y, _w, _h)) {
                GuiGuide.NOTE_PAGE.drawAt(_x, _y);
            }

            _x += 8;
            _y += 4;
            _w -= 16;
            _h -= 4;

            try (AutoGlScissor scissor = GuiUtil.scissor(_x, _y, _w, _h)) {
                PagePosition innerPosition = new PagePosition(index, 4);
                for (GuidePart part : parts) {
                    innerPosition = part.renderIntoArea(_x, _y, _w, 400, innerPosition, index);
                }
            }
        }
        return current.nextLine(HEIGHT, height);
    }

    @Override
    public PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index,
        int mouseX, int mouseY) {
        current = current.guaranteeSpace(HEIGHT, height);
        if (current.page == index) {
            if (thisRect.contains(gui.mouse)) {
                // TODO: Open a seperate note gui
            }
        }
        return current.nextLine(HEIGHT, height);
    }
}
