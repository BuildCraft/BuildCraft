package buildcraft.transport;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.registry.GameRegistry;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.GateExpansions;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.lib.utils.ColorUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.silicon.ItemRedstoneChipset;
import buildcraft.transport.gates.GateDefinition;
import buildcraft.transport.gates.GateExpansionPulsar;
import buildcraft.transport.gates.GateExpansionRedstoneFader;
import buildcraft.transport.gates.GateExpansionTimer;
import buildcraft.transport.gates.ItemGate;
import buildcraft.transport.recipes.AdvancedFacadeRecipe;
import buildcraft.transport.recipes.GateExpansionRecipe;

public final class TransportSiliconRecipes {
	private TransportSiliconRecipes() {

	}

	@Optional.Method(modid = "BuildCraft|Silicon")
	public static void loadSiliconRecipes() {
		GameRegistry.addShapelessRecipe(new ItemStack(BuildCraftTransport.gateCopier, 1), new ItemStack(BuildCraftCore.wrenchItem), ItemRedstoneChipset.Chipset.RED.getStack(1));

		if (Utils.isRegistered(BuildCraftTransport.lensItem)) {
			// Lenses, Filters
			for (int i = 0; i < 16; i++) {
				BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:lens:" + i, 10000, new ItemStack(BuildCraftTransport.lensItem, 2, i),
						ColorUtils.getOreDictionaryName(15 - i), "blockGlass");
				BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:filter:" + i, 10000, new ItemStack(BuildCraftTransport.lensItem, 2, i + 16),
						ColorUtils.getOreDictionaryName(15 - i), "blockGlass", Blocks.iron_bars);
			}

			BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:lens:16", 10000, new ItemStack(BuildCraftTransport.lensItem, 2, 32),
					"blockGlass");
			BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:filter:16", 10000, new ItemStack(BuildCraftTransport.lensItem, 2, 33),
					"blockGlass", Blocks.iron_bars);
		}

		// PIPE WIRE
		if (Utils.isRegistered(PipeWire.item)) {
			BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:redWire", 5000, PipeWire.RED.getStack(8),
					"dyeRed", "dustRedstone", "ingotIron");
			BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:blueWire", 5000, PipeWire.BLUE.getStack(8),
					"dyeBlue", "dustRedstone", "ingotIron");
			BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:greenWire", 5000, PipeWire.GREEN.getStack(8),
					"dyeGreen", "dustRedstone", "ingotIron");
			BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:yellowWire", 5000, PipeWire.YELLOW.getStack(8),
					"dyeYellow", "dustRedstone", "ingotIron");

			if (Utils.isRegistered(ItemRedstoneChipset.Chipset.RED.getStack()) && Utils.isRegistered(BuildCraftTransport.pipeGate)) {
				// GATES
				BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:simpleGate", Math.round(100000 * BuildCraftTransport.gateCostMultiplier),
						ItemGate.makeGateItem(GateDefinition.GateMaterial.REDSTONE, GateDefinition.GateLogic.AND), ItemRedstoneChipset.Chipset.RED.getStack(),
						PipeWire.RED.getStack());

				addGateRecipe("Iron", Math.round(200000 * BuildCraftTransport.gateCostMultiplier), GateDefinition.GateMaterial.IRON, ItemRedstoneChipset.Chipset.IRON, PipeWire.RED, PipeWire.BLUE);
				addGateRecipe("Gold", Math.round(400000 * BuildCraftTransport.gateCostMultiplier), GateDefinition.GateMaterial.GOLD, ItemRedstoneChipset.Chipset.GOLD, PipeWire.RED, PipeWire.BLUE, PipeWire.GREEN);
				addGateRecipe("Quartz", Math.round(600000 * BuildCraftTransport.gateCostMultiplier), GateDefinition.GateMaterial.QUARTZ, ItemRedstoneChipset.Chipset.QUARTZ, PipeWire.RED, PipeWire.BLUE, PipeWire.GREEN);
				addGateRecipe("Diamond", Math.round(800000 * BuildCraftTransport.gateCostMultiplier), GateDefinition.GateMaterial.DIAMOND, ItemRedstoneChipset.Chipset.DIAMOND, PipeWire.RED, PipeWire.BLUE,
						PipeWire.GREEN, PipeWire.YELLOW);
				addGateRecipe("Emerald", Math.round(1200000 * BuildCraftTransport.gateCostMultiplier), GateDefinition.GateMaterial.EMERALD, ItemRedstoneChipset.Chipset.EMERALD, PipeWire.RED, PipeWire.BLUE,
						PipeWire.GREEN, PipeWire.YELLOW);

				BuildcraftRecipeRegistry.integrationTable.addRecipe(new GateExpansionRecipe());
				BuildcraftRecipeRegistry.integrationTable.addRecipe(new AdvancedFacadeRecipe());

				// This will only add recipes to the gate expansions.
				GateExpansions.registerExpansion(GateExpansionPulsar.INSTANCE, ItemRedstoneChipset.Chipset.PULSATING.getStack());
				GateExpansions.registerExpansion(GateExpansionTimer.INSTANCE, ItemRedstoneChipset.Chipset.QUARTZ.getStack());
				GateExpansions.registerExpansion(GateExpansionRedstoneFader.INSTANCE, ItemRedstoneChipset.Chipset.COMP.getStack());
			}
		}
	}

	@Optional.Method(modid = "BuildCraft|Silicon")
	private static void addGateRecipe(String materialName, int energyCost, GateDefinition.GateMaterial material, ItemRedstoneChipset.Chipset chipset,
									  PipeWire... pipeWire) {
		List<ItemStack> temp = new ArrayList<ItemStack>();
		temp.add(chipset.getStack());
		for (PipeWire wire : pipeWire) {
			temp.add(wire.getStack());
		}
		Object[] inputs = temp.toArray();
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:andGate" + materialName, energyCost,
				ItemGate.makeGateItem(material, GateDefinition.GateLogic.AND), inputs);
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:orGate" + materialName, energyCost,
				ItemGate.makeGateItem(material, GateDefinition.GateLogic.OR), inputs);
	}
}
