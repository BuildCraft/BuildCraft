/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import java.util.LinkedList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftEnergy;
import buildcraft.api.core.JavaTools;
import buildcraft.api.core.NetworkData;
import buildcraft.api.gates.IOverrideDefaultTriggers;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.mj.IBatteryIOObject;
import buildcraft.api.mj.IBatteryObject;
import buildcraft.api.mj.ISidedBatteryProvider;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.power.IPowerEmitter;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.core.DefaultProps;
import buildcraft.core.TileBuffer;
import buildcraft.core.TileBuildCraft;
import buildcraft.energy.gui.ContainerEngine;

public abstract class TileEngine extends TileBuildCraft implements ISidedBatteryProvider, IPowerEmitter, IPowerReceptor, IOverrideDefaultTriggers, IPipeConnection {

	// Index corresponds to metadata
	public static final ResourceLocation[] BASE_TEXTURES = new ResourceLocation[]{
			new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/base_wood.png"),
			new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/base_stone.png"),
			new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/base_iron.png"),
			new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/base_creative.png")
	};

	public static final ResourceLocation[] CHAMBER_TEXTURES = new ResourceLocation[]{
			new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/chamber_wood.png"),
			new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/chamber_stone.png"),
			new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/chamber_iron.png"),
			new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/chamber_creative.png")
	};

	// THESE ARE ONLY BLUE TRUNKS. OTHER HEAT STAGES ARE HANDLED PER TILE
	public static final ResourceLocation[] TRUNK_TEXTURES = new ResourceLocation[]{
			new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/trunk_wood.png"),
			new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/trunk_stone.png"),
			new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/trunk_iron.png"),
			new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/trunk_creative.png")
	};

	// TEMP
	public static final ResourceLocation TRUNK_BLUE_TEXTURE = new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/trunk_blue.png");
	public static final ResourceLocation TRUNK_GREEN_TEXTURE = new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/trunk_green.png");
	public static final ResourceLocation TRUNK_YELLOW_TEXTURE = new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/trunk_yellow.png");
	public static final ResourceLocation TRUNK_RED_TEXTURE = new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/trunk_red.png");


	public enum EnergyStage {
		BLUE, GREEN, YELLOW, RED, OVERHEAT;
		public static final EnergyStage[] VALUES = values();
	}

	public static final float MIN_HEAT = 20;
	public static final float IDEAL_HEAT = 100;
	public static final float MAX_HEAT = 250;

	public double currentOutput = 0;
	public boolean isRedstonePowered = false;
	public float progress;
	public float heat = MIN_HEAT;
	@NetworkData
	public EnergyStage energyStage = EnergyStage.BLUE;
	@NetworkData
	public ForgeDirection orientation = ForgeDirection.UP;

	protected int progressPart = 0;
	protected boolean lastPower = false;
	@NetworkData
	protected IBatteryIOObject mjStoredBattery;

	private boolean checkOrienation = false;
	private TileBuffer[] tileCache;

	@NetworkData
	private boolean isPumping = false; // Used for SMP synch
	private double mjStoredInternal;
	private PowerHandler mjStoredHandler;

	@Override
	public void initialize() {
		mjStoredBattery = (IBatteryIOObject) MjAPI.getMjBattery(this, MjAPI.DEFAULT_POWER_FRAMEWORK);
		if (mjStoredInternal != 0) {
			mjStoredBattery.setEnergyStored(mjStoredInternal);
			mjStoredInternal = 0;
		}
		mjStoredHandler = new PowerHandler(this, PowerHandler.Type.ENGINE, mjStoredBattery);
		if (!worldObj.isRemote) {
			checkRedstonePower();
		}
	}

	@Override
	public IBatteryObject getMjBattery(String kind, ForgeDirection side) {
		if (MjAPI.DEFAULT_POWER_FRAMEWORK.equals(kind) && side == orientation) {
			return mjStoredBattery;
		}
		return null;
	}

