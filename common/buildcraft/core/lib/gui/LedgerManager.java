package buildcraft.core.lib.gui;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import buildcraft.core.lib.utils.SessionVars;

public class LedgerManager {
    protected List<Ledger> ledgers = new ArrayList<>();
    private final GuiBuildCraft gui;

    public LedgerManager(GuiBuildCraft gui) {
        this.gui = gui;
    }

    public void add(Ledger ledger) {
        this.ledgers.add(ledger);
        if (SessionVars.getOpenedLedger() != null && ledger.getClass().equals(SessionVars.getOpenedLedger())) {
            ledger.setFullyOpen();
        }
    }

    public ImmutableList<Ledger> getAll() {
        return ImmutableList.copyOf(ledgers);
    }

    /** Inserts a ledger into the next-to-last position.
     *
     * @param ledger */
    public void insert(Ledger ledger) {
        this.ledgers.add(ledgers.size() - 1, ledger);
    }

    protected Ledger getAtPosition(int mX, int mY) {

        int xShift = ((gui.width - gui.xSize()) / 2) + gui.xSize();
        int yShift = ((gui.height - gui.ySize()) / 2) + 8;

        for (Ledger ledger : ledgers) {
            if (!ledger.isVisible()) {
                continue;
            }

            ledger.currentShiftX = xShift;
            ledger.currentShiftY = yShift;
            if (ledger.intersectsWith(mX, mY, xShift, yShift)) {
                return ledger;
            }

            yShift += ledger.getHeight();
        }

        return null;
    }

    protected void drawLedgers(int mouseX, int mouseY) {
        int yPos = 8;
        for (Ledger ledger : ledgers) {

            ledger.update();
            if (!ledger.isVisible()) {
                continue;
            }

            ledger.draw(gui.xSize(), yPos);
            yPos += ledger.getHeight();
        }

        Ledger ledger = getAtPosition(mouseX, mouseY);
        if (ledger != null) {
            int startX = mouseX - ((gui.width - gui.xSize()) / 2) + 12;
            int startY = mouseY - ((gui.height - gui.ySize()) / 2) - 12;

            String tooltip = ledger.getTooltip();
            int textWidth = gui.getFontRenderer().getStringWidth(tooltip);
            gui.drawGradientRect(startX - 3, startY - 3, startX + textWidth + 3, startY + 8 + 3, 0xc0000000, 0xc0000000);
            gui.getFontRenderer().drawStringWithShadow(tooltip, startX, startY, -1);
        }
    }

    public void handleMouseClicked(int x, int y, int mouseButton) {

        if (mouseButton == 0) {

            Ledger ledger = this.getAtPosition(x, y);

            // Default action only if the mouse click was not handled by the
            // ledger itself.
            if (ledger != null && !ledger.handleMouseClicked(x, y, mouseButton)) {

                for (Ledger other : ledgers) {
                    if (other != ledger && other.isOpen()) {
                        other.toggleOpen();
                    }
                }
                ledger.toggleOpen();
            }
        }

    }
}
