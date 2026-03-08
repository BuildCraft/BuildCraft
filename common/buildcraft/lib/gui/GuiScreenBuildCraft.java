package buildcraft.lib.gui;

import buildcraft.lib.gui.json.BuildCraftJsonGui;
import buildcraft.lib.gui.ledger.LedgerHelp;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.GuiUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Function;

/** Reference implementation for a gui that delegates to a {@link BuildCraftGui} for most of its functionality. */
// public class GuiScreenBuildCraft extends GuiScreen
public class GuiScreenBuildCraft<C extends ContainerBC_Neptune<?>> extends Screen {

    public final BuildCraftGui mainGui;

    /** Creates a new {@link GuiScreenBuildCraft} that will occupy the entire screen. */
    public GuiScreenBuildCraft(C container, PlayerInventory inventory, ITextComponent component) {
        this(g -> new BuildCraftGui(g), component);
    }

    /** Creates a new {@link GuiScreenBuildCraft} that will occupy the given {@link IGuiArea} Call
     * {@link GuiUtil#moveAreaToCentre(IGuiArea)} if you want a centred gui. (Ignoring ledgers, which will display off
     * to the side) */
    public GuiScreenBuildCraft(IGuiArea area, ITextComponent component) {
        this(g -> new BuildCraftGui(g, area), component);
    }

    public GuiScreenBuildCraft(Function<GuiScreenBuildCraft<?>, BuildCraftGui> constructor, ITextComponent component) {
        super(component);
        this.mainGui = constructor.apply(this);
        standardLedgerInit();
    }

    /** Creates a new gui that will load its elements from the given json resource. */
    public GuiScreenBuildCraft(ResourceLocation jsonGuiDef, ITextComponent component) {
        super(component);
        BuildCraftJsonGui jsonGui = new BuildCraftJsonGui(this, jsonGuiDef);
        this.mainGui = jsonGui;
        standardLedgerInit();
    }

    /** Creates a new gui that will load its elements from the given json resource. Like
     * {@link #GuiScreenBuildCraft(IGuiArea, ITextComponent)} this will occupy only the given {@link IGuiArea} */
    public GuiScreenBuildCraft(ResourceLocation jsonGuiDef, IGuiArea area, ITextComponent component) {
        super(component);
        BuildCraftJsonGui jsonGui = new BuildCraftJsonGui(this, area, jsonGuiDef);
        this.mainGui = jsonGui;
        standardLedgerInit();
    }

    private final void standardLedgerInit() {
        if (shouldAddHelpLedger()) {
            mainGui.shownElements.add(new LedgerHelp(mainGui, false));
        }
    }

    protected boolean shouldAddHelpLedger() {
        return true;
    }

    @Override
//    public void updateScreen()
    public void tick() {
//        super.updateScreen();
        super.tick();
        mainGui.tick();
    }

    @Override
//    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float partialTicks) {
//        mainGui.drawBackgroundLayer(poseStack, partialTicks, mouseX, mouseY, this::drawMenuBackground);
        mainGui.drawBackgroundLayer(poseStack, partialTicks, mouseX, mouseY, () -> drawMenuBackground(poseStack));
        mainGui.drawElementBackgrounds(poseStack);
//        mainGui.drawElementForegrounds(this::drawMenuBackground);
        mainGui.drawElementForegrounds(() -> drawMenuBackground(poseStack), poseStack);
    }

    // private void drawMenuBackground()
    private void drawMenuBackground(MatrixStack poseStack) {
//        this.drawBackground(0);
        super.renderBackground(poseStack, 0);
    }

    @Override
//    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (!mainGui.onMouseClicked(mouseX, mouseY, mouseButton)) {
            return super.mouseClicked(mouseX, mouseY, mouseButton);
        }
        return true;
    }

    @Override
//    protected void mouseReleased(int mouseX, int mouseY, int state)
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        mainGui.onMouseReleased(mouseX, mouseY, state);
        return true;
    }

    @Override
//    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double startX, double startY) {
//        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        super.mouseDragged(mouseX, mouseY, clickedMouseButton, startX, startY);
//        mainGui.onMouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        mainGui.onMouseDragged(mouseX, mouseY, clickedMouseButton);
        return true;
    }

    @Override
//    protected void keyTyped(char typedChar, int keyCode) throws IOException
    public boolean keyPressed(int typedChar, int keyCode, int modifiers) {
//        if (!mainGui.onKeyTyped(typedChar, keyCode))
        if (!mainGui.onKeyTyped(typedChar, keyCode, modifiers)) {
//            super.keyTyped(typedChar, keyCode);
            return super.keyPressed(typedChar, keyCode, modifiers);
        } else {
            return true;
        }
    }

    public boolean charTyped(char typedChar, int keyCode) {
        if (!mainGui.charTyped(typedChar, keyCode)) {
            return super.charTyped(typedChar, keyCode);
        } else {
            return true;
        }
    }
}
