package net.minecraft.src.buildcraft.silicon;

import java.util.ArrayList;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraftSilicon;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.core.GuiIds;
import net.minecraft.src.buildcraft.factory.TileAssemblyTable;
import net.minecraft.src.forge.ITextureProvider;

public class BlockAssemblyTable extends BlockContainer implements ITextureProvider {

	public BlockAssemblyTable(int i) {
		super(i, Material.iron);
		// TODO Auto-generated constructor stub
		
		setBlockBounds(0, 0, 0, 1, 9F / 16F, 1);
		setHardness(0.5F);
		
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	public boolean isACube() {
		return false;
	}
	
	@Override
	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
		// Drop through if the player is sneaking
		if(entityplayer.isSneaking())
			return false;
		
		if(!APIProxy.isClient(world))
			entityplayer.openGui(mod_BuildCraftSilicon.instance, GuiIds.ASSEMBLY_TABLE, world, i, j, k);
		return true;
	}
	
	@Override
	public int getBlockTextureFromSideAndMetadata(int i, int j) {
		if (i == 1) {
			return 16 * 6 + 12;	
		} else if (i == 0) {
			return 16 * 2 + 15;	
		} else {
			return 16 * 6 + 11;
		}
		
	}

	@Override
	public TileEntity getBlockEntity() {
		return new TileAssemblyTable();
	}

	@Override
	public String getTextureFile() {
		return BuildCraftCore.customBuildCraftTexture;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}
}
