package buildcraft.core.village;

import java.util.Random;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

import cpw.mods.fml.common.registry.VillagerRegistry.IVillageTradeHandler;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftEnergy;
import buildcraft.BuildCraftFactory;
import buildcraft.BuildCraftSilicon;
import buildcraft.BuildCraftTransport;

public class VillagerTradeHandler implements IVillageTradeHandler
{

	@Override
	public void manipulateTradesForVillager(EntityVillager villager, MerchantRecipeList recipeList, Random random)
	{
		recipeList.clear();
		//Buy
		recipeList.add(new MerchantRecipe(new ItemStack(BuildCraftEnergy.bucketFuel), new ItemStack(Items.emerald, 3)));
		recipeList.add(new MerchantRecipe(new ItemStack(BuildCraftTransport.pipeWaterproof, 10 + random.nextInt(6)), new ItemStack(Items.emerald)));
		recipeList.add(new MerchantRecipe(new ItemStack(BuildCraftFactory.tankBlock, 10 + random.nextInt(5)), new ItemStack(Items.emerald)));
		recipeList.add(new MerchantRecipe(new ItemStack(BuildCraftEnergy.engineBlock, 2 + random.nextInt(2)), new ItemStack(Items.emerald)));
		//Sell
		recipeList.add(new MerchantRecipe(new ItemStack(Items.emerald, 3), new ItemStack(BuildCraftSilicon.redstoneChipset, random.nextInt(10))));
		recipeList.add(new MerchantRecipe(new ItemStack(Items.emerald, 1), new ItemStack(BuildCraftTransport.pipeItemsWood), new ItemStack(BuildCraftTransport.pipeFluidsWood, 2)));
		recipeList.add(new MerchantRecipe(new ItemStack(Items.emerald, 1), new ItemStack(BuildCraftTransport.pipeItemsCobblestone), new ItemStack(BuildCraftTransport.pipeFluidsCobblestone, 2)));
		recipeList.add(new MerchantRecipe(new ItemStack(Items.emerald, 1), new ItemStack(BuildCraftTransport.pipeItemsStone), new ItemStack(BuildCraftTransport.pipeFluidsStone, 2)));
		recipeList.add(new MerchantRecipe(new ItemStack(Items.emerald, 1), new ItemStack(BuildCraftTransport.pipeItemsIron), new ItemStack(BuildCraftTransport.pipeFluidsIron, 2)));
		recipeList.add(new MerchantRecipe(new ItemStack(Items.emerald, 1), new ItemStack(BuildCraftTransport.pipeItemsGold), new ItemStack(BuildCraftTransport.pipeFluidsGold, 2)));
		recipeList.add(new MerchantRecipe(new ItemStack(Items.emerald, 1), new ItemStack(BuildCraftTransport.pipeItemsDiamond), new ItemStack(BuildCraftTransport.pipeFluidsDiamond, 2)));
		recipeList.add(new MerchantRecipe(new ItemStack(Items.emerald, 1), new ItemStack(BuildCraftEnergy.bucketOil)));
		recipeList.add(new MerchantRecipe(new ItemStack(Items.emerald, 6 + random.nextInt(4)), new ItemStack(BuildCraftSilicon.laserBlock)));
		villager.setRecipes(recipeList);
	}

}
