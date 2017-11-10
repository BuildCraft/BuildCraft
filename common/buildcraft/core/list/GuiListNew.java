/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.list;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import buildcraft.BuildCraftCore;
import buildcraft.api.lists.ListMatchHandler;
import buildcraft.core.ItemList;
import buildcraft.core.lib.gui.AdvancedSlot;
import buildcraft.core.lib.gui.GuiAdvancedInterface;
import buildcraft.core.lib.gui.buttons.GuiImageButton;
import buildcraft.core.lib.gui.buttons.IButtonClickEventListener;
import buildcraft.core.lib.gui.buttons.IButtonClickEventTrigger;
import buildcraft.core.lib.inventory.StackHelper;

public class GuiListNew extends GuiAdvancedInterface implements IButtonClickEventListener {
	private static final ResourceLocation TEXTURE_BASE = new ResourceLocation(
			"buildcraftcore:textures/gui/list_new.png");
	private static final int BUTTON_COUNT = 3;

	private final Map<Integer, Map<ListMatchHandler.Type, List<ItemStack>>> exampleCache = new HashMap<Integer, Map<ListMatchHandler.Type, List<ItemStack>>>();
	private GuiTextField textField;
	private EntityPlayer player;

	private static class ListSlot extends AdvancedSlot {
		public int lineIndex;
		public int slotIndex;

		public ListSlot(GuiAdvancedInterface gui, int x, int y, int iLineIndex, int iSlotIndex) {
			super(gui, x, y);

			lineIndex = iLineIndex;
			slotIndex = iSlotIndex;
		}

		@Override
		public ItemStack getItemStack() {
			ContainerListNew container = (ContainerListNew) gui.getContainer();
			if (slotIndex == 0 || !container.lines[lineIndex].isOneStackMode()) {
				return container.lines[lineIndex].getStack(slotIndex);
			} else {
				List<ItemStack> data = ((GuiListNew) gui).getExamplesList(lineIndex, container.lines[lineIndex].getSortingType());
				if (data.size() >= slotIndex) {
					return data.get(slotIndex - 1);
				} else {
					return null;
				}
			}
		}

		@Override
		public void drawSprite(int cornerX, int cornerY) {
			if (!shouldDrawHighlight()) {
				Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE_BASE);
				gui.drawTexturedModalRect(cornerX + x, cornerY + y, 176, 0, 16, 16);
			}

			super.drawSprite(cornerX, cornerY);
		}