	public abstract ResourceLocation getBaseTexture();

	public abstract ResourceLocation getChamberTexture();

	public ResourceLocation getTrunkTexture(EnergyStage stage) {
		switch (stage) {
			case BLUE:
				return TRUNK_BLUE_TEXTURE;
			case GREEN:
				return TRUNK_GREEN_TEXTURE;
			case YELLOW:
				return TRUNK_YELLOW_TEXTURE;
			case RED:
				return TRUNK_RED_TEXTURE;
			default:
				return TRUNK_RED_TEXTURE;
		}
	}

	public boolean onBlockActivated(EntityPlayer player, ForgeDirection side) {
		return false;
	}

	public double getEnergyLevel() {
		// 0.01 - overheat support
		return mjStoredBattery == null ? 0 : (mjStoredBattery.getEnergyStored() / mjStoredBattery.maxCapacity() + 0.01d);
	}

	protected EnergyStage computeEnergyStage() {
		float energyLevel = getHeatLevel();
		if (energyLevel < 0.25f) {
			return EnergyStage.BLUE;
		} else if (energyLevel < 0.5f) {
			return EnergyStage.GREEN;
		} else if (energyLevel < 0.75f) {
			return EnergyStage.YELLOW;
		} else if (energyLevel < 1f) {
			return EnergyStage.RED;
		} else {
			return EnergyStage.OVERHEAT;
		}
	}

	public final EnergyStage getEnergyStage() {
		if (!worldObj.isRemote) {
			if (energyStage == EnergyStage.OVERHEAT) {
				return energyStage;
			}

			EnergyStage newStage = computeEnergyStage();

			if (energyStage != newStage) {
				energyStage = newStage;
				sendNetworkUpdate();
			}
		}

		return energyStage;
	}

	public void updateHeatLevel() {
		heat = (float) ((MAX_HEAT - MIN_HEAT) * getEnergyLevel()) + MIN_HEAT;
	}

	public float getHeatLevel() {
		return (heat - MIN_HEAT) / (MAX_HEAT - MIN_HEAT);
	}

	public float getIdealHeatLevel() {
		return heat / IDEAL_HEAT;
	}

	public float getHeat() {
		return heat;
	}

