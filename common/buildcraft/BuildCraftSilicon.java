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
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.ItemRedstoneChipset;
import buildcraft.core.Version;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.silicon.BlockLaser;
import buildcraft.silicon.BlockLaserTable;
import buildcraft.silicon.GuiHandler;
import buildcraft.silicon.ItemLaserTable;
import buildcraft.silicon.SiliconProxy;
import buildcraft.silicon.TileAdvancedCraftingTable;
import buildcraft.silicon.TileAssemblyTable;
import buildcraft.silicon.TileLaser;
import buildcraft.silicon.network.PacketHandlerSilicon;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Property;

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

		new BptBlockRotateMeta(laserBlock.blockID, new int[]{2, 5, 3, 4}, true);
		new BptBlockInventory(assemblyTableBlock.blockID);

		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}

		SiliconProxy.proxy.registerRenderers();
	}

	public static void loadRecipes() {

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(laserBlock),
				new Object[]{"ORR", "DDR", "ORR", 'O', Block.obsidian, 'R', Item.redstone, 'D',
			Item.diamond,});

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(assemblyTableBlock, 1, 0), new Object[]{"ORO", "ODO", "OGO", 'O', Block.obsidian,
			'R', Item.redstone, 'D', Item.diamond, 'G', BuildCraftCore.diamondGearItem,});

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(assemblyTableBlock, 1, 1), new Object[]{"OWO", "OCO", "ORO", 'O', Block.obsidian,
			'W', Block.workbench, 'C', Block.chest, 'R', new ItemStack(redstoneChipset, 1, 0),});
		// Add reverse recipies for all gates

		// Iron
		CoreProxy.proxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGate, 1, 2), new Object[]{new ItemStack(redstoneChipset, 1, 0),
			new ItemStack(BuildCraftTransport.pipeGate, 1, 1)});
		CoreProxy.proxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGate, 1, 1), new Object[]{new ItemStack(redstoneChipset, 1, 0),
			new ItemStack(BuildCraftTransport.pipeGate, 1, 2)});

		// Gold
		CoreProxy.proxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGate, 1, 4), new Object[]{new ItemStack(redstoneChipset, 1, 0),
			new ItemStack(BuildCraftTransport.pipeGate, 1, 3)});
		CoreProxy.proxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGate, 1, 3), new Object[]{new ItemStack(redstoneChipset, 1, 0),
			new ItemStack(BuildCraftTransport.pipeGate, 1, 4)});

		// Diamond
		CoreProxy.proxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGate, 1, 6), new Object[]{new ItemStack(redstoneChipset, 1, 0),
			new ItemStack(BuildCraftTransport.pipeGate, 1, 5)});
		CoreProxy.proxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGate, 1, 5), new Object[]{new ItemStack(redstoneChipset, 1, 0),
			new ItemStack(BuildCraftTransport.pipeGate, 1, 6)});

		// Iron - Autarchic
		CoreProxy.proxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 2), new Object[]{new ItemStack(redstoneChipset, 1, 0),
			new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 1)});
		CoreProxy.proxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 1), new Object[]{new ItemStack(redstoneChipset, 1, 0),
			new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 2)});

		// Gold - Autarchic
		CoreProxy.proxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 4), new Object[]{new ItemStack(redstoneChipset, 1, 0),
			new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 3)});
		CoreProxy.proxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 3), new Object[]{new ItemStack(redstoneChipset, 1, 0),
			new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 4)});

		// Diamond - Autarchic
		CoreProxy.proxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 6), new Object[]{new ItemStack(redstoneChipset, 1, 0),
			new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 5)});
		CoreProxy.proxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 5), new Object[]{new ItemStack(redstoneChipset, 1, 0),
			new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 6)});

		// / REDSTONE CHIPSETS
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[]{new ItemStack(Item.redstone)}, 10000, new ItemStack(redstoneChipset, 1, 0)));
		CoreProxy.proxy.addName(new ItemStack(redstoneChipset, 1, 0), "Redstone Chipset");
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[]{new ItemStack(Item.redstone), new ItemStack(Item.ingotIron)}, 20000,
				new ItemStack(redstoneChipset, 1, 1)));
		CoreProxy.proxy.addName(new ItemStack(redstoneChipset, 1, 1), "Redstone Iron Chipset");
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[]{new ItemStack(Item.redstone), new ItemStack(Item.ingotGold)}, 40000,
				new ItemStack(redstoneChipset, 1, 2)));
		CoreProxy.proxy.addName(new ItemStack(redstoneChipset, 1, 2), "Redstone Golden Chipset");
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[]{new ItemStack(Item.redstone), new ItemStack(Item.diamond)}, 80000,
				new ItemStack(redstoneChipset, 1, 3)));
		CoreProxy.proxy.addName(new ItemStack(redstoneChipset, 1, 3), "Redstone Diamond Chipset");
		// PULSATING CHIPSETS
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[]{new ItemStack(Item.redstone), new ItemStack(Item.enderPearl)}, 40000,
				new ItemStack(redstoneChipset, 2, 4)));
		CoreProxy.proxy.addName(new ItemStack(redstoneChipset, 1, 4), "Pulsating Chipset");

		// / REDSTONE GATES
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[]{new ItemStack(redstoneChipset, 1, 0)}, 20000, new ItemStack(
				BuildCraftTransport.pipeGate, 1, 0)));
		CoreProxy.proxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 0), "Gate");
		AssemblyRecipe.assemblyRecipes
				.add(new AssemblyRecipe(new ItemStack[]{new ItemStack(BuildCraftTransport.pipeGate, 1, 0), new ItemStack(redstoneChipset, 1, 4),
			new ItemStack(redstoneChipset, 1, 1)}, 10000, new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 0)));
		CoreProxy.proxy.addName(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 0), "Autarchic Gate");

		// / IRON AND GATES
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[]{new ItemStack(redstoneChipset, 1, 1),
			new ItemStack(BuildCraftTransport.redPipeWire)}, 40000, new ItemStack(BuildCraftTransport.pipeGate, 1, 1)));
		CoreProxy.proxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 1), "Iron AND Gate");
		AssemblyRecipe.assemblyRecipes
				.add(new AssemblyRecipe(new ItemStack[]{new ItemStack(BuildCraftTransport.pipeGate, 1, 1), new ItemStack(redstoneChipset, 1, 4),
			new ItemStack(redstoneChipset, 1, 1)}, 20000, new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 1)));
		CoreProxy.proxy.addName(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 1), "Autarchic Iron AND Gate");

		// / IRON OR GATES
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[]{new ItemStack(redstoneChipset, 1, 1),
			new ItemStack(BuildCraftTransport.redPipeWire)}, 40000, new ItemStack(BuildCraftTransport.pipeGate, 1, 2)));
		CoreProxy.proxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 2), "Iron OR Gate");
		AssemblyRecipe.assemblyRecipes
				.add(new AssemblyRecipe(new ItemStack[]{new ItemStack(BuildCraftTransport.pipeGate, 1, 2), new ItemStack(redstoneChipset, 1, 4),
			new ItemStack(redstoneChipset, 1, 1)}, 20000, new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 2)));
		CoreProxy.proxy.addName(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 2), "Autarchic Iron OR Gate");

		// / GOLD AND GATES
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[]{new ItemStack(redstoneChipset, 1, 2),
			new ItemStack(BuildCraftTransport.redPipeWire), new ItemStack(BuildCraftTransport.bluePipeWire)}, 80000, new ItemStack(
				BuildCraftTransport.pipeGate, 1, 3)));
		CoreProxy.proxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 3), "Gold AND Gate");
		AssemblyRecipe.assemblyRecipes
				.add(new AssemblyRecipe(new ItemStack[]{new ItemStack(BuildCraftTransport.pipeGate, 1, 3), new ItemStack(redstoneChipset, 1, 4),
			new ItemStack(redstoneChipset, 1, 1)}, 40000, new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 3)));
		CoreProxy.proxy.addName(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 3), "Autarchic Gold AND Gate");

		// / GOLD OR GATES
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[]{new ItemStack(redstoneChipset, 1, 2),
			new ItemStack(BuildCraftTransport.redPipeWire), new ItemStack(BuildCraftTransport.bluePipeWire)}, 80000, new ItemStack(
				BuildCraftTransport.pipeGate, 1, 4)));
		CoreProxy.proxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 4), "Gold OR Gate");
		AssemblyRecipe.assemblyRecipes
				.add(new AssemblyRecipe(new ItemStack[]{new ItemStack(BuildCraftTransport.pipeGate, 1, 4), new ItemStack(redstoneChipset, 1, 4),
			new ItemStack(redstoneChipset, 1, 1)}, 40000, new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 4)));
		CoreProxy.proxy.addName(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 4), "Autarchic Gold OR Gate");

		// / DIAMOND AND GATES
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[]{new ItemStack(redstoneChipset, 1, 3),
			new ItemStack(BuildCraftTransport.redPipeWire), new ItemStack(BuildCraftTransport.bluePipeWire),
			new ItemStack(BuildCraftTransport.greenPipeWire), new ItemStack(BuildCraftTransport.yellowPipeWire)}, 160000, new ItemStack(
				BuildCraftTransport.pipeGate, 1, 5)));
		CoreProxy.proxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 5), "Diamond AND Gate");
		AssemblyRecipe.assemblyRecipes
				.add(new AssemblyRecipe(new ItemStack[]{new ItemStack(BuildCraftTransport.pipeGate, 1, 5), new ItemStack(redstoneChipset, 1, 4),
			new ItemStack(redstoneChipset, 1, 1)}, 80000, new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 5)));
		CoreProxy.proxy.addName(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 5), "Autarchic Diamond AND Gate");

		// / DIAMOND OR GATES
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[]{new ItemStack(redstoneChipset, 1, 3),
			new ItemStack(BuildCraftTransport.redPipeWire), new ItemStack(BuildCraftTransport.bluePipeWire),
			new ItemStack(BuildCraftTransport.greenPipeWire), new ItemStack(BuildCraftTransport.yellowPipeWire)}, 160000, new ItemStack(
				BuildCraftTransport.pipeGate, 1, 6)));
		CoreProxy.proxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 6), "Diamond OR Gate");
		AssemblyRecipe.assemblyRecipes
				.add(new AssemblyRecipe(new ItemStack[]{new ItemStack(BuildCraftTransport.pipeGate, 1, 6), new ItemStack(redstoneChipset, 1, 4),
			new ItemStack(redstoneChipset, 1, 1)}, 80000, new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 6)));
		CoreProxy.proxy.addName(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 6), "Autarchic Diamond OR Gate");

	}
	
	@EventHandler
    public void processIMCRequests(FMLInterModComms.IMCEvent event) {
        InterModComms.processIMC(event);
    }
}
