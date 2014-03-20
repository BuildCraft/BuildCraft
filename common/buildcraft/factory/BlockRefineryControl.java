package buildcraft.factory;

import buildcraft.BuildCraftFactory;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.GuiIds;
import buildcraft.core.IItemPipe;
import buildcraft.core.utils.MultiBlockCheck;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockRefineryControl extends BlockContainer{
	
	public BlockRefineryControl(){
		super(Material.iron);
		setHardness(5F);
		setCreativeTab(CreativeTabBuildCraft.TIER_2.get());
	}

	@Override
	public TileEntity createNewTileEntity(World world, int var) {
		return new TileRefineryControl();
	}
	@Override
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLivingBase entityliving, ItemStack stack){
		if (MultiBlockCheck.isPartOfAMultiBlock("refinery", i, j, k, world)){
			System.out.println("ok");
		}
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ){
		// Drop through if the player is sneaking
				if (player.isSneaking()) {
					return false;
				}

				if (player.getCurrentEquippedItem() != null) {
					if (player.getCurrentEquippedItem().getItem() instanceof IItemPipe) {
						return false;
					}
				}

				if (!world.isRemote) {
					player.openGui(BuildCraftFactory.instance, GuiIds.REFINERY_CONTROL, world, x, y, z);
				}

				return true;
	}
	

}
