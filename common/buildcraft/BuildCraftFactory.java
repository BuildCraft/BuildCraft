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
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.builders.schematics.SchematicIgnoreState;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.Version;
import buildcraft.core.network.BuildCraftChannelHandler;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.ConfigUtils;
import buildcraft.core.utils.Utils;
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
import buildcraft.factory.schematics.SchematicPump;
import buildcraft.factory.schematics.SchematicRefinery;
import buildcraft.factory.schematics.SchematicTank;

@Mod(name = "BuildCraft Factory", version = Version.VERSION, useMetadata = false, modid = "BuildCraftFactory", dependencies = DefaultProps.DEPENDENCY_CORE)
public class BuildCraftFactory extends BuildCraftMod {

	@Mod.Instance("BuildCraftFactory")
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
		FactoryProxy.proxy.initializeNEIIntegration();
		if (quarryLoadsChunks) {
			ForgeChunkManager.setForcedChunkLoadingCallback(instance, new QuarryChunkloadCallback());
		}
	}

	public class QuarryChunkloadCallback implements ForgeChunkManager.OrderedLoadingCallback {

		@Override
		public void ticketsLoaded(List<Ticket> tickets, World world) {
			for (Ticket ticket : tickets) {
				BlockPos quarryPos = Utils.readBlockPos(ticket.getModData());

				if (world.isBlockLoaded(quarryPos)) {
					Block block = world.getBlockState(quarryPos).getBlock();
					if (block == quarryBlock) {
						TileQuarry tq = (TileQuarry) world.getTileEntity(quarryPos);
						tq.forceChunkLoading(ticket);
					}
				}
			}
		}

		@Override
		public List<Ticket> ticketsLoaded(List<Ticket> tickets, World world, int maxTicketCount) {
			List<Ticket> validTickets = Lists.newArrayList();
			for (Ticket ticket : tickets) {
				BlockPos quarryPos = Utils.readBlockPos(ticket.getModData());

				if (world.isBlockLoaded(quarryPos)) {
					Block block = world.getBlockState(quarryPos).getBlock();
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
		FactoryProxy.proxy.initializeEntityRenders();
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
		BuilderAPI.schematicRegistry.registerSchematicBlock(tankBlock, SchematicTank.class);
		BuilderAPI.schematicRegistry.registerSchematicBlock(frameBlock, SchematicIgnoreState.class);
		BuilderAPI.schematicRegistry.registerSchematicBlock(pumpBlock, SchematicPump.class);

		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}
	}

	@Mod.EventHandler
	public void initialize(FMLPreInitializationEvent evt) {
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

		miningWellBlock = new BlockMiningWell();
		CoreProxy.proxy.registerBlock(miningWellBlock.setUnlocalizedName("miningWellBlock"));

		plainPipeBlock = new BlockPlainPipe();
		CoreProxy.proxy.registerBlock(plainPipeBlock.setUnlocalizedName("plainPipeBlock"));

		autoWorkbenchBlock = new BlockAutoWorkbench();
		CoreProxy.proxy.registerBlock(autoWorkbenchBlock.setUnlocalizedName("autoWorkbenchBlock"));

		frameBlock = new BlockFrame();
		CoreProxy.proxy.registerBlock(frameBlock.setUnlocalizedName("frameBlock"));

		quarryBlock = new BlockQuarry();
		CoreProxy.proxy.registerBlock(quarryBlock.setUnlocalizedName("machineBlock"));

		tankBlock = new BlockTank();
		CoreProxy.proxy.registerBlock(tankBlock.setUnlocalizedName("tankBlock"));

		pumpBlock = new BlockPump();
		CoreProxy.proxy.registerBlock(pumpBlock.setUnlocalizedName("pumpBlock"));

		floodGateBlock = new BlockFloodGate();
		CoreProxy.proxy.registerBlock(floodGateBlock.setUnlocalizedName("floodGateBlock"));

		refineryBlock = new BlockRefinery();
		CoreProxy.proxy.registerBlock(refineryBlock.setUnlocalizedName("refineryBlock"));

		hopperBlock = new BlockHopper();
		CoreProxy.proxy.registerBlock(hopperBlock.setUnlocalizedName("blockHopper"));
		
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

	@SubscribeEvent
	public void onModelBakeEvent(ModelBakeEvent event) {
		FactoryProxy.proxy.initializeModels(event);
	}


	@Mod.EventHandler
    public void processIMCRequests(FMLInterModComms.IMCEvent event) {
        InterModComms.processIMC(event);
    }


	@Mod.EventHandler
	public void whiteListAppliedEnergetics(FMLInitializationEvent event) {
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
