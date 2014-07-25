/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import buildcraft.core.DefaultProps;
import buildcraft.core.science.Technology;
import buildcraft.core.science.Tier;

public class GuiScienceBook extends GuiAdvancedInterface {

	private static final ResourceLocation TEXTURE_BASE = new ResourceLocation(
			"buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/science_gui.png");
	private static final ResourceLocation TEXTURE_FOCUS = new ResourceLocation(
			"buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/science_focus_gui.png");
	private static final ResourceLocation TEXTURE_ICONS = new ResourceLocation(
			"buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/science_icons.png");

	private Tier currentTier = null;

	private Technology inFocus = null;

	private List baseInventorySlots;

	static class EmptySlot extends AdvancedSlot {
		public EmptySlot(GuiAdvancedInterface gui, int x, int y) {
			super(gui, x, y);
		}
	}

	static class TechnologySlot extends AdvancedSlot {
		private Technology techno;

		public TechnologySlot(GuiAdvancedInterface gui, int x, int y, Technology iTechno) {
			super(gui, x, y);

			techno = iTechno;
		}

		@Override
		public IIcon getIcon() {
			return techno.getIcon();
		}

		@Override
		public ItemStack getItemStack() {
			return techno.getStackToDisplay();
		}
	}

	public GuiScienceBook(EntityPlayer player) {
		super(new ContainerScienceBook(player), player.inventory, TEXTURE_BASE);

		xSize = 256;
		ySize = 181;

		slots = new AdvancedSlot[50];

		baseInventorySlots = container.inventorySlots;

		setTier(Tier.WoodenGear);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;

		bindTexture(TEXTURE_ICONS);

		for (int i = 0; i < 7; ++i) {
			if (inFocus != null || currentTier.ordinal() != i) {
				drawTexturedModalRect(cornerX + 28 * i, cornerY - 28, 28 * i, 1, 29, 32);
			}

			if (inFocus != null || currentTier.ordinal() != i + 7) {
				drawTexturedModalRect(cornerX + 28 * i, cornerY + ySize - 4, 28 * i, 62, 29, 32);
			}
		}

		if (inFocus == null) {
			texture = TEXTURE_BASE;
		} else {
			texture = TEXTURE_FOCUS;
		}

		super.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);

		bindTexture(TEXTURE_ICONS);

		for (int i = 0; i < 7; ++i) {
			if (inFocus == null && currentTier.ordinal() == i) {
				drawTexturedModalRect(cornerX + 28 * i, cornerY - 28, 28 * i, 32, 29, 32);
			}

			if (inFocus == null && currentTier.ordinal() == i + 7) {
				drawTexturedModalRect(cornerX + 28 * i, cornerY + ySize - 4, 28 * i, 96, 29, 32);
			}
		}

		for (int i = 0; i < 7; ++i) {
			drawStack(Tier.values()[i].getStackToDisplay(), cornerX + 28 * i + 6, cornerY - 28 + 9);

			drawStack(Tier.values()[i + 7].getStackToDisplay(), cornerX + 28 * i + 6, cornerY + ySize - 4 + 7);
		}

		drawBackgroundSlots();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);

		drawTooltipForSlotAt(par1, par2);
	}



	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;

		for (int i = 0; i < 7; ++i) {
			int x1 = cornerX + 28 * i;
			int x2 = x1 + 29;
			int y1 = cornerY - 30;
			int y2 = y1 + 32;

			if (mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2) {
				setTier(Tier.values()[i]);
				return;
			}

			y1 = cornerY + ySize - 2;
			y2 = y1 + 32;

			if (mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2) {
				setTier(Tier.values()[i + 7]);
				return;
			}
		}
	}

	@Override
	protected void slotClicked(AdvancedSlot slot) {
		super.slotClicked(slot);

		if (slot instanceof TechnologySlot) {
			setFocus(((TechnologySlot) slot).techno);
		}
	}

	private void setTier(Tier newTier) {
		if (inFocus != null || newTier != currentTier) {
			slots = new AdvancedSlot[50];
			currentTier = newTier;

			int id = 0;

			Collection<Technology> technos = Technology.getTechnologies(currentTier);

			for (Technology t : technos) {
				int j = id / 10;
				int i = id - j * 10;

				slots[id] = new TechnologySlot(this, 9 + i * 18, 7 + j * 18, t);
				id++;
			}

			while (id < 50) {
				int j = id / 10;
				int i = id - j * 10;

				slots[id] = new EmptySlot(this, 9 + i * 18, 7 + j * 18);
				id++;
			}
		}

		inFocus = null;
		container.inventorySlots = baseInventorySlots;
	}

	private void setFocus(Technology techno) {
		inFocus = techno;
		container.inventorySlots = new ArrayList();
		slots = new AdvancedSlot[5 + 3 + 1 + 10];

		int id = 0;

		for (int i = 0; i < 5; ++i) {
			if (techno.getPrerequisites().size() > i
					&& techno.getPrerequisites().get(i) != null) {
				slots[id++] = new TechnologySlot(this, 33, 43 + 18 * i, techno.getPrerequisites().get(i));
			} else {
				id++;
			}
		}

		for (int i = 0; i < 3; ++i) {
			if (techno.getRequirements() != null) {
				slots[id++] = new ItemSlot(this, 71, 115 + 18 * i, techno.getRequirements()[i]);
			} else {
				id++;
			}
		}

		slots[id++] = new TechnologySlot(this, 89, 79, techno);

		for (int i = 0; i < 5; ++i) {
			for (int j = 0; j < 2; ++j) {
				int followupId = i * 2 + j;
				if (techno.getFollowups().size() > followupId
						&& techno.getFollowups().get(followupId) != null) {
					slots[id++] = new TechnologySlot(this, 145 + 18 * j, 43 + 18 * i, techno.getFollowups().get(
							followupId));
				} else {
					id++;
				}
			}
		}
	}
}
