/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.gui;

import java.util.List;
import java.util.function.Function;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.lib.gui.json.BuildCraftJsonGui;
import ct.buildcraft.lib.gui.json.InventorySlotHolder;
import ct.buildcraft.lib.gui.ledger.LedgerHelp;
import ct.buildcraft.lib.gui.ledger.LedgerOwnership;
import ct.buildcraft.lib.gui.pos.GuiRectangle;
import ct.buildcraft.lib.gui.pos.IGuiArea;
import ct.buildcraft.lib.misc.GuiUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Future rename: "GuiContainerBuildCraft" */
public abstract class GuiBC8<C extends MenuBC_Neptune> extends AbstractContainerScreen<C> {
    public final BuildCraftGui mainGui;
    public final C container;

    public GuiBC8(C container, Inventory inv, Component title) {
        this(container, g -> new BuildCraftGui(g, BuildCraftGui.createWindowedArea(g)), inv, title);
    }

    public GuiBC8(C container, Function<GuiBC8<?>, BuildCraftGui> constructor, Inventory inv, Component title) {
        super(container, inv, title);
        this.container = container;
        this.mainGui = constructor.apply(this);
        standardLedgerInit();
    }

    public GuiBC8(C container, ResourceLocation jsonGuiDef, Inventory inv, Component title) {
        super(container, inv, title);
        this.container = container;
        BuildCraftJsonGui jsonGui = new BuildCraftJsonGui(this, BuildCraftGui.createWindowedArea(this), jsonGuiDef);
        jsonGui.properties.put("player.inventory", new InventorySlotHolder(container, container.playerInventory));
        this.mainGui = jsonGui;
        standardLedgerInit();
        // Force subclasses to set this themselves after calling jsonGui.load
        imageWidth = 10;
        imageHeight = 10;
    }

    private final void standardLedgerInit() {
        if (container instanceof ContainerBCTile<?>) {
//            mainGui.shownElements.add(new LedgerOwnership(mainGui, ((ContainerBCTile<?>) container).tile, true));
        }
        if (shouldAddHelpLedger()) {
            mainGui.shownElements.add(new LedgerHelp(mainGui, false));
        }
    }
    

    @Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		super.render(pose, mouseX, mouseY, partialTicks);
        if (mainGui.currentMenu == null || !mainGui.currentMenu.shouldFullyOverride()) {
            this.renderTooltip(pose, mouseX, mouseY);
        }
        
	}

    protected boolean shouldAddHelpLedger() {
        return true;
    }

    // Protected -> Public

    public void drawGradientRect(PoseStack pose, IGuiArea area, int startColor, int endColor) {
        int left = (int) area.getX();
        int right = (int) area.getEndX();
        int top = (int) area.getY();
        int bottom = (int) area.getEndY();
        fillGradient(pose, left, top, right, bottom, startColor, endColor);
    }

	@Override
    public void fillGradient(PoseStack pose, int left, int top, int right, int bottom, int startColor, int endColor) {
        super.fillGradient(pose, left, top, right, bottom, startColor, endColor);
    }

    public List<Widget> getButtonList() {
        return renderables;
    }

    public Font getFontRenderer() {
        return font;
    }

    // Gui -- double -> int

    public void drawTexturedModalRect(PoseStack pose, double posX, double posY, double textureX, double textureY, double width,
        double height) {
        int x = Mth.floor(posX);
        int y = Mth.floor(posY);
        int u = Mth.floor(textureX);
        int v = Mth.floor(textureY);
        int w = Mth.floor(width);
        int h = Mth.floor(height);
        blit(pose, x, y, u, v, w, h);
    }

    public void drawString(PoseStack pose, Font fontRenderer, String text, double x, double y, int colour) {
        drawString(pose, fontRenderer, text, x, y, colour, true);
    }

    public void drawString(PoseStack pose, Font fontRenderer, String text, double x, double y, int colour, boolean shadow) {
        if(shadow)
        	fontRenderer.drawShadow(pose, text, (float) x, (float) y, colour);
        else
        	fontRenderer.draw(pose, text, (float) x, (float) y, colour);
    }

    // Other

    /** @deprecated Use {@link GuiUtil#drawItemStackAt(ItemStack,int,int)} instead */
    @Deprecated
    public static void drawItemStackAt(ItemStack stack, int x, int y) {
        GuiUtil.drawItemStackAt(stack, x, y);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        mainGui.tick();
    }

    @Override
	protected void renderBg(PoseStack pose, float partialTicks, int mouseX, int mouseY) {
    	
        mainGui.drawBackgroundLayer(pose, partialTicks, mouseX, mouseY, () -> renderBackground(pose));
        drawBackgroundLayer(pose, mouseX, mouseY, partialTicks);
        mainGui.drawElementBackgrounds(pose);
	}
    
    @Override
	protected void renderLabels(PoseStack pose, int mouseX, int mouseY) {
		//super.renderLabels(pose, mouseX, mouseY);  //do not render InventoryLabel
    	//this.font.draw(pose, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
        mainGui.preDrawForeground(pose);
        drawForegroundLayer(pose, mouseX, mouseY);
        mainGui.drawElementForegrounds(pose, () -> renderBackground(pose));
        drawForegroundLayerAboveElements();

        mainGui.postDrawForeground(pose);
	}

    public void drawProgress(PoseStack pose, GuiRectangle rect, GuiIcon icon, double widthPercent, double heightPercent) {
        double nWidth = rect.width * Math.abs(widthPercent);
        double nHeight = rect.height * Math.abs(heightPercent);
        ISprite sprite = GuiUtil.subRelative(icon.sprite, 0, 0, widthPercent, heightPercent);
        double x = rect.x + mainGui.rootElement.getX();
        double y = rect.y + mainGui.rootElement.getY();
        GuiIcon.draw(pose, sprite, x, y, x + nWidth, y + nHeight);
    }
    
    

    @Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
    	boolean result = false;
    	result |= super.mouseClicked(mouseX, mouseY, mouseButton);
    	result |= mainGui.onMouseClicked(mouseX, mouseY, mouseButton);
    	return result;
	}

    @Override
    public boolean mouseDragged(double startX, double startY, int clickedMouseButton, double finalX, double finalY) {
    	boolean result = false;
    	result |= super.mouseDragged(startX, startY, clickedMouseButton, finalX, finalY);
        mainGui.onMouseDragged(startX, startY, clickedMouseButton, finalX, finalY);
        return result;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
    	boolean result = false;
    	result |= super.mouseReleased(mouseX, mouseY, state);
    	mainGui.onMouseReleased(mouseX, mouseY, state);
    	return result;

    }

    @Override
	public boolean keyPressed(int p_97765_, int p_97766_, int p_97767_) {
        if (!mainGui.onKeyTyped(p_97767_, InputConstants.getKey(p_97765_, p_97766_))) {
            return super.keyPressed(p_97765_, p_97766_, p_97767_);
        }
        return true;
	}

    protected void drawBackgroundLayer(PoseStack pose, int mouseX, int mouseY, float partialTicks) {}

    protected void drawForegroundLayer(PoseStack pose, int mouseX, int mouseY) {}

    /** Like {@link #drawForegroundLayer(PoseStack pose, int mouseX, int mouseY)}, but is called after all {@link IGuiElement}'s have been drawn. */
    protected void drawForegroundLayerAboveElements() {}
}
