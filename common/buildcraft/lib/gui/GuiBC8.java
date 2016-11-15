package buildcraft.lib.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import buildcraft.lib.gui.ledger.LedgerManager_Neptune;
import buildcraft.lib.gui.ledger.LedgerOwnership;
import buildcraft.lib.gui.pos.IPositionedElement;
import buildcraft.lib.gui.pos.MousePosition;
import buildcraft.lib.gui.pos.PositionCallable;

public abstract class GuiBC8<C extends ContainerBC_Neptune> extends GuiContainer {
    public final C container;
    public final MousePosition mouse = new MousePosition();
    public final RootPosition rootElement = new RootPosition(this);

    protected final List<IGuiElement> guiElements = new ArrayList<>();
    protected final LedgerManager_Neptune ledgersLeft, ledgersRight;
    private final GuiElementToolTips tooltips = new GuiElementToolTips(this);
    private float lastPartialTicks;

    public GuiBC8(C container) {
        super(container);
        this.container = container;
        ledgersLeft = new LedgerManager_Neptune(this, rootElement.offset(0, 5), false);
        IPositionedElement rightPos = rootElement.offset(new PositionCallable(rootElement::getWidth, 5));
        ledgersRight = new LedgerManager_Neptune(this, rightPos, true);

        if (container instanceof ContainerBCTile<?>) {
            ledgersRight.ledgers.add(new LedgerOwnership(ledgersRight, (ContainerBCTile<?>) container));
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        guiElements.clear();
    }

    // Protected -> Public

    @Override
    public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        super.drawGradientRect(left, top, right, bottom, startColor, endColor);
    }

    public List<GuiButton> getButtonList() {
        return buttonList;
    }

    // Other

    public Stream<IGuiElement> getElementAt(int x, int y) {
        return guiElements.stream().filter(elem -> elem.contains(x, y));
    }

    public void drawItemStackAt(ItemStack stack, int x, int y) {
        RenderHelper.enableGUIStandardItemLighting();
        itemRender.renderItemAndEffectIntoGUI(mc.thePlayer, stack, x, y);
        itemRender.renderItemOverlayIntoGUI(mc.fontRendererObj, stack, x, y, null);
        RenderHelper.disableStandardItemLighting();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        ledgersLeft.update();
        ledgersRight.update();
    }

    @Override
    protected final void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        RenderHelper.disableStandardItemLighting();
        this.lastPartialTicks = partialTicks;
        mouse.setMousePosition(mouseX, mouseY);

        drawBackgroundLayer(partialTicks);

        ledgersLeft.drawBackground(partialTicks);
        ledgersRight.drawBackground(partialTicks);

        for (IGuiElement element : guiElements) {
            element.drawBackground(partialTicks);
        }
    }

    @Override
    protected final void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        GlStateManager.translate(-guiLeft, -guiTop, 0);
        mouse.setMousePosition(mouseX, mouseY);

        drawForegroundLayer();

        ledgersLeft.drawForeground(lastPartialTicks);
        ledgersRight.drawForeground(lastPartialTicks);

        for (IGuiElement element : guiElements) {
            element.drawForeground(lastPartialTicks);
        }

        tooltips.drawForeground(lastPartialTicks);

        GlStateManager.translate(guiLeft, guiTop, 0);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        mouse.setMousePosition(mouseX, mouseY);

        for (IGuiElement element : guiElements) {
            element.onMouseClicked(mouseButton);
        }

        ledgersLeft.onMouseClicked(mouseButton);
        ledgersRight.onMouseClicked(mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        mouse.setMousePosition(mouseX, mouseY);

        for (IGuiElement element : guiElements) {
            element.onMouseDragged(clickedMouseButton, timeSinceLastClick);
        }

        ledgersLeft.onMouseDragged(clickedMouseButton, timeSinceLastClick);
        ledgersRight.onMouseDragged(clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        mouse.setMousePosition(mouseX, mouseY);

        for (IGuiElement element : guiElements) {
            element.onMouseReleased(state);
        }

        ledgersLeft.onMouseReleased(state);
        ledgersRight.onMouseReleased(state);
    }

    protected void drawBackgroundLayer(float partialTicks) {}

    protected void drawForegroundLayer() {}

    public static final class RootPosition implements IPositionedElement {
        public final GuiBC8<?> gui;

        public RootPosition(GuiBC8<?> gui) {
            this.gui = gui;
        }

        @Override
        public int getX() {
            return gui.guiLeft;
        }

        @Override
        public int getY() {
            return gui.guiTop;
        }

        @Override
        public int getWidth() {
            return gui.xSize;
        }

        @Override
        public int getHeight() {
            return gui.ySize;
        }
    }
}
