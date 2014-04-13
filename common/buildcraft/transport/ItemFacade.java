/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.Position;
import buildcraft.api.recipes.BuildcraftRecipes;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.BlockSpring;
import buildcraft.core.BuildCraftConfiguration;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.recipes.AssemblyRecipeManager;
import buildcraft.silicon.ItemRedstoneChipset;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ItemFacade extends ItemBuildCraft {

	public final static LinkedList<ItemStack> allFacades = new LinkedList<ItemStack>();
	public final static LinkedList<String> blacklistedFacades = new LinkedList<String>();

	public ItemFacade() {
		super(CreativeTabBuildCraft.FACADES);

		setHasSubtypes(true);
		setMaxDamage(0);
		setCreativeTab(CreativeTabBuildCraft.FACADES.get());
	}

	@Override
	public String getItemStackDisplayName(ItemStack itemstack) {
		String name = super.getItemStackDisplayName(itemstack);
		Block decodedBlock = ItemFacade.getBlock(itemstack);
		int decodedMeta = ItemFacade.getMetaData(itemstack);
		Block decodedBlock_alt = ItemFacade.getAlternateBlock(itemstack);
		int decodedMeta_alt = ItemFacade.getAlternateMetaData(itemstack);
		if (decodedBlock != null && decodedBlock.getRenderType() == 31) {
			decodedMeta &= 0x3;
		}
		if (decodedBlock_alt != null && decodedBlock_alt.getRenderType() == 31) {
			decodedMeta_alt &= 0x3;
		}
		ItemStack newStack = new ItemStack(decodedBlock, 1, decodedMeta);
		if (Item.getItemFromBlock(decodedBlock) != null) {
			name += ": " + CoreProxy.proxy.getItemDisplayName(newStack);
		} else {
			String localizedName;
			try {
				localizedName = decodedBlock.getLocalizedName();
			} catch(NullPointerException npe) {
				localizedName = "Null";
			}
			name += " < BROKEN (" + localizedName + ":" + decodedMeta + " )>";
		}
		if (decodedBlock_alt != null) {
			ItemStack newStack1 = new ItemStack(decodedBlock_alt, 1, decodedMeta_alt);
			if (Item.getItemFromBlock(decodedBlock_alt) != null) {
				name += " / " + CoreProxy.proxy.getItemDisplayName(newStack1);
			} else {
				String localizedName;
				try {
					localizedName = decodedBlock_alt.getLocalizedName();
				} catch (NullPointerException npe) {
					localizedName = "Null";
				}
				name += " < BROKEN (" + localizedName + ":" + decodedMeta_alt + " )>";
			}
		}
		return name;
	}

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

		if (pipeTile.addFacade(ForgeDirection.getOrientation(side).getOpposite(), stack)) {
			stack.stackSize--;

			return true;
		}

		return false;
	}

	public static void initialize() {
		for (Object o : Block.blockRegistry) {
			Block b = (Block) o;

			if (!isBlockValidForFacade(b)) {
				continue;
			}

			Item item = Item.getItemFromBlock(b);

			if (item == null) {
				continue;
			}

			if (isBlockBlacklisted(b)) {
				continue;
			}

			registerValidFacades(b, item);
		}
	}

	private static void registerValidFacades(Block block, Item item) {
		Set<String> names = Sets.newHashSet();

		for(int i=0; i <= 15; i++) {
			try {
				ItemStack stack = new ItemStack(item, 1, i);

				if(!Strings.isNullOrEmpty(stack.getUnlocalizedName())
						&& names.add(stack.getUnlocalizedName())) {
						ItemFacade.addFacade(stack);

						// prevent adding multiple facades if it's a rotatable block
						if(block.getRenderType() == 31) {
							break;
						}
					}
				} catch(IndexOutOfBoundsException _){

				} catch(Throwable t) {
					t.printStackTrace();
				}
		}
	}

	private static boolean isBlockBlacklisted(Block block) {
		String blockName = Block.blockRegistry.getNameForObject(block);

		if (blockName == null) {
			return true;
		}

		for (String blacklistedBlock : BuildCraftTransport.facadeBlacklist) {
			if(blockName.equals(BuildCraftConfiguration.stripSurroundingQuotes(blacklistedBlock))) {
				return true;
			}
		}

		for(String blacklistedBlock : blacklistedFacades) {
			if(blockName.equals(blacklistedBlock)) {
				return true;
			}
		}

		return false;
	}

	private static boolean isBlockValidForFacade(Block block) {
		if(block.getRenderType() != 0 && block.getRenderType() != 31) {
			return false;
		}

		if(block.getBlockBoundsMaxX() != 1.0 || block.getBlockBoundsMaxY() != 1.0 || block.getBlockBoundsMaxZ() != 1.0) {
			return false;
		}

		if(block instanceof BlockSpring || block instanceof BlockGenericPipe) {
			return false;
		}

		return true;
	}

	public static int getMetaData(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("meta")) {
			return stack.getTagCompound().getInteger("meta");
		} else {
			return stack.getItemDamage() & 0x0000F;
		}
	}

	public static int getAlternateMetaData(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("meta_alt")) {
			return stack.getTagCompound().getInteger("meta_alt");
		} else {
			return stack.getItemDamage() & 0x0000F;
		}
	}

	public static Block getBlock(ItemStack stack) {
		if(!stack.hasTagCompound()) {
			return null;
		}

		Block facadeBlock = null;
		NBTTagCompound stackTagCompound = stack.getTagCompound();
		// reading the 'id' tag is kept to maintain back-compat.
		// The stack gets upgraded the first time this code is run.
		if(stackTagCompound.hasKey("id")) {
			facadeBlock = (Block)Block.blockRegistry.getObjectById(stackTagCompound.getInteger("id"));
			stackTagCompound.removeTag("id");
			stackTagCompound.setString("name", Block.blockRegistry.getNameForObject(facadeBlock));
		} else if (stackTagCompound.hasKey("name")) {
			 facadeBlock = (Block) Block.blockRegistry.getObject(stackTagCompound.getString("name"));
		}

		return facadeBlock;
	}

	public static Block getAlternateBlock(ItemStack stack) {
		if (!stack.hasTagCompound()) {
			return null;
		}

		Block facadeBlock = null;
		NBTTagCompound stackTagCompound = stack.getTagCompound();
		if (stackTagCompound.hasKey("name_alt")) {
			facadeBlock = (Block) Block.blockRegistry.getObject(stackTagCompound.getString("name_alt"));
		}

		return facadeBlock;
	}

	public static PipeWire getWire(ItemStack stack) {
		if (!stack.hasTagCompound()) {
			return null;
		}

		PipeWire wire = null;
		NBTTagCompound stackTagCompound = stack.getTagCompound();
		if (stackTagCompound.hasKey("wire")) {
			wire = PipeWire.fromOrdinal(stackTagCompound.getByte("wire"));
		}

		return wire;
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player) {
		// Simply send shift click to the pipe / mod block.
		return true;
	}

	public static void addFacade(ItemStack itemStack) {
		if(itemStack.stackSize == 0) {
			itemStack.stackSize = 1;
		}

		ItemStack facade = getFacade(Block.getBlockFromItem(itemStack.getItem()), itemStack.getItemDamage());
		if(!allFacades.contains(facade)) {
			allFacades.add(facade);

			ItemStack facade6 = facade.copy();
			facade6.stackSize = 6;

			// 3 Structurepipes + this block makes 6 facades
			BuildcraftRecipes.assemblyTable.addRecipe(8000, facade6, new ItemStack(BuildCraftTransport.pipeStructureCobblestone, 3), itemStack);
		}
	}

	public static void addAdvancedFacades() {
		for (ItemStack facade : allFacades) {
			for (ItemStack facade1 : allFacades) {
				if (!ItemStack.areItemStacksEqual(facade, facade1)) {
					for (PipeWire wire : PipeWire.VALUES) {
						ItemStack result = ItemFacade.getAdvancedFacade(getBlock(facade), getBlock(facade1), getMetaData(facade), getMetaData(facade1), wire);
						result.stackSize = 6;

						AssemblyRecipeManager.INSTANCE.addRecipe(8000, result, facade, facade1, new ItemStack(BuildCraftTransport.pipeWire, 1, wire.ordinal()), ItemRedstoneChipset.Chipset.RED.getStack());
					}
				}
			}
		}
	}

	public static void blacklistFacade(String blockName) {
		if(!blacklistedFacades.contains(blockName)) {
			blacklistedFacades.add(blockName);
		}
	}

	private static final Block NULL_BLOCK = null;
	private static final ItemStack NO_MATCH = new ItemStack(NULL_BLOCK, 0, 0);

	public class FacadeRecipe implements IRecipe {

		@Override
		public boolean matches(InventoryCrafting inventorycrafting, World world) {
			Object[] facade = getFacadeBlockFromCraftingGrid(inventorycrafting);

			return facade != null && facade[0] != null;
		}

		@Override
		public ItemStack getCraftingResult(InventoryCrafting inventorycrafting) {
			Object[] facade = getFacadeBlockFromCraftingGrid(inventorycrafting);
			if(facade == null) {
				return null;
			}

			Block block = (Block)facade[0];
			ItemStack originalFacade = (ItemStack)facade[1];

			if(block == null) {
				return null;
			}

			return getNextFacadeItemStack(block, originalFacade);
		}

		private Object[] getFacadeBlockFromCraftingGrid(InventoryCrafting inventorycrafting) {
			ItemStack slotmatch = null;
			int countOfItems = 0;
			for (int i = 0; i < inventorycrafting.getSizeInventory(); i++) {
				ItemStack slot = inventorycrafting.getStackInSlot(i);

				if (slot != null && slot.getItem() == ItemFacade.this && slotmatch == null) {
					slotmatch = slot;
					countOfItems++;
				} else if (slot != null) {
					slotmatch = NO_MATCH;
				}

				if(countOfItems > 1) {
					return null;
				}
			}

			if (slotmatch != null && slotmatch != NO_MATCH) {
				return new Object[] { ItemFacade.getBlock(slotmatch), slotmatch };
			}

			return null;
		}

		private ItemStack getNextFacadeItemStack(Block block, ItemStack originalFacade)
		{
			int blockMeta = ItemFacade.getMetaData(originalFacade);
			int stackMeta = 0;

			switch(block.getRenderType()) {
				case 1:
					//supports cycling through variants (wool, planks, etc)
					if(blockMeta >= 15) {
						stackMeta = 0;
					} else {
						stackMeta = blockMeta + 1;
					}
					break;
				case 31:
					if ((blockMeta & 0xC) == 0)	{
						// Meta | 4 = true
						stackMeta = (blockMeta & 0x3) | 4;
					} else if ((blockMeta & 0x8) == 0) {
						// Meta | 8 = true
						stackMeta = (blockMeta & 0x3) | 8;
					} else if ((blockMeta & 0x4) == 0) {
						stackMeta = (blockMeta & 0x3);
					}
					break;
				default:
					stackMeta = blockMeta;
			}

			return getFacade(block, stackMeta);
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

	public static ItemStack getFacade(Block block, int metadata) {
		if (block == null) {
			return null;
		}

		ItemStack stack = new ItemStack(BuildCraftTransport.facadeItem, 1, 0);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("meta", metadata);
		nbt.setString("name", Block.blockRegistry.getNameForObject(block));
		stack.setTagCompound(nbt);
		return stack;
	}

	public static ItemStack getPhasedFacade(Block block, int metadata) {
		if (block == null) {
			return null;
		}

		ItemStack stack = new ItemStack(BuildCraftTransport.facadeItem, 1, 0);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("meta", metadata);
		nbt.setString("name", Block.blockRegistry.getNameForObject(block));
		nbt.setBoolean("phased", true);
		stack.setTagCompound(nbt);
		return stack;
	}

	public static ItemStack getAdvancedFacade(Block block1, Block block2, int meta1, int meta2, PipeWire wire) {
		if (block1 == null || block2 == null) {
			return null;
		}

		ItemStack stack = new ItemStack(BuildCraftTransport.facadeItem, 1, 0);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("meta", meta1);
		nbt.setString("name", Block.blockRegistry.getNameForObject(block1));
		nbt.setInteger("meta_alt", meta2);
		nbt.setString("name_alt", Block.blockRegistry.getNameForObject(block2));
		nbt.setByte("wire", (byte) wire.ordinal());
		stack.setTagCompound(nbt);
		return stack;
	}

}
