/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import buildcraft.api.boards.IBoardParameter;
import buildcraft.api.boards.IBoardParameterStack;
import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.core.DefaultProps;
import buildcraft.core.gui.AdvancedSlot;
import buildcraft.core.gui.GuiAdvancedInterface;
import buildcraft.core.gui.ItemSlot;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.utils.NBTUtils;

public class GuiRedstoneBoard extends GuiAdvancedInterface {

	private static final ResourceLocation TEXTURE = new ResourceLocation(
			"buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/generic_ui.png");

	private World world;
	private int x, y, z;
	private RedstoneBoardNBT board;
	private IBoardParameter[] params;

	public GuiRedstoneBoard(EntityPlayer player, int ix, int iy, int iz) {
		super(new ContainerRedstoneBoard(player, ix, iy, iz), player.inventory, TEXTURE);
		x = ix;
		y = iy;
		z = iz;
		xSize = 175;
		ySize = 222;
		world = player.worldObj;

		NBTTagCompound boardNBT = NBTUtils.getItemData(player.getHeldItem());

		board = RedstoneBoardRegistry.instance.getRedstoneBoard(boardNBT);
		params = board.getParameters(boardNBT);

		slots = new AdvancedSlot[params.length];

		for (int i = 0; i < params.length; ++i) {
			slots[i] = new ItemSlot(this, 10, 10 + i * 20);
			slots[i].drawBackround = true;
			((ItemSlot) slots[i]).stack = ((IBoardParameterStack) params[i]).getStack();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		int xscreen = (width - xSize) / 2;
		int yscreen = (height - ySize) / 2;

	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);

		drawBackgroundSlots();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);

		drawForegroundSelection(par1, par2);
	}

	@Override
	protected void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);

		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;

		int position = getSlotAtLocation(i - cornerX, j - cornerY);

		AdvancedSlot slot = null;

		if (position < 0) {
			return;
		}

		slot = slots[position];

		if (slot instanceof ItemSlot) {
			ItemStack stackCopy = mc.thePlayer.inventory.getItemStack().copy();
			stackCopy.stackSize = 1;
			((ItemSlot) slot).stack = stackCopy;
			RPCHandler.rpcServer(container, "setParameterStack", position, stackCopy);
		}
	}
}
