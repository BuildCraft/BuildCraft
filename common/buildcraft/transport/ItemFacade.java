package buildcraft.transport;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftTransport;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.proxy.CoreProxy;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemFacade extends ItemBuildCraft {

	public final static LinkedList<ItemStack> allFacades = new LinkedList<ItemStack>();

	public ItemFacade(int i) {
		super(i);

		setHasSubtypes(true);
		setMaxDamage(0);
		setCreativeTab(CreativeTabBuildCraft.tabBuildCraft);
	}

	@Override
	public String getItemDisplayName(ItemStack itemstack) {
		String name = super.getItemDisplayName(itemstack);
		int decodedBlockId = ItemFacade.getBlockId(itemstack);
		int decodedMeta = ItemFacade.getMetaData(itemstack);
		if (decodedBlockId < Block.blocksList.length && Block.blocksList[decodedBlockId] != null && Block.blocksList[decodedBlockId].getRenderType() == 31) {
		    decodedMeta &= 0x3;
        }
		ItemStack newStack = new ItemStack(decodedBlockId, 1, decodedMeta);
		if (Item.itemsList[decodedBlockId] != null) {
			name += ": " + CoreProxy.proxy.getItemDisplayName(newStack);
		} else {
			name += " < BROKEN (" + decodedBlockId + ":" + decodedMeta + " )>";
		}
		return name;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		return "item.Facade";
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List itemList) {
		// Do not call super, that would add a 0:0 facade
		for (ItemStack stack : allFacades) {
			itemList.add(stack.copy());
		}
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World worldObj, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		if (worldObj.isRemote)
			return false;
		TileEntity tile = worldObj.getBlockTileEntity(x, y, z);
		if (!(tile instanceof TileGenericPipe))
			return false;
		TileGenericPipe pipeTile = (TileGenericPipe) tile;

		if (player.isSneaking()) { // Strip facade
			if (!pipeTile.hasFacade(ForgeDirection.VALID_DIRECTIONS[side]))
				return false;
			pipeTile.dropFacade(ForgeDirection.VALID_DIRECTIONS[side]);
			return true;
		} else {
			if (((TileGenericPipe) tile).addFacade(ForgeDirection.values()[side], ItemFacade.getBlockId(stack),
					ItemFacade.getMetaData(stack))) {
				if (!player.capabilities.isCreativeMode) {
					stack.stackSize--;
				}
				return true;
			}
			return false;
		}
	}

	public static void initialize() {
		for (Field f : Block.class.getDeclaredFields()) {
			if (Modifier.isStatic(f.getModifiers()) && Block.class.isAssignableFrom(f.getType())) {
				Block b;
				try {
					b = (Block) f.get(null);
				} catch (Exception e) {
					continue;
				}

				if (!(b.blockID == 20)){	//Explicitly allow glass
					if (b.blockID == 7 //Bedrock
							|| b.blockID == 2 //Grass block
							|| b.blockID == 18 //Oak leaves
							|| b.blockID == 19 //Sponge
							|| b.blockID == 95 //Locked chest
							) {
						continue;
					}
					if (!b.isOpaqueCube() || b.hasTileEntity(0) || !b.renderAsNormalBlock()) {
						continue;
					}
				}
				ItemStack base = new ItemStack(b, 1);
				if (base.getHasSubtypes()) {
					Set<String> names = Sets.newHashSet();
					for (int meta = 0; meta <= 15; meta++) {
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

	public static int getMetaData(ItemStack stack) {
		if(stack.hasTagCompound() && stack.getTagCompound().hasKey("meta"))
			return stack.getTagCompound().getInteger("meta");
		return stack.getItemDamage() & 0x0000F;
	}

	public static int getBlockId(ItemStack stack) {
		if(stack.hasTagCompound() && stack.getTagCompound().hasKey("id"))
			return stack.getTagCompound().getInteger("id");
		return ((stack.getItemDamage() & 0xFFF0) >>> 4);
	}

	@Override
	public boolean shouldPassSneakingClickToBlock(World worldObj, int x, int y, int z ) {
		// Simply send shift click to the pipe / mod block.
		return true;
	}

	public static void addFacade(ItemStack itemStack) {
		ItemStack facade = getStack(itemStack.itemID, itemStack.getItemDamage());
		allFacades.add(facade);
		
		ItemStack facade6 = facade.copy();
		facade6.stackSize = 6;

		// 3 Structurepipes + this block makes 6 facades
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftTransport.pipeStructureCobblestone, 3), itemStack },
				8000, facade6));
		if (itemStack.itemID < Block.blocksList.length && Block.blocksList[itemStack.itemID] != null) {
		    Block bl = Block.blocksList[itemStack.itemID];

		    // Special handling for logs
	        if (bl.getRenderType() == 31) {
	            ItemStack mainLog = getStack(itemStack.itemID, itemStack.getItemDamage());
	            ItemStack rotLog1 = getStack(itemStack.itemID, itemStack.getItemDamage() | 4);
                ItemStack rotLog2 = getStack(itemStack.itemID, itemStack.getItemDamage() | 8);
                allFacades.add(rotLog1);
                allFacades.add(rotLog2);
                CoreProxy.proxy.addShapelessRecipe(rotLog1, new Object[] { mainLog });
                CoreProxy.proxy.addShapelessRecipe(rotLog2, new Object[] { rotLog1 });
                CoreProxy.proxy.addShapelessRecipe(mainLog, new Object[] { rotLog2 });
	        }
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
	    // NOOP
	}

	@Override
    @SideOnly(Side.CLIENT)
    public int getSpriteNumber()
    {
        return 0;
    }
	
	public static ItemStack getStack(int blockID, int metadata) {
		ItemStack stack = new ItemStack(BuildCraftTransport.facadeItem, 1, 0);
		NBTTagCompound nbt = new NBTTagCompound("tag");
		nbt.setInteger("meta", metadata);
		nbt.setInteger("id", blockID);
		stack.setTagCompound(nbt);
		return stack;
	}
}
