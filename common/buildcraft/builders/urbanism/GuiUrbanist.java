/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.urbanism;

import java.util.LinkedList;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.inventory.IInventory;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.core.DefaultProps;
import buildcraft.core.gui.AdvancedSlot;
import buildcraft.core.gui.GuiAdvancedInterface;

public class GuiUrbanist extends GuiAdvancedInterface {

	private static final ResourceLocation TOOLBAR_TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/urbanist_tools.png");
	private static final int TOOLBAR_TEXTURE_WIDTH = 194;
	private static final int TOOLBAR_TEXTURE_HEIGHT = 27;

	public TileUrbanist urbanist;
	public UrbanistTool[] tools = new UrbanistTool[10];

	private IInventory playerInventory;
	private int selectedTool = -1;

	class ToolSlot extends AdvancedSlot {
		UrbanistTool tool;

		public ToolSlot(UrbanistTool tool) {
			super(GuiUrbanist.this, 0, 0);

			this.tool = tool;
		}

		@SideOnly(Side.CLIENT)
		@Override
		public IIcon getIcon() {
			return tool.getIcon();
		}

		@Override
		public String getDescription() {
			return tool.getDescription();
		}

	}

	public GuiUrbanist(IInventory playerInventory, TileUrbanist urbanist) {
		super(new ContainerUrbanist(playerInventory, urbanist), urbanist, TOOLBAR_TEXTURE);

		xSize = width;
		ySize = height;

		this.playerInventory = playerInventory;
		this.urbanist = urbanist;

		slots = new AdvancedSlot[0];

		urbanist.createUrbanistEntity();

		tools [0] = new UrbanistToolBlock();
		tools [1] = new UrbanistToolErase();
		tools [2] = new UrbanistToolArea();
		tools [3] = new UrbanistToolPath();
		tools [4] = new UrbanistToolFiller();
		tools [5] = new UrbanistToolBlueprint();
		tools [6] = new UrbanistTool();
		tools [7] = new UrbanistTool();
		tools [8] = new UrbanistTool();
		tools [9] = new UrbanistTool();

		LinkedList<AdvancedSlot> tmpSlots = new LinkedList<AdvancedSlot>();

		for (int i = 0; i < 10; ++i) {
			tmpSlots.add(new ToolSlot(tools [i]));
		}

		for (UrbanistTool t : tools) {
			t.createSlots(this, tmpSlots);
		}

		slots = tmpSlots.toArray(new AdvancedSlot [tmpSlots.size()]);
	}

	@Override
	public void drawDefaultBackground() {
		// cancels the dark background
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float par3) {
		xSize = width;
		ySize = height;
		guiLeft = 0;
		guiTop = 0;

		super.drawScreen(mouseX, mouseY, par3);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);

		drawForegroundSelection(par1, par2);

		if (selectedTool != -1) {
			tools [selectedTool].drawGuiContainerForegroundLayer(this, par1, par2);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int cornerX = (width - TOOLBAR_TEXTURE_WIDTH) / 2;
		int cornerY = height - TOOLBAR_TEXTURE_HEIGHT;

		mc.renderEngine.bindTexture(TOOLBAR_TEXTURE);
		drawTexturedModalRect(cornerX, cornerY, 0, 0, TOOLBAR_TEXTURE_WIDTH, TOOLBAR_TEXTURE_HEIGHT);

		for (int i = 0; i < 10; ++i) {
			slots [i].x = cornerX + 8 + i * 18;
			slots [i].y = cornerY + 8;
		}

		if (selectedTool != -1) {
			tools [selectedTool].drawGuiContainerBackgroundLayer(this, f, x, y);
		}

		drawBackgroundSlots();

		if (selectedTool != -1) {
			tools [selectedTool].drawSelection(this, f, x, y);
		}

		if (selectedTool != -1) {
			mc.renderEngine.bindTexture(TOOLBAR_TEXTURE);
			drawTexturedModalRect(cornerX + 8 + selectedTool * 18, cornerY + 8, 194, 0, 18, 18);
		}
	}

	@Override
	public void onGuiClosed() {
		urbanist.destroyUrbanistEntity();
	}

	private boolean onInterface (int mouseX, int mouseY) {
		int cornerX = (width - TOOLBAR_TEXTURE_WIDTH) / 2;
		int cornerY = height - TOOLBAR_TEXTURE_HEIGHT;

		if (mouseX >= cornerX && mouseX <= cornerX + TOOLBAR_TEXTURE_WIDTH
			&& mouseY >= cornerY && mouseY <= cornerY + TOOLBAR_TEXTURE_HEIGHT) {

			return true;
		}

		if (selectedTool != -1 && tools [selectedTool].onInterface(mouseX, mouseY)) {
			return true;
		}

		return false;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (!onInterface(mouseX, mouseY)) {
			if (selectedTool != -1) {
				tools [selectedTool].worldClicked(this, urbanist.urbanist.rayTraceMouse());
			}

			return;
		}

		int clicked = getSlotAtLocation(mouseX, mouseY);

		if (clicked != -1 && clicked < 10) {
			if (clicked != selectedTool) {
				if (selectedTool != -1) {
					tools [selectedTool].hide();
				}

				selectedTool = clicked;
				tools [selectedTool].show();
			}
		}

		if (clicked != -1) {
			slots [clicked].selected();
		}
	}

	@Override
    public boolean doesGuiPauseGame() {
        return false;
    }

	@Override
	public void handleMouseInput() {
		super.handleMouseInput();

		int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
	    int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

	    if (onInterface(x, x)) {
			return;
		}

		if (selectedTool != -1) {
			tools [selectedTool].worldMoved(this, urbanist.urbanist.rayTraceMouse());
		}
	}
}
