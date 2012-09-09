/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package buildcraft;

import java.lang.reflect.Method;

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
import buildcraft.factory.GuiHandler;
import buildcraft.factory.TileAssemblyTable;
import buildcraft.factory.TileAutoWorkbench;
import buildcraft.factory.TileHopper;
import buildcraft.factory.TileMiningWell;
import buildcraft.factory.TilePump;
import buildcraft.factory.TileQuarry;
import buildcraft.factory.TileRefinery;
import buildcraft.factory.TileTank;
import buildcraft.factory.gui.GuiAutoCrafting;
import buildcraft.factory.network.PacketHandlerFactory;
import buildcraft.silicon.TileLaser;
import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

@Mod(name="BuildCraft Factory", version=Version.VERSION, useMetadata = false, modid = "BuildCraft|Factory", dependencies = DefaultProps.DEPENDENCY_CORE)
@NetworkMod(channels = {DefaultProps.NET_CHANNEL_NAME}, packetHandler = PacketHandlerFactory.class, clientSideRequired = true, serverSideRequired = true)
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

	public static int drillTexture;

	public static boolean allowMining = true;

	@Instance("BuildCraft|Factory")
	public static BuildCraftFactory instance;

	@PostInit
	public void postInit(FMLPostInitializationEvent evt)
	{
		try {
			Class<?> neiRenderer = Class.forName("codechicken.nei.DefaultOverlayRenderer");
			Method method = neiRenderer.getMethod("registerGuiOverlay", Class.class, String.class, int.class, int.class);
			method.invoke(null, GuiAutoCrafting.class, "crafting", 5, 11);
			BuildCraftCore.bcLog.fine("NEI detected, adding NEI overlay");
		} catch (Exception e) {
			BuildCraftCore.bcLog.fine("NEI not detected.");
		}
	}
	@Init
	public void load(FMLInitializationEvent evt) {
		NetworkRegistry.instance().registerGuiHandler(instance, new GuiHandler());

//		EntityRegistry.registerModEntity(EntityMechanicalArm.class, "bcMechanicalArm", EntityIds.MECHANICAL_ARM, instance, 50, 1, true);

		CoreProxy.proxy.registerTileEntity(TileQuarry.class, "Machine");
		CoreProxy.proxy.registerTileEntity(TileMiningWell.class, "MiningWell");
		CoreProxy.proxy.registerTileEntity(TileAutoWorkbench.class, "AutoWorkbench");
		CoreProxy.proxy.registerTileEntity(TilePump.class, "net.minecraft.src.buildcraft.factory.TilePump");
		CoreProxy.proxy.registerTileEntity(TileTank.class, "net.minecraft.src.buildcraft.factory.TileTank");
		CoreProxy.proxy.registerTileEntity(TileRefinery.class, "net.minecraft.src.buildcraft.factory.Refinery");
		CoreProxy.proxy.registerTileEntity(TileLaser.class, "net.minecraft.src.buildcraft.factory.TileLaser");
		CoreProxy.proxy.registerTileEntity(TileAssemblyTable.class, "net.minecraft.src.buildcraft.factory.TileAssemblyTable");

		if (!hopperDisabled) {
			CoreProxy.proxy.registerTileEntity(TileHopper.class, "net.minecraft.src.buildcraft.factory.TileHopper");
		}

		FactoryProxy.proxy.initializeTileEntities();
		FactoryProxy.proxy.initializeEntityRenders();
		drillTexture = 2 * 16 + 1;

		new BptBlockAutoWorkbench(autoWorkbenchBlock.blockID);
		new BptBlockFrame(frameBlock.blockID);
		new BptBlockRefinery(refineryBlock.blockID);
		new BptBlockTank(tankBlock.blockID);

		if (BuildCraftCore.loadDefaultRecipes)
			loadRecipes();
	}

	@PreInit
	public void initialize(FMLPreInitializationEvent evt) {
		allowMining = Boolean.parseBoolean(BuildCraftCore.mainConfiguration.getOrCreateBooleanProperty("mining.enabled", Configuration.CATEGORY_GENERAL, true).value);

		Property minigWellId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("miningWell.id", DefaultProps.MINING_WELL_ID);
		Property plainPipeId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("drill.id", DefaultProps.DRILL_ID);
		Property autoWorkbenchId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("autoWorkbench.id", DefaultProps.AUTO_WORKBENCH_ID);
		Property frameId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("frame.id", DefaultProps.FRAME_ID);
		Property quarryId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("quarry.id", DefaultProps.QUARRY_ID);
		Property pumpId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("pump.id", DefaultProps.PUMP_ID);
		Property tankId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("tank.id", DefaultProps.TANK_ID);
		Property refineryId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("refinery.id", DefaultProps.REFINERY_ID);
		Property hopperId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("hopper.id", DefaultProps.HOPPER_ID);
		Property hopperDisable = BuildCraftCore.mainConfiguration.getOrCreateBooleanProperty("hopper.disabled", "Block Savers", false);

		BuildCraftCore.mainConfiguration.save();

		miningWellBlock = new BlockMiningWell(Integer.parseInt(minigWellId.value));
		CoreProxy.proxy.registerBlock(miningWellBlock.setBlockName("miningWellBlock"));
		CoreProxy.proxy.addName(miningWellBlock, "Mining Well");

		plainPipeBlock = new BlockPlainPipe(Integer.parseInt(plainPipeId.value));
		CoreProxy.proxy.registerBlock(plainPipeBlock.setBlockName("plainPipeBlock"));
		CoreProxy.proxy.addName(plainPipeBlock, "Mining Pipe");

		autoWorkbenchBlock = new BlockAutoWorkbench(Integer.parseInt(autoWorkbenchId.value));
		CoreProxy.proxy.registerBlock(autoWorkbenchBlock.setBlockName("autoWorkbenchBlock"));
		CoreProxy.proxy.addName(autoWorkbenchBlock, "Automatic Crafting Table");

		frameBlock = new BlockFrame(Integer.parseInt(frameId.value));
		CoreProxy.proxy.registerBlock(frameBlock.setBlockName("frameBlock"));
		CoreProxy.proxy.addName(frameBlock, "Frame");

		quarryBlock = new BlockQuarry(Integer.parseInt(quarryId.value));
		CoreProxy.proxy.registerBlock(quarryBlock.setBlockName("machineBlock"));
		CoreProxy.proxy.addName(quarryBlock, "Quarry");

		tankBlock = new BlockTank(Integer.parseInt(tankId.value));
		CoreProxy.proxy.registerBlock(tankBlock.setBlockName("tankBlock"));
		CoreProxy.proxy.addName(tankBlock, "Tank");

		pumpBlock = new BlockPump(Integer.parseInt(pumpId.value));
		CoreProxy.proxy.registerBlock(pumpBlock.setBlockName("pumpBlock"));
		CoreProxy.proxy.addName(pumpBlock, "Pump");

		refineryBlock = new BlockRefinery(Integer.parseInt(refineryId.value));
		CoreProxy.proxy.registerBlock(refineryBlock.setBlockName("refineryBlock"));
		CoreProxy.proxy.addName(refineryBlock, "Refinery");

		hopperDisabled = Boolean.parseBoolean(hopperDisable.value);
		if (!hopperDisabled) {
			hopperBlock = new BlockHopper(Integer.parseInt(hopperId.value));
			CoreProxy.proxy.registerBlock(hopperBlock.setBlockName("blockHopper"));
			CoreProxy.proxy.addName(hopperBlock, "Hopper");
		}

		BuildCraftCore.mainConfiguration.save();
	}

	public static void loadRecipes() {

		if (allowMining) {
			CoreProxy.proxy.addCraftingRecipe(new ItemStack(miningWellBlock, 1),
					new Object[] { "ipi", "igi", "iPi", Character.valueOf('p'), Item.redstone, Character.valueOf('i'),
							Item.ingotIron, Character.valueOf('g'), BuildCraftCore.ironGearItem, Character.valueOf('P'),
							Item.pickaxeSteel });

			CoreProxy.proxy.addCraftingRecipe(new ItemStack(quarryBlock), new Object[] { "ipi", "gig", "dDd", Character.valueOf('i'),
					BuildCraftCore.ironGearItem, Character.valueOf('p'), Item.redstone, Character.valueOf('g'),
					BuildCraftCore.goldGearItem, Character.valueOf('d'), BuildCraftCore.diamondGearItem, Character.valueOf('D'),
					Item.pickaxeDiamond, });
		}

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(autoWorkbenchBlock), new Object[] { " g ", "gwg", " g ", Character.valueOf('w'),
				Block.workbench, Character.valueOf('g'), BuildCraftCore.woodenGearItem });

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(pumpBlock), new Object[] { "T ", "W ", Character.valueOf('T'), tankBlock,
				Character.valueOf('W'), miningWellBlock, });

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(tankBlock), new Object[] { "ggg", "g g", "ggg", Character.valueOf('g'),
				Block.glass, });

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(refineryBlock), new Object[] { "   ", "RTR", "TGT", Character.valueOf('T'),
				tankBlock, Character.valueOf('G'), BuildCraftCore.diamondGearItem, Character.valueOf('R'),
				Block.torchRedstoneActive, });
		if (!hopperDisabled) {
			CoreProxy.proxy.addCraftingRecipe(new ItemStack(hopperBlock), new Object[] { "ICI", "IGI", " I ", Character.valueOf('I'),
					Item.ingotIron, Character.valueOf('C'), Block.chest, Character.valueOf('G'), BuildCraftCore.stoneGearItem });
		}

	}
}
