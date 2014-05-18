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
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.Constants;
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
	public static final int MAX_STATES = PipeWire.values().length;

	public static class FacadeState {
		public final Block block;
		public final int metadata;
		public final boolean transparent;
		public final PipeWire wire;

		public FacadeState(Block block, int metadata, PipeWire wire) {
			this.block = block;
			this.metadata = metadata;
			this.wire = wire;
			this.transparent = false;
		}

		public FacadeState(NBTTagCompound nbt) {
			this.block = nbt.hasKey("block") ? (Block) Block.blockRegistry.getObject(nbt.getString("block")) : null;
			this.metadata = nbt.getInteger("metadata");
			this.wire = nbt.hasKey("wire") ? PipeWire.fromOrdinal(nbt.getInteger("wire")) : null;
			this.transparent = nbt.hasKey("transparent") && nbt.getBoolean("transparent");
		}

		private FacadeState(PipeWire wire) {
			this.block = null;
			this.metadata = 0;
			this.wire = wire;
			this.transparent = true;
		}

		public static FacadeState create(Block block, int metadata) {
			return create(block, metadata, null);
		}

		public static FacadeState create(Block block, int metadata, PipeWire wire) {
			return new FacadeState(block, metadata, wire);
		}

		public static FacadeState createTransparent(PipeWire wire) {
			return new FacadeState(wire);
		}

		public void writeToNBT(NBTTagCompound nbt) {
			if (block != null) {
				nbt.setString("block", Block.blockRegistry.getNameForObject(block));
			}
			nbt.setInteger("metadata", metadata);
			if (wire != null) {
				nbt.setInteger("wire", wire.ordinal());
			}
			nbt.setBoolean("transparent", transparent);
		}

		public static NBTTagList writeArray(FacadeState[] states) {
			if (states == null) {
				return null;
			}
			NBTTagList list = new NBTTagList();
			for (FacadeState state : states) {
				NBTTagCompound stateNBT = new NBTTagCompound();
				state.writeToNBT(stateNBT);
				list.appendTag(stateNBT);
			}
			return list;
		}

		public static FacadeState[] readArray(NBTTagList list) {
			if (list == null) {
				return null;
			}
			final int length = list.tagCount();
			FacadeState[] states = new FacadeState[length];
			for (int i = 0; i < length; i++) {
				states[i] = new FacadeState(list.getCompoundTagAt(i));
			}
			return states;
		}
	}

	public static enum FacadeType {
		Basic, Phased;

		public static FacadeType fromOrdinal(int ordinal) {
			return ordinal == 1 ? Phased : Basic;
		}
	}

	public static final LinkedList<ItemStack> allFacades = new LinkedList<ItemStack>();
	public static final LinkedList<String> blacklistedFacades = new LinkedList<String>();

	private static final Block NULL_BLOCK = null;
	private static final ItemStack NO_MATCH = new ItemStack(NULL_BLOCK, 0, 0);

	public ItemFacade() {
		super(CreativeTabBuildCraft.FACADES);

		setHasSubtypes(true);
		setMaxDamage(0);
	}

	@Override
	public String getItemStackDisplayName(ItemStack itemstack) {
		switch(getType(itemstack)) {
			case Basic:
				return super.getItemStackDisplayName(itemstack) + ": " + getFacadeStateDisplayName(getFacadeStates(itemstack)[0]);
			case Phased:
				return StringUtils.localize("item.FacadePhased.name");
			default:
				return "";
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		return "item.Facade";
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean debug) {
		if (getType(stack) == FacadeType.Phased) {
			String stateString = StringUtils.localize("item.FacadePhased.state");
			FacadeState defaultState = null;
			for (FacadeState state : getFacadeStates(stack)) {
				if (state.wire == null) {
					defaultState = state;
					continue;
				}
				list.add(String.format(stateString, state.wire.getColor(), getFacadeStateDisplayName(state)));
			}
			if (defaultState != null) {
				list.add(1, String.format(StringUtils.localize("item.FacadePhased.state_default"), getFacadeStateDisplayName(defaultState)));
			}
		}
	}

	public static String getFacadeStateDisplayName(FacadeState state) {
		if (state.block == null) {
			return StringUtils.localize("item.FacadePhased.state_transparent");
		}
		int meta = state.metadata;
		if (state.block.getRenderType() == 31) {
			meta &= 0x3;
		}
		return CoreProxy.proxy.getItemDisplayName(new ItemStack(state.block, 1, meta));
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

		if (pipeTile.addFacade(ForgeDirection.getOrientation(side).getOpposite(), getFacadeStates(stack))) {
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

	public static FacadeState[] getFacadeStates(ItemStack stack) {
		if (!stack.hasTagCompound()) {
			return new FacadeState[0];
		}
		NBTTagCompound nbt = stack.getTagCompound();
		nbt = migrate(stack, nbt);
		if (!nbt.hasKey("states")) {
			return new FacadeState[0];
		}
		return FacadeState.readArray(nbt.getTagList("states", Constants.NBT.TAG_COMPOUND));
	}

	private static NBTTagCompound migrate(ItemStack stack, NBTTagCompound nbt) {
		Block block = null, blockAlt = null;
		int metadata = 0, metadataAlt;
		PipeWire wire = null;
		if (nbt.hasKey("id")) {
			block = (Block) Block.blockRegistry.getObjectById(nbt.getInteger("id"));
		} else if (nbt.hasKey("name")) {
			block = (Block) Block.blockRegistry.getObject(nbt.getString("name"));
		}
		if (nbt.hasKey("name_alt")) {
			blockAlt = (Block) Block.blockRegistry.getObject(nbt.getString("name_alt"));
		}
		if (nbt.hasKey("meta")) {
			metadata = nbt.getInteger("meta");
		}
		if (nbt.hasKey("meta_alt")) {
			metadataAlt = nbt.getInteger("meta_alt");
		} else {
			metadataAlt = stack.getItemDamage() & 0x0000F;
		}
		if (nbt.hasKey("wire")) {
			wire = PipeWire.fromOrdinal(nbt.getInteger("wire"));
		}
		if (block != null) {
			FacadeState[] states;
			FacadeState mainState = FacadeState.create(block, metadata);
			if (blockAlt != null && wire != null) {
				FacadeState altState = FacadeState.create(blockAlt, metadataAlt, wire);
				states = new FacadeState[] {mainState, altState};
			} else {
				states = new FacadeState[] {mainState};
			}
			NBTTagCompound newNbt = getFacade(states).getTagCompound();
			stack.setTagCompound(newNbt);
			return newNbt;
		}
		return nbt;
	}

	public static Block[] getBlocks(ItemStack stack) {
		FacadeState[] states = getFacadeStates(stack);
		Block[] blocks = new Block[states.length];
		for (int i = 0; i < states.length; i++) {
			blocks[i] = states[i].block;
		}
		return blocks;
	}

	public static int[] getMetaValues(ItemStack stack) {
		FacadeState[] states = getFacadeStates(stack);
		int[] meta = new int[states.length];
		for (int i = 0; i < states.length; i++) {
			meta[i] = states[i].metadata;
		}
		return meta;
	}

	// GETTERS FOR FACADE DATA
	public static FacadeType getType(ItemStack stack) {
		if (!stack.hasTagCompound()) {
			return FacadeType.Basic;
		}
		NBTTagCompound nbt = stack.getTagCompound();
		if (!nbt.hasKey("type")) {
			return FacadeType.Basic;
		}
		return FacadeType.fromOrdinal(nbt.getInteger("type"));
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
		return getFacade(FacadeState.create(block, metadata));
	}

	public static ItemStack getAdvancedFacade(PipeWire wire, Block block, int metadata, Block blockAlt, int metaDataAlt) {
		return getFacade(FacadeState.create(block, metadata), FacadeState.create(blockAlt, metaDataAlt, wire));
	}

	public static ItemStack getFacade(FacadeState... states) {
		if (states == null || states.length == 0) {
			return null;
		}
		final boolean basic = states.length == 1 && states[0].wire == null;

		ItemStack stack = new ItemStack(BuildCraftTransport.facadeItem, 1, 0);

		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("type", (basic ? FacadeType.Basic : FacadeType.Phased).ordinal());
		nbt.setTag("states", FacadeState.writeArray(states));

		stack.setTagCompound(nbt);
		return stack;
	}
}