		@Override
		public boolean shouldDrawHighlight() {
			ContainerListNew container = (ContainerListNew) gui.getContainer();
			return slotIndex == 0 || !container.lines[lineIndex].isOneStackMode();
		}
	}

	public GuiListNew(EntityPlayer iPlayer) {
		super(new ContainerListNew(iPlayer), iPlayer.inventory, TEXTURE_BASE);

		xSize = 176;
		ySize = 191;

		player = iPlayer;
	}

	private void clearExamplesCache(int lineId) {
		Map<ListMatchHandler.Type, List<ItemStack>> exampleList = exampleCache.get(lineId);
		if (exampleList != null) {
			exampleList.clear();
		}
	}

	private List<ItemStack> getExamplesList(int lineId, ListMatchHandler.Type type) {
		Map<ListMatchHandler.Type, List<ItemStack>> exampleList = exampleCache.get(lineId);
		if (exampleList == null) {
			exampleList = new EnumMap<ListMatchHandler.Type, List<ItemStack>>(ListMatchHandler.Type.class);
			exampleCache.put(lineId, exampleList);
		}

		ContainerListNew container = (ContainerListNew) getContainer();

		if (!exampleList.containsKey(type)) {
			List<ItemStack> examples = container.lines[lineId].getExamples();
			ItemStack input = container.lines[lineId].stacks[0];
			if (input != null) {
				List<ItemStack> repetitions = new ArrayList<ItemStack>();
				for (ItemStack is : examples) {
					if (StackHelper.isMatchingItem(input, is, true, false)) {
						repetitions.add(is);
					}
				}
				examples.removeAll(repetitions);
			}
			exampleList.put(type, examples);
		}
		return exampleList.get(type);
	}

	@Override
	public void initGui() {
		super.initGui();

		exampleCache.clear();
		slots.clear();
		buttonList.clear();

		for (int sy = 0; sy < ListHandlerNew.HEIGHT; sy++) {
			for (int sx = 0; sx < ListHandlerNew.WIDTH; sx++) {
				slots.add(new ListSlot(this, 8 + sx * 18, 32 + sy * 33, sy, sx));
			}
			int bOff = sy * BUTTON_COUNT;
			int bOffX = this.guiLeft + 8 + ListHandlerNew.WIDTH * 18 - BUTTON_COUNT * 11;
			int bOffY = this.guiTop + 32 + sy * 33 + 18;

			buttonList.add(new GuiImageButton(bOff + 0, bOffX, bOffY, 11, TEXTURE_BASE, 176, 16, 176, 28));
			buttonList.add(new GuiImageButton(bOff + 1, bOffX + 11, bOffY, 11, TEXTURE_BASE, 176, 16, 185, 28));
			buttonList.add(new GuiImageButton(bOff + 2, bOffX + 22, bOffY, 11, TEXTURE_BASE, 176, 16, 194, 28));
		}

		for (Object o : buttonList) {
			GuiImageButton b = (GuiImageButton) o;
			int lineId = b.id / BUTTON_COUNT;
			int buttonId = b.id % BUTTON_COUNT;
			if (((ContainerListNew) getContainer()).lines[lineId].getOption(buttonId)) {
				b.activate();
			}

			b.registerListener(this);
		}

		textField = new GuiTextField(this.fontRendererObj, 10, 10, 156, 12);
		textField.setMaxStringLength(32);
		textField.setText(BuildCraftCore.listItem.getLabel(player.getCurrentEquippedItem()));
		textField.setFocused(false);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		super.drawGuiContainerBackgroundLayer(f, x, y);

		ContainerListNew containerL = (ContainerListNew) getContainer();
		for (int i = 0; i < 2; i++) {
			if (containerL.lines[i].isOneStackMode()) {
				drawTexturedModalRect(guiLeft + 6, guiTop + 30 + i * 33, 0, ySize, 20, 20);
			}
		}

		drawBackgroundSlots(x, y);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);

		textField.drawTextBox();
		drawTooltipForSlotAt(par1, par2);
	}

	private boolean isCarryingNonEmptyList() {
		ItemStack stack = mc.thePlayer.inventory.getItemStack();
		return stack != null && stack.getItem() instanceof ItemList && stack.getTagCompound() != null;
	}

	private boolean hasListEquipped() {
		return mc.thePlayer.getCurrentEquippedItem() != null && mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemList;
	}

	@Override
	protected void mouseClicked(int x, int y, int b) {
		super.mouseClicked(x, y, b);

		if (isCarryingNonEmptyList() || !hasListEquipped()) {
			return;
		}

		AdvancedSlot slot = getSlotAtLocation(x, y);
		ContainerListNew container = (ContainerListNew) getContainer();

		if (slot instanceof ListSlot) {
			container.setStack(((ListSlot) slot).lineIndex, ((ListSlot) slot).slotIndex, mc.thePlayer.inventory.getItemStack());
			clearExamplesCache(((ListSlot) slot).lineIndex);
		}

		textField.mouseClicked(x - guiLeft, y - guiTop, b);
	}

	@Override
	public void handleButtonClick(IButtonClickEventTrigger sender, int id) {
		int buttonId = id % BUTTON_COUNT;
		int lineId = id / BUTTON_COUNT;

		ContainerListNew container = (ContainerListNew) getContainer();
		container.switchButton(lineId, buttonId);
		clearExamplesCache(lineId);
	}

	@Override
	protected void keyTyped(char c, int i) {
		if (textField.isFocused()) {
			if (c == 13 || c == 27) {
				textField.setFocused(false);
			} else {
				textField.textboxKeyTyped(c, i);
				((ContainerListNew) container).setLabel(textField.getText());
			}
		} else {
			super.keyTyped(c, i);
		}
	}
}