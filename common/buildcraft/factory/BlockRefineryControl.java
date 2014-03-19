package buildcraft.factory;

import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.utils.MultiBlockCheck;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
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

}
