package buildcraft.test.lib.list;

import org.junit.Assert;
import org.junit.Test;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import buildcraft.api.lists.ListMatchHandler.Type;

import buildcraft.lib.list.ListMatchHandlerArmor;
import buildcraft.lib.list.ListMatchHandlerFluid;
import buildcraft.lib.list.ListMatchHandlerTools;
import buildcraft.test.VanillaSetupBaseTester;

public class ListTester extends VanillaSetupBaseTester {
    @Test
    public void testFluids() {
        ListMatchHandlerFluid matcher = new ListMatchHandlerFluid();
        ItemStack emptyBucket = new ItemStack(Items.BUCKET);
        ItemStack lavaBucket = new ItemStack(Items.LAVA_BUCKET);
        ItemStack waterBucket = new ItemStack(Items.WATER_BUCKET);
        ItemStack apple = new ItemStack(Items.APPLE);

        Assert.assertTrue(matcher.isValidSource(Type.TYPE, emptyBucket));
        Assert.assertTrue(matcher.isValidSource(Type.MATERIAL, emptyBucket));
        Assert.assertTrue(matcher.isValidSource(Type.TYPE, waterBucket));
        Assert.assertTrue(matcher.isValidSource(Type.MATERIAL, waterBucket));
        Assert.assertFalse(matcher.isValidSource(Type.TYPE, apple));
        Assert.assertFalse(matcher.isValidSource(Type.MATERIAL, apple));

        Assert.assertTrue(matcher.matches(Type.TYPE, emptyBucket, emptyBucket, false));
        Assert.assertTrue(matcher.matches(Type.TYPE, lavaBucket, emptyBucket, false));
        Assert.assertTrue(matcher.matches(Type.TYPE, waterBucket, emptyBucket, false));
        Assert.assertTrue(matcher.matches(Type.TYPE, emptyBucket, lavaBucket, false));
        Assert.assertFalse(matcher.matches(Type.TYPE, apple, lavaBucket, false));
        Assert.assertFalse(matcher.matches(Type.TYPE, emptyBucket, apple, false));

        Assert.assertTrue(matcher.matches(Type.MATERIAL, emptyBucket, emptyBucket, false));
        Assert.assertTrue(matcher.matches(Type.MATERIAL, lavaBucket, lavaBucket, false));
        Assert.assertFalse(matcher.matches(Type.MATERIAL, waterBucket, lavaBucket, false));
        Assert.assertFalse(matcher.matches(Type.MATERIAL, apple, waterBucket, false));
    }

    @Test
    public void testTools() {
        ListMatchHandlerTools matcher = new ListMatchHandlerTools();
        ItemStack woodenAxe = new ItemStack(Items.WOODEN_AXE);
        ItemStack ironAxe = new ItemStack(Items.IRON_AXE);
        ItemStack woodenShovel = new ItemStack(Items.WOODEN_SHOVEL);
        ItemStack woodenAxeDamaged = new ItemStack(Items.WOODEN_AXE);
        woodenAxeDamaged.setItemDamage(26);
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
