/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.tests.testcase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCSide;
import buildcraft.tests.ItemTester;

public class TileTestCase extends TileEntity {

	/**
	 * This way of handling the current test case means that test cases only
	 * work on single player, as this field needs to be share between both
	 * threads.
	 */
	public static TileTestCase currentTestCase;

	Sequence sequence;
	String testName = "test";

	public TileTestCase() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void updateEntity() {
		if (currentTestCase == null && !worldObj.isRemote) {
			currentTestCase = this;
		}
	}

	@SubscribeEvent
	public void itemUsed(PlayerInteractEvent evt) {
		if (!evt.entity.worldObj.isRemote) {
			if (sequence == null) {
				sequence = new Sequence(evt.entity.worldObj);
			}

			if (evt.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
				ItemStack usedItem = evt.entityPlayer.inventory.getCurrentItem();

				if (usedItem != null && !(usedItem.getItem() instanceof ItemTester)) {
					registerAction(new SequenceActionUseItem(evt.entity.worldObj, usedItem, evt.x, evt.y, evt.z));
				}
			}
		}
	}

	public synchronized void registerAction(SequenceAction action) {
		sequence.actions.add(action);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		MinecraftForge.EVENT_BUS.unregister(this);

		if (sequence != null) {
			File sequenceFile = new File(testName + ".seq");

			OutputStream gzOs = null;
			try {
				NBTTagCompound nbt = new NBTTagCompound();
				sequence.writeToNBT(nbt);

				FileOutputStream f = new FileOutputStream(sequenceFile);
				f.write(CompressedStreamTools.compress(nbt));
				f.close();
			} catch (IOException ex) {
				Logger.getLogger("Buildcraft").log(Level.SEVERE,
						String.format("Failed to save Sequence file: %s %s", sequenceFile, ex.getMessage()));
			} finally {
				try {
					if (gzOs != null) {
						gzOs.close();
					}
				} catch (IOException e) {
				}
			}
		}

		currentTestCase = null;
	}

	@RPC(RPCSide.SERVER)
	private void setName(String name) {
		testName = name;
	}
}
