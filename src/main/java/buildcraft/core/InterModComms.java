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
            if ("add-facade".equals(m.key)) {
                processFacadeIMC(event, m);
            } else if (m.key.equals("oil-lake-biome")) {
                processOilLakeBiomeIMC(event, m);
            } else if (m.key.equals("oil-gen-exclude")) {
                processOilGenExcludeIMC(event, m);
            }
        }
    }

    public static void processFacadeIMC(IMCEvent event, IMCMessage m) {
        try {
            if (m.isStringMessage()) {
                Splitter splitter = Splitter.on("@").trimResults();

                String[] array = Iterables.toArray(splitter.split(m.getStringValue()), String.class);
                if (array.length != 2) {
                    Logger.getLogger("Buildcraft").log(Level.INFO, String.format("Received an invalid add-facade request %s from mod %s", m.getStringValue(), m.getSender()));
                } else {
                    Integer blId = Ints.tryParse(array[0]);
                    Integer metaId = Ints.tryParse(array[1]);
                                                          
                    if (blId == null || metaId == null) {
                        Logger.getLogger("Buildcraft").log(Level.INFO, String.format("Received an invalid add-facade request %s from mod %s", m.getStringValue(), m.getSender()));
                    } else {
                    	Block block = (Block) Block.blockRegistry.getObjectById(blId);
                        ItemFacade.addFacade(new ItemStack(block, 1, metaId));
                    }
                }
            } else if (m.isItemStackMessage()) {
                ItemFacade.addFacade(m.getItemStackValue());
            }
        } catch (Exception ex) {

        }
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
