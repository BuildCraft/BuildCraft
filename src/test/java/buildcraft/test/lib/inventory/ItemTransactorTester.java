package buildcraft.test.lib.inventory;

import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.items.BCStackHelper;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.lib.tile.item.StackInsertionFunction;
import buildcraft.test.VanillaSetupBaseTester;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.junit.Assert;
import org.junit.Test;

public class ItemTransactorTester extends VanillaSetupBaseTester {
    @Test
    public void testSimpleMoving() {
        IItemTransactor trans = new ItemHandlerSimple(2, null);

        Assert.assertTrue(BCStackHelper.isEmpty(trans.extract(null, 1, 1, false)));

        ItemStack insert = new ItemStack(Items.APPLE);
        ItemStack leftOver = trans.insert(insert.copy(), false, false);

        Assert.assertTrue(BCStackHelper.isEmpty(leftOver));

        ItemStack extracted = trans.extract(null, 1, 1, false);

        Assert.assertTrue(ItemStack.areItemStacksEqual(insert, extracted));

        extracted = trans.extract(null, 1, 1, false);

        Assert.assertTrue(BCStackHelper.isEmpty(extracted));
    }

    @Test
    public void testLimitedInventory() {
        IItemTransactor limited = new ItemHandlerSimple(2, (i, s) -> true, StackInsertionFunction.getInsertionFunction(4), null);

        ItemStack toInsert = new ItemStack(Items.APPLE, 9);
        ItemStack toInsertCopy = toInsert.copy();
        ItemStack supposedLeftOver = new ItemStack(Items.APPLE);

        ItemStack actuallyLeftOver = limited.insert(toInsert, false, false);

        Assert.assertTrue(ItemStack.areItemStacksEqual(toInsert, toInsertCopy));
        Assert.assertTrue(ItemStack.areItemStacksEqual(supposedLeftOver, actuallyLeftOver));
    }
}
