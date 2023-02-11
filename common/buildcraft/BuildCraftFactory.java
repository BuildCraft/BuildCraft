/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package buildcraft;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
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
import buildcraft.core.DefaultProps;
import buildcraft.core.Version;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.factory.BlockAutoWorkbench;
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
import buildcraft.factory.TileHopper;
import buildcraft.factory.TileMiningWell;
import buildcraft.factory.TilePump;
import buildcraft.factory.TileQuarry;
import buildcraft.factory.TileRefinery;
import buildcraft.factory.TileTank;
import buildcraft.factory.network.PacketHandlerFactory;

import com.google.common.collect.Lists;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(name = "BuildCraft Factory", version = Version.VERSION_CONSTANT, useMetadata = false, modid = "BuildCraft|Factory", dependencies = DefaultProps.DEPENDENCY_CORE)
@NetworkMod(channels = { DefaultProps.NET_CHANNEL_NAME }, packetHandler = PacketHandlerFactory.class, clientSideRequired = true, serverSideRequired = true)
public class BuildCraftFactory {

	public static BlockQuarry quarryBlock;
	public static BlockMiningWell miningWellBlock;
	public static BlockAutoWorkbench autoWorkbenchBlock;
	public static BlockFrame frameBlock;
	public static BlockPlainPipe plainPipeBlock;
	public static BlockPump pumpBlock;
	public static BlockTank tankBlock;
	public static BlockRefinery refineryBlock;
	public static BlockHopper hopperBlock;
	public static boolean hopperDisabled;

	public static boolean allowMining = true;
	
	public static PumpDimensionList pumpDimensionList;

	@Instance("BuildCraft|Factory")
	public static BuildCraftFactory instance;

	@PostInit
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

