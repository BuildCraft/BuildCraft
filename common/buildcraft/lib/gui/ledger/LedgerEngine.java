package buildcraft.lib.gui.ledger;

import buildcraft.lib.client.sprite.LibSprites;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;

public class LedgerEngine extends Ledger_Neptune {
    public static final SpriteHolder SPRITE_POWER = LibSprites.LOCK;// temp!

    public LedgerEngine(LedgerManager_Neptune manager, GuiBC8<? extends ContainerBCTile<? extends TileEngineBase_BC8>> gui) {
        super(manager);
    }

    @Override
    public int getColour() {
        return 0xFF_FF_55_11;// TEMP!
    }

    @Override
    protected void drawIcon(int x, int y) {
        boolean isOverheat = currentWidth == maxWidth;
        boolean isWarm = currentWidth != CLOSED_WIDTH;
        GuiIcon.draw(isOverheat ? LibSprites.WARNING_MAJOR : isWarm ? LibSprites.WARNING_MINOR : SPRITE_POWER, x, y, x + 16, y + 16);
    }
}
