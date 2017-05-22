package buildcraft.lib.gui.ledger;

import com.mojang.authlib.GameProfile;

import buildcraft.api.core.render.ISprite;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.misc.SpriteUtil;

public class LedgerOwnership extends Ledger_Neptune {
    private final ContainerBCTile<?> container;

    public LedgerOwnership(LedgerManager_Neptune manager, ContainerBCTile<?> container) {
        super(manager);
        this.container = container;
        this.title = "gui.ledger.ownership";

        appendText(this::getOwnerName, 0);

        calculateMaxSize();
    }

    @Override
    public int getColour() {
        return 0xFF_E0_F0_FF;
    }

    @Override
    protected void drawIcon(int x, int y) {
        ISprite sprite = SpriteUtil.getFaceSprite(container.tile.getOwner());
        GuiIcon.draw(sprite, x, y, x + 16, y + 16);
    }

    private String getOwnerName() {
        GameProfile owner = container.tile.getOwner();
        if (owner == null) {
            return "no-one";
        }
        return owner.getName();
    }
}
