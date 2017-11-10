/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;

import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.blueprints.SchematicTile;
import buildcraft.core.BCRegistry;
import buildcraft.core.CompatHooks;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.Version;
import buildcraft.core.builders.schematics.SchematicFree;
import buildcraft.core.builders.schematics.SchematicRotateMeta;
import buildcraft.core.config.ConfigManager;
import buildcraft.core.lib.network.ChannelHandler;
import buildcraft.core.lib.network.PacketHandler;
import buildcraft.factory.BlockAutoWorkbench;
import buildcraft.factory.BlockFloodGate;
import buildcraft.factory.BlockHopper;
import buildcraft.factory.BlockMiningWell;
import buildcraft.factory.BlockPlainPipe;
import buildcraft.factory.BlockPump;
import buildcraft.factory.BlockRefinery;
import buildcraft.factory.BlockTank;
import buildcraft.factory.FactoryGuiHandler;
import buildcraft.factory.FactoryProxy;
import buildcraft.factory.FactoryProxyClient;
import buildcraft.factory.PumpDimensionList;
import buildcraft.factory.TileAutoWorkbench;
import buildcraft.factory.TileFloodGate;
import buildcraft.factory.TileHopper;
import buildcraft.factory.TileMiningWell;
import buildcraft.factory.TilePump;
import buildcraft.factory.TileRefinery;
import buildcraft.factory.TileTank;
import buildcraft.factory.schematics.SchematicAutoWorkbench;
import buildcraft.factory.schematics.SchematicPump;
import buildcraft.factory.schematics.SchematicRefinery;
import buildcraft.factory.schematics.SchematicTileIgnoreState;

