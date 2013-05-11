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

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftFactory;
import buildcraft.api.core.Position;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.Box;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * BlockQuarry
 * 
 * Mines out a large area within a marked area specified.
 * 
 * @author SpaceToad
 * @author BuildCraft team
 */
public class BlockQuarry extends BlockMachineRoot {

	/** Icon array containing the icons used for this block */
	private Icon[] iconBuffer;

	public BlockQuarry(int id) {
		super(id, Material.iron);
		
		this.setHardness(1.5F);
		this.setResistance(10F);
		this.setStepSound(Block.soundStoneFootstep);
	}

	/**
	 * Returns the icon for each side of the block.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta) {
		if ((meta == 0 && side == 3) || side == meta) {
			return iconBuffer[0];
		}
		switch (side) {
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
		
		iconBuffer[0] = ir.registerIcon("buildcraft:quarry_front");
		iconBuffer[1] = ir.registerIcon("buildcraft:quarry_top");
		iconBuffer[2] = ir.registerIcon("buildcraft:quarry_side");
	}
	
	/**
	 * Called when the block is placed in the world.
	 */
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entity, ItemStack stack) {
		super.onBlockPlacedBy(world, x, y, z, entity, stack);
		ForgeDirection orientation = Utils.get2dOrientation(new Position(entity.posX, entity.posY, entity.posZ), new Position(x, y, z));

		world.setBlockMetadataWithNotify(x, y, z, orientation.getOpposite().ordinal(), 1);
		if (entity instanceof EntityPlayer) {
			TileQuarry tile = (TileQuarry) world.getBlockTileEntity(x, y, z);
			if (tile != null) {
				tile.placedBy = (EntityPlayer) entity;
			}
		}
	}

	public void searchFrames(World world, int x, int y, int z) {
		int width = 1;
		if (!world.checkChunksExist(x - width, y - width, z - width, x + width, y + width, z + width))
			return;

		int blockID = world.getBlockId(x, y, z);

		if (blockID != BuildCraftFactory.frameBlock.blockID) {
			return;
		}

		int meta = world.getBlockMetadata(x, y, z);

		if ((meta & 8) == 0) {
			world.setBlockMetadataWithNotify(x, y, z, meta | 8, 0);

			ForgeDirection[] directions = ForgeDirection.VALID_DIRECTIONS;

			for (ForgeDirection direction : directions) {
				switch (direction) {
					case UP:
						searchFrames(world, x, y + 1, z);
					case DOWN:
						searchFrames(world, x, y - 1, z);
					case SOUTH:
						searchFrames(world, x, y, z + 1);
					case NORTH:
						searchFrames(world, x, y, z - 1);
					case EAST:
						searchFrames(world, x + 1, y, z);
					case WEST:
					
					default:
						searchFrames(world, x - 1, y, z);
				}
			}
		}
	}

	/**
	 * Marks a frame block to decay
	 * 
	 * @param world The World
	 * @param x X position of the frame block
	 * @param y Y position of the frame block
	 * @param z Z position of the frame block
	 */
	private void markFrameForDecay(World world, int x, int y, int z) {
		if (world.getBlockId(x, y, z) == BuildCraftFactory.frameBlock.blockID) {
			world.setBlockMetadataWithNotify(x, y, z, 1, 0);
		}
	}

	/**
	 * Called when the block is broken.
	 */
	@Override
	public void breakBlock(World world, int i, int j, int k, int id, int meta) {

		if (!CoreProxy.proxy.isSimulating(world)) {
			return;
		}
		
		TileEntity tile = world.getBlockTileEntity(i, j, k);
		if (tile instanceof TileQuarry) {
			TileQuarry quarry = (TileQuarry) tile;
			Box box = quarry.box;
			if (box.isInitialized() && Integer.MAX_VALUE != box.xMax) {
				// X - Axis
				for (int x = box.xMin; x <= box.xMax; x++) {
					markFrameForDecay(world, x, box.yMin, box.zMin);
					markFrameForDecay(world, x, box.yMax, box.zMin);
					markFrameForDecay(world, x, box.yMin, box.zMax);
					markFrameForDecay(world, x, box.yMax, box.zMax);
				}

				// Z - Axis
				for (int z = box.zMin + 1; z <= box.zMax - 1; z++) {
					markFrameForDecay(world, box.xMin, box.yMin, z);
					markFrameForDecay(world, box.xMax, box.yMin, z);
					markFrameForDecay(world, box.xMin, box.yMax, z);
					markFrameForDecay(world, box.xMax, box.yMax, z);
				}

				// Y - Axis
				for (int y = box.yMin + 1; y <= box.yMax - 1; y++) {

					markFrameForDecay(world, box.xMin, y, box.zMin);
					markFrameForDecay(world, box.xMax, y, box.zMin);
					markFrameForDecay(world, box.xMin, y, box.zMax);
					markFrameForDecay(world, box.xMax, y, box.zMax);
				}
			}
			quarry.destroy();
		}
		Utils.preDestroyBlock(world, i, j, k);
		super.breakBlock(world, i, j, k, id, meta);
	}

	/**
	 * Called when a player right clicks on a block
	 */
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if (player.isSneaking()) {
			return false;
		}
		
		TileQuarry tile = (TileQuarry) world.getBlockTileEntity(x, y, z);

		if (tile != null) {
			return this.tryRestart(tile, player);
		}

		return false;
	}

	/**
	 * Attempts to restart a halted quarry.
	 * 
	 * @param tile The tile associated with the quarry
	 * @param player The player that clicked the quarry
	 * @return If the quarry was restarted.
	 */
	public boolean tryRestart(TileQuarry tile, EntityPlayer player) {
		Item equipped = player.getCurrentEquippedItem() != null ? player.getCurrentEquippedItem().getItem() : null;
		
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(player, tile.xCoord, tile.yCoord, tile.zCoord)) {

			tile.reinitalize();
			((IToolWrench) equipped).wrenchUsed(player, tile.xCoord, tile.yCoord, tile.zCoord);
			return true;
		}
		return false;
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
		return new TileQuarry();
	}
	
	/**
	 * Returns true if this block has a TileEntity
	 */
	@Override
	public boolean hasTileEntity(int meta) {
		return true;
	}

}
