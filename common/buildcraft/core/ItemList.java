/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.items.IList;
import buildcraft.BuildCraftCore;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.items.ItemBuildCraft;
import buildcraft.core.lib.utils.ModelHelper;
import buildcraft.core.lib.utils.NBTUtils;

public class ItemList extends ItemBuildCraft implements IList {
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
                    Block baseBlock = ((ItemBlock) base).block;
                    Block matchedBlock = ((ItemBlock) matched).block;

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

                        if (isOre && ores.size() <= 7 && !StackHelper.isMatchingItemOrList(stacks[0], stack) && oreMatch(stacks[0], stack)) {
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

    public ItemList() {
        super();
        setMaxStackSize(1);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            player.openGui(BuildCraftCore.instance, GuiIds.LIST, world, 0, 0, 0);
        }

        return stack;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        NBTTagCompound nbt = NBTUtils.getItemData(stack);

        if (nbt.hasKey("label")) {
            list.add(nbt.getString("label"));
        }
    }

    public static void saveLine(ItemStack stack, StackLine line, int index) {
        NBTTagCompound nbt = NBTUtils.getItemData(stack);

        stack.setItemDamage(1);

        NBTTagCompound lineNBT = new NBTTagCompound();
        line.writeToNBT(lineNBT);
        nbt.setTag("line[" + index + "]", lineNBT);
    }

    public static void saveLabel(ItemStack stack, String text) {
        NBTTagCompound nbt = NBTUtils.getItemData(stack);

        nbt.setString("label", text);
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

        if (stack.getItemDamage() == 1) {
            for (int i = 0; i < 6; ++i) {
                result[i].readFromNBT(nbt.getCompoundTag("line[" + i + "]"));
            }
        }

        LINE_CACHE.put(stack, result);

        return result;
    }

    @Override
    public boolean setName(ItemStack stack, String name) {
        saveLabel(stack, name);
        return true;
    }

    @Override
    public String getName(ItemStack stack) {
        return getLabel(stack);
    }

    @Override
    public String getLabel(ItemStack stack) {
        return NBTUtils.getItemData(stack).getString("label");
    }

    @Override
    public boolean matches(ItemStack stackList, ItemStack item) {
        StackLine[] lines = getLines(stackList);

        if (lines != null) {
            for (int i = 0; i < lines.length; i++) {
                if (lines[i] != null && lines[i].matches(item)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void registerModels() {
        ModelHelper.registerItemModel(this, 0, "");
        ModelHelper.registerItemModel(this, 1, "Used");
    }
}
