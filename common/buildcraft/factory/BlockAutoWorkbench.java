/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.factory;

import java.util.ArrayList;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import buildcraft.BuildCraftFactory;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.GuiIds;
import buildcraft.core.IItemPipe;
import buildcraft.core.proxy.CoreProxy;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockAutoWorkbench extends BlockBuildCraft {
	private Icon topTexture;
	private Icon sideTexture;

	public BlockAutoWorkbench(int blockID) {
		super(blockID, Material.wood);
		this.setHardness(1);
	}

	@Override
	public Icon getIcon(int side, int metadata) {
		if (side == 1 || side == 0) {
			return topTexture;
		}else{
			return sideTexture;
		}
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float blockX, float blockY, float blockZ) {
		if (player.isSneaking() || (player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() instanceof IItemPipe)) {
				return false;
		}
		
		if (!CoreProxy.proxy.isRenderWorld(world)) {
			player.openGui(BuildCraftFactory.instance, GuiIds.AUTO_CRAFTING_TABLE, world, x, y, z);
		}

		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileAutoWorkbench();
	}
	
	@Override
	public void addCreativeItems(ArrayList list) {
		list.add(new ItemStack(this));
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
	    topTexture = register.registerIcon("buildcraft:autoWorkbench_top");
	    sideTexture = register.registerIcon("buildcraft:autoWorkbench_side");
	}
}
