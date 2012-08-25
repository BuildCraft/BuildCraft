package buildcraft.transport;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.Orientations;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.ProxyCore;

import net.minecraft.src.Block;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class ItemFacade extends ItemBuildCraft {

	public final static LinkedList<ItemStack> allFacades = new LinkedList<ItemStack>();
	
	public ItemFacade(int i) {
		super(i);

		setHasSubtypes(true);
		setMaxDamage(0);
		this.setTabToDisplayOn(CreativeTabs.tabMisc);
	}
	
	@Override
	public String getItemDisplayName(ItemStack itemstack) {
		String name = super.getItemDisplayName(itemstack);
		int decodedBlockId = ItemFacade.getBlockId(itemstack.getItemDamage());
		int decodedMeta = ItemFacade.getMetaData(itemstack.getItemDamage());
		ItemStack newStack = new ItemStack(decodedBlockId, 1, decodedMeta);
		if (Item.itemsList[decodedBlockId] != null){
			name += ": " + ProxyCore.proxy.getItemDisplayName(newStack);
		} else {
			name += " < BROKEN (" + decodedBlockId + ":"+ decodedMeta +" )>";
		}
		return name; 
	}

	@Override
	public String getItemNameIS(ItemStack itemstack) {
		return "item.Facade";
	}

	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List itemList) {
		//Do not call super, that would add a 0:0 facade
		for (ItemStack stack : allFacades){
			itemList.add(stack.copy());
		}
	}
	
	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World worldObj, int x, int y, int z, int side) {
		if (worldObj.isRemote) return false;
		TileEntity tile = worldObj.getBlockTileEntity(x, y, z);
		if (!(tile instanceof TileGenericPipe)) return false;
		TileGenericPipe pipeTile = (TileGenericPipe)tile;

		if (player.isSneaking()) { //Strip facade
			if (!pipeTile.hasFacade(Orientations.dirs()[side])) return false;
			pipeTile.dropFacade(Orientations.dirs()[side]);
			return true;
		} else {
			if (((TileGenericPipe)tile).addFacade(Orientations.values()[side], ItemFacade.getBlockId(stack.getItemDamage()), ItemFacade.getMetaData(stack.getItemDamage()))){
				stack.stackSize--;	
				return true;
			}
			return false;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static void initialize(){
		List creativeItems = getCreativeContents();
		ListIterator creativeIterator = creativeItems.listIterator();
		
		while(creativeIterator.hasNext()){
			ItemStack stack = (ItemStack) creativeIterator.next();
			if (stack.getItem() instanceof ItemBlock){
				ItemBlock itemBlock = (ItemBlock) stack.getItem();
				int blockId = itemBlock.getBlockID();
				//Block certain IDs (Bedrock, leaves and spunge)
				if (blockId == 7 || blockId == 18 || blockId == 19) continue; 

				if (Block.blocksList[blockId] != null 
					&& Block.blocksList[blockId].isOpaqueCube() 
					&& Block.blocksList[blockId].getBlockName() != null 
					&& !Block.blocksList[blockId].hasTileEntity(0) 
					&& Block.blocksList[blockId].renderAsNormalBlock())
				{
					allFacades.add(new ItemStack(BuildCraftTransport.facadeItem, 1, ItemFacade.encode(blockId, stack.getItemDamage())));
					
					//3 Structurepipes + this block makes 6 facades
					AssemblyRecipe.assemblyRecipes.add(
							new AssemblyRecipe(
									new ItemStack[] {new ItemStack(BuildCraftTransport.pipeStructureCobblestone, 3), new ItemStack(blockId, 1, stack.getItemDamage())}, 
									8000, 
									new ItemStack(BuildCraftTransport.facadeItem, 6, ItemFacade.encode(blockId,  stack.getItemDamage()))));
				}
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static List getCreativeContents(){
		List itemList = new ArrayList();
		
		for (Block block : Block.blocksList)
        {
            if (block != null)
            {
            	block.getSubBlocks(block.blockID, null, itemList);
            }
        }
        return itemList;
	}
	
	public static int encode(int blockId, int metaData){
		return metaData + (blockId << 4);
	}
	
	public static int getMetaData(int encoded){
		return encoded & 0x0000F;
	}
	
	public static int getBlockId(int encoded){
		return encoded >>> 4;
	}
	
	
}

