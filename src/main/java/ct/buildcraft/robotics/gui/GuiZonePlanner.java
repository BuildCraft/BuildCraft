/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 */
package ct.buildcraft.robotics.gui;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.lib.gui.GuiBC8;
import ct.buildcraft.lib.gui.GuiIcon;
import ct.buildcraft.lib.gui.help.DummyHelpElement;
import ct.buildcraft.lib.gui.help.ElementHelpInfo;
import ct.buildcraft.lib.gui.pos.GuiRectangle;
import ct.buildcraft.core.BCCoreItems;
import ct.buildcraft.robotics.container.ContainerZonePlanner;
import ct.buildcraft.robotics.tile.TileZonePlanner;
import ct.buildcraft.robotics.zone.ZonePlan;
import ct.buildcraft.robotics.zone.ZonePlannerMapChunk;
import ct.buildcraft.robotics.zone.ZonePlannerMapChunk.MapColourData;
import ct.buildcraft.robotics.zone.ZonePlannerMapChunkKey;
import ct.buildcraft.robotics.zone.ZonePlannerMapDataClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class GuiZonePlanner extends GuiBC8<ContainerZonePlanner> {
    public static final int WINDOWED_MAP_WIDTH = 213;
    public static final int WINDOWED_MAP_HEIGHT = 100;

    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftrobotics:textures/gui/zone_planner.png");
    private static final int SIZE_X = 256;
    private static final int SIZE_Y = 228;

    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE_BASE, 16, 228, 8, 28);
    private static final GuiRectangle RECT_PROGRESS = new GuiRectangle(236, 27, 8, 28);

    private static final int MAP_X = 8;
    private static final int MAP_Y = 9;
    private static final int COLOUR_X = 8;
    private static final int COLOUR_Y = 146;
    private static final int COLOUR_STEP = 18;
    private static final int COLOUR_SIZE = 16;
    private static final int NAME_X = 28;
    private static final int NAME_Y = 129;
    private static final int NAME_W = 156;
    private static final int NAME_H = 12;
    private static final int TOOL_X = 27;
    private static final int TOOL_Y = 111;
    private static final int TOOL_W = 15;
    private static final int TOOL_H = 15;
    private static final int FS_X = 44;
    private static final int FS_Y = 111;
    private static final int FS_W = 20;
    private static final int FS_H = 15;

    private float blocksPerPixel = 1.0F;
    private int centerX;
    private int centerZ;

    private boolean selecting = false;
    private int selX1;
    private int selY1;
    private int selX2;
    private int selY2;
    private BlockPos selectionStartXZ;
    private ZonePlan bufferLayer;

    private boolean addMode = true;
    private boolean fullscreen = false;
    private int selectedLayer = 0;

    private EditBox nameField;

    public GuiZonePlanner(ContainerZonePlanner container, Inventory inv, Component title) {
        super(container, inv, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
        if (container.tile != null) {
            BlockPos tilePos = container.tile.getBlockPos();
            centerX = tilePos.getX();
            centerZ = tilePos.getZ();
            selectedLayer = container.tile.getCurrentSelectedArea();
            container.currentLayer = selectedLayer;
            container.currentAreaSelection = new ZonePlan(container.tile.selectArea(selectedLayer));
            container.mapName = container.tile.mapName;
        }
        addHelpElements();
    }

    private void addHelpElements() {
        mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(MAP_X, MAP_Y, WINDOWED_MAP_WIDTH, WINDOWED_MAP_HEIGHT).offset(mainGui.rootElement).expand(2),
                new ElementHelpInfo(
                        "buildcraftrobotics.help.zone_planner.map.title",
                        0xFF_70_A8_70,
                        "buildcraftrobotics.help.zone_planner.map.desc"
                )
        ));
        mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(COLOUR_X, COLOUR_Y, COLOUR_STEP * 4 - 2, COLOUR_STEP * 4 - 2).offset(mainGui.rootElement).expand(2),
                new ElementHelpInfo(
                        "buildcraftrobotics.help.zone_planner.layers.title",
                        0xFF_D0_70_70,
                        "buildcraftrobotics.help.zone_planner.layers.desc"
                )
        ));
        mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(TOOL_X, TOOL_Y, TOOL_W, TOOL_H).offset(mainGui.rootElement).expand(2),
                new ElementHelpInfo(
                        "buildcraftrobotics.help.zone_planner.mode.title",
                        0xFF_A0_A0_A0,
                        "buildcraftrobotics.help.zone_planner.mode.desc"
                )
        ));
        mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(FS_X, FS_Y, FS_W, FS_H).offset(mainGui.rootElement).expand(2),
                new ElementHelpInfo(
                        "buildcraftrobotics.help.zone_planner.fullscreen.title",
                        0xFF_88_AA_DD,
                        "buildcraftrobotics.help.zone_planner.fullscreen.desc"
                )
        ));
        mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(NAME_X, NAME_Y, NAME_W, NAME_H).offset(mainGui.rootElement).expand(2),
                new ElementHelpInfo(
                        "buildcraftrobotics.help.zone_planner.name.title",
                        0xFF_99_99_99,
                        "buildcraftrobotics.help.zone_planner.name.desc"
                )
        ));
        mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(232, 8, 20, 68).offset(mainGui.rootElement).expand(2),
                new ElementHelpInfo(
                        "buildcraftrobotics.help.zone_planner.crafting.title",
                        0xFF_CC_BB_88,
                        "buildcraftrobotics.help.zone_planner.crafting.desc"
                )
        ));
        mainGui.shownElements.add(new DummyHelpElement(
                new GuiRectangle(7, 124, 20, 20).offset(mainGui.rootElement).expand(2),
                new ElementHelpInfo(
                        "buildcraftrobotics.help.zone_planner.import.title",
                        0xFF_88_CC_CC,
                        "buildcraftrobotics.help.zone_planner.import.desc"
                )
        ));
    }

    @Override
    public void init() {
        super.init();
        nameField = new EditBox(font, leftPos + NAME_X, topPos + NAME_Y, NAME_W, NAME_H, Component.empty());
        nameField.setMaxLength(32);
        nameField.setValue(container.tile == null ? container.mapName : container.tile.mapName);
        nameField.setResponder(container::sendNameToServer);
        addRenderableWidget(nameField);
        clearNameFocus();
        ZonePlannerMapDataClient.INSTANCE.clearCache();
        container.loadArea(selectedLayer);
    }

    private int mapWidth() {
        return fullscreen ? width : WINDOWED_MAP_WIDTH;
    }

    private int mapHeight() {
        return fullscreen ? height : WINDOWED_MAP_HEIGHT;
    }

    private int mapXScreen() {
        return fullscreen ? 0 : leftPos + MAP_X;
    }

    private int mapYScreen() {
        return fullscreen ? 0 : topPos + MAP_Y;
    }

    private int mapXDraw() {
        return mapXScreen();
    }

    private int mapYDraw() {
        return mapYScreen();
    }

    private boolean isInsideMap(double mouseX, double mouseY) {
        int x = mapXScreen();
        int y = mapYScreen();
        return mouseX >= x && mouseY >= y && mouseX < x + mapWidth() && mouseY < y + mapHeight();
    }

    private boolean isInsideNameField(double mouseX, double mouseY) {
        int x = leftPos + NAME_X;
        int y = topPos + NAME_Y;
        return !fullscreen && nameField != null && nameField.visible
                && mouseX >= x && mouseY >= y && mouseX < x + NAME_W && mouseY < y + NAME_H;
    }

    private void clearNameFocus() {
        if (nameField != null && nameField.isFocused()) {
            // EditBox#setFocused(boolean) is protected in this target, but mouseClicked outside the box
            // defocuses it through the widget's own public event path.
            nameField.mouseClicked(-1, -1, 0);
        }
        setFocused(null);
    }

    private BlockPos screenToWorld(double mouseX, double mouseY) {
        int relX = Mth.floor(mouseX) - mapXScreen();
        int relY = Mth.floor(mouseY) - mapYScreen();
        int worldX = Mth.floor(centerX + (relX - mapWidth() / 2.0F) * blocksPerPixel);
        int worldZ = Mth.floor(centerZ + (relY - mapHeight() / 2.0F) * blocksPerPixel);
        int worldY = 0;
        if (minecraft != null && minecraft.level != null) {
            ZonePlannerMapChunk chunk = ZonePlannerMapDataClient.INSTANCE.getChunk(
                    minecraft.level,
                    new ZonePlannerMapChunkKey(new ChunkPos(worldX >> 4, worldZ >> 4), getDimensionId(), getMapLevel())
            );
            if (chunk != null) {
                MapColourData data = chunk.getData(worldX, worldZ);
                if (data != null) {
                    worldY = data.posY;
                }
            }
        }
        return new BlockPos(worldX, worldY, worldZ);
    }

    private int getMapLevel() {
        if (minecraft == null || minecraft.player == null) {
            return 0;
        }
        return Math.max(0, minecraft.player.blockPosition().getY() / ZonePlannerMapChunkKey.LEVEL_HEIGHT);
    }

    private int getDimensionId() {
        if (minecraft == null || minecraft.level == null) {
            return 0;
        }
        return minecraft.level.dimension().location().hashCode();
    }

    private ZonePlan getBaseSelectedArea() {
        if (container.tile != null && selectedLayer >= 0 && selectedLayer < container.tile.layers.length) {
            ZonePlan layer = container.tile.layers[selectedLayer];
            if (layer != null) {
                return layer;
            }
        }
        return container.currentAreaSelection;
    }

    private ZonePlan getRenderedSelectedArea() {
        return bufferLayer != null ? bufferLayer : getBaseSelectedArea();
    }

    private int getColourSlotAt(double mouseX, double mouseY) {
        if (fullscreen) {
            return -1;
        }
        int relX = Mth.floor(mouseX) - (leftPos + COLOUR_X);
        int relY = Mth.floor(mouseY) - (topPos + COLOUR_Y);
        if (relX < 0 || relY < 0) {
            return -1;
        }
        int column = relX / COLOUR_STEP;
        int row = relY / COLOUR_STEP;
        if (column < 0 || column >= 4 || row < 0 || row >= 4) {
            return -1;
        }
        if (relX - column * COLOUR_STEP >= COLOUR_SIZE || relY - row * COLOUR_STEP >= COLOUR_SIZE) {
            return -1;
        }
        return column * 4 + row;
    }

    private boolean insideRelativeButton(double mouseX, double mouseY, int x, int y, int w, int h) {
        if (fullscreen) {
            return false;
        }
        int ax = leftPos + x;
        int ay = topPos + y;
        return mouseX >= ax && mouseY >= ay && mouseX < ax + w && mouseY < ay + h;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (!isInsideNameField(mouseX, mouseY)) {
            clearNameFocus();
        }

        if (isInsideMap(mouseX, mouseY)) {
            if (mouseButton == 1) {
                BlockPos newCenter = screenToWorld(mouseX, mouseY);
                centerX = newCenter.getX();
                centerZ = newCenter.getZ();
                selecting = false;
                selectionStartXZ = null;
                bufferLayer = null;
                return true;
            }

            selecting = true;
            selectionStartXZ = screenToWorld(mouseX, mouseY);
            selX1 = Mth.floor(mouseX);
            selY1 = Mth.floor(mouseY);
            selX2 = selX1;
            selY2 = selY1;
            updateBufferedSelection(mouseX, mouseY);
            return true;
        }

        if (insideRelativeButton(mouseX, mouseY, TOOL_X, TOOL_Y, TOOL_W, TOOL_H)) {
            addMode = !addMode;
            return true;
        }
        if (insideRelativeButton(mouseX, mouseY, FS_X, FS_Y, FS_W, FS_H)) {
            toFullscreen();
            return true;
        }

        int colourSlot = getColourSlotAt(mouseX, mouseY);
        if (colourSlot >= 0) {
            selectLayer(colourSlot);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
        if (selecting && selectionStartXZ != null) {
            selX2 = Mth.clamp(Mth.floor(mouseX), mapXScreen(), mapXScreen() + mapWidth() - 1);
            selY2 = Mth.clamp(Mth.floor(mouseY), mapYScreen(), mapYScreen() + mapHeight() - 1);
            updateBufferedSelection(selX2, selY2);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (selecting && selectionStartXZ != null) {
            updateBufferedSelection(mouseX, mouseY);
            if (bufferLayer != null) {
                container.saveArea(selectedLayer, bufferLayer);
            }
            selecting = false;
            selectionStartXZ = null;
            bufferLayer = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (isInsideMap(mouseX, mouseY)) {
            boolean changed = delta > 0 ? decBlocksPerPixel() : incBlocksPerPixel();
            return changed || super.mouseScrolled(mouseX, mouseY, delta);
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void updateBufferedSelection(double mouseX, double mouseY) {
        if (selectionStartXZ == null) {
            return;
        }
        BlockPos end = screenToWorld(
                Mth.clamp(Mth.floor(mouseX), mapXScreen(), mapXScreen() + mapWidth() - 1),
                Mth.clamp(Mth.floor(mouseY), mapYScreen(), mapYScreen() + mapHeight() - 1)
        );
        bufferLayer = new ZonePlan(getBaseSelectedArea());

        int minX = Math.min(selectionStartXZ.getX(), end.getX());
        int maxX = Math.max(selectionStartXZ.getX(), end.getX());
        int minZ = Math.min(selectionStartXZ.getZ(), end.getZ());
        int maxZ = Math.max(selectionStartXZ.getZ(), end.getZ());

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                bufferLayer.set(x, z, addMode);
            }
        }
    }

    private void selectLayer(int index) {
        if (index < 0 || index >= TileZonePlanner.LAYER_COUNT) {
            return;
        }
        selectedLayer = index;
        bufferLayer = null;
        selecting = false;
        if (container.tile != null) {
            container.currentAreaSelection = new ZonePlan(container.tile.selectArea(index));
        }
        container.loadArea(index);
    }

    private boolean incBlocksPerPixel() {
        if (blocksPerPixel > 0.125F) {
            if (blocksPerPixel <= 1.0F) {
                blocksPerPixel /= 2.0F;
            } else {
                blocksPerPixel -= 1.0F;
            }
            return true;
        }
        return false;
    }

    private boolean decBlocksPerPixel() {
        float max = fullscreen ? 4.0F : 8.0F;
        if (blocksPerPixel < max) {
            if (blocksPerPixel >= 1.0F) {
                blocksPerPixel += 1.0F;
            } else {
                blocksPerPixel *= 2.0F;
            }
            return true;
        }
        return false;
    }

    private void toFullscreen() {
        if (fullscreen) {
            return;
        }
        fullscreen = true;
        if (blocksPerPixel > 4.0F) {
            blocksPerPixel = 4.0F;
        }
        if (nameField != null) {
            nameField.visible = false;
            clearNameFocus();
        }
    }

    private void toWindowed() {
        if (!fullscreen) {
            return;
        }
        fullscreen = false;
        if (nameField != null) {
            nameField.visible = true;
        }
    }


    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        if (!fullscreen) {
            super.render(pose, mouseX, mouseY, partialTicks);
            return;
        }

        renderBackground(pose);
        drawBackgroundLayer(pose, mouseX, mouseY, partialTicks);
        drawForegroundLayer(pose, mouseX, mouseY);
        renderTooltip(pose, mouseX, mouseY);
    }

    @Override
    protected void drawBackgroundLayer(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        if (!fullscreen) {
            ICON_GUI.drawAt(pose, mainGui.rootElement);
            drawMap(pose, mouseX, mouseY);
            drawColourSlots(pose);
            drawButton(pose, leftPos + TOOL_X, topPos + TOOL_Y, TOOL_W, TOOL_H, addMode ? "+" : "-",
                    insideRelativeButton(mouseX, mouseY, TOOL_X, TOOL_Y, TOOL_W, TOOL_H));
            drawButton(pose, leftPos + FS_X, topPos + FS_Y, FS_W, FS_H, "FS",
                    insideRelativeButton(mouseX, mouseY, FS_X, FS_Y, FS_W, FS_H));
            if (container.tile != null) {
                drawProgress(pose, RECT_PROGRESS, ICON_PROGRESS,
                        1.0D,
                        container.tile.deltaProgress.getDynamic(partialTicks) / (double) TileZonePlanner.CRAFT_TIME);
            }
        } else {
            GuiComponent.fill(pose, 0, 0, width, height, 0xFF_20_20_20);
        }
    }

    @Override
    protected void drawForegroundLayer(PoseStack pose, int mouseX, int mouseY) {
        if (fullscreen) {
            drawMap(pose, mouseX, mouseY);
        }
    }

    private void drawButton(PoseStack pose, int x, int y, int w, int h, String label, boolean hovered) {
        GuiComponent.fill(pose, x, y, x + w, y + h, hovered ? 0xFF_B8_B8_B8 : 0xFF_A0_A0_A0);
        GuiComponent.fill(pose, x, y, x + w, y + 1, 0xFF_F0_F0_F0);
        GuiComponent.fill(pose, x, y, x + 1, y + h, 0xFF_F0_F0_F0);
        GuiComponent.fill(pose, x, y + h - 1, x + w, y + h, 0xFF_40_40_40);
        GuiComponent.fill(pose, x + w - 1, y, x + w, y + h, 0xFF_40_40_40);
        font.draw(pose, label, x + (w - font.width(label)) / 2.0F, y + 3, 0x20_20_20);
    }

    private void drawColourSlots(PoseStack pose) {
        for (int column = 0; column < 4; column++) {
            for (int row = 0; row < 4; row++) {
                int index = column * 4 + row;
                int x = leftPos + COLOUR_X + column * COLOUR_STEP;
                int y = topPos + COLOUR_Y + row * COLOUR_STEP;
                Item brush = BCCoreItems.PAINT_BRUSHS.get(DyeColor.byId(index));
                if (brush != null) {
                    ItemStack stack = new ItemStack(brush);
                    this.itemRenderer.renderAndDecorateItem(stack, x, y);
                    this.itemRenderer.renderGuiItemDecorations(this.font, stack, x, y);
                } else {
                    int rgb = DyeColor.byId(index).getTextColor() & 0x00_FF_FF_FF;
                    GuiComponent.fill(pose, x + 2, y + 2, x + COLOUR_SIZE - 2, y + COLOUR_SIZE - 2, 0xFF_00_00_00 | rgb);
                }
                if (index == selectedLayer) {
                    RenderSystem.setShaderTexture(0, TEXTURE_BASE);
                    blit(pose, x, y, 0, 228, 16, 16);
                }
            }
        }
    }

    private void drawMap(PoseStack pose, int mouseX, int mouseY) {
        Minecraft mc = minecraft;
        if (mc == null || mc.level == null) {
            return;
        }

        int x0 = mapXDraw();
        int y0 = mapYDraw();
        int mapW = mapWidth();
        int mapH = mapHeight();
        GuiComponent.fill(pose, x0, y0, x0 + mapW, y0 + mapH, 0xFF_80_80_80);

        int step = fullscreen ? Math.max(1, Mth.ceil(blocksPerPixel)) : 1;
        int dimension = getDimensionId();
        int level = getMapLevel();
        for (int sx = 0; sx < mapW; sx += step) {
            for (int sy = 0; sy < mapH; sy += step) {
                int worldX = Mth.floor(centerX + (sx - mapW / 2.0F) * blocksPerPixel);
                int worldZ = Mth.floor(centerZ + (sy - mapH / 2.0F) * blocksPerPixel);
                int colour = getDisplayedMapColour(mc.level, dimension, level, worldX, worldZ);
                if (colour != 0) {
                    GuiComponent.fill(pose, x0 + sx, y0 + sy, x0 + Math.min(mapW, sx + step),
                            y0 + Math.min(mapH, sy + step), colour);
                }
            }
        }

        drawZoneOverlay(pose, x0, y0, mapW, mapH, step);
        if (selecting) {
            drawSelectionRect(pose);
        }
    }

    private MapColourData getMapData(Level level, int dimension, int mapLevel, int worldX, int worldZ) {
        ZonePlannerMapChunk chunk = ZonePlannerMapDataClient.INSTANCE.getChunk(
                level,
                new ZonePlannerMapChunkKey(new ChunkPos(worldX >> 4, worldZ >> 4), dimension, mapLevel)
        );
        return chunk == null ? null : chunk.getData(worldX, worldZ);
    }

    private int getDisplayedMapColour(Level level, int dimension, int mapLevel, int worldX, int worldZ) {
        MapColourData data = getMapData(level, dimension, mapLevel, worldX, worldZ);
        return data == null ? 0 : data.colour;
    }

    private void drawZoneOverlay(PoseStack pose, int x0, int y0, int mapW, int mapH, int step) {
        ZonePlan selected = getRenderedSelectedArea();
        if (selected == null || selected.getChunkPoses().isEmpty()) {
            return;
        }
        int rgb = DyeColor.byId(selectedLayer).getTextColor() & 0x00_FF_FF_FF;
        int argb = 0x88_00_00_00 | rgb;
        for (int sx = 0; sx < mapW; sx += step) {
            for (int sy = 0; sy < mapH; sy += step) {
                int worldX = Mth.floor(centerX + (sx - mapW / 2.0F) * blocksPerPixel);
                int worldZ = Mth.floor(centerZ + (sy - mapH / 2.0F) * blocksPerPixel);
                if (selected.get(worldX, worldZ)) {
                    GuiComponent.fill(pose, x0 + sx, y0 + sy, x0 + Math.min(mapW, sx + step),
                            y0 + Math.min(mapH, sy + step), argb);
                }
            }
        }
    }

    private void drawSelectionRect(PoseStack pose) {
        int x1 = Math.min(selX1, selX2);
        int x2 = Math.max(selX1, selX2);
        int y1 = Math.min(selY1, selY2);
        int y2 = Math.max(selY1, selY2);
        int rgb = DyeColor.byId(selectedLayer).getTextColor() & 0x00_FF_FF_FF;
        int argb = (addMode ? 0x66_00_00_00 : 0x55_00_00_00) | rgb;
        GuiComponent.fill(pose, x1, y1, x2 + 1, y2 + 1, argb);
        GuiComponent.fill(pose, x1, y1, x2 + 1, y1 + 1, 0xCC_FF_FF_FF);
        GuiComponent.fill(pose, x1, y2, x2 + 1, y2 + 1, 0xCC_FF_FF_FF);
        GuiComponent.fill(pose, x1, y1, x1 + 1, y2 + 1, 0xCC_FF_FF_FF);
        GuiComponent.fill(pose, x2, y1, x2 + 1, y2 + 1, 0xCC_FF_FF_FF);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (nameField != null) {
            nameField.tick();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!fullscreen && nameField != null && nameField.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
                clearNameFocus();
                return true;
            }
            if (nameField.keyPressed(keyCode, scanCode, modifiers) || nameField.canConsumeInput()) {
                return true;
            }
        }

        if (keyCode == GLFW.GLFW_KEY_F5) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_EQUAL || keyCode == GLFW.GLFW_KEY_KP_ADD) {
            return incBlocksPerPixel() || super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == GLFW.GLFW_KEY_MINUS || keyCode == GLFW.GLFW_KEY_KP_SUBTRACT) {
            return decBlocksPerPixel() || super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == GLFW.GLFW_KEY_M) {
            if (Screen.hasShiftDown()) {
                toFullscreen();
            } else {
                toWindowed();
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && fullscreen) {
            toWindowed();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!fullscreen && nameField != null && nameField.isFocused()) {
            return nameField.charTyped(codePoint, modifiers);
        }
        if (codePoint == '+') {
            return incBlocksPerPixel();
        }
        if (codePoint == '-') {
            return decBlocksPerPixel();
        }
        if (codePoint == 'm') {
            toWindowed();
            return true;
        }
        if (codePoint == 'M') {
            toFullscreen();
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void removed() {
        fullscreen = false;
        super.removed();
    }
}
