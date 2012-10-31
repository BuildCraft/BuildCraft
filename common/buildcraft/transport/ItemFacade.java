package buildcraft.transport;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.Orientations;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.proxy.CoreProxy;

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
		this.setCreativeTab(CreativeTabs.tabMisc);
	}

	@Override
	public String getItemDisplayName(ItemStack itemstack) {
		String name = super.getItemDisplayName(itemstack);
		int decodedBlockId = ItemFacade.getBlockId(itemstack.getItemDamage());
		int decodedMeta = ItemFacade.getMetaData(itemstack.getItemDamage());
		ItemStack newStack = new ItemStack(decodedBlockId, 1, decodedMeta);
		if (Item.itemsList[decodedBlockId] != null){
			name += ": " + CoreProxy.proxy.getItemDisplayName(newStack);
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
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World worldObj, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
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
				if (!player.capabilities.isCreativeMode)
					stack.stackSize--;
				return true;
			}
			return false;
		}
	}

	@SuppressWarnings("rawtypes")
	public static void initialize(){
		for (Field f : Block.class.getDeclaredFields())
		{
		    if (Modifier.isStatic(f.getModifiers()) && Block.class.isAssignableFrom(f.getType()))
		    {
		        Block b;
                        try {
                            b = (Block) f.get(null);
                        } catch (Exception e) {
                            continue;
                        }
                        if (b.blockID == 7 || b.blockID == 18 || b.blockID == 19 || b.blockID == 95) continue;
                        if (!b.isOpaqueCube() || b.hasTileEntity(0)|| !b.renderAsNormalBlock()) continue;
                        ItemStack base = new ItemStack(b,1);
                        if (base.getHasSubtypes()) {
                            Set<String> names = Sets.newHashSet();
                            for (int meta = 0; meta < 15; meta++) {
                                ItemStack is = new ItemStack(b, 1, meta);
                                if (!Strings.isNullOrEmpty(is.getItemName()) && names.add(is.getItemName())) {
                                    ItemFacade.addFacade(is);
                                }
                            }
                        } else {
                            ItemFacade.addFacade(base);
                        }
		    }
		}
	}

	public static int encode(int blockId, int metaData){
		return metaData & 0xF | ((blockId & 0xFFF) << 4);
	}

	public static int getMetaData(int encoded){
		return encoded & 0x0000F;
	}

	public static int getBlockId(int encoded){
		return ((encoded & 0xFFF0) >>> 4);
	}

        public static void addFacade(ItemStack itemStack) {
            allFacades.add(new ItemStack(BuildCraftTransport.facadeItem, 1, ItemFacade.encode(itemStack.itemID, itemStack.getItemDamage())));

            //3 Structurepipes + this block makes 6 facades
            AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(
                                            new ItemStack[] {new ItemStack(BuildCraftTransport.pipeStructureCobblestone, 3), itemStack},
                                            8000,
                                            new ItemStack(BuildCraftTransport.facadeItem, 6, ItemFacade.encode(itemStack.itemID,  itemStack.getItemDamage()))));
        }
}

