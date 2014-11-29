/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.biome.BiomeGenBase;
import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.BCLog;
import buildcraft.api.gates.GateExpansions;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.core.recipes.AssemblyRecipeManager;
import buildcraft.core.recipes.IntegrationRecipeManager;
import buildcraft.core.recipes.RefineryRecipeManager;
import buildcraft.energy.worldgen.OilPopulate;
import buildcraft.transport.ItemFacade;
import buildcraft.transport.recipes.GateExpansionRecipe;

public final class InterModComms {

	/**
	 * Deactivate constructor
	 */
	private InterModComms() {
	}

	public static void processIMC(IMCEvent event) {
		for (IMCMessage m : event.getMessages()) {
			if (m.key.equals("add-facade")) {
				processAddFacadeIMC(event, m);
			} else if (m.key.equals("blacklist-facade")) {
				processBlacklistFacadeIMC(event, m);
			} else if (m.key.equals("oil-lake-biome")) {
				processOilLakeBiomeIMC(event, m);
			} else if (m.key.equals("oil-gen-exclude")) {
				processOilGenExcludeIMC(event, m);
			} else if (m.key.equals("add-assembly-recipe")) {
				processAssemblyRecipeAddIMC(event, m);
			} else if (m.key.equals("add-refinery-recipe")) {
				processRefineryRecipeAddIMC(event, m);
			} else if (m.key.equals("add-gate-expansion-recipe")) {
				processGateExpansionRecipeAddIMC(event, m);
			} else if (m.key.equals("remove-assembly-recipe")) {
				processAssemblyRecipeRemoveIMC(event, m);
			} else if (m.key.equals("remove-refinery-recipe")) {
				processRefineryRecipeRemoveIMC(event, m);
			} else {
				BCLog.logger.warn(String.format("Received an IMC message with unknown key ('%s') from %s!", new Object[]{m.key, m.getSender()}));
			}
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
	
	public static void processGateExpansionRecipeAddIMC(IMCEvent event, IMCMessage msg) {
		boolean failed = false;
		if (!msg.isNBTMessage()) {
			failed = true;
		} else {
			NBTTagCompound recipe = msg.getNBTValue();
			if (!recipe.hasKey("id") || !recipe.hasKey("expansion") || !recipe.hasKey("input")) {
				failed = true;
				return;
			}
			IGateExpansion exp = GateExpansions.getExpansion(recipe.getString("expansion"));
			ItemStack is = ItemStack.loadItemStackFromNBT(recipe.getCompoundTag("input"));
			if (exp == null || is == null) {
				failed = true;
				return;
			}
			IntegrationRecipeManager.INSTANCE.addRecipe(new GateExpansionRecipe(
					recipe.getString("id"), exp, is
				));
		}
		if (failed) {
			BCLog.logger.warn("Received invalid gate expansion recipe IMC message from mod %s!", msg.getSender());
		}
	}
	
	public static void processAssemblyRecipeAddIMC(IMCEvent event, IMCMessage msg) {
		boolean failed = false;
		if (!msg.isNBTMessage()) {
			failed = true;
		} else {
			NBTTagCompound recipe = msg.getNBTValue();
			if (!recipe.hasKey("id") || !recipe.hasKey("input", 9) || !recipe.hasKey("output", 10)
					|| !recipe.hasKey("energy", 6)) { // Ints - NBTBase#NBTTypes
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

	public static void processAddFacadeIMC(IMCEvent event, IMCMessage m) {
		try {
			if (m.isStringMessage()) {
				Splitter splitter = Splitter.on("@").trimResults();

				String[] array = Iterables.toArray(splitter.split(m.getStringValue()), String.class);
				if (array.length != 2) {
					BCLog.logger.info(String.format("Received an invalid add-facade request %s from mod %s", m.getStringValue(), m.getSender()));
				} else {
					String blockName = array[0];
					Integer metaId = Ints.tryParse(array[1]);

					if (Strings.isNullOrEmpty(blockName) || metaId == null) {
						BCLog.logger.info(String.format("Received an invalid add-facade request %s from mod %s", m.getStringValue(), m.getSender()));
					} else {
						Block block = (Block) Block.blockRegistry.getObject(blockName);
						if (block.getRenderType() != 0 && block.getRenderType() != 31) {
							BuildCraftTransport.facadeItem.addFacade(new ItemStack(block, 1, metaId));
						} else {
							logRedundantAddFacadeMessage(m, block.toString());
						}
					}
				}
			} else if (m.isItemStackMessage()) {
				ItemStack modItemStack = m.getItemStackValue();

				Block block = Block.getBlockFromItem(modItemStack.getItem());
				if (block != null && block.getRenderType() != 0 && block.getRenderType() != 31) {
					BuildCraftTransport.facadeItem.addFacade(modItemStack);
				} else if (block != null) {
					logRedundantAddFacadeMessage(m, block.toString());
				}
			}
		} catch (Exception ex) {
		}
	}

	public static void processBlacklistFacadeIMC(IMCEvent event, IMCMessage message) {
		try {
			if (message.isItemStackMessage()) {
				ItemStack modItemStack = message.getItemStackValue();

				Block block = Block.getBlockFromItem(modItemStack.getItem());
				if (block != null) {
					String blockName = Block.blockRegistry.getNameForObject(block);
					ItemFacade.blacklistFacade(blockName);
				}
			} else {
				BCLog.logger.info(String.format("Invalid blacklist-facade message from mod %s. Send an ItemStackMessage instead.", message.getSender()));
			}
		} catch (Throwable e) {
		}
	}

	private static void logRedundantAddFacadeMessage(IMCMessage m, String blockName)
	{
		BCLog.logger.warn(String.format("%s is trying to add %s - a facade that BuildCraft will add later. Facade addition not necessary.", m.getSender(), blockName));
	}

	public static void processOilLakeBiomeIMC(IMCEvent event, IMCMessage m) {
		try {
			String biomeID = m.getStringValue().trim();
			int id = Integer.valueOf(biomeID);
			if (id >= BiomeGenBase.getBiomeGenArray().length) {
					throw new IllegalArgumentException("Biome ID must be less than " + BiomeGenBase.getBiomeGenArray().length);
			}
			OilPopulate.INSTANCE.surfaceDepositBiomes.add(id);
		} catch (Exception ex) {
			BCLog.logger.warn(String.format("Received an invalid oil-lake-biome request %s from mod %s", m.getStringValue(), m.getSender()));
		}
		BCLog.logger.info(String.format("Received a successful oil-lake-biome request %s from mod %s", m.getStringValue(), m.getSender()));
	}

	public static void processOilGenExcludeIMC(IMCEvent event, IMCMessage m) {
		try {
			String biomeID = m.getStringValue().trim();
			int id = Integer.valueOf(biomeID);
			if (id >= BiomeGenBase.getBiomeGenArray().length) {
				throw new IllegalArgumentException("Biome ID must be less than " + BiomeGenBase.getBiomeGenArray().length);
			}
			OilPopulate.INSTANCE.excludedBiomes.add(id);
		} catch (Exception ex) {
			BCLog.logger.warn(String.format("Received an invalid oil-gen-exclude request %s from mod %s", m.getStringValue(), m.getSender()));
		}
		BCLog.logger.info(String.format("Received a successful oil-gen-exclude request %s from mod %s", m.getStringValue(), m.getSender()));
	}
}
