package buildcraft.lib.recipe;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

import buildcraft.lib.misc.StackUtil;

public class NBTAwareShapedOreRecipe extends ShapedOreRecipe {

    public NBTAwareShapedOreRecipe(@Nonnull ItemStack result, Object... recipe) {
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

                ItemStack slot = StackUtil.asNonNull(inv.getStackInRowAndColumn(x, y));

                if (target instanceof ItemStack) {
                    ItemStack targetStack = (ItemStack) target;
                    if (!OreDictionary.itemMatches(targetStack, slot, false)) {
                        return false;
                    }
                    if (!StackUtil.doesStackNbtMatch(targetStack, slot)) {
                        return false;
                    }
                } else if (target instanceof List) {
                    boolean matched = false;

                    Iterator<ItemStack> itr = ((List<ItemStack>) target).iterator();
                    while (itr.hasNext() && !matched) {
                        matched = OreDictionary.itemMatches(StackUtil.asNonNull(itr.next()), slot, false);
                    }

                    if (!matched) {
                        return false;
                    }
                } else if (target == null && !slot.isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }
}
