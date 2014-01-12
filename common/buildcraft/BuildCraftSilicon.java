/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile or
 * run the code. It does *NOT* grant the right to redistribute this software or
 * its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */
package buildcraft;

import buildcraft.api.bptblocks.BptBlockInventory;
import buildcraft.api.bptblocks.BptBlockRotateMeta;
import buildcraft.api.recipes.BuildcraftRecipes;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.silicon.ItemRedstoneChipset;
import buildcraft.silicon.ItemRedstoneChipset.Chipset;
import buildcraft.core.Version;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.silicon.BlockLaser;
import buildcraft.silicon.BlockLaserTable;
import buildcraft.silicon.GuiHandler;
import buildcraft.silicon.ItemLaserTable;
import buildcraft.silicon.SiliconProxy;
import buildcraft.silicon.TileAdvancedCraftingTable;
import buildcraft.silicon.TileAssemblyTable;
import buildcraft.silicon.TileIntegrationTable;
import buildcraft.silicon.TileLaser;
import buildcraft.silicon.network.PacketHandlerSilicon;
import buildcraft.transport.gates.GateDefinition.GateMaterial;
import buildcraft.transport.gates.GateExpansionPulsar;
import buildcraft.silicon.recipes.GateExpansionRecipe;
import buildcraft.silicon.recipes.GateLogicSwapRecipe;
import buildcraft.transport.gates.GateDefinition.GateLogic;
import buildcraft.transport.gates.GateExpansionRedstoneFader;
import buildcraft.transport.gates.GateExpansionTimer;
import buildcraft.transport.gates.ItemGate;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Property;