	public float getPistonSpeed() {
		if (!worldObj.isRemote) {
			return Math.max(0.16f * getHeatLevel(), 0.01f);
		}

		switch (getEnergyStage()) {
			case BLUE:
				return 0.02F;
			case GREEN:
				return 0.04F;
			case YELLOW:
				return 0.08F;
			case RED:
				return 0.16F;
			default:
				return 0;
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (worldObj.isRemote) {
			if (progressPart != 0) {
				progress += getPistonSpeed();

				if (progress > 1) {
					progressPart = 0;
					progress = 0;
				}
			} else if (this.isPumping) {
				progressPart = 1;
			}

			return;
		}

		if (checkOrienation) {
			checkOrienation = false;

			if (!isOrientationValid()) {
				switchOrientation(true);
			}
		}

		burn();
		engineUpdate();
		updateHeatLevel();
		getEnergyStage();

		TileEntity tile = getTileBuffer(orientation).getTile();

		if (progressPart != 0) {
			progress += getPistonSpeed();

			if (progress > 0.5 && progressPart == 1) {
				progressPart = 2;
				sendPower();
			} else if (progress >= 1) {
				progress = 0;
				progressPart = 0;
			}
		} else if (isRedstonePowered && isActive()) {
			if (isPoweredTile(tile, orientation)) {
				if (mjStoredBattery.getEnergyStored() > 0) {
					progressPart = 1;
					setPumping(true);
				} else {
					setPumping(false);
				}
			} else {
				setPumping(false);
			}
		} else {
			setPumping(false);
		}
	}

	private boolean canReceive(IBatteryObject battery) {
		return battery != null && (!(battery instanceof IBatteryIOObject) || ((IBatteryIOObject) battery).canReceive());
	}

	private void sendPower() {
		TileEntity tile = getTileBuffer(orientation).getTile();
		IBatteryObject battery = MjAPI.getMjBattery(tile, MjAPI.DEFAULT_POWER_FRAMEWORK, orientation.getOpposite());
		if (battery != null && canReceive(battery)) {
			double extracted = JavaTools.min(mjStoredBattery.getEnergyStored(),
					mjStoredBattery.maxSendedPerCycle(), battery.getEnergyRequested());
			extracted = battery.addEnergy(extracted);
			mjStoredBattery.setEnergyStored(mjStoredBattery.getEnergyStored() - extracted);
		} else if (tile instanceof IPowerReceptor) {
			PowerHandler.PowerReceiver receiver = ((IPowerReceptor) tile).getPowerReceiver(orientation.getOpposite());
			if (receiver == null) {
				return;
			}
			double extracted = JavaTools.min(mjStoredBattery.getEnergyStored(),
					mjStoredBattery.maxSendedPerCycle(), receiver.getMaxEnergyReceived());
			if (extracted < receiver.getMinEnergyReceived()) {
				return;
			}
			extracted = receiver.receiveEnergy(PowerHandler.Type.ENGINE, extracted, orientation.getOpposite());
			mjStoredBattery.setEnergyStored(mjStoredBattery.getEnergyStored() - extracted);
		}
	}

	protected void burn() {
	}

	protected void engineUpdate() {
		if (!isRedstonePowered) {
			double stored = mjStoredBattery.getEnergyStored();
			if (stored > 1d) {
				mjStoredBattery.setEnergyStored(stored - 1d);
			}
		}
	}

	public boolean isActive() {
		return true;
	}

	protected final void setPumping(boolean isActive) {
		if (this.isPumping == isActive) {
			return;
		}

		this.isPumping = isActive;
		sendNetworkUpdate();
	}

	public boolean isOrientationValid() {
		TileEntity tile = getTileBuffer(orientation).getTile();

		return isPoweredTile(tile, orientation);
	}

	public boolean switchOrientation(boolean preferPipe) {
		return preferPipe && switchOrientationDo(true) || switchOrientationDo(false);
	}

	private boolean switchOrientationDo(boolean pipesOnly) {
		for (int i = orientation.ordinal() + 1; i <= orientation.ordinal() + 6; ++i) {
			ForgeDirection o = ForgeDirection.VALID_DIRECTIONS[i % 6];

			TileEntity tile = getTileBuffer(o).getTile();

			if ((!pipesOnly || tile instanceof IPipeTile) && isPoweredTile(tile, o)) {
				orientation = o;
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, worldObj.getBlock(xCoord, yCoord, zCoord));

				return true;
			}
		}

		return false;
	}

	public TileBuffer getTileBuffer(ForgeDirection side) {
		if (tileCache == null) {
			tileCache = TileBuffer.makeBuffer(worldObj, xCoord, yCoord, zCoord, false);
		}

		return tileCache[side.ordinal()];
	}

	@Override
	public void invalidate() {
		super.invalidate();
		tileCache = null;
		checkOrienation = true;
	}

	@Override
	public void validate() {
		super.validate();
		tileCache = null;
		checkOrienation = true;
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);

