/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package buildcraft;

import cpw.mods.fml.client.registry.ClientRegistry;
import buildcraft.mod_BuildCraftCore;
import buildcraft.core.DefaultProps;
import buildcraft.core.ProxyCore;
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
import buildcraft.factory.TileAssemblyTable;
import buildcraft.factory.TileAutoWorkbench;
import buildcraft.factory.TileHopper;
import buildcraft.factory.TileMiningWell;
import buildcraft.factory.TilePump;
import buildcraft.factory.TileQuarry;
import buildcraft.factory.TileRefinery;
import buildcraft.factory.TileTank;
import buildcraft.factory.render.RenderHopper;
import buildcraft.factory.render.RenderRefinery;
import buildcraft.factory.render.RenderTank;
import buildcraft.mod_BuildCraftCore.EntityRenderIndex;
import buildcraft.silicon.TileLaser;
import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

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

	private static boolean initialized = false;

	public static boolean allowMining = true;

	public static void load() {
		// Register gui handler
		//MinecraftForge.setGuiHandler(mod_BuildCraftFactory.instance, new GuiHandler());

		// MinecraftForge.registerEntity(EntityMechanicalArm.class,
		// mod_BuildCraftFactory.instance, EntityIds.MECHANICAL_ARM, 50, 10,
		// true);
	}

	public static void initialize() {
		if (initialized)
			return;
		else
			initialized = true;

		mod_BuildCraftCore.initialize();
		BuildCraftCore.initializeGears();

		allowMining = Boolean.parseBoolean(BuildCraftCore.mainConfiguration.getOrCreateBooleanProperty("mining.enabled",
				Configuration.CATEGORY_GENERAL, true).value);

		Property minigWellId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("miningWell.id",
				DefaultProps.MINING_WELL_ID);
		Property plainPipeId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("drill.id", DefaultProps.DRILL_ID);
		Property autoWorkbenchId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("autoWorkbench.id",
				DefaultProps.AUTO_WORKBENCH_ID);
		Property frameId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("frame.id", DefaultProps.FRAME_ID);
		Property quarryId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("quarry.id", DefaultProps.QUARRY_ID);
		Property pumpId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("pump.id", DefaultProps.PUMP_ID);
		Property tankId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("tank.id", DefaultProps.TANK_ID);
		Property refineryId = BuildCraftCore.mainConfiguration
				.getOrCreateBlockIdProperty("refinery.id", DefaultProps.REFINERY_ID);
		Property hopperId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("hopper.id", DefaultProps.HOPPER_ID);
		Property hopperDisable = BuildCraftCore.mainConfiguration.getOrCreateBooleanProperty("hopper.disabled", "Block Savers",
				false);

		BuildCraftCore.mainConfiguration.save();

		miningWellBlock = new BlockMiningWell(Integer.parseInt(minigWellId.value));
		ProxyCore.proxy.registerBlock(miningWellBlock.setBlockName("miningWellBlock"));
		ProxyCore.proxy.addName(miningWellBlock, "Mining Well");

		plainPipeBlock = new BlockPlainPipe(Integer.parseInt(plainPipeId.value));
		ProxyCore.proxy.registerBlock(plainPipeBlock.setBlockName("plainPipeBlock"));
		ProxyCore.proxy.addName(plainPipeBlock, "Mining Pipe");

		autoWorkbenchBlock = new BlockAutoWorkbench(Integer.parseInt(autoWorkbenchId.value));
		ProxyCore.proxy.registerBlock(autoWorkbenchBlock.setBlockName("autoWorkbenchBlock"));
		ProxyCore.proxy.addName(autoWorkbenchBlock, "Automatic Crafting Table");

		frameBlock = new BlockFrame(Integer.parseInt(frameId.value));
		ProxyCore.proxy.registerBlock(frameBlock.setBlockName("frameBlock"));
		ProxyCore.proxy.addName(frameBlock, "Frame");

		quarryBlock = new BlockQuarry(Integer.parseInt(quarryId.value));
		ProxyCore.proxy.registerBlock(quarryBlock.setBlockName("machineBlock"));
		ProxyCore.proxy.addName(quarryBlock, "Quarry");

		tankBlock = new BlockTank(Integer.parseInt(tankId.value));
		ProxyCore.proxy.registerBlock(tankBlock.setBlockName("tankBlock"));
		ProxyCore.proxy.addName(tankBlock, "Tank");

		pumpBlock = new BlockPump(Integer.parseInt(pumpId.value));
		ProxyCore.proxy.registerBlock(pumpBlock.setBlockName("pumpBlock"));
		ProxyCore.proxy.addName(pumpBlock, "Pump");

		refineryBlock = new BlockRefinery(Integer.parseInt(refineryId.value));
		ProxyCore.proxy.registerBlock(refineryBlock.setBlockName("refineryBlock"));
		ProxyCore.proxy.addName(refineryBlock, "Refinery");

		hopperDisabled = Boolean.parseBoolean(hopperDisable.value);
		if (!hopperDisabled) {
			hopperBlock = new BlockHopper(Integer.parseInt(hopperId.value));
			ProxyCore.proxy.registerBlock(hopperBlock.setBlockName("blockHopper"));
			ProxyCore.proxy.addName(hopperBlock, "Hopper");
		}

		ProxyCore.proxy.registerTileEntity(TileQuarry.class, "Machine");
		ProxyCore.proxy.registerTileEntity(TileMiningWell.class, "MiningWell");
		ProxyCore.proxy.registerTileEntity(TileAutoWorkbench.class, "AutoWorkbench");
		ProxyCore.proxy.registerTileEntity(TilePump.class, "net.minecraft.src.buildcraft.factory.TilePump");
		ProxyCore.proxy.registerTileEntity(TileTank.class, "net.minecraft.src.buildcraft.factory.TileTank");
		ProxyCore.proxy.registerTileEntity(TileRefinery.class, "net.minecraft.src.buildcraft.factory.Refinery");
		ProxyCore.proxy.registerTileEntity(TileLaser.class, "net.minecraft.src.buildcraft.factory.TileLaser");
		ProxyCore.proxy.registerTileEntity(TileAssemblyTable.class, "net.minecraft.src.buildcraft.factory.TileAssemblyTable");

		if (!hopperDisabled) {
			ProxyCore.proxy.registerTileEntity(TileHopper.class, "net.minecraft.src.buildcraft.factory.TileHopper");
		}

		/// FIXME: Render registration needs to move into a client side proxy.
		ClientRegistry.bindTileEntitySpecialRenderer(TileTank.class, new RenderTank());
		ClientRegistry.bindTileEntitySpecialRenderer(TileRefinery.class, new RenderRefinery());
		mod_BuildCraftCore.blockByEntityRenders.put(new EntityRenderIndex(BuildCraftFactory.refineryBlock, 0),
				new RenderRefinery());

		if(!hopperDisabled) {
			ClientRegistry.bindTileEntitySpecialRenderer(TileHopper.class, new RenderHopper());
			mod_BuildCraftCore.blockByEntityRenders.put(new EntityRenderIndex(BuildCraftFactory.hopperBlock, 0), new RenderHopper());
		}

		
		drillTexture = 2 * 16 + 1;

		BuildCraftCore.mainConfiguration.save();

		new BptBlockAutoWorkbench(autoWorkbenchBlock.blockID);
		new BptBlockFrame(frameBlock.blockID);
		new BptBlockRefinery(refineryBlock.blockID);
		new BptBlockTank(tankBlock.blockID);

		if (BuildCraftCore.loadDefaultRecipes)
			loadRecipes();
	}

	public static void loadRecipes() {

		if (allowMining) {
			ProxyCore.proxy.addCraftingRecipe(new ItemStack(miningWellBlock, 1),
					new Object[] { "ipi", "igi", "iPi", Character.valueOf('p'), Item.redstone, Character.valueOf('i'),
							Item.ingotIron, Character.valueOf('g'), BuildCraftCore.ironGearItem, Character.valueOf('P'),
							Item.pickaxeSteel });

			ProxyCore.proxy.addCraftingRecipe(new ItemStack(quarryBlock), new Object[] { "ipi", "gig", "dDd", Character.valueOf('i'),
					BuildCraftCore.ironGearItem, Character.valueOf('p'), Item.redstone, Character.valueOf('g'),
					BuildCraftCore.goldGearItem, Character.valueOf('d'), BuildCraftCore.diamondGearItem, Character.valueOf('D'),
					Item.pickaxeDiamond, });
		}

		ProxyCore.proxy.addCraftingRecipe(new ItemStack(autoWorkbenchBlock), new Object[] { " g ", "gwg", " g ", Character.valueOf('w'),
				Block.workbench, Character.valueOf('g'), BuildCraftCore.woodenGearItem });

		ProxyCore.proxy.addCraftingRecipe(new ItemStack(pumpBlock), new Object[] { "T ", "W ", Character.valueOf('T'), tankBlock,
				Character.valueOf('W'), miningWellBlock, });

		ProxyCore.proxy.addCraftingRecipe(new ItemStack(tankBlock), new Object[] { "ggg", "g g", "ggg", Character.valueOf('g'),
				Block.glass, });

		ProxyCore.proxy.addCraftingRecipe(new ItemStack(refineryBlock), new Object[] { "   ", "RTR", "TGT", Character.valueOf('T'),
				tankBlock, Character.valueOf('G'), BuildCraftCore.diamondGearItem, Character.valueOf('R'),
				Block.torchRedstoneActive, });
		if (!hopperDisabled) {
			ProxyCore.proxy.addCraftingRecipe(new ItemStack(hopperBlock), new Object[] { "ICI", "IGI", " I ", Character.valueOf('I'),
					Item.ingotIron, Character.valueOf('C'), Block.chest, Character.valueOf('G'), BuildCraftCore.stoneGearItem });
		}

	}
}
