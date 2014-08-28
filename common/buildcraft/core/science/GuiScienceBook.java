/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.science;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import buildcraft.core.DefaultProps;
import buildcraft.core.gui.AdvancedSlot;
import buildcraft.core.gui.ContainerScienceBook;
import buildcraft.core.gui.GuiAdvancedInterface;
import buildcraft.core.gui.ItemSlot;
import buildcraft.core.gui.slots.SlotHidden;

public class GuiScienceBook extends GuiAdvancedInterface {

	private static final ResourceLocation TEXTURE_BASE = new ResourceLocation(
			"buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/science_gui.png");
	private static final ResourceLocation TEXTURE_FOCUS = new ResourceLocation(
			"buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/science_focus_gui.png");
	private static final ResourceLocation TEXTURE_INFO = new ResourceLocation(
			"buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/science_blank.png");

	private static final ResourceLocation TEXTURE_ICONS = new ResourceLocation(
			"buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/science_icons.png");
	private static final ResourceLocation TEXTURE_TAB = new ResourceLocation(
			"buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/science_tab.png");

	private static final int EXTRA_ADVANCED_SLOTS = 1;

	private Tier currentTier = null;

	private Technology inFocus = null;

	private GuiButton startResearch;
	private GuiButton wiki;
	private ArrayList<String> infoText = new ArrayList<String>();

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

		@Override
		public String getDescription() {
			return techno.getLocalizedName();
		}

		@Override
		public void drawSprite(int cornerX, int cornerY) {
			super.drawSprite(cornerX, cornerY);

			if (!getBook().isKnown(techno)) {
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				int j1 = cornerX + x;
				int k1 = cornerY + y;
				GL11.glColorMask(true, true, true, false);

				int color;

				if (getBook().canBeResearched(techno)) {
					color = 0x550000FF;
				} else {
					color = 0x55FF0000;
				}

				((GuiScienceBook) gui).drawGradientRect(j1, k1, j1 + 16, k1 + 16, color, color);
				GL11.glColorMask(true, true, true, true);
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
			}
		}

