/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
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
import buildcraft.core.network.BuildCraftChannelHandler;
import buildcraft.core.network.PacketHandler;
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
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.config.Property;

@Mod(name = "BuildCraft Silicon", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Silicon", dependencies = DefaultProps.DEPENDENCY_TRANSPORT)
public class BuildCraftSilicon extends BuildCraftMod {

	public static ItemRedstoneChipset redstoneChipset;
	public static BlockLaser laserBlock;
	public static BlockLaserTable assemblyTableBlock;
	@Instance("BuildCraft|Silicon")
	public static BuildCraftSilicon instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {		
		BuildCraftCore.mainConfiguration.save();

		laserBlock = new BlockLaser();
		laserBlock.setBlockName("laserBlock");
		CoreProxy.proxy.registerBlock(laserBlock);

		assemblyTableBlock = new BlockLaserTable();
		CoreProxy.proxy.registerBlock(assemblyTableBlock, ItemLaserTable.class);

		LanguageRegistry.addName(new ItemStack(assemblyTableBlock, 0, 0), "Assembly Table");
		LanguageRegistry.addName(new ItemStack(assemblyTableBlock, 0, 1), "Advanced Crafting Table");
		LanguageRegistry.addName(new ItemStack(assemblyTableBlock, 0, 2), "Integration Table");

		redstoneChipset = new ItemRedstoneChipset();
		redstoneChipset.setUnlocalizedName("redstoneChipset");
		CoreProxy.proxy.registerItem(redstoneChipset);
		redstoneChipset.registerItemStacks();
	}

	@EventHandler
	public void init(FMLInitializationEvent evt) {
		channels = NetworkRegistry.INSTANCE.newChannel
				(DefaultProps.NET_CHANNEL_NAME + "-SILICON", new BuildCraftChannelHandler(), new PacketHandlerSilicon());
		
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
		CoreProxy.proxy.registerTileEntity(TileLaser.class, "net.minecraft.src.buildcraft.factory.TileLaser");
		CoreProxy.proxy.registerTileEntity(TileAssemblyTable.class, "net.minecraft.src.buildcraft.factory.TileAssemblyTable");
		CoreProxy.proxy.registerTileEntity(TileAdvancedCraftingTable.class, "net.minecraft.src.buildcraft.factory.TileAssemblyAdvancedWorkbench");
		CoreProxy.proxy.registerTileEntity(TileIntegrationTable.class, "net.minecraft.src.buildcraft.factory.TileIntegrationTable");

		//new BptBlockRotateMeta(laserBlock.blockID, new int[]{2, 5, 3, 4}, true);
		//new BptBlockInventory(assemblyTableBlock.blockID);

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
				'O', Blocks.obsidian,
				'R', Items.redstone,
				'D', Items.diamond);

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(assemblyTableBlock, 1, 0),
				"ORO",
				"ODO",
				"OGO",
				'O', Blocks.obsidian,
				'R', Items.redstone,
				'D', Items.diamond,
				'G', BuildCraftCore.diamondGearItem);

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(assemblyTableBlock, 1, 1),
				"OWO",
				"OCO",
				"ORO",
				'O', Blocks.obsidian,
				'W', Blocks.crafting_table,
				'C', Blocks.chest,
				'R', new ItemStack(redstoneChipset, 1, 0));

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(assemblyTableBlock, 1, 2),
				"ORO",
				"OCO",
				"OGO",
				'O', Blocks.obsidian,
				'R', Items.redstone,
				'C', new ItemStack(redstoneChipset, 1, 0),
				'G', BuildCraftCore.diamondGearItem);

		// PIPE WIRE
		BuildcraftRecipes.assemblyTable.addRecipe(500, PipeWire.RED.getStack(8), "dyeRed", 1, Items.redstone, Items.iron_ingot);
		BuildcraftRecipes.assemblyTable.addRecipe(500, PipeWire.BLUE.getStack(8), "dyeBlue", 1, Items.redstone, Items.iron_ingot);
		BuildcraftRecipes.assemblyTable.addRecipe(500, PipeWire.GREEN.getStack(8), "dyeGreen", 1, Items.redstone, Items.iron_ingot);
		BuildcraftRecipes.assemblyTable.addRecipe(500, PipeWire.YELLOW.getStack(8), "dyeYellow", 1, Items.redstone, Items.iron_ingot);

		// CHIPSETS
		BuildcraftRecipes.assemblyTable.addRecipe(10000, Chipset.RED.getStack(), Items.redstone);
		BuildcraftRecipes.assemblyTable.addRecipe(20000, Chipset.IRON.getStack(), Items.redstone, Items.iron_ingot);
		BuildcraftRecipes.assemblyTable.addRecipe(40000, Chipset.GOLD.getStack(), Items.redstone, Items.gold_ingot);
		BuildcraftRecipes.assemblyTable.addRecipe(80000, Chipset.DIAMOND.getStack(), Items.redstone, Items.diamond);
		BuildcraftRecipes.assemblyTable.addRecipe(40000, Chipset.PULSATING.getStack(2), Items.redstone, Items.ender_pearl);
		BuildcraftRecipes.assemblyTable.addRecipe(60000, Chipset.QUARTZ.getStack(), Items.redstone, Items.quartz);
		BuildcraftRecipes.assemblyTable.addRecipe(60000, Chipset.COMP.getStack(), Items.redstone, Items.comparator);

		// GATES		
		BuildcraftRecipes.assemblyTable.addRecipe(10000, ItemGate.makeGateItem(GateMaterial.REDSTONE, GateLogic.AND), Chipset.RED.getStack(), PipeWire.RED.getStack());

		addGateRecipe(20000, GateMaterial.IRON, Chipset.IRON, PipeWire.RED, PipeWire.BLUE);
		addGateRecipe(40000, GateMaterial.GOLD, Chipset.GOLD, PipeWire.RED, PipeWire.BLUE, PipeWire.GREEN);
		addGateRecipe(80000, GateMaterial.DIAMOND, Chipset.DIAMOND, PipeWire.RED, PipeWire.BLUE, PipeWire.GREEN, PipeWire.YELLOW);

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
