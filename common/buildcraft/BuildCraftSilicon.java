/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package buildcraft;

import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraftforge.common.Property;
import buildcraft.api.bptblocks.BptBlockInventory;
import buildcraft.api.bptblocks.BptBlockRotateMeta;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.core.DefaultProps;
import buildcraft.core.ItemRedstoneChipset;
import buildcraft.core.ProxyCore;
import buildcraft.silicon.BlockAssemblyTable;
import buildcraft.silicon.BlockLaser;
import buildcraft.silicon.GuiHandler;
import buildcraft.silicon.SiliconProxy;
import buildcraft.silicon.network.PacketHandlerSilicon;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;

@Mod(name="BuildCraft Silicon", version=DefaultProps.VERSION, useMetadata = false, modid = "BuildCraft|Silicon", dependencies = DefaultProps.DEPENDENCY_TRANSPORT)
@NetworkMod(channels = {DefaultProps.NET_CHANNEL_NAME}, packetHandler = PacketHandlerSilicon.class, clientSideRequired = true, serverSideRequired = true)
public class BuildCraftSilicon {
	public static Item redstoneChipset;
	public static BlockLaser laserBlock;
	public static BlockAssemblyTable assemblyTableBlock;

	@Instance
	public static BuildCraftSilicon instance;

	@Init
	public void load(FMLInitializationEvent evt) {
		NetworkRegistry.instance().registerGuiHandler(instance, new GuiHandler());

		new BptBlockRotateMeta(laserBlock.blockID, new int[] { 2, 5, 3, 4 }, true);
		new BptBlockInventory(assemblyTableBlock.blockID);

		if (BuildCraftCore.loadDefaultRecipes)
			loadRecipes();

		SiliconProxy.proxy.registerRenderers();
	}

	@PreInit
	public void initialize(FMLPreInitializationEvent evt) {
		Property laserId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("laser.id", DefaultProps.LASER_ID);

		Property assemblyTableId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("assemblyTable.id", DefaultProps.ASSEMBLY_TABLE_ID);

		BuildCraftCore.mainConfiguration.save();

		laserBlock = new BlockLaser(Integer.parseInt(laserId.value));
		ProxyCore.proxy.addName(laserBlock.setBlockName("laserBlock"), "Laser");
		ProxyCore.proxy.registerBlock(laserBlock);

		assemblyTableBlock = new BlockAssemblyTable(Integer.parseInt(assemblyTableId.value));
		ProxyCore.proxy.addName(assemblyTableBlock.setBlockName("assemblyTableBlock"), "Assembly Table");
		ProxyCore.proxy.registerBlock(assemblyTableBlock);

		redstoneChipset = new ItemRedstoneChipset(DefaultProps.REDSTONE_CHIPSET);
		redstoneChipset.setItemName("redstoneChipset");

	}

