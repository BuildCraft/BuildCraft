package buildcraft.transport;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.Position;
import buildcraft.api.recipes.BuildcraftRecipes;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.proxy.CoreProxy;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class ItemFacade extends ItemBuildCraft {

	public final static LinkedList<ItemStack> allFacades = new LinkedList<ItemStack>();

	public ItemFacade() {
		super();

		setHasSubtypes(true);
		setMaxDamage(0);
		setCreativeTab(CreativeTabBuildCraft.FACADES.get());
	}

	//TODO: how to control name of items?
	/*@Override
	public String getItemDisplayName(ItemStack itemstack) {
		String name = super.getItemDisplayName(itemstack);
		Block decodedBlock = ItemFacade.getBlock(itemstack);
		int decodedMeta = ItemFacade.getMetaData(itemstack);
		if (decodedBlock != null && decodedBlock.getRenderType() == 31) {
			decodedMeta &= 0x3;
		}
		ItemStack newStack = new ItemStack(decodedBlock, 1, decodedMeta);
		if (Item.getItemFromBlock(decodedBlock) != null) {
			name += ": " + CoreProxy.proxy.getItemDisplayName(newStack);
		} else {
			name += " < BROKEN (" + decodedBlock.getLocalizedName() + ":" + decodedMeta + " )>";
		}
		return name;
	}*/

	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		return "item.Facade";
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs par2CreativeTabs, List itemList) {
		// Do not call super, that would add a 0:0 facade
		for (ItemStack stack : allFacades) {
			itemList.add(stack.copy());
		}
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World worldObj, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		if (worldObj.isRemote)
			return false;
		Position pos = new Position(x, y, z, ForgeDirection.getOrientation(side));
		pos.moveForwards(1.0);

		TileEntity tile = worldObj.getTileEntity((int) pos.x, (int) pos.y, (int) pos.z);
		if (!(tile instanceof TileGenericPipe))
			return false;
		TileGenericPipe pipeTile = (TileGenericPipe) tile;

		if (pipeTile.addFacade(ForgeDirection.getOrientation(side).getOpposite(), ItemFacade.getBlock(stack), ItemFacade.getMetaData(stack))) {
			if (!player.capabilities.isCreativeMode) {
				stack.stackSize--;
			}
			return true;
		}
		return false;
	}

	public static void initialize() {
		for (Object o : Block.blockRegistry) {			
			Block b = (Block) o;

			if (!(b == Blocks.glass)) {
				if (b == Blocks.bedrock
						|| b == Blocks.grass 
						|| b == Blocks.leaves
						|| b == Blocks.sponge
						|| b == Blocks.chest
						|| b == Blocks.redstone_lamp
						|| b == Blocks.lit_redstone_lamp
						|| b == Blocks.lit_pumpkin) {
					continue;
				}
					
				if (!b.isOpaqueCube() 
						|| b.hasTileEntity(0)
						|| !b.renderAsNormalBlock() 
						|| b.getRenderType() != 0) {
					continue;
				}
			}			
									
			Item base = Item.getItemFromBlock(b);			
			
			if (base != null) {
				List <ItemStack> stackList = new ArrayList<ItemStack> ();
				if ( FMLCommonHandler.instance().getSide() == Side.CLIENT ) {
					base.getSubItems(base, null, stackList);
				}
				
				for (ItemStack s : stackList) {
					ItemFacade.addFacade(s);
				}				
			}
		}
	}

	public static int getMetaData(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("meta")) {
			return stack.getTagCompound().getInteger("meta");
		} else {
			return stack.getItemDamage() & 0x0000F;
		}
	}

	public static Block getBlock(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("id")) {
			return (Block) Block.blockRegistry.getObjectById(stack.getTagCompound().getInteger("id"));
		} else {
			return (Block) Block.blockRegistry.getObjectById((stack.getItemDamage() & 0xFFF0) >>> 4);
		}
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player) {
		// Simply send shift click to the pipe / mod block.
		return true;
	}

	public static void addFacade(ItemStack itemStack) {
		ItemStack facade = getStack(Block.getBlockFromItem(itemStack.getItem()), itemStack.getItemDamage());
		allFacades.add(facade);

		ItemStack facade6 = facade.copy();
		facade6.stackSize = 6;

		// 3 Structurepipes + this block makes 6 facades
		BuildcraftRecipes.assemblyTable.addRecipe(8000, facade6, new ItemStack(BuildCraftTransport.pipeStructureCobblestone, 3), itemStack);
		
		Block bl = Block.getBlockFromItem(itemStack.getItem());

		// Special handling for logs
		if (bl != null && bl.getRenderType() == 31) {
			ItemStack rotLog1 = getStack(
					Block.getBlockFromItem(itemStack.getItem()),
					itemStack.getItemDamage() | 4);
			ItemStack rotLog2 = getStack(
					Block.getBlockFromItem(itemStack.getItem()),
					itemStack.getItemDamage() | 8);
			allFacades.add(rotLog1);
			allFacades.add(rotLog2);
		}		
	}
	
	private static final Block NULL_BLOCK = null;
	private static final ItemStack NO_MATCH = new ItemStack(NULL_BLOCK, 0, 0);

	public class FacadeRecipe implements IRecipe {

		@Override
		public boolean matches(InventoryCrafting inventorycrafting, World world) {
			ItemStack slotmatch = null;
			for (int i = 0; i < inventorycrafting.getSizeInventory(); i++) {
				ItemStack slot = inventorycrafting.getStackInSlot(i);
				if (slot != null && slot.getItem() == ItemFacade.this && slotmatch == null) {
					slotmatch = slot;
				} else if (slot != null) {
					slotmatch = NO_MATCH;
				}
			}
			if (slotmatch != null && slotmatch != NO_MATCH) {
				Block block = ItemFacade.getBlock(slotmatch);
				return block != null && block.getRenderType() == 31;
			}

			return false;
		}

		@Override
		public ItemStack getCraftingResult(InventoryCrafting inventorycrafting) {
			ItemStack slotmatch = null;
			for (int i = 0; i < inventorycrafting.getSizeInventory(); i++) {
				ItemStack slot = inventorycrafting.getStackInSlot(i);
				if (slot != null && slot.getItem() == ItemFacade.this && slotmatch == null) {
					slotmatch = slot;
				} else if (slot != null) {
					slotmatch = NO_MATCH;
				}
			}
			if (slotmatch != null && slotmatch != NO_MATCH) {
				Block block = ItemFacade.getBlock(slotmatch);
				int blockMeta = ItemFacade.getMetaData(slotmatch);
				
				
				if (block != null && block.getRenderType() == 31 && (blockMeta & 0xC) == 0)
					return getStack(block, (blockMeta & 0x3) | 4);
				// Meta | 4 = true
				if (block != null && block.getRenderType() == 31 && (blockMeta & 0x8) == 0)
					return getStack(block, (blockMeta & 0x3) | 8);
				// Meta | 8 = true
				if (block != null && block.getRenderType() == 31 && (blockMeta & 0x4) == 0)
					return getStack(block, (blockMeta & 0x3));
			}
			return null;
		}

		@Override
		public int getRecipeSize() {
			return 1;
		}

		@Override
		public ItemStack getRecipeOutput() {
			return null;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		// NOOP
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getSpriteNumber() {
		return 0;
	}

	public static ItemStack getStack(Block block, int metadata) {
		ItemStack stack = new ItemStack(BuildCraftTransport.facadeItem, 1, 0);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("meta", metadata);		
		nbt.setInteger("id", Block.blockRegistry.getIDForObject(block));
		stack.setTagCompound(nbt);
		return stack;
	}
}
