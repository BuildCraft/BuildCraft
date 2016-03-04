package buildcraft.core.lib.recipe;

import java.util.Iterator;
import java.util.List;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class NBTAwareShapedOreRecipe extends ShapedOreRecipe {

    public NBTAwareShapedOreRecipe(ItemStack result, Object[] recipe) {
        super(result, recipe);
    }

    @Override
    protected boolean checkMatch(InventoryCrafting inv, int startX, int startY, boolean mirror) {
        for (int x = 0; x < MAX_CRAFT_GRID_WIDTH; x++) {
            for (int y = 0; y < MAX_CRAFT_GRID_HEIGHT; y++) {
                int subX = x - startX;
                int subY = y - startY;
                Object target = null;

                if (subX >= 0 && subY >= 0 && subX < width && subY < height) {
                    if (mirror) {
                        target = input[width - subX - 1 + subY * width];
                    } else {
                        target = input[subX + subY * width];
                    }
                }

                ItemStack slot = inv.getStackInRowAndColumn(x, y);

                if (target instanceof ItemStack) {
                    ItemStack targetStack = (ItemStack) target;
                    if (!OreDictionary.itemMatches(targetStack, slot, false)) {
                        return false;
                    }
                    if (targetStack.hasTagCompound()) {
                        if (!slot.hasTagCompound()) return false;
                        if (!targetStack.getTagCompound().equals(slot.getTagCompound())) return false;
                    }
                } else if (target instanceof List) {
                    boolean matched = false;

                    Iterator<ItemStack> itr = ((List<ItemStack>) target).iterator();
                    while (itr.hasNext() && !matched) {
                        matched = OreDictionary.itemMatches(itr.next(), slot, false);
                    }

                    if (!matched) {
                        return false;
                    }
                } else if (target == null && slot != null) {
                    return false;
                }
            }
        }

        return true;
    }
}
