package buildcraft.silicon.gui;

import java.util.Iterator;
import java.util.LinkedList;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.IInventory;
import net.minecraft.src.IRecipe;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.core.DefaultProps;
import buildcraft.core.gui.BuildCraftContainer;
import buildcraft.core.gui.GuiAdvancedInterface;
import buildcraft.core.gui.GuiAdvancedInterface.AdvancedSlot;
import buildcraft.core.gui.GuiAdvancedInterface.IInventorySlot;
import buildcraft.core.network.PacketCoordinates;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketSlotChange;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.StringUtil;
import buildcraft.silicon.TileAssemblyAdvancedWorkbench;
import buildcraft.silicon.TileAssemblyTable;
import buildcraft.silicon.TileAssemblyTable.SelectionMessage;
import buildcraft.silicon.gui.GuiAssemblyTable.AssemblyLedger;
import buildcraft.silicon.gui.GuiAssemblyTable.RecipeSlot;

public class GuiAssemblyAdvancedWorkbench extends GuiAdvancedInterface {
	class AssemblyWorkbenchLedger extends Ledger
	{
		int headerColour = 0xe1c92f;
		int subheaderColour = 0xaaafb8;
		int textColour = 0x000000;

		public AssemblyWorkbenchLedger() {
			maxHeight = 94;
			overlayColor = 0xd46c1f;
		}
		@Override
		public void draw(int x, int y) {

			// Draw background
			drawBackground(x, y);

			// Draw icon
			drawIcon(DefaultProps.TEXTURE_ICONS, 0, x + 3, y + 4);

			if (!isFullyOpened())
				return;

			fontRenderer.drawStringWithShadow(StringUtil.localize("gui.energy"), x + 22, y + 8, headerColour);
			fontRenderer.drawStringWithShadow(StringUtil.localize("gui.assemblyCurrentRequired") + ":", x + 22, y + 20, subheaderColour);
			fontRenderer.drawString(String.format("%2.1f MJ", workbench.getRequiredEnergy()), x + 22, y + 32, textColour);
			fontRenderer.drawStringWithShadow(StringUtil.localize("gui.stored") + ":", x + 22, y + 44, subheaderColour);
			fontRenderer.drawString(String.format("%2.1f MJ", workbench.getStoredEnergy()), x + 22, y + 56, textColour);
			fontRenderer.drawStringWithShadow(StringUtil.localize("gui.assemblyRate") + ":", x + 22, y + 68, subheaderColour);
			fontRenderer.drawString(String.format("%3.2f MJ/t",workbench.getRecentEnergyAverage() / 100.0f), x + 22, y + 80, textColour);

		}

		@Override
		public String getTooltip() {
			return String.format("%3.2f MJ/t",workbench.getRecentEnergyAverage() / 100.0f);
		}

	}

	class RecipeSlot extends AdvancedSlot {

		public IRecipe recipe;

		public RecipeSlot(int x, int y) {
			super(x, y);
		}

		@Override
		public ItemStack getItemStack() {
			if (this.recipe != null)
				return this.recipe.getRecipeOutput();
			else
				return null;
		}
	}

	TileAssemblyAdvancedWorkbench workbench;

	public GuiAssemblyAdvancedWorkbench(InventoryPlayer playerInventory, TileAssemblyAdvancedWorkbench advancedWorkbench) {
		super(new ContainerAssemblyAdvancedWorkbench(playerInventory, advancedWorkbench), advancedWorkbench);
		this.workbench = advancedWorkbench;
		xSize = 175;
		ySize = 240;


		slots = new AdvancedSlot[10];

		int id = 0;
		for (int k = 0; k < 3; k++)
			for (int j1 = 0; j1 < 3; j1++) {
				slots[id++] = new IInventorySlot(31 + j1 * 18, 16 + k * 18, workbench, j1 + k * 3 + 27);
			}

		slots[id] = new RecipeSlot(124, 35);
		// Request current selection from server
		if(CoreProxy.proxy.isRenderWorld(advancedWorkbench.worldObj))
			CoreProxy.proxy.sendToServer(new PacketCoordinates(PacketIds.SELECTION_ADVANCED_WORKBENCH_GET, advancedWorkbench.xCoord,
				advancedWorkbench.yCoord, advancedWorkbench.zCoord).getPacket());
	}


	@Override
	protected void drawGuiContainerForegroundLayer() {
		super.drawGuiContainerForegroundLayer();
		String title = StringUtil.localize("tile.assemblyWorkbenchBlock");
		fontRenderer.drawString(title, getCenteredOffset(title), 6, 0x404040);
		fontRenderer.drawString(StringUtil.localize("gui.inventory"), 8, ySize - 97, 0x404040);
		drawForegroundSelection();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		int i = mc.renderEngine.getTexture(DefaultProps.TEXTURE_PATH_GUI + "/assembly_advancedworkbench.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;
		drawTexturedModalRect(cornerX, cornerY, 0, 0, xSize, ySize);
		drawBackgroundSlots();
	}

	@Override
	protected void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);

		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;

		int position = getSlotAtLocation(i - cornerX, j - cornerY);

		IInventorySlot slot = null;

		if (position != -1)
			slot = (IInventorySlot) slots[position];

		if (slot != null) {
			ItemStack playerStack = mc.thePlayer.inventory.getItemStack();

			ItemStack newStack;
			if (playerStack != null)
				newStack = new ItemStack(playerStack.itemID, 1, playerStack.getItemDamage());
			else
				newStack = null;

			workbench.setInventorySlotContents(position, newStack);

			if (CoreProxy.proxy.isRenderWorld(workbench.worldObj)) {
				PacketSlotChange packet = new PacketSlotChange(PacketIds.SELECTION_ADVANCED_WORKBENCH_SEND, workbench.xCoord,
						workbench.yCoord, workbench.zCoord, position, newStack);
				CoreProxy.proxy.sendToServer(packet.getPacket());
			}
		}

	}
	@Override
	protected void initLedgers(IInventory inventory) {
		super.initLedgers(inventory);
		ledgerManager.add(new AssemblyWorkbenchLedger());
	}
}
