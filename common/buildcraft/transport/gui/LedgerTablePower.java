package buildcraft.transport.gui;

import buildcraft.api.core.render.ISprite;

import buildcraft.lib.BCLibSprites;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.ledger.LedgerManager_Neptune;
import buildcraft.lib.gui.ledger.Ledger_Neptune;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.silicon.tile.TileAssemblyTable;
import buildcraft.silicon.tile.TileLaserTableBase;

public class LedgerTablePower extends Ledger_Neptune {
    private static final int OVERLAY_COLOUR = 0xFF_D4_6C_1F;// 0xFF_FF_55_11;// TEMP!
    private static final int HEADER_COLOUR = 0xFF_E1_C9_2F;
    private static final int SUB_HEADER_COLOUR = 0xFF_AA_AF_b8;
    private static final int TEXT_COLOUR = 0xFF_00_00_00;

    public final TileLaserTableBase tile;

    public LedgerTablePower(LedgerManager_Neptune manager, TileAssemblyTable tile) {
        super(manager);
        this.tile = tile;
        this.title = "gui.power";

        appendText(LocaleUtil.localize("gui.assemblyCurrentRequired") + ":", SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(() -> LocaleUtil.localizeMj(tile.getTarget()), TEXT_COLOUR);
        appendText(LocaleUtil.localize("gui.stored") + ":", SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(() -> LocaleUtil.localizeMj(tile.power), TEXT_COLOUR);
        appendText(LocaleUtil.localize("gui.assemblyRate") + ":", SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(() -> LocaleUtil.localizeMjFlow(tile.avgPowerClient), TEXT_COLOUR);
        calculateMaxSize();
    }

    @Override
    public int getColour() {
        return OVERLAY_COLOUR;
    }

    @Override
    public int getTitleColour() {
        return HEADER_COLOUR;
    }

    @Override
    protected void drawIcon(int x, int y) {
        ISprite sprite = tile.avgPowerClient > 0 ? BCLibSprites.ENGINE_ACTIVE : BCLibSprites.ENGINE_INACTIVE;
        GuiIcon.draw(sprite, x, y, x + 16, y + 16);
    }
}
