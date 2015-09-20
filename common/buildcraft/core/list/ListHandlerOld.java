package buildcraft.core.list;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;

import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.utils.NBTUtils;

public final class ListHandlerOld {
	private static final WeakHashMap<ItemStack, StackLine[]> LINE_CACHE = new WeakHashMap<ItemStack, StackLine[]>();

	public static class StackLine {
		public boolean oreWildcard = false;
		public boolean subitemsWildcard = false;
		public boolean isOre;

		private ItemStack[] stacks = new ItemStack[7];
		private ArrayList<ItemStack> ores = new ArrayList<ItemStack>();
		private ArrayList<ItemStack> relatedItems = new ArrayList<ItemStack>();

		public ItemStack getStack(int index) {
			if (index == 0 || (!oreWildcard && !subitemsWildcard)) {
				if (index < 7) {
					return stacks[index];
				} else {
					return null;
				}
			} else if (oreWildcard) {
				if (ores.size() >= index) {
					return ores.get(index - 1);
				} else {
					return null;
				}
			} else {
				if (relatedItems.size() >= index) {
					return relatedItems.get(index - 1);
				} else {
					return null;
				}
			}
		}

		public void setStack(int slot, ItemStack stack) {
			stacks[slot] = stack;

			if (stack != null) {
				stacks[slot] = stacks[slot].copy();
				stacks[slot].stackSize = 1;
			}

			if (slot == 0) {
				relatedItems.clear();
				ores.clear();

				if (stack == null) {
					isOre = false;
				} else {
					if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
						setClientPreviewLists();
					} else {
						isOre = OreDictionary.getOreIDs(stacks[0]).length > 0;
					}
				}
			}
		}

		public void writeToNBT(NBTTagCompound nbt) {
			nbt.setBoolean("ore", oreWildcard);
			nbt.setBoolean("sub", subitemsWildcard);

			for (int i = 0; i < 7; ++i) {
				if (stacks[i] != null) {
					NBTTagCompound stackNBT = new NBTTagCompound();
					stacks[i].writeToNBT(stackNBT);
					nbt.setTag("stacks[" + i + "]", stackNBT);
				}
			}
		}

		public void readFromNBT(NBTTagCompound nbt) {
			oreWildcard = nbt.getBoolean("ore");
			subitemsWildcard = nbt.getBoolean("sub");

			for (int i = 0; i < 7; ++i) {
				if (nbt.hasKey("stacks[" + i + "]")) {
					setStack(i, ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("stacks[" + i + "]")));
				}
			}
		}

		private boolean classMatch(Item base, Item matched) {
			if (base.getClass() == Item.class) {
				return base == matched;
			} else if (base.getClass() == matched.getClass()) {
				if (base instanceof ItemBlock) {
					Block baseBlock = ((ItemBlock) base).field_150939_a;
					Block matchedBlock = ((ItemBlock) matched).field_150939_a;

					if (baseBlock.getClass() == Block.class) {
						return baseBlock == matchedBlock;
					} else {
						return baseBlock.equals(matchedBlock);
					}
				} else {
					return true;
				}
			} else {
				return false;
			}
		}

		private boolean oreMatch(ItemStack base, ItemStack matched) {
			int[] oreIds = OreDictionary.getOreIDs(base);
			int[] matchesIds = OreDictionary.getOreIDs(matched);

			for (int stackId : oreIds) {
				for (int matchId : matchesIds) {
					if (stackId == matchId) {
						return true;
					}
				}
			}

			return false;
		}

		private void setClientPreviewLists() {
			Item baseItem = stacks[0].getItem();

			int[] oreIds = OreDictionary.getOreIDs(stacks[0]);

			isOre = oreIds.length > 0;

			for (Object o : Item.itemRegistry) {
				Item item = (Item) o;
				boolean classMatch = classMatch(baseItem, item);

				List list = new LinkedList();

				for (CreativeTabs tab : item.getCreativeTabs()) {
					item.getSubItems(item, tab, list);
				}

				if (list.size() > 0) {
					for (Object ol : list) {
						ItemStack stack = (ItemStack) ol;

						if (classMatch && relatedItems.size() <= 7 && !StackHelper.isMatchingItemOrList(stacks[0], stack)) {
							relatedItems.add(stack);
						}

						if (isOre && ores.size() <= 7 && !StackHelper.isMatchingItemOrList(stacks[0], stack)
								&& oreMatch(stacks[0], stack)) {
							ores.add(stack);
						}
					}
				}
			}
		}

		public boolean matches(ItemStack item) {
			if (subitemsWildcard) {
				if (stacks[0] == null) {
					return false;
				}

				return classMatch(stacks[0].getItem(), item.getItem());
			} else if (oreWildcard) {
				if (stacks[0] == null) {
					return false;
				}

				return oreMatch(stacks[0], item);
			} else {
				for (ItemStack stack : stacks) {
					if (stack != null && StackHelper.isMatchingItem(stack, item, true, false)) {
						return true;
					}
				}

				return false;
			}
		}
	}

	private ListHandlerOld() {

	}

	public static void saveLine(ItemStack stack, StackLine line, int index) {
		NBTTagCompound nbt = NBTUtils.getItemData(stack);

		nbt.setBoolean("written", true);

		NBTTagCompound lineNBT = new NBTTagCompound();
		line.writeToNBT(lineNBT);
		nbt.setTag("line[" + index + "]", lineNBT);
	}

	public static StackLine[] getLines(ItemStack stack) {
		if (LINE_CACHE.containsKey(stack)) {
			return LINE_CACHE.get(stack);
		}

		StackLine[] result = new StackLine[6];

		for (int i = 0; i < 6; ++i) {
			result[i] = new StackLine();
		}

		NBTTagCompound nbt = NBTUtils.getItemData(stack);

		if (nbt.hasKey("written")) {
			for (int i = 0; i < 6; ++i) {
				result[i].readFromNBT(nbt.getCompoundTag("line[" + i + "]"));
			}
		}

		LINE_CACHE.put(stack, result);

		return result;
	}

	public static boolean matches(ItemStack stackList, ItemStack item) {
		StackLine[] lines = getLines(stackList);

		if (lines != null) {
			for (StackLine line : lines) {
				if (line != null && line.matches(item)) {
					return true;
				}
			}
		}

		return false;
	}
}