	@Init
	public void load(FMLInitializationEvent evt) {
		NetworkRegistry.instance().registerGuiHandler(instance, new GuiHandler());

		// EntityRegistry.registerModEntity(EntityMechanicalArm.class, "bcMechanicalArm", EntityIds.MECHANICAL_ARM, instance, 50, 1, true);

		CoreProxy.proxy.registerTileEntity(TileQuarry.class, "Machine");
		CoreProxy.proxy.registerTileEntity(TileMiningWell.class, "MiningWell");
		CoreProxy.proxy.registerTileEntity(TileAutoWorkbench.class, "AutoWorkbench");
		CoreProxy.proxy.registerTileEntity(TilePump.class, "net.minecraft.src.buildcraft.factory.TilePump");
		CoreProxy.proxy.registerTileEntity(TileTank.class, "net.minecraft.src.buildcraft.factory.TileTank");
		CoreProxy.proxy.registerTileEntity(TileRefinery.class, "net.minecraft.src.buildcraft.factory.Refinery");

		if (!hopperDisabled) {
			CoreProxy.proxy.registerTileEntity(TileHopper.class, "net.minecraft.src.buildcraft.factory.TileHopper");
		}

		FactoryProxy.proxy.initializeTileEntities();

		new BptBlockAutoWorkbench(autoWorkbenchBlock.blockID);
		new BptBlockFrame(frameBlock.blockID);
		new BptBlockRefinery(refineryBlock.blockID);
		new BptBlockTank(tankBlock.blockID);

		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}
	}

	@PreInit
	public void initialize(FMLPreInitializationEvent evt) {
		allowMining = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "mining.enabled", true).getBoolean(true);
		
		pumpDimensionList = new PumpDimensionList(BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "pumping.controlList", DefaultProps.PUMP_DIMENSION_LIST).getString());

		Property miningWellId = BuildCraftCore.mainConfiguration.getBlock("miningWell.id", DefaultProps.MINING_WELL_ID);
		Property plainPipeId = BuildCraftCore.mainConfiguration.getBlock("drill.id", DefaultProps.DRILL_ID);
		Property autoWorkbenchId = BuildCraftCore.mainConfiguration.getBlock("autoWorkbench.id", DefaultProps.AUTO_WORKBENCH_ID);
		Property frameId = BuildCraftCore.mainConfiguration.getBlock("frame.id", DefaultProps.FRAME_ID);
		Property quarryId = BuildCraftCore.mainConfiguration.getBlock("quarry.id", DefaultProps.QUARRY_ID);
		Property pumpId = BuildCraftCore.mainConfiguration.getBlock("pump.id", DefaultProps.PUMP_ID);
		Property tankId = BuildCraftCore.mainConfiguration.getBlock("tank.id", DefaultProps.TANK_ID);
		Property refineryId = BuildCraftCore.mainConfiguration.getBlock("refinery.id", DefaultProps.REFINERY_ID);
		Property hopperId = BuildCraftCore.mainConfiguration.getBlock("hopper.id", DefaultProps.HOPPER_ID);
		Property hopperDisable = BuildCraftCore.mainConfiguration.get("Block Savers", "hopper.disabled", false);

		if (BuildCraftCore.mainConfiguration.hasChanged())
		{
		    BuildCraftCore.mainConfiguration.save();
		}

		miningWellBlock = new BlockMiningWell(miningWellId.getInt());
		CoreProxy.proxy.registerBlock(miningWellBlock.setUnlocalizedName("miningWellBlock"));
		CoreProxy.proxy.addName(miningWellBlock, "Mining Well");

		plainPipeBlock = new BlockPlainPipe(plainPipeId.getInt());
		CoreProxy.proxy.registerBlock(plainPipeBlock.setUnlocalizedName("plainPipeBlock"));
		CoreProxy.proxy.addName(plainPipeBlock, "Mining Pipe");

		autoWorkbenchBlock = new BlockAutoWorkbench(autoWorkbenchId.getInt());
		CoreProxy.proxy.registerBlock(autoWorkbenchBlock.setUnlocalizedName("autoWorkbenchBlock"));
		CoreProxy.proxy.addName(autoWorkbenchBlock, "Automatic Crafting Table");

		frameBlock = new BlockFrame(frameId.getInt());
		CoreProxy.proxy.registerBlock(frameBlock.setUnlocalizedName("frameBlock"));
		CoreProxy.proxy.addName(frameBlock, "Frame");

		quarryBlock = new BlockQuarry(quarryId.getInt());
		CoreProxy.proxy.registerBlock(quarryBlock.setUnlocalizedName("machineBlock"));
		CoreProxy.proxy.addName(quarryBlock, "Quarry");

		tankBlock = new BlockTank(tankId.getInt());
		CoreProxy.proxy.registerBlock(tankBlock.setUnlocalizedName("tankBlock"));
		CoreProxy.proxy.addName(tankBlock, "Tank");

		pumpBlock = new BlockPump(pumpId.getInt());
		CoreProxy.proxy.registerBlock(pumpBlock.setUnlocalizedName("pumpBlock"));
		CoreProxy.proxy.addName(pumpBlock, "Pump");

		refineryBlock = new BlockRefinery(refineryId.getInt());
		CoreProxy.proxy.registerBlock(refineryBlock.setUnlocalizedName("refineryBlock"));
		CoreProxy.proxy.addName(refineryBlock, "Refinery");

		hopperDisabled = hopperDisable.getBoolean(false);
		if (!hopperDisabled) {
			hopperBlock = new BlockHopper(hopperId.getInt());
			CoreProxy.proxy.registerBlock(hopperBlock.setUnlocalizedName("blockHopper"));
			CoreProxy.proxy.addName(hopperBlock, "Hopper");
		}

		FactoryProxy.proxy.initializeEntityRenders();
		if (BuildCraftCore.mainConfiguration.hasChanged())
		{
		    BuildCraftCore.mainConfiguration.save();
		}

		MinecraftForge.EVENT_BUS.register(this);
	}

	public static void loadRecipes() {

		if (allowMining) {
			CoreProxy.proxy.addCraftingRecipe(new ItemStack(miningWellBlock, 1), new Object[] { "ipi", "igi", "iPi", Character.valueOf('p'), Item.redstone,
					Character.valueOf('i'), Item.ingotIron, Character.valueOf('g'), BuildCraftCore.ironGearItem, Character.valueOf('P'), Item.pickaxeIron });

			CoreProxy.proxy.addCraftingRecipe(
					new ItemStack(quarryBlock),
					new Object[] { "ipi", "gig", "dDd", Character.valueOf('i'), BuildCraftCore.ironGearItem, Character.valueOf('p'), Item.redstone,
							Character.valueOf('g'), BuildCraftCore.goldGearItem, Character.valueOf('d'), BuildCraftCore.diamondGearItem,
							Character.valueOf('D'), Item.pickaxeDiamond, });

			CoreProxy.proxy.addCraftingRecipe(new ItemStack(pumpBlock), new Object[] { "T ", "W ", Character.valueOf('T'), tankBlock, Character.valueOf('W'),
					miningWellBlock, });
		}
		else {
			CoreProxy.proxy.addCraftingRecipe(new ItemStack(pumpBlock), new Object[] { "iri", "iTi", "gpg",
					Character.valueOf('r'), Item.redstone,
					Character.valueOf('i'), Item.ingotIron,
					Character.valueOf('T'), tankBlock,
					Character.valueOf('g'), BuildCraftCore.ironGearItem,
					Character.valueOf('p'), BuildCraftTransport.pipeLiquidsGold });
		}

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(autoWorkbenchBlock), new Object[] { " g ", "gwg", " g ", Character.valueOf('w'), Block.workbench,
				Character.valueOf('g'), BuildCraftCore.woodenGearItem });


		CoreProxy.proxy.addCraftingRecipe(new ItemStack(tankBlock), new Object[] { "ggg", "g g", "ggg", Character.valueOf('g'), Block.glass, });

		CoreProxy.proxy.addCraftingRecipe(
				new ItemStack(refineryBlock),
				new Object[] { "   ", "RTR", "TGT", Character.valueOf('T'), tankBlock, Character.valueOf('G'), BuildCraftCore.diamondGearItem,
						Character.valueOf('R'), Block.torchRedstoneActive, });
		if (!hopperDisabled) {
			CoreProxy.proxy.addCraftingRecipe(new ItemStack(hopperBlock),
					new Object[] { "ICI", "IGI", " I ", Character.valueOf('I'), Item.ingotIron, Character.valueOf('C'), Block.chest, Character.valueOf('G'),
							BuildCraftCore.stoneGearItem });
		}

	}


	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void loadTextures(TextureStitchEvent.Pre evt) {
	    if (evt.map == Minecraft.getMinecraft().renderEngine.textureMapBlocks) {
	        TextureMap terrainTextures = evt.map;
	        FactoryProxyClient.pumpTexture = terrainTextures.registerIcon("buildcraft:pump_tube");
	        FactoryProxyClient.drillTexture = terrainTextures.registerIcon("buildcraft:blockDrillTexture");
	        FactoryProxyClient.drillHeadTexture = terrainTextures.registerIcon("buildcraft:blockDrillHeadTexture");
	    }
	}
}
