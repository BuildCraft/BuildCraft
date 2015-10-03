package buildcraft.energy;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.biome.BiomeGenBase;

import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import buildcraft.api.core.BCLog;
import buildcraft.api.fuels.ICoolant;
import buildcraft.core.IMCHandler;
import buildcraft.energy.fuels.CoolantManager;
import buildcraft.energy.worldgen.OilPopulate;

public class IMCHandlerEnergy extends IMCHandler {
	@Override
	public void processIMCEvent(IMCEvent event, IMCMessage m) {
		if (m.key.equals("oil-lake-biome")) {
			processOilLakeBiomeIMC(event, m);
		} else if (m.key.equals("oil-gen-exclude")) {
			processOilGenExcludeIMC(event, m);
		} else if (m.key.equals("add-coolant")) {
			processCoolantAddIMC(event, m);
		} else if (m.key.equals("remove-coolant")) {
			processCoolantRemoveIMC(event, m);
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

	public static void processCoolantAddIMC(IMCEvent event, IMCMessage m) {
		boolean failed = false;
		if (!m.isNBTMessage()) {
			failed = true;
		} else {
			NBTTagCompound tag = m.getNBTValue();
			if (!tag.hasKey("coolant") || !tag.hasKey("degrees", 3)) {
				failed = true;
			} else {
				Fluid coolant = FluidRegistry.getFluid(tag.getString("coolant"));
				if (coolant != null) {
					CoolantManager.INSTANCE.addCoolant(coolant, tag.getInteger("degrees"));
				} else {
					failed = true;
				}
			}
		}
		if (failed) {
			BCLog.logger.warn("Received invalid coolant IMC message from mod %s!", m.getSender());
		}
	}

	public static void processCoolantRemoveIMC(IMCEvent event, IMCMessage m) {
		boolean failed = false;
		if (m.isStringMessage()) {
			ICoolant coolant = CoolantManager.INSTANCE.getCoolant(FluidRegistry.getFluid(m.getStringValue()));
			if (coolant != null) {
				CoolantManager.INSTANCE.getCoolants().remove(coolant);
			} else {
				failed = true;
			}
		} else {
			failed = true;
		}
		if (failed) {
			BCLog.logger.warn("Received invalid coolant IMC message from mod %s!", m.getSender());
		}
	}
}
