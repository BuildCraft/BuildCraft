/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.tests.testcase;

import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import net.minecraftforge.common.util.Constants;

import buildcraft.api.blueprints.MappingNotFoundException;
import buildcraft.api.blueprints.MappingRegistry;

public class Sequence {

	private static HashMap<String, Class> strToClass = new HashMap<String, Class>();
	private static HashMap<Class, String> classToStr = new HashMap<Class, String>();

	public LinkedList<SequenceAction> actions = new LinkedList<SequenceAction>();

	public World world;
	public long initialDate;

	public Sequence(World iWorld) {
		world = iWorld;
		initialDate = iWorld.getTotalWorldTime();
	}

	public static void registerSequenceAction(String name, Class clas) {
		strToClass.put(name, clas);
		classToStr.put(clas, name);
	}

	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setLong("initialDate", initialDate);

		MappingRegistry registry = new MappingRegistry();

		NBTTagList list = new NBTTagList();

		for (SequenceAction action : actions) {
			NBTTagCompound cpt = new NBTTagCompound();
			action.writeToNBT(cpt);
			cpt.setString("class", classToStr.get(action.getClass()));
			registry.scanAndTranslateStacksToRegistry(cpt);
			list.appendTag(cpt);
		}

		nbt.setTag("actions", list);

		NBTTagCompound registryNBT = new NBTTagCompound();
		registry.write(registryNBT);
		nbt.setTag("registry", registryNBT);
	}

	public void readFromNBT(NBTTagCompound nbt) {
		initialDate = nbt.getLong("initialDate");

		MappingRegistry registry = new MappingRegistry();
		registry.read(nbt.getCompoundTag("registry"));

		NBTTagList list = nbt.getTagList("actions", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < list.tagCount(); ++i) {
			NBTTagCompound cpt = list.getCompoundTagAt(i);

			try {
				registry.scanAndTranslateStacksToWorld(cpt);
				SequenceAction action = (SequenceAction) strToClass.get(cpt.getString("class")).newInstance();
				action.world = world;
				action.readFromNBT(cpt);

				action.date = (action.date - initialDate) + world.getTotalWorldTime();

				actions.add(action);
			} catch (MappingNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean done() {
		return actions.size() == 0;
	}

	public void iterate() {
		SequenceAction next = actions.getFirst();

		if (world.getTotalWorldTime() >= next.date) {
			next.execute();
			actions.removeFirst();
		}
	}

}
