/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.blueprints.SchematicTile;
import buildcraft.builders.schematics.SchematicIgnoreMeta;
import buildcraft.compat.CompatHooks;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.Version;
import buildcraft.core.network.BuildCraftChannelHandler;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.ConfigUtils;
import buildcraft.factory.BlockAutoWorkbench;
import buildcraft.factory.BlockFloodGate;
import buildcraft.factory.BlockFrame;
import buildcraft.factory.BlockHopper;
import buildcraft.factory.BlockMiningWell;
import buildcraft.factory.BlockPlainPipe;
import buildcraft.factory.BlockPump;
import buildcraft.factory.BlockQuarry;
import buildcraft.factory.BlockRefinery;
import buildcraft.factory.BlockTank;
import buildcraft.factory.FactoryProxy;
import buildcraft.factory.FactoryProxyClient;
import buildcraft.factory.GuiHandler;
import buildcraft.factory.PumpDimensionList;
import buildcraft.factory.TileAutoWorkbench;
import buildcraft.factory.TileFloodGate;
import buildcraft.factory.TileHopper;
import buildcraft.factory.TileMiningWell;
import buildcraft.factory.TilePump;
import buildcraft.factory.TileQuarry;
import buildcraft.factory.TileRefinery;
import buildcraft.factory.TileTank;
import buildcraft.factory.network.PacketHandlerFactory;
import buildcraft.factory.schematics.SchematicAutoWorkbench;
import buildcraft.factory.schematics.SchematicPump;
import buildcraft.factory.schematics.SchematicRefinery;
import buildcraft.factory.schematics.SchematicTileIgnoreState;

