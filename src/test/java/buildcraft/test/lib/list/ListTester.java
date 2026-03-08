package buildcraft.test.lib.list;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.Assert;
import org.junit.Test;

import buildcraft.api.lists.ListMatchHandler.Type;

import buildcraft.lib.list.ListMatchHandlerTools;
import buildcraft.test.VanillaSetupBaseTester;

public class ListTester extends VanillaSetupBaseTester {
    @Test
    public void testTools() {
        ListMatchHandlerTools matcher = new ListMatchHandlerTools();
        ItemStack woodenAxe = new ItemStack(Items.WOODEN_AXE);
        ItemStack ironAxe = new ItemStack(Items.IRON_AXE);
        ItemStack woodenShovel = new ItemStack(Items.WOODEN_SHOVEL);
        ItemStack woodenAxeDamaged = new ItemStack(Items.WOODEN_AXE);
//        woodenAxeDamaged.setItemDamage(26);
        woodenAxeDamaged.setDamageValue(26);
        ItemStack apple = new ItemStack(Items.APPLE);

        Assert.assertTrue(matcher.isValidSource(Type.TYPE, woodenAxe));
        Assert.assertTrue(matcher.isValidSource(Type.TYPE, woodenAxeDamaged));
        Assert.assertFalse(matcher.isValidSource(Type.TYPE, apple));

        Assert.assertTrue(matcher.matches(Type.TYPE, woodenAxe, ironAxe, false));
        Assert.assertTrue(matcher.matches(Type.TYPE, woodenAxe, woodenAxeDamaged, false));
        Assert.assertFalse(matcher.matches(Type.TYPE, woodenAxe, woodenShovel, false));
        Assert.assertFalse(matcher.matches(Type.TYPE, woodenAxe, apple, false));
    }
}
