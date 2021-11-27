/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import buildcraft.core.crops.CropHandlerPlantable;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.core.BCLog;
import buildcraft.core.recipes.AssemblyRecipeManager;
import buildcraft.core.recipes.RefineryRecipeManager;

public final class InterModComms {
	private static final Set<IMCHandler> handlers = new HashSet<IMCHandler>();

	/**
	 * Deactivate constructor
	 */
	private InterModComms() {
	}

	public static void registerHandler(IMCHandler handler) {
		handlers.add(handler);
	}

	public static void processIMC(IMCEvent event) {
		for (IMCMessage m : event.getMessages()) {
			if (m.key.equals("add-assembly-recipe")) {
				processAssemblyRecipeAddIMC(event, m);
			} else if (m.key.equals("add-refinery-recipe")) {
				processRefineryRecipeAddIMC(event, m);
			} else if (m.key.equals("remove-assembly-recipe")) {
				processAssemblyRecipeRemoveIMC(event, m);
			} else if (m.key.equals("remove-refinery-recipe")) {
				processRefineryRecipeRemoveIMC(event, m);
			} else if (m.key.equals("remove-plantable-block")) {
				processPlantableBlockRemoveIMC(event, m);
			} else {
				for (IMCHandler h : handlers) {
					h.processIMCEvent(event, m);
				}
			}
		}
	}

	public static void processPlantableBlockRemoveIMC(IMCEvent event, IMCMessage msg) {
		if (msg.isStringMessage()) {
			Object blockObj = Block.blockRegistry.getObject(msg.getStringValue());
			if (blockObj instanceof Block) {
				CropHandlerPlantable.forbidBlock((Block) blockObj);
			}
			BCLog.logger.info(String.format("Received a plantable block '%s' removal request from mod %s", msg.getStringValue(), msg.getSender()));
		}
	}

	public static void processAssemblyRecipeRemoveIMC(IMCEvent event, IMCMessage msg) {
		if (msg.isStringMessage()) {
			AssemblyRecipeManager.INSTANCE.removeRecipe(msg.getStringValue());

			BCLog.logger.info(String.format("Received an assembly recipe '%s' removal request from mod %s", msg.getStringValue(), msg.getSender()));
		}
	}

	public static void processRefineryRecipeRemoveIMC(IMCEvent event, IMCMessage msg) {
		if (msg.isStringMessage()) {
			RefineryRecipeManager.INSTANCE.removeRecipe(msg.getStringValue());

			BCLog.logger.info(String.format("Received a refinery recipe '%s' removal request from mod %s", msg.getStringValue(), msg.getSender()));
		}
	}

	public static void processAssemblyRecipeAddIMC(IMCEvent event, IMCMessage msg) {
		boolean failed = false;
		if (!msg.isNBTMessage()) {
			failed = true;
		} else {
			NBTTagCompound recipe = msg.getNBTValue();
			if (!recipe.hasKey("id") || !recipe.hasKey("input", 9) || !recipe.hasKey("output", 10)
					|| !recipe.hasKey("energy", 3)) { // Ints - NBTBase#NBTTypes
				failed = true;
			} else {
				NBTTagList list = (NBTTagList) recipe.getTag("input");
				List<ItemStack> input = new ArrayList<ItemStack>();
				for (int i = 0; i < list.tagCount(); i++) {
					ItemStack is = ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i));
					if (is != null) {
						input.add(is);
					}
				}
				String id = recipe.getString("id");
				ItemStack is = ItemStack.loadItemStackFromNBT(recipe.getCompoundTag("output"));
				if (is != null && !input.isEmpty() && id.length() > 0) {
					AssemblyRecipeManager.INSTANCE.addRecipe(id, recipe.getInteger("energy"), is,
							(Object[]) input.toArray(new ItemStack[input.size()]));
				} else {
					failed = true;
				}
			}
		}
		if (failed) {
			BCLog.logger.warn("Received invalid assembly recipe IMC message from mod %s!", msg.getSender());
		}
	}

	public static void processRefineryRecipeAddIMC(IMCEvent event, IMCMessage msg) {
		boolean failed = false;
		if (!msg.isNBTMessage()) {
			failed = true;
		} else {
			NBTTagCompound recipe = msg.getNBTValue();
			if (!recipe.hasKey("id") && !recipe.hasKey("input", 10) || !recipe.hasKey("output", 10)
					|| !recipe.hasKey("energy", 3) || !recipe.hasKey("delay", 3)) {
				failed = true;
			} else {
				FluidStack output = FluidStack.loadFluidStackFromNBT(recipe.getCompoundTag("output"));
				FluidStack input = FluidStack.loadFluidStackFromNBT(recipe.getCompoundTag("input"));
				FluidStack input2 = null;
				String id = recipe.getString("id");
				if (recipe.hasKey("input_2", 10)) {
					input2 = FluidStack.loadFluidStackFromNBT(recipe.getCompoundTag("input_2"));
				}
				if (input != null && output != null && id.length() > 0) {
					RefineryRecipeManager.INSTANCE.addRecipe(id, input, input2, output, recipe.getInteger("energy"),
							recipe.getInteger("delay"));
				} else {
					failed = true;
				}
			}
		}
		if (failed) {
			BCLog.logger.warn("Received invalid refinery recipe IMC message from mod %s!", msg.getSender());
		}
	}
}
