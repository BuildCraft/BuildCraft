package buildcraft.lib.gui.ledger;

import buildcraft.lib.client.sprite.ISprite;
import buildcraft.lib.client.sprite.LibSprites;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.GuiRectangle;
import buildcraft.lib.gui.elem.GuiElementText;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.permission.PlayerOwner;

public class LedgerOwnership extends Ledger_Neptune {
    public static final SpriteHolder OWNER_UNKNOWN = LibSprites.OWNER_UNKNOWN;

    private final ContainerBCTile<?> container;

    public LedgerOwnership(LedgerManager_Neptune manager, ContainerBCTile<?> container) {
        super(manager);
        this.container = container;
        this.title = "gui.ledger.ownership";

        GuiBC8<?> gui = manager.gui;
        GuiRectangle rectangle = new GuiRectangle(0, 0, 0, 0);

        IGuiPosition position = positionLedgerInnerStart.offset(0, 15);
        GuiElementText text = new GuiElementText(gui, position, rectangle, this::getOwnerName, () -> 0, false);
        openElements.add(text);

        calculateMaxSize();
    }

    @Override
    public int getColour() {
        return 0xFF_E0_F0_FF;
    }

    @Override
    protected void drawIcon(int x, int y) {
        final ISprite sprite;
        if (false) {
            sprite = null;// TODO: Use the owning player's face
        } else {
            sprite = OWNER_UNKNOWN;
        }
        GuiIcon.draw(sprite, x, y, x + 16, y + 16);
    }

    private String getOwnerName() {
        PlayerOwner owner = container.tile.getOwner();
        if (owner == null) {
            return "unknown";
        }
        return owner.getOwnerName();
    }
}
