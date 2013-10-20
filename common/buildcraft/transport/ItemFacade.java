package buildcraft.transport;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.Position;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.proxy.CoreProxy;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class ItemFacade extends ItemBuildCraft {

	public final static LinkedList<ItemStack> allFacades = new LinkedList<ItemStack>();

	public ItemFacade(int i) {
		super(i);

		setHasSubtypes(true);
		setMaxDamage(0);
		setCreativeTab(CreativeTabBuildCraft.FACADES.get());
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

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List itemList) {
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

		TileEntity tile = worldObj.getBlockTileEntity((int) pos.x, (int) pos.y, (int) pos.z);
		if (!(tile instanceof TileGenericPipe))
			return false;
		TileGenericPipe pipeTile = (TileGenericPipe) tile;

		if (pipeTile.addFacade(ForgeDirection.getOrientation(side).getOpposite(), ItemFacade.getBlockId(stack), ItemFacade.getMetaData(stack))) {
			if (!player.capabilities.isCreativeMode) {
				stack.stackSize--;
			}
			return true;
		}
		return false;
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

				if (!(b.blockID == 20)) {	//Explicitly allow glass
					if (b.blockID == 7 //Bedrock
							|| b.blockID == 2 //Grass block
							|| b.blockID == 18 //Oak leaves
							|| b.blockID == 19 //Sponge
							|| b.blockID == 95 //Locked chest
							|| b.blockID == Block.redstoneLampIdle.blockID
							|| b.blockID == Block.redstoneLampActive.blockID
							|| b.blockID == Block.pumpkinLantern.blockID) {
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
						if (!Strings.isNullOrEmpty(is.getUnlocalizedName()) && names.add(is.getUnlocalizedName())) {
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
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("meta"))
			return stack.getTagCompound().getInteger("meta");
		return stack.getItemDamage() & 0x0000F;
	}

	public static int getBlockId(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("id"))
			return stack.getTagCompound().getInteger("id");
		return ((stack.getItemDamage() & 0xFFF0) >>> 4);
	}

	@Override
	public boolean shouldPassSneakingClickToBlock(World worldObj, int x, int y, int z) {
		// Simply send shift click to the pipe / mod block.
		return true;
	}

	public static void addFacade(ItemStack itemStack) {
		ItemStack facade = getStack(itemStack.itemID, itemStack.getItemDamage());
		allFacades.add(facade);

		ItemStack facade6 = facade.copy();
		facade6.stackSize = 6;

		// 3 Structurepipes + this block makes 6 facades
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[]{new ItemStack(BuildCraftTransport.pipeStructureCobblestone, 3), itemStack},
				8000, facade6));
		if (itemStack.itemID < Block.blocksList.length && Block.blocksList[itemStack.itemID] != null) {
			Block bl = Block.blocksList[itemStack.itemID];

			// Special handling for logs
			if (bl.getRenderType() == 31) {
				ItemStack rotLog1 = getStack(itemStack.itemID, itemStack.getItemDamage() | 4);
				ItemStack rotLog2 = getStack(itemStack.itemID, itemStack.getItemDamage() | 8);
				allFacades.add(rotLog1);
				allFacades.add(rotLog2);
			}
		}
	}
	private static final ItemStack NO_MATCH = new ItemStack(0, 0, 0);

	public class FacadeRecipe implements IRecipe {

		@Override
		public boolean matches(InventoryCrafting inventorycrafting, World world) {
			ItemStack slotmatch = null;
			for (int i = 0; i < inventorycrafting.getSizeInventory(); i++) {
				ItemStack slot = inventorycrafting.getStackInSlot(i);
				if (slot != null && slot.itemID == ItemFacade.this.itemID && slotmatch == null) {
					slotmatch = slot;
				} else if (slot != null) {
					slotmatch = NO_MATCH;
				}
			}
			if (slotmatch != null && slotmatch != NO_MATCH) {
				int blockId = ItemFacade.getBlockId(slotmatch);
				return blockId < Block.blocksList.length && Block.blocksList[blockId] != null && Block.blocksList[blockId].getRenderType() == 31;
			}

			return false;
		}

		@Override
		public ItemStack getCraftingResult(InventoryCrafting inventorycrafting) {
			ItemStack slotmatch = null;
			for (int i = 0; i < inventorycrafting.getSizeInventory(); i++) {
				ItemStack slot = inventorycrafting.getStackInSlot(i);
				if (slot != null && slot.itemID == ItemFacade.this.itemID && slotmatch == null) {
					slotmatch = slot;
				} else if (slot != null) {
					slotmatch = NO_MATCH;
				}
			}
			if (slotmatch != null && slotmatch != NO_MATCH) {
				int blockId = ItemFacade.getBlockId(slotmatch);
				int blockMeta = ItemFacade.getMetaData(slotmatch);
				if (blockId >= Block.blocksList.length)
					return null;
				Block bl = Block.blocksList[blockId];
				// No Meta
				if (bl != null && bl.getRenderType() == 31 && (blockMeta & 0xC) == 0)
					return getStack(bl, (blockMeta & 0x3) | 4);
				// Meta | 4 = true
				if (bl != null && bl.getRenderType() == 31 && (blockMeta & 0x8) == 0)
					return getStack(bl, (blockMeta & 0x3) | 8);
				// Meta | 8 = true
				if (bl != null && bl.getRenderType() == 31 && (blockMeta & 0x4) == 0)
					return getStack(bl, (blockMeta & 0x3));
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
	public void registerIcons(IconRegister par1IconRegister) {
		// NOOP
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getSpriteNumber() {
		return 0;
	}

	public static ItemStack getStack(Block block, int metadata) {
		return getStack(block.blockID, metadata);
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
