package buildcraft.transport;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.Orientations;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.core.CoreProxy;
import buildcraft.core.ItemBuildCraft;

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
	}
	
	@Override
	public String getItemDisplayName(ItemStack itemstack) {
		String name = super.getItemDisplayName(itemstack);
		int decodedBlockId = ItemFacade.getBlockId(itemstack.getItemDamage());
		int decodedMeta = ItemFacade.getMetaData(itemstack.getItemDamage());
		ItemStack newStack = new ItemStack(decodedBlockId, 1, decodedMeta);
		if (Item.itemsList[decodedBlockId] != null){
			name += ": " + CoreProxy.getItemDisplayName(newStack);
		} else {
			name += " < BROKEN >";
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
		super.getSubItems(par1, par2CreativeTabs, itemList);
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
				//Block certain IDs
				if (blockId == 7 || blockId == 18 || blockId == 19) continue; 

				if (Block.blocksList[blockId] != null 
					&& Block.blocksList[blockId].isOpaqueCube() 
					&& Block.blocksList[blockId].getBlockName() != null 
					&& !Block.blocksList[blockId].hasTileEntity() 
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
		
//		Block[] var2 = new Block[] {Block.cobblestone, Block.stone, Block.oreDiamond, Block.oreGold, Block.oreIron, Block.oreCoal, Block.oreLapis, Block.oreRedstone, Block.stoneBrick, Block.stoneBrick, Block.stoneBrick, Block.stoneBrick, Block.blockClay, Block.blockDiamond, Block.blockGold, Block.blockSteel, Block.bedrock, Block.blockLapis, Block.brick, Block.cobblestoneMossy, Block.stairSingle, Block.stairSingle, Block.stairSingle, Block.stairSingle, Block.stairSingle, Block.stairSingle, Block.obsidian, Block.netherrack, Block.slowSand, Block.glowStone, Block.wood, Block.wood, Block.wood, Block.wood, Block.leaves, Block.leaves, Block.leaves, Block.leaves, Block.dirt, Block.grass, Block.sand, Block.sandStone, Block.sandStone, Block.sandStone, Block.gravel, Block.web, Block.planks, Block.planks, Block.planks, Block.planks, Block.sapling, Block.sapling, Block.sapling, Block.sapling, Block.deadBush, Block.sponge, Block.ice, Block.blockSnow, Block.plantYellow, Block.plantRed, Block.mushroomBrown, Block.mushroomRed, Block.cactus, Block.melon, Block.pumpkin, Block.pumpkinLantern, Block.vine, Block.fenceIron, Block.thinGlass, Block.netherBrick, Block.netherFence, Block.stairsNetherBrick, Block.whiteStone, Block.mycelium, Block.waterlily, Block.tallGrass, Block.tallGrass, Block.chest, Block.workbench, Block.glass, Block.tnt, Block.bookShelf, Block.cloth, Block.cloth, Block.cloth, Block.cloth, Block.cloth, Block.cloth, Block.cloth, Block.cloth, Block.cloth, Block.cloth, Block.cloth, Block.cloth, Block.cloth, Block.cloth, Block.cloth, Block.cloth, Block.dispenser, Block.stoneOvenIdle, Block.music, Block.jukebox, Block.pistonStickyBase, Block.pistonBase, Block.fence, Block.fenceGate, Block.ladder, Block.rail, Block.railPowered, Block.railDetector, Block.torchWood, Block.stairCompactPlanks, Block.stairCompactCobblestone, Block.stairsBrick, Block.stairsStoneBrickSmooth, Block.lever, Block.pressurePlateStone, Block.pressurePlatePlanks, Block.torchRedstoneActive, Block.button, Block.trapdoor, Block.enchantmentTable, Block.redstoneLampIdle};
//        int var3 = 0;
//        int var4 = 0;
//        int var5 = 0;
//        int var6 = 0;
//        int var7 = 0;
//        int var8 = 0;
//        int var9 = 0;
//        int var10 = 0;
//        int var11 = 1;
//        int var12;
//        int var13;
//
//        for (var12 = 0; var12 < var2.length; ++var12)
//        {
//            var13 = 0;
//
//            if (var2[var12] == Block.cloth)
//            {
//                var13 = var3++;
//            }
//            else if (var2[var12] == Block.stairSingle)
//            {
//                var13 = var4++;
//            }
//            else if (var2[var12] == Block.wood)
//            {
//                var13 = var5++;
//            }
//            else if (var2[var12] == Block.planks)
//            {
//                var13 = var6++;
//            }
//            else if (var2[var12] == Block.sapling)
//            {
//                var13 = var7++;
//            }
//            else if (var2[var12] == Block.stoneBrick)
//            {
//                var13 = var8++;
//            }
//            else if (var2[var12] == Block.sandStone)
//            {
//                var13 = var9++;
//            }
//            else if (var2[var12] == Block.tallGrass)
//            {
//                var13 = var11++;
//            }
//            else if (var2[var12] == Block.leaves)
//            {
//                var13 = var10++;
//            }
//
//            itemList.add(new ItemStack(var2[var12], 1, var13));
//        }
//
//        for (Block block : Block.blocksList)
//        {
//            if (block != null)
//            {
//                block.addCreativeItems((ArrayList) itemList);
//            }
//        }
//        
//        int x = 0;
//        for (Item item : Item.itemsList)
//        {
//            if (x++ >= 256 && item != null)
//            {
//                item.addCreativeItems((ArrayList) itemList);
//            }
//        }
        
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

