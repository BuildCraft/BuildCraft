/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

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

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.JavaTools;
import buildcraft.api.core.Position;
import buildcraft.api.recipes.BuildcraftRecipes;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.BlockSpring;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.StringUtils;

public class ItemFacade extends ItemBuildCraft {

	public static final LinkedList<ItemStack> allFacades = new LinkedList<ItemStack>();
	public static final LinkedList<String> blacklistedFacades = new LinkedList<String>();

	public static final int TYPE_BASIC = 0;
	public static final int TYPE_PHASED = 1;

	private static final Block NULL_BLOCK = null;
	private static final ItemStack NO_MATCH = new ItemStack(NULL_BLOCK, 0, 0);

	public ItemFacade() {
		super(CreativeTabBuildCraft.FACADES);

		setHasSubtypes(true);
		setMaxDamage(0);
	}

	@Override
	public String getItemStackDisplayName(ItemStack itemstack) {
		String name = super.getItemStackDisplayName(itemstack);

		if (getType(itemstack) == TYPE_PHASED) {
			name = StringUtils.localize("item.FacadePhased.name");
		}

		Block block = ItemFacade.getBlocks(itemstack)[0];
		int meta = ItemFacade.getMetaValues(itemstack)[0];
		Block blockAlt = ItemFacade.getBlocks(itemstack)[1];
		int metaAlt = ItemFacade.getMetaValues(itemstack)[1];

		if (block != null && block.getRenderType() == 31) {
			meta &= 0x3;
		}

		if (blockAlt != null && blockAlt.getRenderType() == 31) {
			metaAlt &= 0x3;
		}

		ItemStack stack = new ItemStack(block, 1, meta);
		ItemStack stackAlt = new ItemStack(blockAlt, 1, metaAlt);

		if (getType(itemstack) == TYPE_BASIC) {
			if (Item.getItemFromBlock(block) != null) {
				name += ": " + CoreProxy.proxy.getItemDisplayName(stack);
			}
		} else if (getType(itemstack) == TYPE_PHASED) {
			if (Item.getItemFromBlock(block) != null) {
				name += ": " + CoreProxy.proxy.getItemDisplayName(stack);
			}

			if (Item.getItemFromBlock(blockAlt) != null) {
				name += " / " + CoreProxy.proxy.getItemDisplayName(stackAlt);
			}
		}

		return name;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		return "item.Facade";
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean debug) {
		if (getType(stack) == TYPE_PHASED) {
			list.add("Wire: " + PipeWire.fromOrdinal(getWireType(stack)).getColor());
		}
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
		if (worldObj.isRemote) {
			return false;
		}
		Position pos = new Position(x, y, z, ForgeDirection.getOrientation(side));
		pos.moveForwards(1.0);

		TileEntity tile = worldObj.getTileEntity((int) pos.x, (int) pos.y, (int) pos.z);
		if (!(tile instanceof TileGenericPipe)) {
			return false;
		}
		TileGenericPipe pipeTile = (TileGenericPipe) tile;

		if (pipeTile.addFacade(ForgeDirection.getOrientation(side).getOpposite(), ItemFacade.getType(stack), ItemFacade.getWireType(stack), ItemFacade.getBlocks(stack), ItemFacade.getMetaValues(stack))) {
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

		for (int i = 0; i <= 15; i++) {
			try {
				ItemStack stack = new ItemStack(item, 1, i);

				if (!Strings.isNullOrEmpty(stack.getUnlocalizedName())
						&& names.add(stack.getUnlocalizedName())) {
						ItemFacade.addFacade(stack);

						// prevent adding multiple facades if it's a rotatable block
					if (block.getRenderType() == 31) {
							break;
						}
					}
			} catch (IndexOutOfBoundsException e) {

			} catch (Throwable t) {
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
			if (blockName.equals(JavaTools.stripSurroundingQuotes(blacklistedBlock))) {
				return true;
			}
		}

		for (String blacklistedBlock : blacklistedFacades) {
			if (blockName.equals(blacklistedBlock)) {
				return true;
			}
		}

		return false;
	}

	private static boolean isBlockValidForFacade(Block block) {
		try {
			if (block.getRenderType() != 0 && block.getRenderType() != 31) {
				return false;
			}

			if (block.getBlockBoundsMaxX() != 1.0 || block.getBlockBoundsMaxY() != 1.0 || block.getBlockBoundsMaxZ() != 1.0) {
				return false;
			}

			if (block instanceof BlockSpring || block instanceof BlockGenericPipe) {
				return false;
			}

			return true;
		} catch (Throwable ignored) {
			return false;
		}
	}

	// GETTERS FOR FACADE DATA
	public static int getType(ItemStack stack) {
		// Type is based on various other data included in the stack
		int wireType = getWireType(stack);

		if (wireType == -1) {
			// Automatically considered a basic facade
			return TYPE_BASIC;
		}

		Block[] blocks = getBlocks(stack);

		if (blocks.length == 1 || blocks[1] == null) {
			return TYPE_BASIC;
		}

		return TYPE_PHASED;
	}

	public static int getWireType(ItemStack stack) {
		int type = -1;

		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("wire")) {
			type = stack.getTagCompound().getInteger("wire");
		}

		return type;
	}

	public static Block[] getBlocks(ItemStack stack) {
		if (!stack.hasTagCompound()) {
			return null;
		}

		Block facadeBlock = null;
		Block facadeBlockAlt = null;

		NBTTagCompound stackTagCompound = stack.getTagCompound();

		// reading the 'id' tag is kept to maintain back-compat.
		// The stack gets upgraded the first time this code is run.
		if (stackTagCompound.hasKey("id")) {
			facadeBlock = (Block) Block.blockRegistry.getObjectById(stackTagCompound.getInteger("id"));
			stackTagCompound.removeTag("id");
			stackTagCompound.setString("name", Block.blockRegistry.getNameForObject(facadeBlock));
		} else if (stackTagCompound.hasKey("name")) {
			 facadeBlock = (Block) Block.blockRegistry.getObject(stackTagCompound.getString("name"));
		}

		if (stackTagCompound.hasKey("name_alt")) {
			facadeBlockAlt = (Block) Block.blockRegistry.getObject(stack.getTagCompound().getString("name_alt"));
		}

		return new Block[] {facadeBlock, facadeBlockAlt};
	}

	public static int[] getMetaValues(ItemStack stack) {
		int meta = 0;
		int metaAlt = 0;

		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("meta")) {
			meta = stack.getTagCompound().getInteger("meta");
		} else {
			meta = stack.getItemDamage() & 0x0000F;
		}

		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("meta_alt")) {
			metaAlt = stack.getTagCompound().getInteger("meta_alt");
		} else {
			metaAlt = stack.getItemDamage() & 0x0000F;
		}

