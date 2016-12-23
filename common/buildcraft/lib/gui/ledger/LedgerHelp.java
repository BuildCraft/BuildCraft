package buildcraft.lib.gui.ledger;

import net.minecraft.client.renderer.GlStateManager;

import buildcraft.lib.client.sprite.ISprite;
import buildcraft.lib.client.sprite.LibSprites;
import buildcraft.lib.client.sprite.SpriteSplit;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.elem.GuiElementContainer;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.RenderUtil;

public class LedgerHelp extends Ledger_Neptune {

    private static final SpriteSplit[][] SPRITE_HELP_SPLIT = new SpriteSplit[2][2];

    static {
        SPRITE_HELP_SPLIT[0][0] = LibSprites.HELP_SPLIT.subRelative(0, 0, 8, 8, 16).split(2, 2, 6, 6, 8);
        SPRITE_HELP_SPLIT[0][1] = LibSprites.HELP_SPLIT.subRelative(0, 8, 8, 8, 16).split(2, 2, 6, 6, 8);
        SPRITE_HELP_SPLIT[1][0] = LibSprites.HELP_SPLIT.subRelative(8, 0, 8, 8, 16).split(2, 2, 6, 6, 8);
        SPRITE_HELP_SPLIT[1][1] = LibSprites.HELP_SPLIT.subRelative(8, 8, 8, 8, 16).split(2, 2, 6, 6, 8);
    }

    private IGuiElement selected = null;
    private boolean foundAny = false, init = false;

    public LedgerHelp(LedgerManager_Neptune manager) {
        super(manager);
        title = LocaleUtil.localize("gui.ledger.help");
        calculateMaxSize();
    }

    @Override
    public void update() {
        super.update();
        if (currentWidth == CLOSED_WIDTH && currentHeight == CLOSED_HEIGHT) {
            selected = null;
            if (openElements.size() == 2) {
                openElements.remove(1);
                title = LocaleUtil.localize("gui.ledger.help");
                calculateMaxSize();
            }
        }
    }

    @Override
    public int getColour() {
        return 0xFF_CC_99_FF;// light blue -- temp
    }

    @Override
    protected void drawIcon(int x, int y) {
        if (!init) {
            init = true;
            for (IGuiElement element : manager.gui.guiElements) {
                ElementHelpInfo info = element.getHelpInfo();
                if (info == null) continue;
                foundAny = true;
                break;
            }
        }
        ISprite sprite = foundAny ? LibSprites.HELP : LibSprites.WARNING_MINOR;
        GuiIcon.draw(sprite, x, y, x + 16, y + 16);
    }

    @Override
    public void drawForeground(int x, int y, float partialTicks) {
        super.drawForeground(x, y, partialTicks);
        if (!shouldDrawOpen()) {
            return;
        }
        boolean set = false;
        for (IGuiElement element : manager.gui.guiElements) {
            ElementHelpInfo info = element.getHelpInfo();
            if (info == null) continue;
            foundAny = true;
            IGuiArea rect = info.position;
            boolean isHovered = rect.contains(manager.gui.mouse);
            if (isHovered) {
                if (selected != element && !set) {
                    selected = element;
                    GuiElementContainer container = new GuiElementContainer(manager.gui, positionLedgerInnerStart);
                    info.addGuiElements(container);
                    if (openElements.size() == 2) {
                        openElements.remove(1);
                    }
                    openElements.add(container);
                    title = LocaleUtil.localize("gui.ledger.help") + ": " + LocaleUtil.localize(info.title);
                    calculateMaxSize();
                    set = true;
                }
            }
            boolean isSelected = selected == element;
            SpriteSplit split = SPRITE_HELP_SPLIT[isHovered ? 1 : 0][isSelected ? 1 : 0];
            RenderUtil.setGLColorFromInt(info.colour);
            split.draw(rect);
        }
        GlStateManager.color(1, 1, 1);
    }
}
