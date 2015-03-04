/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.builders.schematics.SchematicRotateMeta;
import buildcraft.commander.BlockRequester;
import buildcraft.commander.BlockZonePlan;
import buildcraft.commander.TileRequester;
import buildcraft.commander.TileZonePlan;
import buildcraft.core.CompatHooks;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.Version;
import buildcraft.core.network.ChannelHandler;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.silicon.BlockLaser;
import buildcraft.silicon.BlockLaserTable;
import buildcraft.silicon.GuiHandler;
import buildcraft.silicon.ItemLaserTable;
import buildcraft.silicon.ItemRedstoneChipset;
import buildcraft.silicon.ItemRedstoneChipset.Chipset;
import buildcraft.silicon.SiliconProxy;
import buildcraft.silicon.TileAdvancedCraftingTable;
import buildcraft.silicon.TileAssemblyTable;
import buildcraft.silicon.TileChargingTable;
import buildcraft.silicon.TileIntegrationTable;
import buildcraft.silicon.TileLaser;
import buildcraft.silicon.TileProgrammingTable;
import buildcraft.silicon.network.PacketHandlerSilicon;

@Mod(name = "BuildCraft Silicon", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Silicon", dependencies = DefaultProps.DEPENDENCY_TRANSPORT)
public class BuildCraftSilicon extends BuildCraftMod {
	@Mod.Instance("BuildCraft|Silicon")
	public static BuildCraftSilicon instance;

	public static ItemRedstoneChipset redstoneChipset;
	public static BlockLaser laserBlock;
	public static BlockLaserTable assemblyTableBlock;
	public static BlockZonePlan zonePlanBlock;
	public static BlockRequester requesterBlock;
	public static Item redstoneCrystal;

	public static Achievement timeForSomeLogicAchievement;
	public static Achievement tinglyLaserAchievement;

	public static float chipsetCostMultiplier = 1.0F;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		chipsetCostMultiplier = BuildCraftCore.mainConfiguration.getFloat("chipset.costMultiplier", "general", 1.0F, 0.001F, 1000.0F, "The multiplier for chipset recipe cost.");

		BuildCraftCore.mainConfiguration.save();

		laserBlock = (BlockLaser) CompatHooks.INSTANCE.getBlock(BlockLaser.class);
		laserBlock.setBlockName("laserBlock");
		CoreProxy.proxy.registerBlock(laserBlock);

		assemblyTableBlock = (BlockLaserTable) CompatHooks.INSTANCE.getBlock(BlockLaserTable.class);
		assemblyTableBlock.setBlockName("laserTableBlock");
		CoreProxy.proxy.registerBlock(assemblyTableBlock, ItemLaserTable.class);

		zonePlanBlock = (BlockZonePlan) CompatHooks.INSTANCE.getBlock(BlockZonePlan.class);
		zonePlanBlock.setBlockName("zonePlan");
		CoreProxy.proxy.registerBlock(zonePlanBlock);

		requesterBlock = (BlockRequester) CompatHooks.INSTANCE.getBlock(BlockRequester.class);
		requesterBlock.setBlockName("requester");
		CoreProxy.proxy.registerBlock(requesterBlock);

		redstoneChipset = new ItemRedstoneChipset();
		redstoneChipset.setUnlocalizedName("redstoneChipset");
		CoreProxy.proxy.registerItem(redstoneChipset);
		redstoneChipset.registerItemStacks();

		redstoneCrystal = (new ItemBuildCraft()).setUnlocalizedName("redstoneCrystal");
		CoreProxy.proxy.registerItem(redstoneCrystal);
		OreDictionary.registerOre("redstoneCrystal", new ItemStack(redstoneCrystal));
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {
		channels = NetworkRegistry.INSTANCE
				.newChannel
				(DefaultProps.NET_CHANNEL_NAME + "-SILICON", new ChannelHandler(), new PacketHandlerSilicon());

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
		CoreProxy.proxy.registerTileEntity(TileLaser.class, "net.minecraft.src.buildcraft.factory.TileLaser");
		CoreProxy.proxy.registerTileEntity(TileAssemblyTable.class,
				"net.minecraft.src.buildcraft.factory.TileAssemblyTable");
		CoreProxy.proxy.registerTileEntity(TileAdvancedCraftingTable.class,
				"net.minecraft.src.buildcraft.factory.TileAssemblyAdvancedWorkbench");
		CoreProxy.proxy.registerTileEntity(TileIntegrationTable.class,
				"net.minecraft.src.buildcraft.factory.TileIntegrationTable");
        CoreProxy.proxy.registerTileEntity(TileChargingTable.class,
                "net.minecraft.src.buildcraft.factory.TileChargingTable");
		CoreProxy.proxy.registerTileEntity(TileProgrammingTable.class,
				"net.minecraft.src.buildcraft.factory.TileProgrammingTable");
		CoreProxy.proxy.registerTileEntity(TileZonePlan.class, "net.minecraft.src.buildcraft.commander.TileZonePlan");
		CoreProxy.proxy.registerTileEntity(TileRequester.class, "net.minecraft.src.buildcraft.commander.TileRequester");

		BuilderAPI.schematicRegistry.registerSchematicBlock(laserBlock, SchematicRotateMeta.class, new int[] {2, 5, 3, 4}, true);

		timeForSomeLogicAchievement = BuildCraftCore.achievementManager.registerAchievement(new Achievement("achievement.timeForSomeLogic", "timeForSomeLogicAchievement", 9, -2, assemblyTableBlock, BuildCraftCore.diamondGearAchievement));
		tinglyLaserAchievement = BuildCraftCore.achievementManager.registerAchievement(new Achievement("achievement.tinglyLaser", "tinglyLaserAchievement", 11, -2, laserBlock, timeForSomeLogicAchievement));

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
				'R', "dustRedstone",
				'D', "gemDiamond");

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(assemblyTableBlock, 1, 0),
				"ORO",
				"ODO",
				"OGO",
				'O', Blocks.obsidian,
				'R', "dustRedstone",
				'D', "gemDiamond",
				'G', "gearDiamond");

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
				'R', "dustRedstone",
				'C', new ItemStack(redstoneChipset, 1, 0),
				'G', "gearDiamond");

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(assemblyTableBlock, 1, 3),
				"ORO",
				"OCO",
				"OGO",
				'O', Blocks.obsidian,
				'R', "dustRedstone",
				'C', new ItemStack(redstoneChipset, 1, 0),
				'G', "gearGold");

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(assemblyTableBlock, 1, 4),
				"ORO",
				"OCO",
				"OGO",
				'O', Blocks.obsidian,
				'R', "dustRedstone",
				'C', Items.emerald,
				'G', "gearDiamond");

		// COMMANDER BLOCKS
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(zonePlanBlock, 1, 0),
				"IRI",
				"GMG",
				"IDI",
				'M', Items.map,
				'R', "dustRedstone",
				'G', "gearGold",
				'D', "gearDiamond",
				'I', "ingotIron");
		
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(requesterBlock, 1, 0),
				"IPI",
				"GCG",
				"IRI",
				'C', Blocks.chest,
				'R', "dustRedstone",
				'P', Blocks.piston,
				'G', "gearIron",
				'I', "ingotIron");
		
		// CHIPSETS
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:redstoneChipset", Math.round(100000 * chipsetCostMultiplier), Chipset.RED.getStack(),
				"dustRedstone");
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:ironChipset", Math.round(200000 * chipsetCostMultiplier), Chipset.IRON.getStack(),
				"dustRedstone", "ingotIron");
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:goldChipset", Math.round(400000 * chipsetCostMultiplier), Chipset.GOLD.getStack(),
				"dustRedstone", "ingotGold");
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:diamondChipset", Math.round(800000 * chipsetCostMultiplier),
				Chipset.DIAMOND.getStack(), "dustRedstone", "gemDiamond");
        BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:emeraldChipset", Math.round(1200000 * chipsetCostMultiplier),
                Chipset.EMERALD.getStack(), "dustRedstone", "gemEmerald");
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:pulsatingChipset", Math.round(400000 * chipsetCostMultiplier),
				Chipset.PULSATING.getStack(2), "dustRedstone", Items.ender_pearl);
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:quartzChipset", Math.round(600000 * chipsetCostMultiplier), Chipset.QUARTZ.getStack(),
				"dustRedstone", "gemQuartz");
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:compChipset", Math.round(600000 * chipsetCostMultiplier), Chipset.COMP.getStack(),
				"dustRedstone", Items.comparator);

		// ROBOTS AND BOARDS
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:redstoneCrystal", 10000000, new ItemStack(
				redstoneCrystal), new ItemStack(
				Blocks.redstone_block));
	}

	@Mod.EventHandler
	public void processRequests(FMLInterModComms.IMCEvent event) {
		InterModComms.processIMC(event);
	}

	@Mod.EventHandler
	public void whiteListAppliedEnergetics(FMLInitializationEvent event) {
		FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
				TileLaser.class.getCanonicalName());
		FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
				TileAssemblyTable.class.getCanonicalName());
		FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
				TileAdvancedCraftingTable.class.getCanonicalName());
		FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
				TileIntegrationTable.class.getCanonicalName());
	}

	@Mod.EventHandler
	public void remap(FMLMissingMappingsEvent event) {
		for (FMLMissingMappingsEvent.MissingMapping mapping: event.get()) {
			if (mapping.name.equals("BuildCraft|Silicon:null")) {
				if (mapping.type == GameRegistry.Type.ITEM) {
					mapping.remap(Item.getItemFromBlock(assemblyTableBlock));
				} else {
					mapping.remap(assemblyTableBlock);
				}
			}
		}
	}
}
