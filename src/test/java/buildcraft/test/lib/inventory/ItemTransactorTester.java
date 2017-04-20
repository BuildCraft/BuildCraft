package buildcraft.test.lib.inventory;

import org.junit.Assert;
import org.junit.Test;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import buildcraft.api.inventory.IItemTransactor;

import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.test.VanillaSetupBaseTester;

public class ItemTransactorTester extends VanillaSetupBaseTester {
    @Test
    public void testSimpleMoving() {
        IItemTransactor trans = new ItemHandlerSimple(2, null);

        Assert.assertTrue(trans.extract(null, 1, 1, false).isEmpty());

        ItemStack insert = new ItemStack(Items.APPLE);
        ItemStack leftOver = trans.insert(insert.copy(), false, false);

        Assert.assertTrue(leftOver.isEmpty());

        ItemStack extracted = trans.extract(null, 1, 1, false);

        Assert.assertTrue(ItemStack.areItemStacksEqual(insert, extracted));

        extracted = trans.extract(null, 1, 1, false);

        Assert.assertTrue(extracted.isEmpty());
    }
}
