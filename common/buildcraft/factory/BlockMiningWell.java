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
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftFactory;
import buildcraft.api.core.Position;
import buildcraft.core.utils.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * BlockMiningWell
 * 
 * Mines out a 1 block well down to bedrock
 * 
 * @author SpaceToad
 * @author BuildCraft team
 */
public class BlockMiningWell extends BlockMachineRoot {

	/** Icon array containing the icons used for this block */
	private Icon[] iconBuffer;

	public BlockMiningWell(int id) {
		super(id, Material.iron);

		this.setHardness(1.5F);
		this.setResistance(10F);
		this.setStepSound(soundStoneFootstep);
	}

	/**
	 * Returns the icon for each side of the block.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta) {
		if (meta == 0 && side == 3 || side == meta)
			return iconBuffer[2];
		
		if (meta < 6 && ForgeDirection.values()[meta].getOpposite().ordinal() == side)
			return iconBuffer[0];
		
		switch(side) {
			case 0:
				return iconBuffer[0];
			case 1:
				return iconBuffer[1];
			default:
				return iconBuffer[3];
		}
	}
	
	/**
	 * Registers the blocks icons with the IconRegister.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister) {
		iconBuffer = new Icon[4];
		
		iconBuffer[0] = par1IconRegister.registerIcon("buildcraft:miningwell_back");
		iconBuffer[1] = par1IconRegister.registerIcon("buildcraft:miningwell_top");
		iconBuffer[2] = par1IconRegister.registerIcon("buildcraft:miningwell_front");
		iconBuffer[3] = par1IconRegister.registerIcon("buildcraft:miningwell_side");
	}
	
	/**
	 * Called when the block is placed in the world.
	 */
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entity, ItemStack stack) {
		ForgeDirection orientation = Utils.get2dOrientation(new Position(entity.posX, entity.posY, entity.posZ), new Position(x, y, z));
		world.setBlockMetadataWithNotify(x, y, z, orientation.getOpposite().ordinal(), 1);
	}

	/**
	 * Called when the block is broken.
	 */
	@Override
	public void breakBlock(World world, int x, int y, int z, int id, int meta) {
		super.breakBlock(world, x, y, z, id, meta);

		for (int depth = y - 1; depth > 0; depth--) {
			int pipeID = world.getBlockId(x, depth, z);
			if (pipeID != BuildCraftFactory.plainPipeBlock.blockID) {
				break;
			}
			world.setBlock(x, depth, z, 0);
		}
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
		return new TileMiningWell();
	}
	
	/**
	 * Returns true if this block has a TileEntity
	 */
	@Override
	public boolean hasTileEntity(int meta) {
		return true;
	}
}