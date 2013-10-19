/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile or
 * run the code. It does *NOT* grant the right to redistribute this software or
 * its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */
package buildcraft;

import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.Version;
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
import buildcraft.factory.BptBlockAutoWorkbench;
import buildcraft.factory.BptBlockFrame;
import buildcraft.factory.BptBlockRefinery;
import buildcraft.factory.BptBlockTank;
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
import com.google.common.collect.Lists;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import net.minecraftforge.event.ForgeSubscribe;

@Mod(name = "BuildCraft Factory", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Factory", dependencies = DefaultProps.DEPENDENCY_CORE)
@NetworkMod(channels = {DefaultProps.NET_CHANNEL_NAME}, packetHandler = PacketHandlerFactory.class, clientSideRequired = true, serverSideRequired = true)
public class BuildCraftFactory {

	public static final int MINING_MJ_COST_PER_BLOCK = 64;
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
	public static boolean allowMining = true;
	public static boolean quarryOneTimeUse = false;
	public static float miningMultiplier = 1;
	public static int miningDepth = 256;
	public static PumpDimensionList pumpDimensionList;
	@Instance("BuildCraft|Factory")
	public static BuildCraftFactory instance;

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt) {
		FactoryProxy.proxy.initializeNEIIntegration();
		ForgeChunkManager.setForcedChunkLoadingCallback(instance, new QuarryChunkloadCallback());
	}

	public class QuarryChunkloadCallback implements ForgeChunkManager.OrderedLoadingCallback {

		@Override
		public void ticketsLoaded(List<Ticket> tickets, World world) {
			for (Ticket ticket : tickets) {
				int quarryX = ticket.getModData().getInteger("quarryX");
				int quarryY = ticket.getModData().getInteger("quarryY");
				int quarryZ = ticket.getModData().getInteger("quarryZ");
				TileQuarry tq = (TileQuarry) world.getBlockTileEntity(quarryX, quarryY, quarryZ);
				tq.forceChunkLoading(ticket);

			}
		}

		@Override
		public List<Ticket> ticketsLoaded(List<Ticket> tickets, World world, int maxTicketCount) {
			List<Ticket> validTickets = Lists.newArrayList();
			for (Ticket ticket : tickets) {
				int quarryX = ticket.getModData().getInteger("quarryX");
				int quarryY = ticket.getModData().getInteger("quarryY");
				int quarryZ = ticket.getModData().getInteger("quarryZ");

				int blId = world.getBlockId(quarryX, quarryY, quarryZ);
				if (blId == quarryBlock.blockID) {
					validTickets.add(ticket);
				}
			}
			return validTickets;
		}
	}

	@EventHandler
	public void load(FMLInitializationEvent evt) {
		NetworkRegistry.instance().registerGuiHandler(instance, new GuiHandler());

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

		new BptBlockAutoWorkbench(autoWorkbenchBlock.blockID);
		new BptBlockFrame(frameBlock.blockID);
		new BptBlockRefinery(refineryBlock.blockID);
		new BptBlockTank(tankBlock.blockID);

		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}
	}

	@EventHandler
	public void initialize(FMLPreInitializationEvent evt) {
		ConfigUtils genCat = new ConfigUtils(BuildCraftCore.mainConfiguration, Configuration.CATEGORY_GENERAL);

		allowMining = genCat.get("mining.enabled", true, "disables the recipes for automated mining machines");
		quarryOneTimeUse = genCat.get("quarry.one.time.use", false, "Quarry cannot be picked back up after placement");
		miningMultiplier = genCat.get("mining.cost.multipler", 1F, 1F, 10F, "cost multiplier for mining operations, range (1.0 - 10.0)\nhigh values may render engines incapable of powering machines directly");
		miningDepth = genCat.get("mining.depth", 2, 256, 256, "how far below the machine can mining machines dig, range (2 - 256), default 256");

		Property pumpList = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "pumping.controlList", DefaultProps.PUMP_DIMENSION_LIST);
		pumpList.comment = "Allows admins to whitelist or blacklist pumping of specific fluids in specific dimensions.\n"
				+ "Eg. \"-/-1/Lava\" will disable lava in the nether. \"-/*/Lava\" will disable lava in any dimension. \"+/0/*\" will enable any fluid in the overworld.\n"
				+ "Entries are comma seperated, banned fluids have precedence over allowed ones."
				+ "Default is \"+/*/*,+/-1/Lava\" - the second redundant entry (\"+/-1/lava\") is there to show the format.";
		pumpDimensionList = new PumpDimensionList(pumpList.getString());

		int miningWellId = BuildCraftCore.mainConfiguration.getBlock("miningWell.id", DefaultProps.MINING_WELL_ID).getInt(DefaultProps.MINING_WELL_ID);
		int plainPipeId = BuildCraftCore.mainConfiguration.getBlock("drill.id", DefaultProps.DRILL_ID).getInt(DefaultProps.DRILL_ID);
		int autoWorkbenchId = BuildCraftCore.mainConfiguration.getBlock("autoWorkbench.id", DefaultProps.AUTO_WORKBENCH_ID).getInt(DefaultProps.AUTO_WORKBENCH_ID);
		int frameId = BuildCraftCore.mainConfiguration.getBlock("frame.id", DefaultProps.FRAME_ID).getInt(DefaultProps.FRAME_ID);
		int quarryId = BuildCraftCore.mainConfiguration.getBlock("quarry.id", DefaultProps.QUARRY_ID).getInt(DefaultProps.QUARRY_ID);
		int pumpId = BuildCraftCore.mainConfiguration.getBlock("pump.id", DefaultProps.PUMP_ID).getInt(DefaultProps.PUMP_ID);
		int floodGateId = BuildCraftCore.mainConfiguration.getBlock("floodGate.id", DefaultProps.FLOOD_GATE_ID).getInt(DefaultProps.FLOOD_GATE_ID);
		int tankId = BuildCraftCore.mainConfiguration.getBlock("tank.id", DefaultProps.TANK_ID).getInt(DefaultProps.TANK_ID);
		int refineryId = BuildCraftCore.mainConfiguration.getBlock("refinery.id", DefaultProps.REFINERY_ID).getInt(DefaultProps.REFINERY_ID);
		int hopperId = BuildCraftCore.mainConfiguration.getBlock("hopper.id", DefaultProps.HOPPER_ID).getInt(DefaultProps.HOPPER_ID);

		if (BuildCraftCore.mainConfiguration.hasChanged()) {
			BuildCraftCore.mainConfiguration.save();
		}

		if (miningWellId > 0) {
			miningWellBlock = new BlockMiningWell(miningWellId);
			CoreProxy.proxy.registerBlock(miningWellBlock.setUnlocalizedName("miningWellBlock"));
			CoreProxy.proxy.addName(miningWellBlock, "Mining Well");
		}
		if (plainPipeId > 0) {
			plainPipeBlock = new BlockPlainPipe(plainPipeId);
			CoreProxy.proxy.registerBlock(plainPipeBlock.setUnlocalizedName("plainPipeBlock"));
			CoreProxy.proxy.addName(plainPipeBlock, "Mining Pipe");
		}
		if (autoWorkbenchId > 0) {
			autoWorkbenchBlock = new BlockAutoWorkbench(autoWorkbenchId);
			CoreProxy.proxy.registerBlock(autoWorkbenchBlock.setUnlocalizedName("autoWorkbenchBlock"));
			CoreProxy.proxy.addName(autoWorkbenchBlock, "Automatic Crafting Table");
		}
		if (frameId > 0) {
			frameBlock = new BlockFrame(frameId);
			CoreProxy.proxy.registerBlock(frameBlock.setUnlocalizedName("frameBlock"));
			CoreProxy.proxy.addName(frameBlock, "Frame");
		}
		if (quarryId > 0) {
			quarryBlock = new BlockQuarry(quarryId);
			CoreProxy.proxy.registerBlock(quarryBlock.setUnlocalizedName("machineBlock"));
			CoreProxy.proxy.addName(quarryBlock, "Quarry");
		}
		if (tankId > 0) {
			tankBlock = new BlockTank(tankId);
			CoreProxy.proxy.registerBlock(tankBlock.setUnlocalizedName("tankBlock"));
			CoreProxy.proxy.addName(tankBlock, "Tank");
		}
		if (pumpId > 0) {
			pumpBlock = new BlockPump(pumpId);
			CoreProxy.proxy.registerBlock(pumpBlock.setUnlocalizedName("pumpBlock"));
			CoreProxy.proxy.addName(pumpBlock, "Pump");
		}
		if (floodGateId > 0) {
			floodGateBlock = new BlockFloodGate(floodGateId);
			CoreProxy.proxy.registerBlock(floodGateBlock.setUnlocalizedName("floodGateBlock"));
			CoreProxy.proxy.addName(floodGateBlock, "Flood Gate");
		}
		if (refineryId > 0) {
			refineryBlock = new BlockRefinery(refineryId);
			CoreProxy.proxy.registerBlock(refineryBlock.setUnlocalizedName("refineryBlock"));
			CoreProxy.proxy.addName(refineryBlock, "Refinery");
		}
		if (hopperId > 0) {
			hopperBlock = new BlockHopper(hopperId);
			CoreProxy.proxy.registerBlock(hopperBlock.setUnlocalizedName("blockHopper"));
			CoreProxy.proxy.addName(hopperBlock, "Hopper");
		}

		FactoryProxy.proxy.initializeEntityRenders();
		if (BuildCraftCore.mainConfiguration.hasChanged()) {
			BuildCraftCore.mainConfiguration.save();
		}

		MinecraftForge.EVENT_BUS.register(this);
	}

	public static void loadRecipes() {

		if (allowMining) {
			if (miningWellBlock != null)
				CoreProxy.proxy.addCraftingRecipe(new ItemStack(miningWellBlock, 1),
						"ipi",
						"igi",
						"iPi",
						'p', Item.redstone,
						'i', Item.ingotIron,
						'g', BuildCraftCore.ironGearItem,
						'P', Item.pickaxeIron);

			if (quarryBlock != null)
				CoreProxy.proxy.addCraftingRecipe(
						new ItemStack(quarryBlock),
						"ipi",
						"gig",
						"dDd",
						'i', BuildCraftCore.ironGearItem,
						'p', Item.redstone,
						'g', BuildCraftCore.goldGearItem,
						'd', BuildCraftCore.diamondGearItem,
						'D', Item.pickaxeDiamond);

			if (pumpBlock != null && miningWellBlock != null)
				CoreProxy.proxy.addCraftingRecipe(new ItemStack(pumpBlock),
						"T",
						"W",
						'T', tankBlock != null ? tankBlock : Block.glass,
						'W', miningWellBlock);
		}

		if (!allowMining || miningWellBlock == null) {
			if (pumpBlock != null)
				CoreProxy.proxy.addCraftingRecipe(new ItemStack(pumpBlock),
						"iri",
						"iTi",
						"gpg",
						'r', Item.redstone,
						'i', Item.ingotIron,
						'T', tankBlock != null ? tankBlock : Block.glass,
						'g', BuildCraftCore.ironGearItem,
						'p', BuildCraftTransport.pipeFluidsGold);
		}

		if (autoWorkbenchBlock != null)
			CoreProxy.proxy.addCraftingRecipe(new ItemStack(autoWorkbenchBlock),
					" g ",
					"gwg",
					" g ",
					'w', Block.workbench,
					'g', BuildCraftCore.woodenGearItem);


		if (tankBlock != null)
			CoreProxy.proxy.addCraftingRecipe(new ItemStack(tankBlock),
					"ggg",
					"g g",
					"ggg",
					'g', Block.glass);

		if (refineryBlock != null)
			CoreProxy.proxy.addCraftingRecipe(new ItemStack(refineryBlock),
					"RTR",
					"TGT",
					'T', tankBlock != null ? tankBlock : Block.glass,
					'G', BuildCraftCore.diamondGearItem,
					'R', Block.torchRedstoneActive);

		if (hopperBlock != null)
			CoreProxy.proxy.addCraftingRecipe(new ItemStack(hopperBlock),
					"ICI",
					"IGI",
					" I ",
					'I', Item.ingotIron,
					'C', Block.chest,
					'G', BuildCraftCore.stoneGearItem);

		if (floodGateBlock != null)
			CoreProxy.proxy.addCraftingRecipe(new ItemStack(floodGateBlock),
					"IGI",
					"FTF",
					"IFI",
					'I', Item.ingotIron,
					'T', tankBlock != null ? tankBlock : Block.glass,
					'G', BuildCraftCore.ironGearItem,
					'F', new ItemStack(Block.fenceIron));
	}
	
	@EventHandler
    public void processIMCRequests(FMLInterModComms.IMCEvent event) {
        InterModComms.processIMC(event);
    }

	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void loadTextures(TextureStitchEvent.Pre evt) {
		if (evt.map.textureType == 0) {
			TextureMap terrainTextures = evt.map;
			FactoryProxyClient.pumpTexture = terrainTextures.registerIcon("buildcraft:pump_tube");
			FactoryProxyClient.drillTexture = terrainTextures.registerIcon("buildcraft:blockDrillTexture");
			FactoryProxyClient.drillHeadTexture = terrainTextures.registerIcon("buildcraft:blockDrillHeadTexture");
		}
	}
}