		return new int[] {meta, metaAlt};
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player) {
		// Simply send shift click to the pipe / mod block.
		return true;
	}

	public static void addFacade(ItemStack itemStack) {
		if (itemStack.stackSize == 0) {
			itemStack.stackSize = 1;
		}

		ItemStack facade = getFacade(Block.getBlockFromItem(itemStack.getItem()), itemStack.getItemDamage());
		if (!allFacades.contains(facade)) {
			allFacades.add(facade);

			ItemStack facade6 = facade.copy();
			facade6.stackSize = 6;

			// 3 Structurepipes + this block makes 6 facades
			BuildcraftRecipes.assemblyTable.addRecipe(8000, facade6, new ItemStack(BuildCraftTransport.pipeStructureCobblestone, 3), itemStack);
		}
	}

	public static void blacklistFacade(String blockName) {
		if (!blacklistedFacades.contains(blockName)) {
			blacklistedFacades.add(blockName);
		}
	}

	public class FacadeRecipe implements IRecipe {

		@Override
		public boolean matches(InventoryCrafting inventorycrafting, World world) {
			Object[] facade = getFacadeBlockFromCraftingGrid(inventorycrafting);

			return facade != null && facade[0] != null;
		}

		@Override
		public ItemStack getCraftingResult(InventoryCrafting inventorycrafting) {
			Object[] facade = getFacadeBlockFromCraftingGrid(inventorycrafting);
			if (facade == null) {
				return null;
			}

			Block block = (Block) facade[0];
			ItemStack originalFacade = (ItemStack) facade[1];

			if (block == null) {
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

				if (countOfItems > 1) {
					return null;
				}
			}

			if (slotmatch != null && slotmatch != NO_MATCH) {
				return new Object[] { ItemFacade.getBlocks(slotmatch), slotmatch };
			}

			return null;
		}

		private ItemStack getNextFacadeItemStack(Block block, ItemStack originalFacade)
		{
			int blockMeta = ItemFacade.getMetaValues(originalFacade)[0];
			int stackMeta = 0;

			switch(block.getRenderType()) {
				case 1:
					//supports cycling through variants (wool, planks, etc)
				if (blockMeta >= 15) {
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
					stackMeta = blockMeta & 0x3;
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
		ItemStack stack = new ItemStack(BuildCraftTransport.facadeItem, 1, 0);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("meta", metadata);
		nbt.setString("name", Block.blockRegistry.getNameForObject(block));
		stack.setTagCompound(nbt);
		return stack;
	}

	public static ItemStack getAdvancedFacade(PipeWire wire, Block block, int metadata, Block blockAlt, int metaDataAlt) {
		ItemStack stack = new ItemStack(BuildCraftTransport.facadeItem, 1, 0);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("wire", wire.ordinal());
		nbt.setString("name", Block.blockRegistry.getNameForObject(block));
		nbt.setInteger("meta", metadata);
		nbt.setString("name_alt", Block.blockRegistry.getNameForObject(blockAlt));
		nbt.setInteger("meta_alt", metaDataAlt);
		stack.setTagCompound(nbt);
		return stack;
	}
}
