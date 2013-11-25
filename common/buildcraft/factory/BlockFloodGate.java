/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import buildcraft.api.tools.IToolWrench;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.utils.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class BlockFloodGate extends BlockBuildCraft {

	private Icon textureTop;
	private Icon textureBottom;
	private Icon textureSide;

	public BlockFloodGate(int i) {
		super(i, Material.iron);
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new TileFloodGate();
	}

	@Override
	public Icon getIcon(int i, int j) {
		switch (i) {
			case 0:
				return textureBottom;
			case 1:
				return textureTop;
			default:
				return textureSide;
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
		Utils.preDestroyBlock(world, x, y, z);
		super.breakBlock(world, x, y, z, par5, par6);
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
		TileEntity tile = world.getBlockTileEntity(i, j, k);

		if (tile instanceof TileFloodGate) {
			TileFloodGate floodGate = (TileFloodGate) tile;

			// Drop through if the player is sneaking
			if (entityplayer.isSneaking())
				return false;

			// Restart the quarry if its a wrench
			Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
			if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(entityplayer, i, j, k)) {

				floodGate.rebuildQueue();
				((IToolWrench) equipped).wrenchUsed(entityplayer, i, j, k);
				return true;
			}
		}

		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int id) {
		super.onNeighborBlockChange(world, x, y, z, id);
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if (tile instanceof TileFloodGate) {
			((TileFloodGate) tile).onNeighborBlockChange(id);
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister) {
		textureTop = par1IconRegister.registerIcon("buildcraft:floodgate_top");
		textureBottom = par1IconRegister.registerIcon("buildcraft:floodgate_bottom");
		textureSide = par1IconRegister.registerIcon("buildcraft:floodgate_side");
	}
}