		public TechnologyNBT getBook() {
			return ((ContainerScienceBook) gui.getContainer()).book;
		}
	}

	static class ResearchedSlot extends AdvancedSlot {
		public ResearchedSlot(GuiAdvancedInterface gui, int x, int y) {
			super(gui, x, y);
		}

		@Override
		public IIcon getIcon() {
			Technology t = getResearchedTechnology();

			if (t != null) {
				return t.getIcon();
			} else {
				return null;
			}
		}

		@Override
		public ItemStack getItemStack() {
			Technology t = getResearchedTechnology();

			if (t != null) {
				return t.getStackToDisplay();
			} else {
				return null;
			}
		}

		@Override
		public String getDescription() {
			Technology t = getResearchedTechnology();

			if (t != null) {
				return t.getLocalizedName();
			} else {
				return null;
			}

		}

		public Technology getResearchedTechnology() {
			return ((ContainerScienceBook) gui.getContainer()).book.getResearchedTechnology();
		}
	}

	public GuiScienceBook(EntityPlayer player) {
		super(new ContainerScienceBook(player), player.inventory, TEXTURE_BASE);

		xSize = 256;
		ySize = 181;

		resetNullSlots(50);

		setTier(Tier.WoodenGear);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();

		startResearch = new GuiButton(0, guiLeft + 10, guiTop + 145, 70, 20, "Start");
		wiki = new GuiButton(0, guiLeft + 115, guiTop + 145, 70, 20, "Wiki");
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
		bindTexture(TEXTURE_ICONS);

		for (int i = 0; i < 7; ++i) {
			if (inFocus != null || currentTier.ordinal() != i) {
				drawTexturedModalRect(guiLeft + 28 * i, guiTop - 28, 28 * i, 1, 29, 32);
			}

			if (inFocus != null || currentTier.ordinal() != i + 7) {
				drawTexturedModalRect(guiLeft + 28 * i, guiTop + ySize - 4, 28 * i, 62, 29, 32);
			}
		}

		bindTexture(TEXTURE_TAB);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		super.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);

		bindTexture(TEXTURE_ICONS);

		for (int i = 0; i < 7; ++i) {
			if (inFocus == null && currentTier.ordinal() == i) {
				drawTexturedModalRect(guiLeft + 28 * i, guiTop - 28, 28 * i, 32, 29, 32);
			}

			if (inFocus == null && currentTier.ordinal() == i + 7) {
				drawTexturedModalRect(guiLeft + 28 * i, guiTop + ySize - 4, 28 * i, 96, 29, 32);
			}
		}

		int arrowHeight = (int) (22 * getContainer().progress);
		drawTexturedModalRect(
				guiLeft + 215,
				guiTop + 73 + (22 - arrowHeight),
				0,
				128 + (22 - arrowHeight),
				16,
				arrowHeight);

		for (int i = 0; i < 7; ++i) {
			drawStack(Tier.values()[i].getStackToDisplay(), guiLeft + 28 * i + 6, guiTop - 28 + 9);

			drawStack(Tier.values()[i + 7].getStackToDisplay(), guiLeft + 28 * i + 6, guiTop + ySize - 4 + 7);
		}

		drawBackgroundSlots();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);

		if (infoText != null) {
			for (int i = 0; i < infoText.size(); ++i) {
				fontRendererObj.drawString(infoText.get(i), 10, 25 + i * 10, 0x404040);
			}
		}

		drawTooltipForSlotAt(par1, par2);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button == startResearch) {
			getContainer().startResearch(inFocus);
		} else if (button == wiki) {
			try {
				java.awt.Desktop.getDesktop().browse(
						java.net.URI.create(inFocus.getWikiLink()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		for (int i = 0; i < 7; ++i) {
			int x1 = guiLeft + 28 * i;
			int x2 = x1 + 29;
			int y1 = guiTop - 30;
			int y2 = y1 + 32;

			if (mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2) {
				setTier(Tier.values()[i]);
				return;
			}

			y1 = guiTop + ySize - 2;
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
		texture = TEXTURE_BASE;

		if (inFocus != null || newTier != currentTier) {
			resetNullSlots(50);

			currentTier = newTier;

			int id = 0;

			Collection<Technology> technos = Technology.getTechnologies(currentTier);

			for (Technology t : technos) {
				int j = id / 10;
				int i = id - j * 10;

				slots.set(id, new TechnologySlot(this, 9 + i * 18, 7 + j * 18, t));
				id++;
			}

			while (id < 50) {
				int j = id / 10;
				int i = id - j * 10;

				slots.set(id, new EmptySlot(this, 9 + i * 18, 7 + j * 18));
				id++;
			}
		}

		inFocus = null;

		for (Object s : container.inventorySlots) {
			if (s instanceof SlotHidden) {
				SlotHidden h = (SlotHidden) s;

				h.show();
			}
		}

		buttonList.clear();

		setExtraAdvancedSlots();
	}

	private void setFocus(Technology techno) {
		inFocus = techno;
		texture = TEXTURE_FOCUS;

		for (Object s : container.inventorySlots) {
			if (s instanceof SlotHidden) {
				SlotHidden h = (SlotHidden) s;

				h.hide();
			}
		}

		slots.clear();

		for (int i = 0; i < 5; ++i) {
			if (techno.getPrerequisites().size() > i
					&& techno.getPrerequisites().get(i) != null) {
				slots.add(new TechnologySlot(this, 33, 43 + 18 * i, techno.getPrerequisites().get(i)));
			} else {
				slots.add(null);
			}
		}

		for (int i = 0; i < 3; ++i) {
			if (techno.getRequirements() != null) {
				slots.add(new ItemSlot(this, 71 + 18 * i, 115, techno.getRequirements()[i]));
			} else {
				slots.add(null);
			}
		}

		slots.add(new TechnologySlot(this, 89, 79, techno));

		for (int i = 0; i < 5; ++i) {
			for (int j = 0; j < 2; ++j) {
				int followupId = i * 2 + j;
				if (techno.getFollowups().size() > followupId
						&& techno.getFollowups().get(followupId) != null) {
					slots.add(new TechnologySlot(this, 145 + 18 * j, 43 + 18 * i, techno.getFollowups().get(
							followupId)));
				} else {
					slots.add(null);
				}
			}
		}

		buttonList.clear();

		if (getContainer().book.canBeResearched(techno)) {
			buttonList.add(startResearch);
		}

		if (wiki != null) {
			buttonList.add(wiki);
		}

		setExtraAdvancedSlots();
	}

	@Override
	public ContainerScienceBook getContainer() {
		return (ContainerScienceBook) super.getContainer();
	}

	public void setExtraAdvancedSlots() {
		slots.add(new ResearchedSlot(this, 216, 28));
	}

	@Override
	public void drawGradientRect(int p1, int p2, int p3, int p4, int p5, int p6) {
		super.drawGradientRect(p1, p2, p3, p4, p5, p6);
	}
}