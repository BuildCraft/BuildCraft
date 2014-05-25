/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

import buildcraft.BuildCraftMod;
import buildcraft.core.DefaultProps;
import buildcraft.core.Version;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.tests.testcase.BlockTestCase;
import buildcraft.tests.testcase.Sequence;
import buildcraft.tests.testcase.SequenceActionCheckBlockMeta;
import buildcraft.tests.testcase.SequenceActionUseItem;
import buildcraft.tests.testcase.TileTestCase;

@Mod(name = "BuildCraft Tests", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Tests", dependencies = DefaultProps.DEPENDENCY_CORE)
public class BuildCraftTests extends BuildCraftMod {

	public static Block blockTestPathfinding;
	public static Block blockTestCase;

	public static Item tester;

	@Mod.Instance("BuildCraft|Tests")
	public static BuildCraftTests instance;

	private long startTestTime = 0;
	private String testFile = "";
	private Sequence testSequence;

	private boolean quitAfterRun = false;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		blockTestPathfinding = new BlockTestPathfinding();
		CoreProxy.proxy.registerBlock(blockTestPathfinding);
		blockTestPathfinding.setBlockName("testPathFinding");
		CoreProxy.proxy.registerTileEntity(TileTestPathfinding.class, "net.minecraft.src.builders.TileTestPathfinding");

		blockTestCase = new BlockTestCase();
		blockTestCase.setBlockName("testCase");
		CoreProxy.proxy.registerBlock(blockTestCase);
		CoreProxy.proxy.registerTileEntity(TileTestCase.class, "buildcraft.tests.testcase.TileTestCase");

		tester = new ItemTester();
		tester.setUnlocalizedName("tester");
		CoreProxy.proxy.registerItem(tester);

		Sequence.registerSequenceAction("useItem", SequenceActionUseItem.class);
		Sequence.registerSequenceAction("checkBlockMeta", SequenceActionCheckBlockMeta.class);
	}

	@Mod.EventHandler
	public void load(FMLInitializationEvent evt) {
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());

		String commandLine = System.getProperty("sun.java.command");
		OptionParser optionparser = new OptionParser();
		optionparser.allowsUnrecognizedOptions();

		ArgumentAcceptingOptionSpec<String> testOption = optionparser.accepts("test").withRequiredArg();
		OptionSpecBuilder quitOption = optionparser.accepts("quit");

		OptionSet optionset = optionparser.parse(commandLine.split(" "));
		testFile = optionset.valueOf(testOption);
		quitAfterRun = optionset.has(quitOption);

		if (testFile != null && !"".equals(testFile)) {
			FMLCommonHandler.instance().bus().register(this);
			System.out.println("[TEST 0] [LOAD TEST] \"" + testFile + "\"");
		}
	}

	@SubscribeEvent
	public void tick(WorldTickEvent evt) {
		WorldServer world = MinecraftServer.getServer().worldServers[0];
		long time = world.getTotalWorldTime();

		if (startTestTime == 0) {
			startTestTime = time;
		} else if (testSequence == null) {
			if (time - startTestTime > 10) {

				try {
					testSequence = new Sequence(world);

					File file = new File(testFile);
					FileInputStream f = new FileInputStream(file);
					byte[] data = new byte[(int) file.length()];
					f.read(data);
					f.close();

					NBTTagCompound nbt = CompressedStreamTools.decompress(data);

					testSequence.readFromNBT(nbt);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			if (!testSequence.done()) {
				testSequence.iterate();
			} else {
				if (quitAfterRun) {
					MinecraftServer.getServer().stopServer();
					System.exit(0);
				}
			}
		}
	}
}
