package buildcraft.lib.gui.statement;

import net.minecraft.util.ResourceLocation;

import buildcraft.api.statements.IStatementContainer;

import buildcraft.lib.client.sprite.RawSprite;
import buildcraft.lib.client.sprite.SpriteNineSliced;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;

public abstract class GuiStatementSelector<C extends ContainerBC_Neptune> extends GuiBC8<C> {
    public static final ResourceLocation TEXTURE_SELECTOR = new ResourceLocation("buildcraftlib:textures/gui/statement_selector.png");

    public static final GuiIcon SLOT_COLOUR = new GuiIcon(TEXTURE_SELECTOR, 0, 0, 18, 18);
    public static final GuiIcon ICON_SLOT_BLOCKED = SLOT_COLOUR.offset(18, 0);
    public static final GuiIcon ICON_SLOT_NOT_SET = ICON_SLOT_BLOCKED.offset(18, 0);

    public static final RawSprite ICON_SELECT_HOVER = new RawSprite(TEXTURE_SELECTOR, 18, 18, 36, 36, 256);
    public static final SpriteNineSliced SELECTION_HOVER = new SpriteNineSliced(ICON_SELECT_HOVER, 8, 8, 28, 28, 36);

    public ElementGuiSlot<?, ?> currentHover = null;
    public StatementWrapper draggingElement;

    public GuiStatementSelector(C container) {
        super(container);
    }

    public abstract IStatementContainer getStatementContainer();
}