@Mod(name = "BuildCraft Silicon", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Silicon", dependencies = DefaultProps.DEPENDENCY_TRANSPORT)
@NetworkMod(channels = {DefaultProps.NET_CHANNEL_NAME}, packetHandler = PacketHandlerSilicon.class, clientSideRequired = true, serverSideRequired = true)
public class BuildCraftSilicon {

	public static ItemRedstoneChipset redstoneChipset;
	public static BlockLaser laserBlock;
	public static BlockLaserTable assemblyTableBlock;
	@Instance("BuildCraft|Silicon")
	public static BuildCraftSilicon instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		Property laserId = BuildCraftCore.mainConfiguration.getBlock("laser.id", DefaultProps.LASER_ID);

		Property assemblyTableId = BuildCraftCore.mainConfiguration.getBlock("assemblyTable.id", DefaultProps.ASSEMBLY_TABLE_ID);

		Property redstoneChipsetId = BuildCraftCore.mainConfiguration.getItem("redstoneChipset.id", DefaultProps.REDSTONE_CHIPSET);

		BuildCraftCore.mainConfiguration.save();

		laserBlock = new BlockLaser(laserId.getInt());
		CoreProxy.proxy.addName(laserBlock.setUnlocalizedName("laserBlock"), "Laser");
		CoreProxy.proxy.registerBlock(laserBlock);

		assemblyTableBlock = new BlockLaserTable(assemblyTableId.getInt());
		CoreProxy.proxy.registerBlock(assemblyTableBlock, ItemLaserTable.class);

		LanguageRegistry.addName(new ItemStack(assemblyTableBlock, 0, 0), "Assembly Table");
		LanguageRegistry.addName(new ItemStack(assemblyTableBlock, 0, 1), "Advanced Crafting Table");
		LanguageRegistry.addName(new ItemStack(assemblyTableBlock, 0, 2), "Integration Table");

		redstoneChipset = new ItemRedstoneChipset(redstoneChipsetId.getInt());
		redstoneChipset.setUnlocalizedName("redstoneChipset");
		CoreProxy.proxy.registerItem(redstoneChipset);
		redstoneChipset.registerItemStacks();
	}

	@EventHandler
	public void init(FMLInitializationEvent evt) {
		NetworkRegistry.instance().registerGuiHandler(instance, new GuiHandler());
		CoreProxy.proxy.registerTileEntity(TileLaser.class, "net.minecraft.src.buildcraft.factory.TileLaser");
		CoreProxy.proxy.registerTileEntity(TileAssemblyTable.class, "net.minecraft.src.buildcraft.factory.TileAssemblyTable");
		CoreProxy.proxy.registerTileEntity(TileAdvancedCraftingTable.class, "net.minecraft.src.buildcraft.factory.TileAssemblyAdvancedWorkbench");
		CoreProxy.proxy.registerTileEntity(TileIntegrationTable.class, "net.minecraft.src.buildcraft.factory.TileIntegrationTable");

		new BptBlockRotateMeta(laserBlock.blockID, new int[]{2, 5, 3, 4}, true);
		new BptBlockInventory(assemblyTableBlock.blockID);

		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}

		SiliconProxy.proxy.registerRenderers();
	}

	public static void loadRecipes() {

		// TABLES
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(laserBlock),
				"ORR",
				"DDR",
				"ORR",
				'O', Block.obsidian,
				'R', Item.redstone,
				'D', Item.diamond);

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(assemblyTableBlock, 1, 0),
				"ORO",
				"ODO",
				"OGO",
				'O', Block.obsidian,
				'R', Item.redstone,
				'D', Item.diamond,
				'G', BuildCraftCore.diamondGearItem);

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(assemblyTableBlock, 1, 1),
				"OWO",
				"OCO",
				"ORO",
				'O', Block.obsidian,
				'W', Block.workbench,
				'C', Block.chest,
				'R', new ItemStack(redstoneChipset, 1, 0));

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(assemblyTableBlock, 1, 2),
				"ORO",
				"OCO",
				"OGO",
				'O', Block.obsidian,
				'R', Item.redstone,
				'C', new ItemStack(redstoneChipset, 1, 0),
				'G', BuildCraftCore.diamondGearItem);

		// PIPE WIRE
		BuildcraftRecipes.assemblyTable.addRecipe(500, PipeWire.RED.getStack(8), "dyeRed", 1, Item.redstone, Item.ingotIron);
		BuildcraftRecipes.assemblyTable.addRecipe(500, PipeWire.BLUE.getStack(8), "dyeBlue", 1, Item.redstone, Item.ingotIron);
		BuildcraftRecipes.assemblyTable.addRecipe(500, PipeWire.GREEN.getStack(8), "dyeGreen", 1, Item.redstone, Item.ingotIron);
		BuildcraftRecipes.assemblyTable.addRecipe(500, PipeWire.YELLOW.getStack(8), "dyeYellow", 1, Item.redstone, Item.ingotIron);

		// CHIPSETS
		BuildcraftRecipes.assemblyTable.addRecipe(10000, Chipset.RED.getStack(), Item.redstone);
		BuildcraftRecipes.assemblyTable.addRecipe(20000, Chipset.IRON.getStack(), Item.redstone, Item.ingotIron);
		BuildcraftRecipes.assemblyTable.addRecipe(40000, Chipset.GOLD.getStack(), Item.redstone, Item.ingotGold);
		BuildcraftRecipes.assemblyTable.addRecipe(80000, Chipset.DIAMOND.getStack(), Item.redstone, Item.diamond);
		BuildcraftRecipes.assemblyTable.addRecipe(40000, Chipset.PULSATING.getStack(2), Item.redstone, Item.enderPearl);
		BuildcraftRecipes.assemblyTable.addRecipe(60000, Chipset.QUARTZ.getStack(), Item.redstone, Item.netherQuartz);
		BuildcraftRecipes.assemblyTable.addRecipe(60000, Chipset.COMP.getStack(), Item.redstone, Item.comparator);

		// GATES		
		BuildcraftRecipes.assemblyTable.addRecipe(10000, ItemGate.makeGateItem(GateMaterial.REDSTONE, GateLogic.AND), Chipset.RED.getStack(), PipeWire.RED.getStack());

		addGateRecipe(20000, GateMaterial.IRON, Chipset.IRON, PipeWire.RED, PipeWire.BLUE);
		addGateRecipe(40000, GateMaterial.GOLD, Chipset.GOLD, PipeWire.RED, PipeWire.BLUE, PipeWire.YELLOW);
		addGateRecipe(80000, GateMaterial.DIAMOND, Chipset.DIAMOND, PipeWire.RED, PipeWire.BLUE, PipeWire.YELLOW, PipeWire.GREEN);

		// REVERSAL RECIPES
		EnumSet<GateMaterial> materials = EnumSet.allOf(GateMaterial.class);
		materials.remove(GateMaterial.REDSTONE);
		for (GateMaterial material : materials) {
			BuildcraftRecipes.integrationTable.addRecipe(new GateLogicSwapRecipe(material, GateLogic.AND, GateLogic.OR));
			BuildcraftRecipes.integrationTable.addRecipe(new GateLogicSwapRecipe(material, GateLogic.OR, GateLogic.AND));
		}

		// EXPANSIONS
		BuildcraftRecipes.integrationTable.addRecipe(new GateExpansionRecipe(GateExpansionPulsar.INSTANCE, Chipset.PULSATING.getStack()));
		BuildcraftRecipes.integrationTable.addRecipe(new GateExpansionRecipe(GateExpansionTimer.INSTANCE, Chipset.QUARTZ.getStack()));
		BuildcraftRecipes.integrationTable.addRecipe(new GateExpansionRecipe(GateExpansionRedstoneFader.INSTANCE, Chipset.COMP.getStack()));
	}

	private static void addGateRecipe(double energyCost, GateMaterial material, Chipset chipset, PipeWire... pipeWire) {
		List temp = new ArrayList();
		temp.add(chipset.getStack());
		for (PipeWire wire : pipeWire) {
			temp.add(wire.getStack());
		}
		Object[] inputs = temp.toArray();
		BuildcraftRecipes.assemblyTable.addRecipe(energyCost, ItemGate.makeGateItem(material, GateLogic.AND), inputs);
		BuildcraftRecipes.assemblyTable.addRecipe(energyCost, ItemGate.makeGateItem(material, GateLogic.OR), inputs);
	}

	@EventHandler
	public void processIMCRequests(FMLInterModComms.IMCEvent event) {
		InterModComms.processIMC(event);
	}
}
