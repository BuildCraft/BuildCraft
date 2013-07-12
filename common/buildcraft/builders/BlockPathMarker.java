/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.builders;

import buildcraft.core.utils.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockPathMarker extends BlockMarker {

	private Icon activeMarker;

    public BlockPathMarker(int i) {
		super(i);
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new TilePathMarker();
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
		Utils.preDestroyBlock(world, x, y, z);
		super.breakBlock(world, x, y, z, par5, par6);
	}

	@SuppressWarnings({ "all" })
	// @Override (client only)
	public Icon getBlockTexture(IBlockAccess iblockaccess, int i, int j, int k, int l) {
		TilePathMarker marker = (TilePathMarker) iblockaccess.getBlockTileEntity(i, j, k);

		if (l == 1 || (marker != null && marker.tryingToConnect))
			return activeMarker;
		else
			return super.getBlockTexture(iblockaccess, i, j, k, l);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
	    blockIcon = par1IconRegister.registerIcon("buildcraft:blockPathMarker");
	    activeMarker = par1IconRegister.registerIcon("buildcraft:blockPathMarkerActive");
	}
}
