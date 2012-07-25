package buildcraft.factory;

import java.util.ArrayList;

import buildcraft.BuildCraftCore;
import buildcraft.mod_BuildCraftFactory;
import buildcraft.api.APIProxy;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.GuiIds;
import buildcraft.core.IItemPipe;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class BlockHopper extends BlockBuildCraft {

	public BlockHopper(int blockId) {
		super(blockId, Material.iron);
		setHardness(5F);
	}

	@Override
	public TileEntity getBlockEntity() {
		return new TileHopper();
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public int getRenderType() {
		return BuildCraftCore.blockByEntityModel;
	}

	@Override
	public int getBlockTextureFromSide(int par1) {
		return 1;
	}

	@Override
	public boolean blockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer) {
		super.blockActivated(world, x, y, z, entityPlayer);

		// Drop through if the player is sneaking
		if (entityPlayer.isSneaking())
			return false;

		if (entityPlayer.getCurrentEquippedItem() != null) {
			if (entityPlayer.getCurrentEquippedItem().getItem() instanceof IItemPipe) {
				return false;
			}
		}

		if (!APIProxy.isClient(world))
			entityPlayer.openGui(mod_BuildCraftFactory.instance, GuiIds.HOPPER, world, x, y, z);

		return true;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}

}
