/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.builders.gui;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.builders.menu.ContainerArchitectTable;
import ct.buildcraft.lib.gui.GuiBC8;
import ct.buildcraft.lib.gui.GuiIcon;
import ct.buildcraft.lib.gui.pos.GuiRectangle;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiArchitectTable extends GuiBC8<ContainerArchitectTable> {
	private static final ResourceLocation TEXTURE_BASE = new ResourceLocation(
			"buildcraftbuilders:textures/gui/architect.png");
	private static final int SIZE_X = 256, SIZE_Y = 166;
	private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
	private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE_BASE, 0, 166, 24, 17);
	private static final GuiRectangle RECT_PROGRESS = new GuiRectangle(159, 34, 24, 17);

	private EditBox nameField;

	private CycleButton<Boolean> isCreativeButton;
	private CycleButton<Boolean> enableRotateButton;
    private CycleButton<Boolean> canExcavateButton;
    
    private boolean isCreative = true;
    private boolean canRotate = true;
    private boolean canExcavate = true;

	public GuiArchitectTable(ContainerArchitectTable container, Inventory inv, Component title) {
		super(container, inv, title);
		imageWidth = SIZE_X;
		imageHeight = SIZE_Y;
	}

	@Override
	public void init() {
		super.init();
		nameField = new EditBox(font, leftPos + 90, topPos + 62, 156, 12, Component.empty());
		nameField.setValue(container.tile.name);
		nameField.setResponder((s) -> container.sendNameToServer(s.trim()));
		
		int p = container.setting.get();
		isCreative = (p&0b1) == 0b1;
		canRotate = (p&0b10) == 0b10;
		canExcavate = (p&0b100) == 0b100;
		
		this.isCreativeButton = this.addRenderableWidget(
				CycleButton.booleanBuilder(Component.translatable("block.architect.allowCreative"), Component.translatable("block.architect.noallowCreative"))
					.displayOnlyValue().withTooltip((b) -> getFontRenderer().split(Component.translatable((b ? "block.architect.tooltip.allowCreative.1" : "block.architect.tooltip.allowCreative.2")), 60))
					.create(this.width / 2 - 122, this.height / 2 - 55, 77, 20,
						Component.empty(), (p_169727_, p_169728_) -> {
							this.isCreative = p_169728_;
							container.setting
									.set((isCreative ? 1 : 0) | (canRotate ? 0b10 : 0) | (canExcavate ? 0b100 : 0));
						}));
		
		this.enableRotateButton = this.addRenderableWidget(
				CycleButton.onOffBuilder(canRotate).create(this.width / 2 - 122, this.height / 2 - 30, 77, 20,
						Component.translatable("block.architect.rotate"), (p_169727_, p_169728_) -> {
							this.canRotate = p_169728_;
							container.setting
									.set((isCreative ? 1 : 0) | (canRotate ? 0b10 : 0) | (canExcavate ? 0b100 : 0));
						}));

		this.canExcavateButton = this.addRenderableWidget(
				CycleButton.onOffBuilder(canRotate).create(this.width / 2 - 122, this.height / 2 - 5, 77, 20,
						Component.translatable("block.architect.excavate"), (p_169727_, p_169728_) -> {
							this.canExcavate = p_169728_;
							container.setting
									.set((isCreative ? 1 : 0) | (canRotate ? 0b10 : 0) | (canExcavate ? 0b100 : 0));
						}));
		this.addWidget(nameField);
		setInitialFocus(nameField);
	}

	@Override
	protected void drawBackgroundLayer(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		RenderSystem._setShaderTexture(0, TEXTURE_BASE);
		ICON_GUI.drawAt(pose, mainGui.rootElement);
		drawProgress(pose, RECT_PROGRESS, ICON_PROGRESS,
				// DeltaInt.getDynamic(container.deltaProgress, partialTicks),
				container.tile.deltaProgress.getDynamic(partialTicks), 1);
	}

	@Override
	protected void drawForegroundLayer(PoseStack pose, int mouseX, int mouseY) {
		nameField.renderButton(pose, mouseX, mouseY, 0);
	}

	@Override
	public void containerTick() {
		nameField.tick();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		return nameField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public boolean keyPressed(int a, int b, int c) {
		if (a == 256) {
			this.minecraft.player.closeContainer();
		}
		return !this.nameField.keyPressed(a, b, c) && !this.nameField.canConsumeInput() ? super.keyPressed(a, b, c)
				: true;
	}
}
