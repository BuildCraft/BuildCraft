package buildcraft.lib.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;

public abstract class GuiBC8<C extends ContainerBC8> extends GuiContainer {
    public final C container;
    public final MousePosition mouse = new MousePosition();
    public final IPositionedElement rootElement = new IPositionedElement() {
        @Override
        public int getX() {
            return GuiBC8.this.guiLeft;
        }

        @Override
        public int getY() {
            return GuiBC8.this.guiTop;
        }

        @Override
        public int getWidth() {
            return GuiBC8.this.width;
        }

        @Override
        public int getHeight() {
            return GuiBC8.this.height;
        }
    };

    protected final List<IGuiElement> guiElements = new ArrayList<>();
    private final GuiElementToolTips tooltips = new GuiElementToolTips(this);

    public GuiBC8(C container) {
        super(container);
        this.container = container;
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

    // Other

    public Stream<IGuiElement> getElementAt(int x, int y) {
        return guiElements.stream().filter(elem -> elem.contains(x, y));
    }

    @Override
    protected final void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mouse.setMousePosition(mouseX, mouseY);

        drawBackgroundLayer();

        for (IGuiElement element : guiElements) {
            element.drawBackground();
        }
    }

    @Override
    protected final void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        GlStateManager.translate(-guiLeft, -guiTop, 0);
        mouse.setMousePosition(mouseX, mouseY);

        drawForegroundLayer();

        for (IGuiElement element : guiElements) {
            element.drawForeground();
        }

        tooltips.drawForeground();

        GlStateManager.translate(guiLeft, guiTop, 0);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        mouse.setMousePosition(mouseX, mouseY);

        for (IGuiElement element : guiElements) {
            element.onMouseClicked(mouseButton);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        mouse.setMousePosition(mouseX, mouseY);

        for (IGuiElement element : guiElements) {
            element.onMouseDragged(clickedMouseButton, timeSinceLastClick);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        mouse.setMousePosition(mouseX, mouseY);

        for (IGuiElement element : guiElements) {
            element.onMouseReleased(state);
        }
    }

    protected void drawBackgroundLayer() {}

    protected void drawForegroundLayer() {}
}