		orientation = ForgeDirection.getOrientation(data.getInteger("orientation"));
		progress = data.getFloat("progress");
		heat = data.getFloat("heat");
		mjStoredInternal = data.getDouble("energy");
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);

		data.setInteger("orientation", orientation.ordinal());
		data.setFloat("progress", progress);
		data.setFloat("heat", heat);
		data.setDouble("energy", mjStoredBattery == null ? mjStoredInternal : mjStoredBattery.getEnergyStored());
	}

	public void getGUINetworkData(int id, int value) {
		switch (id) {
			case 0:
				if (mjStoredBattery == null) {
					return;
				}
				int iEnergy = (int) Math.round(mjStoredBattery.getEnergyStored() * 10);
				iEnergy = (iEnergy & 0xffff0000) | (value & 0xffff);
				mjStoredBattery.setEnergyStored(iEnergy / 10d);
				break;
			case 1:
				if (mjStoredBattery == null) {
					return;
				}
				iEnergy = (int) Math.round(mjStoredBattery.getEnergyStored() * 10);
				iEnergy = (iEnergy & 0xffff) | ((value & 0xffff) << 16);
				mjStoredBattery.setEnergyStored(iEnergy / 10d);
				break;
			case 2:
				currentOutput = value / 10F;
				break;
			case 3:
				heat = value / 100F;
				break;
		}
	}

	public void sendGUINetworkData(ContainerEngine containerEngine, ICrafting iCrafting) {
		iCrafting.sendProgressBarUpdate(containerEngine, 0, (int) Math.round(mjStoredBattery.getEnergyStored() * 10) & 0xffff);
		iCrafting.sendProgressBarUpdate(containerEngine, 1, (int) (Math.round(mjStoredBattery.getEnergyStored() * 10) & 0xffff0000) >> 16);
		iCrafting.sendProgressBarUpdate(containerEngine, 2, (int) Math.round(currentOutput * 10));
		iCrafting.sendProgressBarUpdate(containerEngine, 3, Math.round(heat * 100));
	}

	/* STATE INFORMATION */
	public abstract boolean isBurning();

	public abstract int getScaledBurnTime(int scale);

	public void addEnergy(double addition) {
		double stored = mjStoredBattery.getEnergyStored();
		mjStoredBattery.setEnergyStored(stored + addition);

		if (getEnergyStage() == EnergyStage.OVERHEAT) {
			worldObj.createExplosion(null, xCoord, yCoord, zCoord, explosionRange(), true);
			worldObj.setBlockToAir(xCoord, yCoord, zCoord);
		}

		if (stored + addition > getMaxEnergy()) {
			mjStoredBattery.setEnergyStored(getMaxEnergy());
		}
	}

	public boolean isPoweredTile(TileEntity tile, ForgeDirection side) {
		return MjAPI.getMjBattery(tile, MjAPI.DEFAULT_POWER_FRAMEWORK, side.getOpposite()) != null ||
				tile instanceof IPowerReceptor && ((IPowerReceptor) tile).getPowerReceiver(side.getOpposite()) != null;
	}

	public double getMaxEnergy() {
		return mjStoredBattery == null ? 0 : mjStoredBattery.maxCapacity();
	}

	public abstract float explosionRange();

	public double getEnergyStored() {
		return mjStoredBattery == null ? 0 : mjStoredBattery.getEnergyStored();
	}

	@Override
	public LinkedList<ITrigger> getTriggers() {
		LinkedList<ITrigger> triggers = new LinkedList<ITrigger>();

		triggers.add(BuildCraftEnergy.triggerBlueEngineHeat);
		triggers.add(BuildCraftEnergy.triggerGreenEngineHeat);
		triggers.add(BuildCraftEnergy.triggerYellowEngineHeat);
		triggers.add(BuildCraftEnergy.triggerRedEngineHeat);

		return triggers;
	}

	@Override
	public ConnectOverride overridePipeConnection(PipeType type, ForgeDirection with) {
		if (type == PipeType.POWER) {
			return ConnectOverride.DEFAULT;
		} else if (with == orientation) {
			return ConnectOverride.DISCONNECT;
		} else {
			return ConnectOverride.DEFAULT;
		}
	}

	public void checkRedstonePower() {
		isRedstonePowered = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
	}

	@Override
	public void doWork(PowerHandler workProvider) {

	}

	@Override
	public PowerHandler.PowerReceiver getPowerReceiver(ForgeDirection side) {
		return side != orientation ? mjStoredHandler.getPowerReceiver() : null;
	}

	@Override
	public boolean canEmitPowerFrom(ForgeDirection side) {
		return side == orientation;
	}
}
