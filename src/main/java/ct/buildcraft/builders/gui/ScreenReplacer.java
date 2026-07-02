package ct.buildcraft.builders.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.api.core.InvalidInputDataException;
import ct.buildcraft.api.schematics.ISchematicBlock;
import ct.buildcraft.builders.item.ItemSchematicSingle;
import ct.buildcraft.builders.item.ItemSnapshot;
import ct.buildcraft.builders.snapshot.Blueprint;
import ct.buildcraft.builders.snapshot.ClientSnapshots;
import ct.buildcraft.builders.snapshot.Snapshot;
import ct.buildcraft.lib.gui.BuildCraftGui;
import ct.buildcraft.lib.gui.help.DummyHelpElement;
import ct.buildcraft.lib.gui.help.ElementHelpInfo;
import ct.buildcraft.lib.gui.ledger.LedgerHelp;
import ct.buildcraft.lib.gui.ledger.LedgerOwnership;
import ct.buildcraft.lib.gui.pos.GuiRectangle;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ScreenReplacer extends AbstractContainerScreen<MenuReplacer> {

    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftbuilders", "textures/gui/replacer.png");
    private static final int SIZE_X = 176;
    private static final int SIZE_Y = 241;
    private static final int PREVIEW_X = 8;
    private static final int PREVIEW_Y = 9;
    private static final int PREVIEW_W = 160;
    private static final int PREVIEW_H = 100;
    private static final int PREVIEW_COLS = 8;
    private static final int PREVIEW_ROWS = 4;
    private static final int PREVIEW_CELL = 18;
    private static final int TEXT_COLOUR = 4210752;
    private static final int ERROR_COLOUR = 0xAA0000;
    private static final int OK_COLOUR = 0x2F6F2F;

    private final BuildCraftGui mainGui;

    public ScreenReplacer(MenuReplacer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.mainGui = new BuildCraftGui(this, BuildCraftGui.createWindowedArea(this));
        if (menu.tile != null) {
            this.mainGui.shownElements.add(new LedgerOwnership(this.mainGui, menu.tile, true));
        }
        this.mainGui.shownElements.add(new DummyHelpElement(
            new GuiRectangle(PREVIEW_X, PREVIEW_Y, PREVIEW_W, PREVIEW_H).offset(this.mainGui.rootElement).expand(2),
            new ElementHelpInfo(
                "buildcraft.help.replacer.preview.title",
                0xFF_CC_AA_88,
                "buildcraft.help.replacer.preview.desc"
            )
        ));
        this.mainGui.shownElements.add(new DummyHelpElement(
            new GuiRectangle(8, 115, 18, 18).offset(this.mainGui.rootElement).expand(2),
            new ElementHelpInfo(
                "buildcraft.help.replacer.blueprint.title",
                0xFF_88_AA_CC,
                "buildcraft.help.replacer.blueprint.desc"
            )
        ));
        this.mainGui.shownElements.add(new DummyHelpElement(
            new GuiRectangle(8, 137, 18, 18).offset(this.mainGui.rootElement).expand(2),
            new ElementHelpInfo(
                "buildcraft.help.replacer.from.title",
                0xFF_CC_88_88,
                "buildcraft.help.replacer.from.desc"
            )
        ));
        this.mainGui.shownElements.add(new DummyHelpElement(
            new GuiRectangle(56, 137, 18, 18).offset(this.mainGui.rootElement).expand(2),
            new ElementHelpInfo(
                "buildcraft.help.replacer.to.title",
                0xFF_88_CC_88,
                "buildcraft.help.replacer.to.desc"
            )
        ));
        this.mainGui.shownElements.add(new LedgerHelp(this.mainGui, false));
        this.imageWidth = SIZE_X;
        this.imageHeight = SIZE_Y;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 148;
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.mainGui.tick();
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(pose);
        super.render(pose, mouseX, mouseY, partialTick);
        this.renderTooltip(pose, mouseX, mouseY);
        ItemStack hoveredPreviewStack = getPreviewStackAt(mouseX, mouseY);
        if (!hoveredPreviewStack.isEmpty()) {
            this.renderTooltip(pose, hoveredPreviewStack, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(PoseStack pose, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE_BASE);
        this.blit(pose, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        renderBlueprintPreview(pose);
        this.mainGui.drawBackgroundLayer(pose, partialTick, mouseX, mouseY, () -> {});
        this.mainGui.drawElementBackgrounds(pose);
    }

    @Override
    protected void renderLabels(PoseStack pose, int mouseX, int mouseY) {
        // Keep the GUI visually clean: the only text we render is the centred preview message inside the preview panel.
        this.mainGui.preDrawForeground(pose);
        this.mainGui.drawElementForegrounds(pose, () -> {});
        this.mainGui.postDrawForeground(pose);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean result = super.mouseClicked(mouseX, mouseY, button);
        this.mainGui.onMouseClicked(mouseX, mouseY, button);
        return result;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean result = super.mouseReleased(mouseX, mouseY, button);
        this.mainGui.onMouseReleased(mouseX, mouseY, button);
        return result;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        boolean result = super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        this.mainGui.onMouseDragged(mouseX, mouseY, button, dragX, dragY);
        return result;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.mainGui.onKeyTyped(modifiers, InputConstants.getKey(keyCode, scanCode))) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void renderBlueprintPreview(PoseStack pose) {
        Snapshot snapshot = getInsertedSnapshot();
        if (snapshot == null) {
            renderPreviewMessage(pose, getPreviewMessage(), TEXT_COLOUR);
            return;
        }
        if (!(snapshot instanceof Blueprint blueprint)) {
            renderPreviewMessage(pose, Component.translatable("gui.buildcraftbuilders.replacer.preview.not_blueprint"), ERROR_COLOUR);
            return;
        }

        List<ItemStack> previewStacks = getPreviewStacks(blueprint);
        if (previewStacks.isEmpty()) {
            renderPreviewMessage(pose, Component.translatable("gui.buildcraftbuilders.replacer.preview.empty"), TEXT_COLOUR);
            return;
        }

        int x0 = this.leftPos + PREVIEW_X + 8;
        int y0 = this.topPos + PREVIEW_Y + 5;
        int max = Math.min(previewStacks.size(), PREVIEW_COLS * PREVIEW_ROWS);
        for (int i = 0; i < max; i++) {
            int x = x0 + (i % PREVIEW_COLS) * PREVIEW_CELL;
            int y = y0 + (i / PREVIEW_COLS) * PREVIEW_CELL;
            ItemStack stack = previewStacks.get(i);
            this.itemRenderer.renderAndDecorateItem(stack, x, y);
            this.itemRenderer.renderGuiItemDecorations(this.font, stack, x, y);
        }
        if (previewStacks.size() > max) {
            String more = "+" + (previewStacks.size() - max);
            this.font.draw(pose, more, this.leftPos + PREVIEW_X + PREVIEW_W - this.font.width(more) - 4, this.topPos + PREVIEW_Y + PREVIEW_H - 23, TEXT_COLOUR);
        }
    }

    private void renderReplacementStatus(PoseStack pose) {
        StatusLine status = getReplacementStatus();
        int maxWidth = PREVIEW_W - 8;
        String text = this.font.plainSubstrByWidth(status.message.getString(), maxWidth);
        int x = this.leftPos + PREVIEW_X + 4;
        int y = this.topPos + PREVIEW_Y + PREVIEW_H - 12;
        this.font.draw(pose, text, x, y, status.colour);
    }

    private Snapshot getInsertedSnapshot() {
        ItemStack stack = this.menu.getSlot(0).getItem();
        Snapshot.Header header = ItemSnapshot.getHeader(stack);
        if (header == null) {
            return null;
        }
        return ClientSnapshots.INSTANCE.getSnapshot(header.key);
    }

    private Component getPreviewMessage() {
        ItemStack stack = this.menu.getSlot(0).getItem();
        Snapshot.Header header = ItemSnapshot.getHeader(stack);
        return header == null
            ? Component.translatable("gui.buildcraftbuilders.replacer.preview.no_blueprint")
            : Component.translatable("gui.buildcraftbuilders.replacer.preview.loading");
    }

    private StatusLine getReplacementStatus() {
        ItemStack blueprintStack = this.menu.getSlot(0).getItem();
        Snapshot.Header header = ItemSnapshot.getHeader(blueprintStack);
        if (header == null) {
            return new StatusLine(Component.translatable("gui.buildcraftbuilders.replacer.status.no_blueprint"), TEXT_COLOUR);
        }
        ItemStack fromStack = this.menu.getSlot(1).getItem();
        if (fromStack.isEmpty()) {
            return new StatusLine(Component.translatable("gui.buildcraftbuilders.replacer.status.missing_from"), TEXT_COLOUR);
        }
        ItemStack toStack = this.menu.getSlot(2).getItem();
        if (toStack.isEmpty()) {
            return new StatusLine(Component.translatable("gui.buildcraftbuilders.replacer.status.missing_to"), TEXT_COLOUR);
        }
        Snapshot snapshot = ClientSnapshots.INSTANCE.getSnapshot(header.key);
        if (!(snapshot instanceof Blueprint blueprint)) {
            return new StatusLine(Component.translatable("gui.buildcraftbuilders.replacer.status.loading"), TEXT_COLOUR);
        }
        try {
            ISchematicBlock from = ItemSchematicSingle.getSchematic(fromStack);
            ISchematicBlock to = ItemSchematicSingle.getSchematic(toStack);
            if (from == null || to == null) {
                return new StatusLine(Component.translatable("gui.buildcraftbuilders.replacer.status.invalid_schematic"), ERROR_COLOUR);
            }
            if (Blueprint.schematicMatchesForReplacement(from, to)) {
                return new StatusLine(Component.translatable("gui.buildcraftbuilders.replacer.status.same_schematic"), TEXT_COLOUR);
            }
            int matches = blueprint.countMatchingSchematic(from);
            return matches > 0
                ? new StatusLine(Component.translatable("gui.buildcraftbuilders.replacer.status.ready", matches), OK_COLOUR)
                : new StatusLine(Component.translatable("gui.buildcraftbuilders.replacer.status.no_match"), ERROR_COLOUR);
        } catch (InvalidInputDataException e) {
            return new StatusLine(Component.translatable("gui.buildcraftbuilders.replacer.status.invalid_schematic"), ERROR_COLOUR);
        }
    }

    private void renderPreviewMessage(PoseStack pose, Component message, int colour) {
        int x = this.leftPos + PREVIEW_X + (PREVIEW_W - this.font.width(message.getString())) / 2;
        int y = this.topPos + PREVIEW_Y + PREVIEW_H / 2 - 4;
        this.font.draw(pose, message, x, y, colour);
    }

    private List<ItemStack> getPreviewStacks(Blueprint blueprint) {
        List<ItemStack> stacks = new ArrayList<>();
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return stacks;
        }
        for (ISchematicBlock schematicBlock : blueprint.palette) {
            if (schematicBlock == null || schematicBlock.isAir()) {
                continue;
            }
            try {
                List<ItemStack> requiredItems = schematicBlock.computeRequiredItems(level);
                if (!requiredItems.isEmpty() && !requiredItems.get(0).isEmpty()) {
                    stacks.add(requiredItems.get(0).copy());
                }
            } catch (RuntimeException ignored) {
                // A broken schematic must not crash the GUI preview. The builder/replacer logic still validates it server-side.
            }
        }
        return stacks;
    }

    private ItemStack getPreviewStackAt(int mouseX, int mouseY) {
        Snapshot snapshot = getInsertedSnapshot();
        if (!(snapshot instanceof Blueprint blueprint)) {
            return ItemStack.EMPTY;
        }
        int relX = mouseX - (this.leftPos + PREVIEW_X + 8);
        int relY = mouseY - (this.topPos + PREVIEW_Y + 5);
        if (relX < 0 || relY < 0) {
            return ItemStack.EMPTY;
        }
        int col = relX / PREVIEW_CELL;
        int row = relY / PREVIEW_CELL;
        if (col < 0 || col >= PREVIEW_COLS || row < 0 || row >= PREVIEW_ROWS) {
            return ItemStack.EMPTY;
        }
        if (relX % PREVIEW_CELL >= 16 || relY % PREVIEW_CELL >= 16) {
            return ItemStack.EMPTY;
        }
        int index = row * PREVIEW_COLS + col;
        List<ItemStack> stacks = getPreviewStacks(blueprint);
        return index >= 0 && index < stacks.size() ? stacks.get(index) : ItemStack.EMPTY;
    }

    private static final class StatusLine {
        private final Component message;
        private final int colour;

        private StatusLine(Component message, int colour) {
            this.message = message;
            this.colour = colour;
        }
    }
}