@Mod(name = "BuildCraft Factory", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Factory", dependencies = DefaultProps.DEPENDENCY_CORE)
public class BuildCraftFactory extends BuildCraftMod {

	@Mod.Instance("BuildCraft|Factory")
	public static BuildCraftFactory instance;

	public static BlockMiningWell miningWellBlock;
	public static BlockAutoWorkbench autoWorkbenchBlock;
	public static BlockPlainPipe plainPipeBlock;
	public static BlockPump pumpBlock;
	public static BlockFloodGate floodGateBlock;
	public static BlockTank tankBlock;
	public static BlockRefinery refineryBlock;
	public static BlockHopper hopperBlock;

	public static Achievement aLotOfCraftingAchievement;
	public static Achievement straightDownAchievement;
	public static Achievement refineAndRedefineAchievement;

	public static int miningDepth = 256;
	public static boolean pumpsNeedRealPower = false;
	public static PumpDimensionList pumpDimensionList;

	@Mod.EventHandler
	public void load(FMLInitializationEvent evt) {
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new FactoryGuiHandler());

		BCRegistry.INSTANCE.registerTileEntity(TileMiningWell.class, "MiningWell");
		BCRegistry.INSTANCE.registerTileEntity(TileAutoWorkbench.class, "AutoWorkbench");
		BCRegistry.INSTANCE.registerTileEntity(TilePump.class, "net.minecraft.src.buildcraft.factory.TilePump");
		BCRegistry.INSTANCE.registerTileEntity(TileFloodGate.class, "net.minecraft.src.buildcraft.factory.TileFloodGate");
		BCRegistry.INSTANCE.registerTileEntity(TileTank.class, "net.minecraft.src.buildcraft.factory.TileTank");
		BCRegistry.INSTANCE.registerTileEntity(TileRefinery.class, "net.minecraft.src.buildcraft.factory.Refinery");
		BCRegistry.INSTANCE.registerTileEntity(TileHopper.class, "net.minecraft.src.buildcraft.factory.TileHopper");

		FactoryProxy.proxy.initializeTileEntities();

		BuilderAPI.schematicRegistry.registerSchematicBlock(refineryBlock, SchematicRefinery.class);
		BuilderAPI.schematicRegistry.registerSchematicBlock(tankBlock, SchematicTileIgnoreState.class);
		BuilderAPI.schematicRegistry.registerSchematicBlock(pumpBlock, SchematicPump.class);
		BuilderAPI.schematicRegistry.registerSchematicBlock(miningWellBlock, SchematicRotateMeta.class, new int[]{2, 5, 3, 4}, true);
		BuilderAPI.schematicRegistry.registerSchematicBlock(floodGateBlock, SchematicTileIgnoreState.class);
		BuilderAPI.schematicRegistry.registerSchematicBlock(autoWorkbenchBlock, SchematicAutoWorkbench.class);
		BuilderAPI.schematicRegistry.registerSchematicBlock(hopperBlock, SchematicTile.class);
		BuilderAPI.schematicRegistry.registerSchematicBlock(plainPipeBlock, SchematicFree.class);

		aLotOfCraftingAchievement = BuildCraftCore.achievementManager.registerAchievement(new Achievement("achievement.aLotOfCrafting", "aLotOfCraftingAchievement", 1, 2, autoWorkbenchBlock, BuildCraftCore.woodenGearAchievement));
		straightDownAchievement = BuildCraftCore.achievementManager.registerAchievement(new Achievement("achievement.straightDown", "straightDownAchievement", 5, 2, miningWellBlock, BuildCraftCore.ironGearAchievement));
		refineAndRedefineAchievement = BuildCraftCore.achievementManager.registerAchievement(new Achievement("achievement.refineAndRedefine", "refineAndRedefineAchievement", 10, 0, refineryBlock, BuildCraftCore.diamondGearAchievement));

		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}
	}

	@Mod.EventHandler
	public void initialize(FMLPreInitializationEvent evt) {
		channels = NetworkRegistry.INSTANCE.newChannel
				(DefaultProps.NET_CHANNEL_NAME + "-FACTORY", new ChannelHandler(), new PacketHandler());

		String plc = "Allows admins to whitelist or blacklist pumping of specific fluids in specific dimensions.\n"
				+ "Eg. \"-/-1/Lava\" will disable lava in the nether. \"-/*/Lava\" will disable lava in any dimension. \"+/0/*\" will enable any fluid in the overworld.\n"
				+ "Entries are comma seperated, banned fluids have precedence over allowed ones."
				+ "Default is \"+/*/*,+/-1/Lava\" - the second redundant entry (\"+/-1/lava\") is there to show the format.";

		BuildCraftCore.mainConfigManager.register("general.miningDepth", 256, "Should the mining well only be usable once after placing?", ConfigManager.RestartRequirement.NONE);

		BuildCraftCore.mainConfigManager.get("general.miningDepth").setMinValue(2).setMaxValue(256);
		BuildCraftCore.mainConfigManager.register("general.pumpDimensionControl", DefaultProps.PUMP_DIMENSION_LIST, plc, ConfigManager.RestartRequirement.NONE);
		BuildCraftCore.mainConfigManager.register("general.pumpsNeedRealPower", false, "Do pumps need real (non-redstone) power?", ConfigManager.RestartRequirement.WORLD);

		reloadConfig(ConfigManager.RestartRequirement.GAME);

		miningWellBlock = (BlockMiningWell) CompatHooks.INSTANCE.getBlock(BlockMiningWell.class);
		if (BCRegistry.INSTANCE.registerBlock(miningWellBlock.setBlockName("miningWellBlock"), false)) {
			plainPipeBlock = new BlockPlainPipe();
			BCRegistry.INSTANCE.registerBlock(plainPipeBlock.setBlockName("plainPipeBlock"), true);
		}

		autoWorkbenchBlock = (BlockAutoWorkbench) CompatHooks.INSTANCE.getBlock(BlockAutoWorkbench.class);
		BCRegistry.INSTANCE.registerBlock(autoWorkbenchBlock.setBlockName("autoWorkbenchBlock"), false);

		tankBlock = (BlockTank) CompatHooks.INSTANCE.getBlock(BlockTank.class);
		BCRegistry.INSTANCE.registerBlock(tankBlock.setBlockName("tankBlock"), false);

		pumpBlock = (BlockPump) CompatHooks.INSTANCE.getBlock(BlockPump.class);
		BCRegistry.INSTANCE.registerBlock(pumpBlock.setBlockName("pumpBlock"), false);

		floodGateBlock = (BlockFloodGate) CompatHooks.INSTANCE.getBlock(BlockFloodGate.class);
		BCRegistry.INSTANCE.registerBlock(floodGateBlock.setBlockName("floodGateBlock"), false);

		refineryBlock = (BlockRefinery) CompatHooks.INSTANCE.getBlock(BlockRefinery.class);
		BCRegistry.INSTANCE.registerBlock(refineryBlock.setBlockName("refineryBlock"), false);

		hopperBlock = (BlockHopper) CompatHooks.INSTANCE.getBlock(BlockHopper.class);
		BCRegistry.INSTANCE.registerBlock(hopperBlock.setBlockName("blockHopper"), false);

		FactoryProxy.proxy.initializeEntityRenders();

		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}

	public static void loadRecipes() {
		if (miningWellBlock != null) {
			BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(miningWellBlock, 1),
					"ipi",
					"igi",
					"iPi",
					'p', "dustRedstone",
					'i', "ingotIron",
					'g', "gearIron",
					'P', Items.iron_pickaxe);
		}

		if (pumpBlock != null) {
			BCRegistry.INSTANCE.addCraftingRecipe(
					new ItemStack(pumpBlock),
					"ipi",
					"igi",
					"TBT",
					'p', "dustRedstone",
					'i', "ingotIron",
					'T', tankBlock,
					'g', "gearIron",
					'B', Items.bucket);
		}

		if (autoWorkbenchBlock != null) {
			BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(autoWorkbenchBlock),
					"gwg",
					'w', "craftingTableWood",
					'g', "gearStone");

			BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(autoWorkbenchBlock),
					"g",
					"w",
					"g",
					'w', "craftingTableWood",
					'g', "gearStone");
		}


		if (tankBlock != null) {
			BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(tankBlock),
					"ggg",
					"g g",
					"ggg",
					'g', "blockGlass");
		}

		if (refineryBlock != null) {
			BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(refineryBlock),
					"RTR",
					"TGT",
					'T', tankBlock != null ? tankBlock : "blockGlass",
					'G', "gearDiamond",
					'R', Blocks.redstone_torch);
		}

		if (hopperBlock != null) {
			BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(hopperBlock),
					"ICI",
					" G ",
					'I', "ingotIron",
					'C', "chestWood",
					'G', "gearStone");

			BCRegistry.INSTANCE.addShapelessRecipe(new ItemStack(hopperBlock), Blocks.hopper, "gearStone");
		}

		if (floodGateBlock != null) {
			BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(floodGateBlock),
					"IGI",
					"FTF",
					"IFI",
					'I', "ingotIron",
					'T', tankBlock != null ? tankBlock : "blockGlass",
					'G', "gearIron",
					'F', new ItemStack(Blocks.iron_bars));
		}
	}

	public void reloadConfig(ConfigManager.RestartRequirement restartType) {
		if (restartType == ConfigManager.RestartRequirement.GAME) {
			reloadConfig(ConfigManager.RestartRequirement.WORLD);
		} else if (restartType == ConfigManager.RestartRequirement.WORLD) {
			reloadConfig(ConfigManager.RestartRequirement.NONE);
		} else {
			miningDepth = BuildCraftCore.mainConfigManager.get("general.miningDepth").getInt();
			pumpsNeedRealPower = BuildCraftCore.mainConfigManager.get("general.pumpsNeedRealPower").getBoolean();
			pumpDimensionList = new PumpDimensionList(BuildCraftCore.mainConfigManager.get("general.pumpDimensionControl").getString());

			if (BuildCraftCore.mainConfiguration.hasChanged()) {
				BuildCraftCore.mainConfiguration.save();
			}
		}
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if ("BuildCraft|Core".equals(event.modID)) {
			reloadConfig(event.isWorldRunning ? ConfigManager.RestartRequirement.NONE : ConfigManager.RestartRequirement.WORLD);
		}
	}

	@Mod.EventHandler
	public void processIMCRequests(FMLInterModComms.IMCEvent event) {
		InterModComms.processIMC(event);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void loadTextures(TextureStitchEvent.Pre evt) {
		if (evt.map.getTextureType() == 0) {
			TextureMap terrainTextures = evt.map;
			FactoryProxyClient.pumpTexture = terrainTextures.registerIcon("buildcraftfactory:pumpBlock/tube");
		}
	}

	@Mod.EventHandler
	public void whiteListAppliedEnergetics(FMLInitializationEvent event) {
		//FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
		//		TileMiningWell.class.getCanonicalName());
		FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
				TileAutoWorkbench.class.getCanonicalName());
		//FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
		//		TilePump.class.getCanonicalName());
		FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
				TileFloodGate.class.getCanonicalName());
		FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
				TileTank.class.getCanonicalName());
		FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
				TileRefinery.class.getCanonicalName());
		FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
				TileHopper.class.getCanonicalName());
	}

	@Mod.EventHandler
	public void remap(FMLMissingMappingsEvent event) {
		for (FMLMissingMappingsEvent.MissingMapping mapping : event.get()) {
			if (mapping.name.equals("BuildCraft|Factory:machineBlock")) {
				if (Loader.isModLoaded("BuildCraft|Builders")) {
					if (mapping.type == GameRegistry.Type.BLOCK) {
						mapping.remap(Block.getBlockFromName("BuildCraft|Builders:machineBlock"));
					} else if (mapping.type == GameRegistry.Type.ITEM) {
						mapping.remap(Item.getItemFromBlock(Block.getBlockFromName("BuildCraft|Builders:machineBlock")));
					}
				} else {
					mapping.warn();
				}
			} else if (mapping.name.equals("BuildCraft|Factory:frameBlock")) {
				if (Loader.isModLoaded("BuildCraft|Builders")) {
					if (mapping.type == GameRegistry.Type.BLOCK) {
						mapping.remap(Block.getBlockFromName("BuildCraft|Builders:frameBlock"));
					} else if (mapping.type == GameRegistry.Type.ITEM) {
						mapping.remap(Item.getItemFromBlock(Block.getBlockFromName("BuildCraft|Builders:frameBlock")));
					}
				} else {
					mapping.ignore();
				}
			}
		}
	}
}
