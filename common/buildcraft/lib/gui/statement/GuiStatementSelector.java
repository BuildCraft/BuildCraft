package buildcraft.lib.gui.statement;

import java.io.IOException;
import java.util.List;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.statements.IStatementContainer;

import buildcraft.lib.client.sprite.SpriteNineSliced;
import buildcraft.lib.client.sprite.SpriteRaw;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.ITooltipElement;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.statement.StatementWrapper;

@Deprecated
public abstract class GuiStatementSelector<C extends ContainerBC_Neptune> extends GuiBC8<C> implements ITooltipElement {
    public static final ResourceLocation TEXTURE_SELECTOR =
        new ResourceLocation("buildcraftlib:textures/gui/statement_selector.png");

    public static final int GUI_WIDTH = 176;

    public static final GuiIcon SLOT_COLOUR = new GuiIcon(TEXTURE_SELECTOR, 0, 0, 18, 18);
    public static final GuiIcon ICON_SLOT_BLOCKED = SLOT_COLOUR.offset(18, 0);
    public static final GuiIcon ICON_SLOT_NOT_SET = ICON_SLOT_BLOCKED.offset(18, 0);

    public static final SpriteRaw ICON_SELECT_HOVER = new SpriteRaw(TEXTURE_SELECTOR, 18, 18, 36, 36, 256);
    public static final SpriteNineSliced SELECTION_HOVER = new SpriteNineSliced(ICON_SELECT_HOVER, 8, 8, 28, 28, 36);

    public ElementGuiSlot<?, ?> currentHover = null;
    public boolean isDragging;
    public StatementWrapper draggingElement;

    public GuiStatementSelector(C container) {
        super(container);
    }

    public abstract IStatementContainer getStatementContainer();

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        iteratePossible((wrapper, pos) -> {
            ElementStatement.draw(this, wrapper, pos);
            if (currentHover != null) {
                drawGradientRect(pos.getX(), pos.getY(), pos.getX() + 18, pos.getY() + 18, 0x55_00_00_00,
                    0x55_00_00_00);
            }
        });
    }

    @Override
    protected void drawForegroundLayer() {
        if (currentHover != null) {
            GlStateManager.disableDepth();
            drawGradientRect(rootElement.getX(), rootElement.getY(), rootElement.getX() + GUI_WIDTH,
                rootElement.getY() + ySize, 0x55_00_00_00, 0x55_00_00_00);
            GlStateManager.enableDepth();
        }

        if (isDragging) {
            ElementStatement.draw(this, draggingElement, mouse.offset(-9, -9));
        }
    }

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        if (currentHover != null) {
            return;
        }

        iteratePossible((wrapper, pos) -> {
            if (pos.contains(mouse) && wrapper != null) {
                String[] arr = { wrapper.getDescription() };
                EnumFacing face = wrapper.sourcePart.face;
                if (face != null) {
                    String translated = ColourUtil.getTextFullTooltip(face);
                    translated = LocaleUtil.localize("gate.side", translated);
                    arr = new String[] { arr[0], translated };
                }
                tooltips.add(new ToolTip(arr));
            }
        });
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        // Test for dragging statements from the side contexts
        iteratePossible((wrapper, pos) -> {
            if (pos.contains(mouse)) {
                draggingElement = wrapper;
                isDragging = true;
            }
        });
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        // Test for dragging statements from the side contexts
        if (isDragging) {
            for (IGuiElement elem : shownElements) {
                if (elem instanceof ElementStatement<?, ?>) {
                    ElementStatement<?, ?> element = (ElementStatement<?, ?>) elem;
                    if (element.contains(mouse)) {
                        element.reference.setIfCan(draggingElement);
                        break;
                    }
                }
            }
            draggingElement = null;
            isDragging = false;
        }
    }

    protected interface OnStatement {
        void iterate(StatementWrapper wrapper, IGuiArea pos);
    }

    protected abstract void iteratePossible(OnStatement iterator);
}