	public static void loadRecipes() {

		ProxyCore.proxy.addCraftingRecipe(new ItemStack(laserBlock), new Object[] { "ORR", "DDR", "ORR", Character.valueOf('O'),
				Block.obsidian, Character.valueOf('R'), Item.redstone, Character.valueOf('D'), Item.diamond, });

		ProxyCore.proxy.addCraftingRecipe(new ItemStack(assemblyTableBlock),
				new Object[] { "ORO", "ODO", "OGO", Character.valueOf('O'), Block.obsidian, Character.valueOf('R'),
						Item.redstone, Character.valueOf('D'), Item.diamond, Character.valueOf('G'),
						BuildCraftCore.diamondGearItem, });

		//Add reverse recipies for all gates

		//Iron
		ProxyCore.proxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGate, 1, 2),
				new Object[]{new ItemStack(redstoneChipset, 1, 0), new ItemStack(BuildCraftTransport.pipeGate, 1, 1)});
		ProxyCore.proxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGate, 1, 1),
				new Object[]{new ItemStack(redstoneChipset, 1, 0), new ItemStack(BuildCraftTransport.pipeGate, 1, 2)});

		//Gold
		ProxyCore.proxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGate, 1, 4),
				new Object[]{new ItemStack(redstoneChipset, 1, 0), new ItemStack(BuildCraftTransport.pipeGate, 1, 3)});
		ProxyCore.proxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGate, 1, 3),
				new Object[]{new ItemStack(redstoneChipset, 1, 0), new ItemStack(BuildCraftTransport.pipeGate, 1, 4)});

		//Diamond
		ProxyCore.proxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGate, 1, 6),
				new Object[]{new ItemStack(redstoneChipset, 1, 0), new ItemStack(BuildCraftTransport.pipeGate, 1, 5)});
		ProxyCore.proxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGate, 1, 5),
				new Object[]{new ItemStack(redstoneChipset, 1, 0), new ItemStack(BuildCraftTransport.pipeGate, 1, 6)});

		//Iron - Autarchic
		ProxyCore.proxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 2),
				new Object[]{new ItemStack(redstoneChipset, 1, 0), new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 1)});
		ProxyCore.proxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 1),
				new Object[]{new ItemStack(redstoneChipset, 1, 0), new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 2)});

		//Gold - Autarchic
		ProxyCore.proxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 4),
				new Object[]{new ItemStack(redstoneChipset, 1, 0), new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 3)});
		ProxyCore.proxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 3),
				new Object[]{new ItemStack(redstoneChipset, 1, 0), new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 4)});

		//Diamond - Autarchic
		ProxyCore.proxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 6),
				new Object[]{new ItemStack(redstoneChipset, 1, 0), new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 5)});
		ProxyCore.proxy.addShapelessRecipe(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 5),
				new Object[]{new ItemStack(redstoneChipset, 1, 0), new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 6)});

		// / REDSTONE CHIPSETS
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(Item.redstone) }, 10000,
				new ItemStack(redstoneChipset, 1, 0)));
		ProxyCore.proxy.addName(new ItemStack(redstoneChipset, 1, 0), "Redstone Chipset");
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(Item.redstone),
				new ItemStack(Item.ingotIron) }, 20000, new ItemStack(redstoneChipset, 1, 1)));
		ProxyCore.proxy.addName(new ItemStack(redstoneChipset, 1, 1), "Redstone Iron Chipset");
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(Item.redstone),
				new ItemStack(Item.ingotGold) }, 40000, new ItemStack(redstoneChipset, 1, 2)));
		ProxyCore.proxy.addName(new ItemStack(redstoneChipset, 1, 2), "Redstone Golden Chipset");
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(Item.redstone),
				new ItemStack(Item.diamond) }, 80000, new ItemStack(redstoneChipset, 1, 3)));
		ProxyCore.proxy.addName(new ItemStack(redstoneChipset, 1, 3), "Redstone Diamond Chipset");
		// PULSATING CHIPSETS
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(Item.redstone),
				new ItemStack(Item.enderPearl) }, 40000, new ItemStack(redstoneChipset, 2, 4)));
		ProxyCore.proxy.addName(new ItemStack(redstoneChipset, 1, 4), "Pulsating Chipset");

		// / REDSTONE GATES
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(redstoneChipset, 1, 0) }, 20000,
				new ItemStack(BuildCraftTransport.pipeGate, 1, 0)));
		ProxyCore.proxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 0), "Gate");
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {
				new ItemStack(BuildCraftTransport.pipeGate, 1, 0), new ItemStack(redstoneChipset, 1, 4),
				new ItemStack(redstoneChipset, 1, 1) }, 10000, new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 0)));
		ProxyCore.proxy.addName(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 0), "Autarchic Gate");

		// / IRON AND GATES
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(redstoneChipset, 1, 1),
				new ItemStack(BuildCraftTransport.redPipeWire) }, 40000, new ItemStack(BuildCraftTransport.pipeGate, 1, 1)));
		ProxyCore.proxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 1), "Iron AND Gate");
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {
				new ItemStack(BuildCraftTransport.pipeGate, 1, 1), new ItemStack(redstoneChipset, 1, 4),
				new ItemStack(redstoneChipset, 1, 1) }, 20000, new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 1)));
		ProxyCore.proxy.addName(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 1), "Autarchic Iron AND Gate");

		// / IRON OR GATES
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(redstoneChipset, 1, 1),
				new ItemStack(BuildCraftTransport.redPipeWire) }, 40000, new ItemStack(BuildCraftTransport.pipeGate, 1, 2)));
		ProxyCore.proxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 2), "Iron OR Gate");
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {
				new ItemStack(BuildCraftTransport.pipeGate, 1, 2), new ItemStack(redstoneChipset, 1, 4),
				new ItemStack(redstoneChipset, 1, 1) }, 20000, new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 2)));
		ProxyCore.proxy.addName(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 2), "Autarchic Iron OR Gate");

		// / GOLD AND GATES
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(redstoneChipset, 1, 2),
				new ItemStack(BuildCraftTransport.redPipeWire), new ItemStack(BuildCraftTransport.bluePipeWire) }, 80000,
				new ItemStack(BuildCraftTransport.pipeGate, 1, 3)));
		ProxyCore.proxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 3), "Gold AND Gate");
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {
				new ItemStack(BuildCraftTransport.pipeGate, 1, 3), new ItemStack(redstoneChipset, 1, 4),
				new ItemStack(redstoneChipset, 1, 1) }, 40000, new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 3)));
		ProxyCore.proxy.addName(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 3), "Autarchic Gold AND Gate");

		// / GOLD OR GATES
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(redstoneChipset, 1, 2),
				new ItemStack(BuildCraftTransport.redPipeWire), new ItemStack(BuildCraftTransport.bluePipeWire) }, 80000,
				new ItemStack(BuildCraftTransport.pipeGate, 1, 4)));
		ProxyCore.proxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 4), "Gold OR Gate");
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {
				new ItemStack(BuildCraftTransport.pipeGate, 1, 4), new ItemStack(redstoneChipset, 1, 4),
				new ItemStack(redstoneChipset, 1, 1) }, 40000, new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 4)));
		ProxyCore.proxy.addName(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 4), "Autarchic Gold OR Gate");

		// / DIAMOND AND GATES
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(redstoneChipset, 1, 3),
				new ItemStack(BuildCraftTransport.redPipeWire), new ItemStack(BuildCraftTransport.bluePipeWire),
				new ItemStack(BuildCraftTransport.greenPipeWire), new ItemStack(BuildCraftTransport.yellowPipeWire) }, 160000,
				new ItemStack(BuildCraftTransport.pipeGate, 1, 5)));
		ProxyCore.proxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 5), "Diamond AND Gate");
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {
				new ItemStack(BuildCraftTransport.pipeGate, 1, 5), new ItemStack(redstoneChipset, 1, 4),
				new ItemStack(redstoneChipset, 1, 1) }, 80000, new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 5)));
		ProxyCore.proxy.addName(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 5), "Autarchic Diamond AND Gate");

		// / DIAMOND OR GATES
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(redstoneChipset, 1, 3),
				new ItemStack(BuildCraftTransport.redPipeWire), new ItemStack(BuildCraftTransport.bluePipeWire),
				new ItemStack(BuildCraftTransport.greenPipeWire), new ItemStack(BuildCraftTransport.yellowPipeWire) }, 160000,
				new ItemStack(BuildCraftTransport.pipeGate, 1, 6)));
		ProxyCore.proxy.addName(new ItemStack(BuildCraftTransport.pipeGate, 1, 6), "Diamond OR Gate");
		AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] {
				new ItemStack(BuildCraftTransport.pipeGate, 1, 6), new ItemStack(redstoneChipset, 1, 4),
				new ItemStack(redstoneChipset, 1, 1) }, 80000, new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 6)));
		ProxyCore.proxy.addName(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, 6), "Autarchic Diamond OR Gate");

	}
}
