package buildcraft.lib.gui.ledger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;

import buildcraft.api.core.BCLog;
import buildcraft.api.permission.EnumProtectionStatus;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.GuiRectangle;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.elem.GuiElementDrawable;
import buildcraft.lib.gui.elem.GuiElementText;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.gui.pos.IPositionedElement;
import buildcraft.lib.gui.widget.WidgetOwnership;

public class LedgerOwnership extends Ledger_Neptune {
    private static final String TEXT_SPRITE_START = "buildcraftlib:icons/ownership_";
    public static final SpriteHolder SPRITE_ANYONE;
    public static final SpriteHolder SPRITE_OWNER_ONLY;
    public static final SpriteHolder SPRITE_NO_AUTOMATION;

    static {
        // TODO: Draw these sprites!
        SPRITE_ANYONE = SpriteHolderRegistry.getHolder(TEXT_SPRITE_START + "anyone");
        SPRITE_OWNER_ONLY = SpriteHolderRegistry.getHolder(TEXT_SPRITE_START + "owner_only");
        SPRITE_NO_AUTOMATION = SpriteHolderRegistry.getHolder(TEXT_SPRITE_START + "no_auto");
    }

    public static void fmlPreInitClient() {
        // Just to init the sprites
    }

    private final WidgetOwnership widget;
    private final IPositionedElement clickableArea;
    private String cachedAccess;
    private EnumProtectionStatus cachedStatus = null;

    public LedgerOwnership(LedgerManager_Neptune manager, WidgetOwnership widget) {
        super(manager);
        this.widget = widget;
        this.title = "gui.ledger.ownership";

        GuiBC8<?> gui = manager.gui;
        GuiRectangle rectangle = new GuiRectangle(0, 0, 0, 0);

        IGuiPosition position = positionLedgerInnerStart.offset(0, 15);
        GuiElementText text = new GuiElementText(gui, position, rectangle, this::getOwnerName, () -> 0, false);
        openElements.add(text);

        position = position.offset(0, 12);
        text = new GuiElementText(gui, position, rectangle, I18n.format("gui.ledger.access"), 0xEE_EE_EE, false);
        text.dropShadow = true;
        openElements.add(text);

        position = position.offset(0, 12);
        clickableArea = new GuiRectangle(0, 0, 400, 16).offset(position);
        ISimpleDrawable drawable = this::drawIcon;
        GuiElementDrawable element = new GuiElementDrawable(gui, position, new GuiRectangle(0, 0, 16, 16), drawable, false);
        openElements.add(element);

        position = position.offset(17, 4);
        text = new GuiElementText(gui, position, rectangle, this::getStatusName, () -> 0, false);
        openElements.add(text);

        calculateMaxSize();
    }

    @Override
    protected void calculateMaxSize() {
        super.calculateMaxSize();
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        for (EnumProtectionStatus status : EnumProtectionStatus.VALUES) {
            int width = fr.getStringWidth(getTextForStatus(status));
            maxWidth = Math.max(maxWidth, positionLedgerInnerStart.getX() + 17 + width + LEDGER_GAP * 2);
        }
    }

    @Override
    public int getColour() {
        return 0xFF_E0_F0_FF;
    }

    @Override
    protected void drawIcon(int x, int y) {
        EnumProtectionStatus status = widget.getStatus();
        final SpriteHolder sprite;
        if (status == EnumProtectionStatus.ANYONE) {
            sprite = SPRITE_ANYONE;
        } else if (status == EnumProtectionStatus.OWNER_ONLY) {
            sprite = SPRITE_OWNER_ONLY;
        } else {
            sprite = SPRITE_NO_AUTOMATION;
        }
        GuiIcon.draw(sprite, x, y, x + 16, y + 16);
    }

    private String getOwnerName() {
        return widget.getOwnerName();
    }

    private String getStatusName() {
        EnumProtectionStatus status = widget.getStatus();
        if (status != cachedStatus) {
            BCLog.logger.warn(cachedStatus + " was not equal to the new status " + status);
            cachedStatus = status;
            cachedAccess = getTextForStatus(status);
        }
        return cachedAccess;
    }

    private static String getTextForStatus(EnumProtectionStatus status) {
        final String l;
        if (status == EnumProtectionStatus.ANYONE) {
            l = I18n.format("gui.ledger.access.public");
        } else if (status == EnumProtectionStatus.OWNER_ONLY) {
            l = I18n.format("gui.ledger.access.protected");
        } else {
            l = I18n.format("gui.ledger.access.private");
        }
        return l;
    }

    @Override
    public void onMouseClicked(int button) {
        if (clickableArea.contains(manager.gui.mouse)) {
            if (button == 0) {
                widget.decrementStatus();
            } else if (button == 1) {
                widget.incrementStatus();
            }
        } else {
            super.onMouseClicked(button);
        }
    }
}
