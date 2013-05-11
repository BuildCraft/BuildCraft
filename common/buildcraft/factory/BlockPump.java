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
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.utils.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * BlockPump
 * 
 * Designed to reach down and suck liquids up below the block.
 * 
 * @author SpaceToad
 * @author BuildCraft team
 */
public class BlockPump extends BlockMachineRoot {

	/** Icon array containing the icons used for this block */
	private Icon[] iconBuffer;

    public BlockPump(int id) {
		super(id, Material.iron);
		
		this.setHardness(5F);
		this.setCreativeTab(CreativeTabBuildCraft.tabBuildCraft);
		this.setStepSound(soundStoneFootstep);
	}

	/**
	 * Returns the icon for each side of the block.
	 */
	@Override
	public Icon getIcon(int side, int meta) {
		switch (side) {
			case 0:
				return iconBuffer[0];
			case 1:
				return iconBuffer[1];
			default:
				return iconBuffer[2];
		}
	}
	
	/**
	 * Registers the blocks icons with the IconRegister.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister ir) {
		iconBuffer = new Icon[3];
		iconBuffer[0] = ir.registerIcon("buildcraft:pump_bottom");
		iconBuffer[1] = ir.registerIcon("buildcraft:pump_top");
		iconBuffer[2] = ir.registerIcon("buildcraft:pump_side");
	}

	/**
	 * Called when the block is broken.
	 */
	@Override
	public void breakBlock(World world, int x, int y, int z, int id, int meta) {
		Utils.preDestroyBlock(world, x, y, z);
		super.breakBlock(world, x, y, z, id, meta);
	}

	/**
	 * Adds the item to the creative tab.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}

	/**
	 * Returns a new instance of the TileEntity for this block.
	 */
	@Override
	public TileEntity createTileEntity(World world, int meta) {
		return new TilePump();
	}
	
	/**
	 * Returns true if this block has a TileEntity
	 */
	@Override
	public boolean hasTileEntity(int meta) {
		return true;
	}
}