@Mod(name = "BuildCraft Factory", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Factory", dependencies = DefaultProps.DEPENDENCY_CORE)
public class BuildCraftFactory extends BuildCraftMod {

	@Mod.Instance("BuildCraft|Factory")
	public static BuildCraftFactory instance;

	public static BlockQuarry quarryBlock;
	public static BlockMiningWell miningWellBlock;
	public static BlockAutoWorkbench autoWorkbenchBlock;
	public static BlockFrame frameBlock;
	public static BlockPlainPipe plainPipeBlock;
	public static BlockPump pumpBlock;
	public static BlockFloodGate floodGateBlock;
	public static BlockTank tankBlock;
	public static BlockRefinery refineryBlock;
	public static BlockHopper hopperBlock;

	public static boolean quarryLoadsChunks = true;
	public static boolean allowMining = true;
	public static boolean quarryOneTimeUse = false;
	public static float miningMultiplier = 1;
	public static int miningDepth = 256;
	public static PumpDimensionList pumpDimensionList;

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent evt) {
		if (disabled) {
			return;
		}
		FactoryProxy.proxy.initializeNEIIntegration();
		if (quarryLoadsChunks) {
			ForgeChunkManager.setForcedChunkLoadingCallback(instance, new QuarryChunkloadCallback());
		}
	}

	public class QuarryChunkloadCallback implements ForgeChunkManager.OrderedLoadingCallback {

		@Override
		public void ticketsLoaded(List<Ticket> tickets, World world) {
			for (Ticket ticket : tickets) {
				int quarryX = ticket.getModData().getInteger("quarryX");
				int quarryY = ticket.getModData().getInteger("quarryY");
				int quarryZ = ticket.getModData().getInteger("quarryZ");

				if (world.blockExists(quarryX, quarryY, quarryZ)) {
					Block block = world.getBlock(quarryX, quarryY, quarryZ);
					if (block == quarryBlock) {
						TileQuarry tq = (TileQuarry) world.getTileEntity(quarryX, quarryY, quarryZ);
						tq.forceChunkLoading(ticket);
					}
				}
			}
		}

		@Override
		public List<Ticket> ticketsLoaded(List<Ticket> tickets, World world, int maxTicketCount) {
			List<Ticket> validTickets = Lists.newArrayList();
			for (Ticket ticket : tickets) {
				int quarryX = ticket.getModData().getInteger("quarryX");
				int quarryY = ticket.getModData().getInteger("quarryY");
				int quarryZ = ticket.getModData().getInteger("quarryZ");

				if (world.blockExists(quarryX, quarryY, quarryZ)) {
					Block block = world.getBlock(quarryX, quarryY, quarryZ);
					if (block == quarryBlock) {
						validTickets.add(ticket);
					}
				}
			}
			return validTickets;
		}
	}

	@Mod.EventHandler
	public void load(FMLInitializationEvent evt) {
		if (disabled) {
			return;
		}
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());

		// EntityRegistry.registerModEntity(EntityMechanicalArm.class, "bcMechanicalArm", EntityIds.MECHANICAL_ARM, instance, 50, 1, true);

		CoreProxy.proxy.registerTileEntity(TileQuarry.class, "Machine");
		CoreProxy.proxy.registerTileEntity(TileMiningWell.class, "MiningWell");
		CoreProxy.proxy.registerTileEntity(TileAutoWorkbench.class, "AutoWorkbench");
		CoreProxy.proxy.registerTileEntity(TilePump.class, "net.minecraft.src.buildcraft.factory.TilePump");
		CoreProxy.proxy.registerTileEntity(TileFloodGate.class, "net.minecraft.src.buildcraft.factory.TileFloodGate");
		CoreProxy.proxy.registerTileEntity(TileTank.class, "net.minecraft.src.buildcraft.factory.TileTank");
		CoreProxy.proxy.registerTileEntity(TileRefinery.class, "net.minecraft.src.buildcraft.factory.Refinery");
		CoreProxy.proxy.registerTileEntity(TileHopper.class, "net.minecraft.src.buildcraft.factory.TileHopper");

		FactoryProxy.proxy.initializeTileEntities();

		BuilderAPI.schematicRegistry.registerSchematicBlock(refineryBlock, SchematicRefinery.class);
		BuilderAPI.schematicRegistry.registerSchematicBlock(tankBlock, SchematicTileIgnoreState.class);
		BuilderAPI.schematicRegistry.registerSchematicBlock(frameBlock, SchematicIgnoreMeta.class);
		BuilderAPI.schematicRegistry.registerSchematicBlock(pumpBlock, SchematicPump.class);
		BuilderAPI.schematicRegistry.registerSchematicBlock(miningWellBlock, SchematicTileIgnoreState.class);
		BuilderAPI.schematicRegistry.registerSchematicBlock(floodGateBlock, SchematicTileIgnoreState.class);
		BuilderAPI.schematicRegistry.registerSchematicBlock(autoWorkbenchBlock, SchematicAutoWorkbench.class);
		BuilderAPI.schematicRegistry.registerSchematicBlock(hopperBlock, SchematicTile.class);

		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}
	}

	@Mod.EventHandler
	public void initialize(FMLPreInitializationEvent evt) {
		if (isDisabled(evt.getModMetadata().modId)) {
			disabled = true;
			return;
		}
		channels = NetworkRegistry.INSTANCE.newChannel
				(DefaultProps.NET_CHANNEL_NAME + "-FACTORY", new BuildCraftChannelHandler(), new PacketHandlerFactory());

		ConfigUtils genCat = new ConfigUtils(BuildCraftCore.mainConfiguration, Configuration.CATEGORY_GENERAL);

		allowMining = genCat.get("mining.enabled", true, "disables the recipes for automated mining machines");
		quarryOneTimeUse = genCat.get("quarry.one.time.use", false, "Quarry cannot be picked back up after placement");
		miningMultiplier = genCat.get("mining.cost.multipler", 1F, 1F, 10F, "cost multiplier for mining operations, range (1.0 - 10.0)\nhigh values may render engines incapable of powering machines directly");
		miningDepth = genCat.get("mining.depth", 2, 256, 256, "how far below the machine can mining machines dig, range (2 - 256), default 256");
		quarryLoadsChunks = genCat.get("quarry.loads.chunks", true, "Quarry loads chunks required for mining");

		Property pumpList = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "pumping.controlList", DefaultProps.PUMP_DIMENSION_LIST);
		pumpList.comment = "Allows admins to whitelist or blacklist pumping of specific fluids in specific dimensions.\n"
				+ "Eg. \"-/-1/Lava\" will disable lava in the nether. \"-/*/Lava\" will disable lava in any dimension. \"+/0/*\" will enable any fluid in the overworld.\n"
				+ "Entries are comma seperated, banned fluids have precedence over allowed ones."
				+ "Default is \"+/*/*,+/-1/Lava\" - the second redundant entry (\"+/-1/lava\") is there to show the format.";
		pumpDimensionList = new PumpDimensionList(pumpList.getString());

		if (BuildCraftCore.mainConfiguration.hasChanged()) {
			BuildCraftCore.mainConfiguration.save();
		}


		miningWellBlock = (BlockMiningWell) CompatHooks.INSTANCE.getBlock(BlockMiningWell.class);
		CoreProxy.proxy.registerBlock(miningWellBlock.setBlockName("miningWellBlock"));

		plainPipeBlock = new BlockPlainPipe();
		CoreProxy.proxy.registerBlock(plainPipeBlock.setBlockName("plainPipeBlock"));

		autoWorkbenchBlock = (BlockAutoWorkbench) CompatHooks.INSTANCE.getBlock(BlockAutoWorkbench.class);
		CoreProxy.proxy.registerBlock(autoWorkbenchBlock.setBlockName("autoWorkbenchBlock"));

		frameBlock = new BlockFrame();
		CoreProxy.proxy.registerBlock(frameBlock.setBlockName("frameBlock"));

		quarryBlock = (BlockQuarry) CompatHooks.INSTANCE.getBlock(BlockQuarry.class);
		CoreProxy.proxy.registerBlock(quarryBlock.setBlockName("machineBlock"));

		tankBlock = (BlockTank) CompatHooks.INSTANCE.getBlock(BlockTank.class);
		CoreProxy.proxy.registerBlock(tankBlock.setBlockName("tankBlock"));

		pumpBlock = (BlockPump) CompatHooks.INSTANCE.getBlock(BlockPump.class);
		CoreProxy.proxy.registerBlock(pumpBlock.setBlockName("pumpBlock"));

		floodGateBlock = (BlockFloodGate) CompatHooks.INSTANCE.getBlock(BlockFloodGate.class);
		CoreProxy.proxy.registerBlock(floodGateBlock.setBlockName("floodGateBlock"));

		refineryBlock = (BlockRefinery) CompatHooks.INSTANCE.getBlock(BlockRefinery.class);
		CoreProxy.proxy.registerBlock(refineryBlock.setBlockName("refineryBlock"));

		hopperBlock = (BlockHopper) CompatHooks.INSTANCE.getBlock(BlockHopper.class);
		CoreProxy.proxy.registerBlock(hopperBlock.setBlockName("blockHopper"));


		FactoryProxy.proxy.initializeEntityRenders();
		if (BuildCraftCore.mainConfiguration.hasChanged()) {
			BuildCraftCore.mainConfiguration.save();
		}

		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public static void loadRecipes() {
		if (allowMining) {
			if (miningWellBlock != null) {
				CoreProxy.proxy.addCraftingRecipe(new ItemStack(miningWellBlock, 1),
						"ipi",
						"igi",
						"iPi",
						'p', "dustRedstone",
						'i', "ingotIron",
						'g', "gearIron",
						'P', Items.iron_pickaxe);
			}

			if (quarryBlock != null) {
				CoreProxy.proxy.addCraftingRecipe(
						new ItemStack(quarryBlock),
						"ipi",
						"gig",
						"dDd",
						'i', "gearIron",
						'p', "dustRedstone",
						'g', "gearGold",
						'd', "gearDiamond",
						'D', Items.diamond_pickaxe);
			}

			if (pumpBlock != null && miningWellBlock != null) {
				CoreProxy.proxy.addCraftingRecipe(new ItemStack(pumpBlock),
						"T",
						"W",
						'T', tankBlock != null ? tankBlock : "blockGlass",
						'W', miningWellBlock);
			}
		}

		if (!allowMining || miningWellBlock == null) {
			if (pumpBlock != null) {
				CoreProxy.proxy.addCraftingRecipe(new ItemStack(pumpBlock),
						"iri",
						"iTi",
						"gpg",
						'r', "dustRedstone",
						'i', "ingotIron",
						'T', tankBlock != null ? tankBlock : "blockGlass",
						'g', "gearIron",
						'p', BuildCraftTransport.pipeFluidsGold);
			}
		}

		if (autoWorkbenchBlock != null) {
			CoreProxy.proxy.addCraftingRecipe(new ItemStack(autoWorkbenchBlock),
					" g ",
					"gwg",
					" g ",
					'w', Blocks.crafting_table,
					'g', "gearWood");
		}


		if (tankBlock != null) {
			CoreProxy.proxy.addCraftingRecipe(new ItemStack(tankBlock),
					"ggg",
					"g g",
					"ggg",
					'g', "blockGlass");
		}

		if (refineryBlock != null) {
			CoreProxy.proxy.addCraftingRecipe(new ItemStack(refineryBlock),
					"RTR",
					"TGT",
					'T', tankBlock != null ? tankBlock : "blockGlass",
					'G', "gearDiamond",
					'R', Blocks.redstone_torch);
		}

		if (hopperBlock != null) {
			CoreProxy.proxy.addCraftingRecipe(new ItemStack(hopperBlock),
					"ICI",
					"IGI",
					" I ",
					'I', "ingotIron",
					'C', Blocks.chest,
					'G', "gearStone");

			CoreProxy.proxy.addShapelessRecipe(new ItemStack(hopperBlock), Blocks.hopper, "gearStone");
		}

		if (floodGateBlock != null) {
			CoreProxy.proxy.addCraftingRecipe(new ItemStack(floodGateBlock),
					"IGI",
					"FTF",
					"IFI",
					'I', "ingotIron",
					'T', tankBlock != null ? tankBlock : "blockGlass",
					'G', "gearIron",
					'F', new ItemStack(Blocks.iron_bars));
		}
	}

	@Mod.EventHandler
    public void processIMCRequests(FMLInterModComms.IMCEvent event) {
		if (disabled) {
			return;
		}
        InterModComms.processIMC(event);
    }

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void loadTextures(TextureStitchEvent.Pre evt) {
		if (disabled) {
			return;
		}
		if (evt.map.getTextureType() == 0) {
			TextureMap terrainTextures = evt.map;
			FactoryProxyClient.pumpTexture = terrainTextures.registerIcon("buildcraft:pump_tube");
			FactoryProxyClient.drillTexture = terrainTextures.registerIcon("buildcraft:blockDrillTexture");
			FactoryProxyClient.drillHeadTexture = terrainTextures.registerIcon("buildcraft:blockDrillHeadTexture");
		}
	}

	@Mod.EventHandler
	public void whiteListAppliedEnergetics(FMLInitializationEvent event) {
		if (disabled) {
			return;
		}
		//FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
		//		TileQuarry.class.getCanonicalName());
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
}
