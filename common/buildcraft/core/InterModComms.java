/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeGenBase;
import buildcraft.energy.worldgen.OilPopulate;
import buildcraft.transport.ItemFacade;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;

import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;

public class InterModComms {

    public static void processIMC(IMCEvent event) {
        for (IMCMessage m : event.getMessages()) {
            if (m.key.equals("add-facade")) {
                processAddFacadeIMC(event, m);
            } else if (m.key.equals("blacklist-facade")) {
								processBlacklistFacadeIMC(event, m);
						}	else if (m.key.equals("oil-lake-biome")) {
                processOilLakeBiomeIMC(event, m);
            } else if (m.key.equals("oil-gen-exclude")) {
                processOilGenExcludeIMC(event, m);
            }
        }
    }

    public static void processAddFacadeIMC(IMCEvent event, IMCMessage m) {
				try {
            if (m.isStringMessage()) {
                Splitter splitter = Splitter.on("@").trimResults();

                String[] array = Iterables.toArray(splitter.split(m.getStringValue()), String.class);
                if (array.length != 2) {
                    Logger.getLogger("Buildcraft").log(Level.INFO, String.format("Received an invalid add-facade request %s from mod %s", m.getStringValue(), m.getSender()));
                } else {
                    String blockName = array[0];
									  Integer metaId = Ints.tryParse(array[1]);

                    if (Strings.isNullOrEmpty(blockName)|| metaId == null) {
                        Logger.getLogger("Buildcraft").log(Level.INFO, String.format("Received an invalid add-facade request %s from mod %s", m.getStringValue(), m.getSender()));
                    } else {
												Block block = (Block) Block.blockRegistry.getObject(blockName);
												if(block.getRenderType() != 0 && block.getRenderType() != 31) {
													ItemFacade.addFacade(new ItemStack(block, 1, metaId));
												} else {
													logRedundantAddFacadeMessage(m, block.toString());
												}
                    }
                }
            } else if (m.isItemStackMessage()) {
								ItemStack modItemStack = m.getItemStackValue();

								Block block = Block.getBlockFromItem(modItemStack.getItem());
								if(block != null && block.getRenderType() != 0 && block.getRenderType() != 31) {
									ItemFacade.addFacade(modItemStack);
								} else {
									logRedundantAddFacadeMessage(m, block.toString());
								}
            }
        } catch (Exception ex) {
        }
    }

		public static void processBlacklistFacadeIMC(IMCEvent event, IMCMessage message){
			try {
				if(message.isItemStackMessage()) {
					ItemStack modItemStack = message.getItemStackValue();

					Block block = Block.getBlockFromItem(modItemStack.getItem());
					if(block != null) {
						String blockName = Block.blockRegistry.getNameForObject(block);
						ItemFacade.blacklistFacade(blockName);
					}

				} else {
					Logger.getLogger("Buildcraft").log(Level.INFO, String.format("Invalid blacklist-facade message from mod %s. Send an ItemStackMessage instead.", message.getSender()));
				}
			} catch (Throwable _) {
			}
		}

		private static void logRedundantAddFacadeMessage(IMCMessage m, String blockName)
		{
				Logger.getLogger("Buildcraft").log(Level.WARNING, String.format("%s is trying to add %s a facade that Buildcraft will add later. Facade not added.", m.getSender(), blockName));
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
            Logger.getLogger("Buildcraft").log(Level.WARNING, String.format("Received an invalid oil-lake-biome request %s from mod %s", m.getStringValue(), m.getSender()));
        }
        Logger.getLogger("Buildcraft").log(Level.INFO, String.format("Received an successfull oil-lake-biome request %s from mod %s", m.getStringValue(), m.getSender()));
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
            Logger.getLogger("Buildcraft").log(Level.WARNING, String.format("Received an invalid oil-gen-exclude request %s from mod %s", m.getStringValue(), m.getSender()));
        }
        Logger.getLogger("Buildcraft").log(Level.INFO, String.format("Received an successfull oil-gen-exclude request %s from mod %s", m.getStringValue(), m.getSender()));
    }
}
